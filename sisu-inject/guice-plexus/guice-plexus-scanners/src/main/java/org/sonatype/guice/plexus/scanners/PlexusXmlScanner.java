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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.Streams;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.config.Strategies;

public final class PlexusXmlScanner
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger( PlexusXmlScanner.class.getName() );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<?, ?> variables;

    private final URL plexusXml;

    private final Map<String, PlexusBeanMetadata> metadata;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates an XML scanner that also accumulates Plexus bean metadata in the given map.
     * 
     * @param variables The filter variables
     * @param plexusXml The plexus.xml URL
     * @param metadata The metadata map
     */
    public PlexusXmlScanner( final Map<?, ?> variables, final URL plexusXml,
                             final Map<String, PlexusBeanMetadata> metadata )
    {
        this.variables = variables;
        this.plexusXml = plexusXml;
        this.metadata = metadata;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<Component, DeferredClass<?>> scan( final ClassSpace space )
    {
        final PlexusTypeRegistry registry = new PlexusTypeRegistry( space );
        if ( null != plexusXml )
        {
            parsePlexusXml( plexusXml, registry );
        }

        final Enumeration<URL> e = space.getResources( "META-INF/plexus/components.xml" );
        while ( e.hasMoreElements() )
        {
            parseComponentsXml( e.nextElement(), registry );
        }

        return registry.getComponents();
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
        final Reader reader = ReaderFactory.newXmlReader( in );
        if ( null != variables )
        {
            return new InterpolationFilterReader( reader, variables );
        }
        return reader;
    }

    /**
     * Parses a {@code plexus.xml} resource into load-on-start settings and Plexus bean metadata.
     * 
     * @param url The plexus.xml URL
     * @param registry The parsed components
     */
    private void parsePlexusXml( final URL url, final PlexusTypeRegistry registry )
    {
        try
        {
            final InputStream in = Streams.openStream( url );
            try
            {
                final MXParser parser = new MXParser();
                parser.setInput( filteredXmlReader( in, variables ) );

                parser.nextTag();
                parser.require( XmlPullParser.START_TAG, null, null ); // this may be <component-set> or <plexus>

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    final String name = parser.getName();
                    if ( Strategies.LOAD_ON_START.equals( name ) )
                    {
                        while ( parser.nextTag() == XmlPullParser.START_TAG )
                        {
                            parseLoadOnStart( parser, registry );
                        }
                    }
                    else if ( "components".equals( name ) )
                    {
                        while ( parser.nextTag() == XmlPullParser.START_TAG )
                        {
                            parseComponent( parser, registry );
                        }
                    }
                    else
                    {
                        parser.skipSubTree();
                    }
                }
            }
            finally
            {
                IOUtil.close( in );
            }
        }
        catch ( final Throwable e )
        {
            reportResourceProblem( url, e );
        }
    }

    /**
     * Parses a {@code components.xml} resource into a series of Plexus bean metadata.
     * 
     * @param url The components.xml URL
     * @param registry The parsed components
     */
    private void parseComponentsXml( final URL url, final PlexusTypeRegistry registry )
    {
        try
        {
            final InputStream in = Streams.openStream( url );
            try
            {
                final MXParser parser = new MXParser();
                parser.setInput( filteredXmlReader( in, variables ) );

                parser.nextTag();
                parser.require( XmlPullParser.START_TAG, null, null ); // this may be <component-set> or <plexus>
                parser.nextTag();
                parser.require( XmlPullParser.START_TAG, null, "components" );

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    parseComponent( parser, registry );
                }
            }
            finally
            {
                IOUtil.close( in );
            }
        }
        catch ( final Throwable e )
        {
            reportResourceProblem( url, e );
        }
    }

    /**
     * Parses a load-on-start &lt;component&gt; XML stanza into a Plexus role-hint.
     * 
     * @param parser The XML parser
     * @param registry The parsed components
     */
    private void parseLoadOnStart( final MXParser parser, final PlexusTypeRegistry registry )
        throws XmlPullParserException, IOException
    {
        if ( "component".equals( parser.getName() ) )
        {
            String role = null;
            String hint = "";

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

            registry.loadOnStart( role, hint );
        }
        else
        {
            parser.skipSubTree();
        }
    }

    /**
     * Parses a &lt;component&gt; XML stanza into a deferred implementation, configuration, and requirements.
     * 
     * @param parser The XML parser
     * @param registry The parsed components
     */
    private void parseComponent( final MXParser parser, final PlexusTypeRegistry registry )
        throws XmlPullParserException, IOException
    {
        String role = null;
        String hint = "";
        String instantiationStrategy = Strategies.SINGLETON;
        String description = "";

        String implementation = null;

        final Map<String, Requirement> requirementMap = new HashMap<String, Requirement>();
        final Map<String, Configuration> configurationMap = new HashMap<String, Configuration>();
        final ClassSpace space = registry.getSpace();

        parser.require( XmlPullParser.START_TAG, null, "component" );

        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            final String name = parser.getName();
            if ( "requirements".equals( name ) )
            {
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    parseRequirement( parser, space, requirementMap );
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
            else if ( "description".equals( name ) )
            {
                description = TEXT( parser );
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

        implementation = registry.addComponent( role, hint, instantiationStrategy, description, implementation );
        if ( null != implementation )
        {
            updatePlexusBeanMetadata( implementation, configurationMap, requirementMap );
        }
    }

    /**
     * Updates the shared Plexus bean metadata with the given local information.
     * 
     * @param implementation The component implementation
     * @param configurationMap The field -> @{@link Configuration} map
     * @param requirementMap The field -> @{@link Requirement} map
     */
    private void updatePlexusBeanMetadata( final String implementation,
                                           final Map<String, Configuration> configurationMap,
                                           final Map<String, Requirement> requirementMap )
    {
        if ( null != metadata && ( !configurationMap.isEmpty() || !requirementMap.isEmpty() ) )
        {
            final PlexusXmlMetadata beanMetadata = (PlexusXmlMetadata) metadata.get( implementation );
            if ( beanMetadata != null )
            {
                beanMetadata.merge( configurationMap, requirementMap );
            }
            else
            {
                metadata.put( implementation, new PlexusXmlMetadata( configurationMap, requirementMap ) );
            }
        }
    }

    /**
     * Parses a &lt;requirement&gt; XML stanza into a mapping from a field name to a @{@link Requirement}.
     * 
     * @param parser The XML parser
     * @param space The class space
     * @param requirementMap The field -> @{@link Requirement} map
     */
    private void parseRequirement( final MXParser parser, final ClassSpace space,
                                   final Map<String, Requirement> requirementMap )
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
            fieldName = role; // use fully-qualified role as the field name (see PlexusXmlMetadata)
        }

        requirementMap.put( fieldName,
                            new RequirementImpl( space.deferLoadClass( role ), optional,
                                                 Hints.canonicalHints( hintList ) ) );
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
        final String fieldName = Roles.camelizeName( name );
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

    private static void reportResourceProblem( final URL url, final Throwable cause )
    {
        if ( LOGGER.isLoggable( Level.FINE ) )
        {
            LOGGER.fine( "Problem parsing resource: " + url + " cause: " + cause );
        }
    }
}
