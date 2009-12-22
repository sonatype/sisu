/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.plexus.scanners;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Roles;

/**
 * {@link PlexusBeanSource} that collects {@link PlexusBeanMetadata} by scanning XML resources.
 */
public final class XmlPlexusBeanSource
    implements PlexusBeanSource
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String COMPONENTS_XML_PATH = "META-INF/plexus/components.xml";

    private static final String LOAD_ON_START = "load-on-start";

    private static final String SINGLETON = "singleton";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final URL plexusDotXml;

    private final ClassSpace space;

    private final Map<?, ?> variables;

    private final Logger logger = LoggerFactory.getLogger( XmlPlexusBeanSource.class );

    private Map<String, MappedPlexusBeanMetadata> metadataMap = Collections.emptyMap();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Scans the optional {@code plexus.xml} resource and the given class space for XML based Plexus metadata.
     * 
     * @param plexusDotXml Optional plexus.xml URL
     * @param space The containing class space
     * @param variables The filter variables
     */
    public XmlPlexusBeanSource( final URL plexusDotXml, final ClassSpace space, final Map<?, ?> variables )
    {
        this.plexusDotXml = plexusDotXml;
        this.space = space;
        this.variables = variables;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<Component, DeferredClass<?>> findPlexusComponentBeans()
    {
        // rebuild metadata as we scan the XML for Plexus components
        metadataMap = new HashMap<String, MappedPlexusBeanMetadata>();

        final Map<Component, DeferredClass<?>> components = new HashMap<Component, DeferredClass<?>>();
        final Map<String, String> strategies = new HashMap<String, String>();

        URL url = plexusDotXml;

        try
        {
            if ( null != url )
            {
                final InputStream in = url.openStream();
                try
                {
                    parsePlexusDotXml( filteredXmlReader( in, variables ), components, strategies );
                }
                finally
                {
                    IOUtil.close( in );
                }
            }

            for ( final Enumeration<URL> i = space.getResources( COMPONENTS_XML_PATH ); i.hasMoreElements(); )
            {
                url = i.nextElement();
                final InputStream in = url.openStream();
                try
                {
                    parseComponentsDotXml( filteredXmlReader( in, variables ), components, strategies );
                }
                finally
                {
                    IOUtil.close( in );
                }
            }
        }
        catch ( final XmlPullParserException e )
        {
            throw new RuntimeException( "Problem parsing: " + url + " reason: " + e );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( "Problem reading: " + url + " reason: " + e );
        }

        return components;
    }

    public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        final PlexusBeanMetadata metadata = metadataMap.remove( implementation.getName() );
        if ( metadataMap.isEmpty() )
        {
            // avoid leaving sparse maps around
            metadataMap = Collections.emptyMap();
        }
        return metadata;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Wraps the given {@link InputStream} as a {@link Reader} with XML encoding detection and optional interpolation.
     * 
     * @param in The input stream
     * @param variables The filter variables
     * @return Reader that can automatically detect XML encodings and optionally interpolate variables
     */
    private static Reader filteredXmlReader( final InputStream in, final Map<?, ?> variables )
        throws IOException
    {
        final Reader reader = new XmlStreamReader( in );
        return null != variables ? new InterpolationFilterReader( reader, variables ) : reader;
    }

    /**
     * Parses a {@code plexus.xml} resource into load-on-start settings and Plexus bean metadata.
     * 
     * @param reader The XML resource reader
     * @param components The parsed component implementations
     * @param strategies The role instantiation strategies
     */
    private void parsePlexusDotXml( final Reader reader, final Map<Component, DeferredClass<?>> components,
                                    final Map<String, String> strategies )
        throws XmlPullParserException, IOException
    {
        final MXParser parser = new MXParser();
        parser.setInput( reader );

        parser.nextTag();
        parser.require( XmlPullParser.START_TAG, null, "plexus" );

        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            final String name = parser.getName();
            if ( LOAD_ON_START.equals( name ) )
            {
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    parseLoadOnStart( parser, strategies );
                }
            }
            else if ( "components".equals( name ) )
            {
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    parseComponent( parser, components, strategies );
                }
            }
            else
            {
                parser.skipSubTree();
            }
        }
    }

    /**
     * Parses a {@code components.xml} resource into a series of Plexus bean metadata.
     * 
     * @param reader The XML resource reader
     * @param components The parsed component implementations
     * @param strategies The role instantiation strategies
     */
    private void parseComponentsDotXml( final Reader reader, final Map<Component, DeferredClass<?>> components,
                                        final Map<String, String> strategies )
        throws XmlPullParserException, IOException
    {
        final MXParser parser = new MXParser();
        parser.setInput( reader );

        parser.nextTag();
        parser.require( XmlPullParser.START_TAG, null, "component-set" );
        parser.nextTag();
        parser.require( XmlPullParser.START_TAG, null, "components" );

        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            parseComponent( parser, components, strategies );
        }
    }

    /**
     * Parses a load-on-start &lt;component&gt; XML stanza into a Plexus role-hint.
     * 
     * @param parser The XML parser
     * @param strategies The role instantiation strategies
     */
    private void parseLoadOnStart( final MXParser parser, final Map<String, String> strategies )
        throws XmlPullParserException, IOException
    {
        String role = null;
        String hint = "";

        parser.require( XmlPullParser.START_TAG, null, "component" );

        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( "role".equals( parser.getName() ) )
            {
                role = TEXT( parser );
            }
            else if ( "role-hint".equals( parser.getName() ) )
            {
                hint = TEXT( parser );
            }
            else
            {
                parser.skipSubTree();
            }
        }

        if ( null == role )
        {
            throw new XmlPullParserException( "Missing <role> element.", parser, null );
        }

        strategies.put( Roles.canonicalRoleHint( role, hint ), LOAD_ON_START );
    }

    /**
     * Parses a &lt;component&gt; XML stanza into a deferred implementation, configuration, and requirements.
     * 
     * @param parser The XML parser
     * @param components The parsed component implementations
     * @param strategies The role instantiation strategies
     */
    private void parseComponent( final MXParser parser, final Map<Component, DeferredClass<?>> components,
                                 final Map<String, String> strategies )
        throws XmlPullParserException, IOException
    {
        String role = null;
        String hint = "";

        String instantiationStrategy = SINGLETON;
        String implementation = null;

        final Map<String, Requirement> requirementMap = new HashMap<String, Requirement>();
        final Map<String, Configuration> configurationMap = new HashMap<String, Configuration>();

        parser.require( XmlPullParser.START_TAG, null, "component" );

        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            final String name = parser.getName();
            if ( "requirements".equals( name ) )
            {
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    parseRequirement( parser, requirementMap );
                }
            }
            else if ( "configuration".equals( name ) )
            {
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    parseConfiguration( parser, configurationMap );
                }
            }
            else if ( "role".equals( name ) )
            {
                role = TEXT( parser ).intern();
            }
            else if ( "role-hint".equals( name ) )
            {
                hint = TEXT( parser );
            }
            else if ( "instantiation-strategy".equals( name ) )
            {
                instantiationStrategy = TEXT( parser ).intern();
            }
            else if ( "implementation".equals( name ) )
            {
                implementation = TEXT( parser ).intern();
            }
            else
            {
                parser.skipSubTree();
            }
        }

        if ( null == implementation )
        {
            throw new XmlPullParserException( "Missing <implementation> element.", parser, null );
        }

        if ( null == role )
        {
            role = implementation;
        }

        final Class<?> clazz;

        try
        {
            // check the role actually exists
            clazz = space.loadClass( role );
            if ( implementation.equals( role ) )
            {
                // also check constructor types
                clazz.getDeclaredConstructors();
            }
        }
        catch ( final Throwable e )
        {
            // not all roles are used, so just note for now
            logger.debug( "Missing Plexus role: " + role, e );
            return; // missing roles don't need any metadata
        }

        if ( !configurationMap.isEmpty() || !requirementMap.isEmpty() )
        {
            final MappedPlexusBeanMetadata beanMetadata = metadataMap.get( implementation );
            if ( beanMetadata != null )
            {
                beanMetadata.merge( configurationMap, requirementMap );
            }
            else
            {
                metadataMap.put( implementation, new MappedPlexusBeanMetadata( configurationMap, requirementMap ) );
            }
        }

        final String roleHintKey = Roles.canonicalRoleHint( role, hint );
        final String strategy = strategies.get( roleHintKey );
        if ( null != strategy )
        {
            instantiationStrategy = strategy;
        }
        else
        {
            strategies.put( roleHintKey, instantiationStrategy );
        }

        final Component component = new ComponentImpl( clazz, Hints.canonicalHint( hint ), instantiationStrategy );
        components.put( component, space.deferLoadClass( implementation ) );
    }

    /**
     * Parses a &lt;requirement&gt; XML stanza into a mapping from a field name to a @{@link Requirement}.
     * 
     * @param parser The XML parser
     * @param requirementMap The field -> @{@link Requirement} map
     */
    private void parseRequirement( final MXParser parser, final Map<String, Requirement> requirementMap )
        throws XmlPullParserException, IOException
    {
        String role = null;
        final List<String> hintList = new ArrayList<String>();
        String fieldName = null;
        boolean optional = false;

        parser.require( XmlPullParser.START_TAG, null, "requirement" );

        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            final String name = parser.getName();
            if ( "role".equals( name ) )
            {
                role = TEXT( parser ).intern();
            }
            else if ( "role-hint".equals( name ) )
            {
                hintList.add( TEXT( parser ) );
            }
            else if ( "role-hints".equals( name ) )
            {
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    hintList.add( TEXT( parser ) );
                }
            }
            else if ( "field-name".equals( name ) )
            {
                fieldName = TEXT( parser );
            }
            else if ( "optional".equals( name ) )
            {
                optional = Boolean.parseBoolean( TEXT( parser ) );
            }
            else
            {
                parser.skipSubTree();
            }
        }

        if ( null == role )
        {
            throw new XmlPullParserException( "Missing <role> element.", parser, null );
        }

        if ( null == fieldName )
        {
            // missing name, try and use simple role class name as the basis for the field name
            final int cursor = Math.max( role.lastIndexOf( '.' ), role.lastIndexOf( '$' ) ) + 1;
            if ( cursor < role.length() )
            {
                fieldName = Character.toLowerCase( role.charAt( cursor ) ) + role.substring( cursor + 1 );
            }
            else
            {
                throw new XmlPullParserException( "Missing <field-name> element.", parser, null );
            }
        }

        final String[] hints = Hints.canonicalHints( hintList.toArray( new String[hintList.size()] ) );
        requirementMap.put( fieldName, new RequirementImpl( space.deferLoadClass( role ), optional, hints ) );
    }

    /**
     * Parses a &lt;configuration&gt; XML stanza into a mapping from a field name to a @{@link Configuration}.
     * 
     * @param parser The XML parser
     * @param configurationMap The field -> @{@link Configuration} map
     */
    private static void parseConfiguration( final MXParser parser, final Map<String, Configuration> configurationMap )
        throws XmlPullParserException, IOException
    {
        final String name = parser.getName();

        // make sure we have a valid Java identifier
        final String fieldName = camelizeName( name );
        final StringBuilder buf = new StringBuilder();

        final String header = parser.getText().trim();
        final int depth = parser.getDepth();

        while ( parser.next() != XmlPullParser.END_TAG || parser.getDepth() > depth )
        {
            // combine children into single string
            buf.append( parser.getText().trim() );
        }

        // add header+footer when there's nested XML or attributes
        if ( buf.indexOf( "<" ) == 0 || header.indexOf( '=' ) > 0 )
        {
            buf.insert( 0, header );
            if ( !header.endsWith( "/>" ) )
            {
                // follow up with basic footer
                buf.append( "</" + name + '>' );
            }
        }

        configurationMap.put( fieldName, new ConfigurationImpl( fieldName, buf.toString() ) );
    }

    /**
     * Removes any non-Java identifiers from the name and converts it to camelCase.
     * 
     * @param name The element name
     * @return CamelCased name with no dashes
     */
    private static String camelizeName( final String name )
    {
        final StringBuilder buf = new StringBuilder();

        boolean capitalize = false;
        for ( int i = 0; i < name.length(); i++ )
        {
            final char c = name.charAt( i );
            if ( !Character.isJavaIdentifierPart( c ) )
            {
                capitalize = true;
            }
            else if ( capitalize )
            {
                buf.append( Character.toUpperCase( c ) );
                capitalize = false;
            }
            else
            {
                buf.append( c );
            }
        }

        return buf.toString();
    }

    /**
     * Returns the text contained inside the current XML element, without any surrounding whitespace.
     * 
     * @param parser The XML parser
     * @return Trimmed TEXT element
     */
    private static String TEXT( final XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        return parser.nextText().trim();
    }
}
