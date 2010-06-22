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
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.DeferredProvider;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.Strategies;

import com.google.inject.TypeLiteral;

public class PlexusXmlScannerTest
    extends TestCase
{
    static class NamedProperty
        implements BeanProperty<Object>
    {
        final String name;

        final TypeLiteral<Object> type;

        public NamedProperty( final String name )
        {
            this.name = name;
            type = TypeLiteral.get( Object.class );
        }

        @SuppressWarnings( "unchecked" )
        public NamedProperty( final String name, final TypeLiteral type )
        {
            this.name = name;
            this.type = type;
        }

        public <A extends Annotation> A getAnnotation( final Class<A> annotationType )
        {
            return null;
        }

        public String getName()
        {
            return name;
        }

        public TypeLiteral<Object> getType()
        {
            return type;
        }

        public <B> void set( final B bean, final Object value )
        {
        }
    }

    interface Bean
    {
    }

    protected static class DefaultBean
    {
    }

    static class DebugBean
    {
    }

    static class AnotherBean
    {
    }

    static class EmptyClassSpace
        implements ClassSpace
    {
        public Class<?> loadClass( final String name )
        {
            try
            {
                return Class.forName( name );
            }
            catch ( final ClassNotFoundException e )
            {
                throw new TypeNotPresentException( name, e );
            }
        }

        @SuppressWarnings( "unchecked" )
        public DeferredClass<?> deferLoadClass( final String name )
        {
            return new LoadedClass( loadClass( name ) );
        }

        public URL getResource( final String name )
        {
            return null;
        }

        public Enumeration<URL> getResources( final String name )
        {
            // hide components.xml so we can just test plexus.xml parsing
            return Collections.enumeration( Collections.<URL> emptyList() );
        }

        public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
        {
            // hide components.xml so we can just test plexus.xml parsing
            return Collections.enumeration( Collections.<URL> emptyList() );
        }
    }

    public void testLoadOnStart()
        throws IOException
    {
        final URL plexusXml = getClass().getResource( "/META-INF/plexus/plexus.xml" );
        final PlexusXmlScanner scanner = new PlexusXmlScanner( null, plexusXml, null );

        final Map<Component, DeferredClass<?>> componentMap = scanner.scan( new EmptyClassSpace(), false );

        assertEquals( 2, componentMap.size() );

        final Component component1 =
            new ComponentImpl( DefaultBean.class, Hints.DEFAULT_HINT, Strategies.LOAD_ON_START, "" );
        assertEquals( DefaultBean.class, componentMap.get( component1 ).load() );

        final Component component2 = new ComponentImpl( Bean.class, "debug", Strategies.LOAD_ON_START, "For debugging" );
        assertEquals( DebugBean.class, componentMap.get( component2 ).load() );
    }

    public void testBadPlexusXml()
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) PlexusXmlScannerTest.class.getClassLoader() );

        try
        {
            final URL plexusXml = getClass().getResource( "/META-INF/plexus/bad_plexus_1.xml" );
            new PlexusXmlScanner( null, plexusXml, null ).scan( space, false );
            fail( "Expected IOException" );
        }
        catch ( final IOException e )
        {
        }
    }

    public void testComponents()
        throws IOException
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) PlexusXmlScannerTest.class.getClassLoader() );

        final Map<String, PlexusBeanMetadata> metadata = new HashMap<String, PlexusBeanMetadata>();
        final PlexusXmlScanner scanner = new PlexusXmlScanner( null, null, metadata );

        final Map<Component, DeferredClass<?>> componentMap = scanner.scan( space, true );

        assertEquals( 4, componentMap.size() );

        final Component component1 =
            new ComponentImpl( DefaultBean.class, Hints.DEFAULT_HINT, Strategies.PER_LOOKUP, "" );
        assertEquals( DefaultBean.class, componentMap.get( component1 ).load() );

        final Component component2 = new ComponentImpl( Bean.class, "debug", Strategies.SINGLETON, "For debugging" );
        assertEquals( DebugBean.class, componentMap.get( component2 ).load() );

        final Component component3 = new ComponentImpl( Bean.class, Hints.DEFAULT_HINT, Strategies.SINGLETON, "" );
        assertEquals( AnotherBean.class, componentMap.get( component3 ).load() );

        final Component component4 = new ComponentImpl( Bean.class, "clone", Strategies.SINGLETON, "" );
        assertEquals( DefaultBean.class, componentMap.get( component4 ).load().getSuperclass() );

        final PlexusBeanMetadata metadata1 = metadata.get( DefaultBean.class.getName() );

        assertFalse( metadata1.isEmpty() );

        assertEquals(
                      new ConfigurationImpl( "someFieldName", "<some-field.name><item>PRIMARY</item></some-field.name>" ),
                      metadata1.getConfiguration( new NamedProperty( "someFieldName" ) ) );

        assertEquals( new ConfigurationImpl( "simple", "value" ),
                      metadata1.getConfiguration( new NamedProperty( "simple" ) ) );

        assertEquals( new ConfigurationImpl( "value", "<value with=\"attribute\"></value>" ),
                      metadata1.getConfiguration( new NamedProperty( "value" ) ) );

        assertEquals( new ConfigurationImpl( "emptyValue1", "<empty.value1 with=\"attribute\" />" ),
                      metadata1.getConfiguration( new NamedProperty( "emptyValue1" ) ) );

        assertEquals( new ConfigurationImpl( "emptyValue2", "" ),
                      metadata1.getConfiguration( new NamedProperty( "emptyValue2" ) ) );

        assertFalse( metadata1.isEmpty() );

        assertEquals( new RequirementImpl( Bean.class, true, "debug" ),
                      metadata1.getRequirement( new NamedProperty( "bean", TypeLiteral.get( Bean.class ) ) ) );

        assertFalse( metadata1.isEmpty() );

        metadata1.getConfiguration( new NamedProperty( "foo" ) );

        assertEquals( new RequirementImpl( Bean.class, false, Hints.DEFAULT_HINT, "debug" ),
                      metadata1.getRequirement( new NamedProperty( "beanMap" ) ) );

        assertFalse( metadata1.isEmpty() );

        assertEquals( new RequirementImpl( Bean.class, false ),
                      metadata1.getRequirement( new NamedProperty( "beanField" ) ) );

        assertTrue( metadata1.isEmpty() );

        assertNotNull( metadata.get( AnotherBean.class.getName() ) );
        assertNull( metadata.get( DebugBean.class.getName() ) );
    }

    static class FixedClassSpace
        implements ClassSpace
    {
        final String fixedResourceName;

        FixedClassSpace( final String fixedResourceName )
        {
            this.fixedResourceName = fixedResourceName;
        }

        public Class<?> loadClass( final String name )
        {
            try
            {
                return Class.forName( name );
            }
            catch ( final ClassNotFoundException e )
            {
                throw new TypeNotPresentException( name, e );
            }
        }

        @SuppressWarnings( "unchecked" )
        public DeferredClass<?> deferLoadClass( final String name )
        {
            return new DeferredClass()
            {
                public Class load()
                {
                    return loadClass( name );
                }

                public String getName()
                {
                    return name;
                }

                public DeferredProvider asProvider()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public URL getResource( final String name )
        {
            return getClass().getResource( fixedResourceName );
        }

        public Enumeration<URL> getResources( final String name )
        {
            return Collections.enumeration( Collections.singleton( getClass().getResource( fixedResourceName ) ) );
        }

        public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
        {
            return Collections.enumeration( Collections.singleton( getClass().getResource( fixedResourceName ) ) );
        }
    }

    public void testBadComponentsXml()
        throws IOException
    {
        try
        {
            final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/bad_components_1.xml" );
            new PlexusXmlScanner( null, null, null ).scan( space, true );
            fail( "Expected IOException" );
        }
        catch ( final IOException e )
        {
        }

        try
        {
            final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/bad_components_2.xml" );
            new PlexusXmlScanner( null, null, null ).scan( space, true );
            fail( "Expected IOException" );
        }
        catch ( final IOException e )
        {
        }

        try
        {
            final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/bad_components_3.xml" );
            final Map<String, PlexusBeanMetadata> metadata = new HashMap<String, PlexusBeanMetadata>();
            final PlexusXmlScanner scanner = new PlexusXmlScanner( null, null, metadata );

            scanner.scan( space, true );

            final Requirement badReq =
                metadata.get( DefaultBean.class.getName() ).getRequirement( new NamedProperty( "no.such.class" ) );

            badReq.role();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
        }

        {
            final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/bad_components_4.xml" );
            final PlexusXmlScanner scanner = new PlexusXmlScanner( null, null, null );
            assertTrue( scanner.scan( space, true ).isEmpty() );
        }
    }

    public void testInterpolatedComponentsXml()
        throws IOException
    {
        final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/variable_components.xml" );

        final Map<String, PlexusBeanMetadata> metadata = new HashMap<String, PlexusBeanMetadata>();

        new PlexusXmlScanner( null, null, metadata ).scan( space, true );

        assertEquals(
                      "${some.value}",
                      metadata.get( DefaultBean.class.getName() ).getConfiguration( new NamedProperty( "variable" ) ).value() );

        final Map<?, ?> variables = Collections.singletonMap( "some.value", "INTERPOLATED" );

        new PlexusXmlScanner( variables, null, metadata ).scan( space, true );

        assertEquals(
                      "INTERPOLATED",
                      metadata.get( DefaultBean.class.getName() ).getConfiguration( new NamedProperty( "variable" ) ).value() );
    }

    public void testLocalizedXmlScanning()
        throws IOException
    {
        final ClassLoader parent = PlexusXmlScannerTest.class.getClassLoader();
        final ClassSpace space = new URLClassSpace( parent, null );

        assertTrue( new PlexusXmlScanner( null, null, null ).scan( space, true ).isEmpty() );
    }

    public void testOptionalLogging()
        throws Exception
    {
        final Level level = Logger.getLogger( "" ).getLevel();
        try
        {
            Logger.getLogger( "" ).setLevel( Level.SEVERE );

            // check everything still works without any SLF4J jars
            final ClassLoader noLoggingLoader =
                new URLClassLoader( ( (URLClassLoader) getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.contains( "slf4j" ) )
                        {
                            throw new ClassNotFoundException( name );
                        }
                        if ( name.contains( "cobertura" ) )
                        {
                            return PlexusXmlScannerTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            noLoggingLoader.loadClass( SimpleScanningExample.class.getName() ).newInstance();
        }
        finally
        {
            Logger.getLogger( "" ).setLevel( level );
        }
    }
}
