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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Named;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperties;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Strategies;

public class AnnotatedPlexusBeanSourceTest
    extends TestCase
{
    @Component( role = Bean.class )
    protected static class Bean
    {
        @Configuration( name = "1", value = "BLANK" )
        String fixed;

        @Configuration( name = "2", value = "${some.value}" )
        String variable;

        String dummy1;

        @Requirement( role = Bean.class, hint = "mock", optional = true )
        Bean self;

        String dummy2;
    }

    @Component( role = Bean.class )
    static class DuplicateBean
        extends Bean
    {
    }

    @Component( role = AnotherBean.class )
    static class AnotherBean
    {
    }

    @Named( "qualified" )
    static class QualifiedBean
        extends Bean
    {
    }

    public void testNonComponent()
    {
        final PlexusBeanSource source = new AnnotatedPlexusBeanSource( null );
        assertTrue( source.findPlexusComponentBeans().isEmpty() );
        assertNull( source.getBeanMetadata( String.class ) );
        assertNotNull( source.getBeanMetadata( Bean.class ) );
        assertFalse( source.getBeanMetadata( Bean.class ).isEmpty() );
    }

    public void testInterpolatedAnnotations()
    {
        Iterator<BeanProperty<Object>> propertyIterator;

        final PlexusBeanSource uninterpolatedSource = new AnnotatedPlexusBeanSource( null );
        final PlexusBeanMetadata metadata1 = uninterpolatedSource.getBeanMetadata( Bean.class );

        propertyIterator = new BeanProperties( Bean.class ).iterator();
        final Configuration configuration11 = metadata1.getConfiguration( propertyIterator.next() );
        final Configuration configuration12 = metadata1.getConfiguration( propertyIterator.next() );
        final Configuration configuration13 = metadata1.getConfiguration( propertyIterator.next() );
        final Requirement requirement11 = metadata1.getRequirement( propertyIterator.next() );
        final Requirement requirement12 = metadata1.getRequirement( propertyIterator.next() );
        assertFalse( propertyIterator.hasNext() );

        assertFalse( configuration11 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "1", "BLANK" ), configuration11 );
        assertFalse( configuration12 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "2", "${some.value}" ), configuration12 );
        assertNull( configuration13 );
        assertEquals( new RequirementImpl( Bean.class, true, "mock" ), requirement11 );
        assertNull( requirement12 );

        final Map<?, ?> variables = Collections.singletonMap( "some.value", "INTERPOLATED" );

        final PlexusBeanSource interpolatedSource = new AnnotatedPlexusBeanSource( variables );
        final PlexusBeanMetadata metadata2 = interpolatedSource.getBeanMetadata( Bean.class );

        propertyIterator = new BeanProperties( Bean.class ).iterator();
        final Configuration configuration21 = metadata2.getConfiguration( propertyIterator.next() );
        final Configuration configuration22 = metadata2.getConfiguration( propertyIterator.next() );
        final Configuration configuration23 = metadata2.getConfiguration( propertyIterator.next() );
        final Requirement requirement21 = metadata2.getRequirement( propertyIterator.next() );
        final Requirement requirement22 = metadata2.getRequirement( propertyIterator.next() );
        assertFalse( propertyIterator.hasNext() );

        assertFalse( configuration21 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "1", "BLANK" ), configuration21 );
        assertTrue( configuration22 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "2", "INTERPOLATED" ), configuration22 );
        assertNull( configuration23 );
        assertEquals( new RequirementImpl( Bean.class, true, "mock" ), requirement21 );
        assertNull( requirement22 );
    }

    public void testComponentScanning()
    {
        final PlexusBeanSource source =
            new AnnotatedPlexusBeanSource( new URLClassSpace( (URLClassLoader) getClass().getClassLoader() ), null );

        final Map<Component, DeferredClass<?>> components = source.findPlexusComponentBeans();
        assertEquals( 3, components.size() ); // TODO: 4

        final Component beanComponent = new ComponentImpl( Bean.class, "default", Strategies.SINGLETON, "" );
        final Component testComponent = new ComponentImpl( Runnable.class, "test", Strategies.PER_LOOKUP, "Some Test" );

        assertEquals( DuplicateBean.class.getName(), components.get( beanComponent ).getName() );
        assertEquals( TestBean.class, components.get( testComponent ).load() );

        // final Component qualfiedComponent = new ComponentImpl( Bean.class, "qualified", Strategies.PER_LOOKUP, "" );
        // assertEquals( QualifiedBean.class, components.get( qualfiedComponent ).load() );
    }

    public void testBadClassFile()
        throws IOException
    {
        final URL badClassURL = new File( "bad.class" ).toURL();

        final PlexusBeanSource source = new AnnotatedPlexusBeanSource( new ClassSpace()
        {
            public Class<?> loadClass( final String name )
                throws ClassNotFoundException
            {
                throw new ClassNotFoundException();
            }

            public URL getResource( final String name )
            {
                return null;
            }

            public Enumeration<URL> getResources( final String name )
                throws IOException
            {
                throw new IOException();
            }

            public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
                throws IOException
            {
                return new Enumeration<URL>()
                {
                    public boolean hasMoreElements()
                    {
                        return true;
                    }

                    public URL nextElement()
                    {
                        return badClassURL;
                    }
                };
            }

            public DeferredClass<?> deferLoadClass( final String name )
            {
                return null;
            }
        }, null );

        try
        {
            source.findPlexusComponentBeans();
            fail( "Expected RuntimeException" );
        }
        catch ( final RuntimeException e )
        {
        }
    }

    public void testDisambiguatedClassSpace()
        throws IOException
    {
        final ClassSpace originalSpace = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );
        final ClassSpace disambiguatedSpace = new DisambiguatedClassSpace( originalSpace );

        final DeferredClass<?> clazz1 = disambiguatedSpace.deferLoadClass( Bean.class.getName() );
        final DeferredClass<?> clazz2 = disambiguatedSpace.deferLoadClass( Bean.class.getName() );

        final String name1 = clazz1.getName();
        final String name2 = clazz2.getName();

        assertTrue( name1.contains( Bean.class.getName() ) );
        assertTrue( name2.contains( Bean.class.getName() ) );

        assertFalse( name1.equals( name2 ) );

        assertEquals( name1.substring( 0, name1.length() - 1 ), name2.substring( 0, name2.length() - 1 ) );

        assertSame( Bean.class, clazz1.load().getSuperclass() );
        assertSame( Bean.class, clazz2.load().getSuperclass() );

        try
        {
            disambiguatedSpace.findEntries( null, null, false );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testClassVisitorAPI()
    {
        final PlexusBeanScanner visitor = new PlexusBeanScanner( null );

        visitor.setHint( "foo" );
        visitor.setRole( "bar" );

        assertEquals( "foo", visitor.getHint() );
        assertEquals( "bar", visitor.getRole() );

        visitor.setHint( null );

        assertSame( Hints.DEFAULT_HINT, visitor.getHint() );
    }
}
