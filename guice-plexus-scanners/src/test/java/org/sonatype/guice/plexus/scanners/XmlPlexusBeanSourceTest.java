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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.WeakClassSpace;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.TypeLiteral;

public class XmlPlexusBeanSourceTest
    extends TestCase
{
    static class NamedProperty
        implements BeanProperty<Object>
    {
        final String name;

        public NamedProperty( final String name )
        {
            this.name = name;
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
            return TypeLiteral.get( Object.class );
        }

        public <B> void set( final B bean, final Object value )
        {
        }
    }

    interface Bean
    {
    }

    static class DefaultBean
    {
    }

    static class DebugBean
    {
    }

    public void testLoadOnStart()
        throws XmlPullParserException, IOException
    {
        final ClassSpace space = new ClassSpace()
        {
            public Enumeration<URL> getResources( final String name )
            {
                // hide components.xml so we can just test plexus.xml parsing
                return Collections.enumeration( Collections.<URL> emptyList() );
            }

            public Class<?> loadClass( final String name )
                throws ClassNotFoundException
            {
                return Class.forName( name );
            }
        };

        final URL plexusDotXml = getClass().getResource( "/META-INF/plexus/plexus.xml" );
        final PlexusBeanSource source = new XmlPlexusBeanSource( plexusDotXml, space, null );
        final Map<Component, DeferredClass<?>> componentMap = source.findPlexusComponentBeans();

        assertEquals( 2, componentMap.size() );

        final Component component1 = new ComponentImpl( Roles.defer( DefaultBean.class ), "default", "load-on-start" );
        assertEquals( DefaultBean.class, componentMap.get( component1 ).get() );

        final Component component2 = new ComponentImpl( Roles.defer( Bean.class ), "debug", "load-on-start" );
        assertEquals( DebugBean.class, componentMap.get( component2 ).get() );
    }

    public void testBadPlexusDotXml()
        throws IOException
    {
        final ClassSpace space = new WeakClassSpace( XmlPlexusBeanSourceTest.class.getClassLoader() );

        try
        {
            new XmlPlexusBeanSource( getClass().getResource( "/META-INF/plexus/bad_plexus_1.xml" ), space, null );
            fail( "Expected XmlPullParserException" );
        }
        catch ( final XmlPullParserException e )
        {
            System.out.println( e );
        }
    }

    public void testComponents()
        throws XmlPullParserException, IOException
    {
        final ClassSpace space = new WeakClassSpace( XmlPlexusBeanSourceTest.class.getClassLoader() );
        final PlexusBeanSource source = new XmlPlexusBeanSource( null, space, null );
        final Map<Component, DeferredClass<?>> componentMap = source.findPlexusComponentBeans();

        assertEquals( 2, componentMap.size() );

        final Component component1 = new ComponentImpl( Roles.defer( DefaultBean.class ), "default", "per-lookup" );
        assertEquals( DefaultBean.class, componentMap.get( component1 ).get() );

        final Component component2 = new ComponentImpl( Roles.defer( Bean.class ), "debug", "singleton" );
        assertEquals( DebugBean.class, componentMap.get( component2 ).get() );

        final PlexusBeanMetadata metadata1 = source.getBeanMetadata( DefaultBean.class );

        assertEquals(
                      new ConfigurationImpl( "someFieldName", "<some-field.name><item>PRIMARY</item></some-field.name>" ),
                      metadata1.getConfiguration( new NamedProperty( "someFieldName" ) ) );

        assertEquals( new RequirementImpl( Roles.defer( Bean.class ), true, "debug" ),
                      metadata1.getRequirement( new NamedProperty( "bean" ) ) );

        assertEquals( new RequirementImpl( Roles.defer( Bean.class ), false, "default", "debug" ),
                      metadata1.getRequirement( new NamedProperty( "beanMap" ) ) );

    }

    static class FixedClassSpace
        implements ClassSpace
    {
        final String fixedResourceName;

        FixedClassSpace( final String fixedResourceName )
        {
            this.fixedResourceName = fixedResourceName;
        }

        public Enumeration<URL> getResources( final String name )
        {
            return Collections.enumeration( Collections.singleton( getClass().getResource( fixedResourceName ) ) );
        }

        public Class<?> loadClass( final String name )
            throws ClassNotFoundException
        {
            return Class.forName( name );
        }
    }

    public void testBadComponentsDotXml()
        throws IOException
    {
        try
        {
            final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/bad_components_1.xml" );
            new XmlPlexusBeanSource( null, space, null );
            fail( "Expected XmlPullParserException" );
        }
        catch ( final XmlPullParserException e )
        {
            System.out.println( e );
        }

        try
        {
            final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/bad_components_2.xml" );
            new XmlPlexusBeanSource( null, space, null );
            fail( "Expected XmlPullParserException" );
        }
        catch ( final XmlPullParserException e )
        {
            System.out.println( e );
        }

        try
        {
            final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/bad_components_3.xml" );
            new XmlPlexusBeanSource( null, space, null );
            fail( "Expected XmlPullParserException" );
        }
        catch ( final XmlPullParserException e )
        {
            System.out.println( e );
        }
    }

    public void testInterpolatedComponentsDotXml()
        throws XmlPullParserException, IOException
    {
        final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/variable_components.xml" );

        final PlexusBeanSource uninterpolatedSource = new XmlPlexusBeanSource( null, space, null );
        final PlexusBeanMetadata metadata1 = uninterpolatedSource.getBeanMetadata( DefaultBean.class );
        assertEquals( "<variable>${some.value}</variable>",
                      metadata1.getConfiguration( new NamedProperty( "variable" ) ).value() );

        final Map<?, ?> variables = Collections.singletonMap( "some.value", "INTERPOLATED" );

        final PlexusBeanSource interpolatedSource = new XmlPlexusBeanSource( null, space, variables );
        final PlexusBeanMetadata metadata2 = interpolatedSource.getBeanMetadata( DefaultBean.class );
        assertEquals( "<variable>INTERPOLATED</variable>",
                      metadata2.getConfiguration( new NamedProperty( "variable" ) ).value() );
    }
}
