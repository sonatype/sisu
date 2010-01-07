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
package org.sonatype.guice.bean.inject;

import junit.framework.TestCase;

import org.sonatype.guice.bean.reflect.BeanProperty;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeEncounter;

public class PropertyListenerTest
    extends TestCase
{
    static class Base
    {
        String a;
    }

    static class Bean0
        extends Base
    {
        String last;
    }

    static class Bean1
        extends Base
    {
        String b;
    }

    static class Bean2
        extends Base
    {
        String b;

        String ignore;

        String c;

        String last;

        String d;
    }

    static class Bean3
        extends Base
    {
        String b;

        String error;

        String c;
    }

    static class Bean4
        extends Bean1
    {
        void setA( @SuppressWarnings( "unused" ) final String unused )
        {
            assertNull( a ); // hidden by our setter method
            assertNotNull( b ); // should be injected first
            a = "correct order";
        }
    }

    static class NamedPropertyBinder
        implements PropertyBinder
    {
        public <T> PropertyBinding bindProperty( final BeanProperty<T> property )
        {
            if ( "last".equals( property.getName() ) )
            {
                return PropertyBinder.LAST_BINDING;
            }
            if ( "ignore".equals( property.getName() ) )
            {
                return null;
            }
            if ( "error".equals( property.getName() ) )
            {
                throw new RuntimeException( "Broken binding" );
            }
            return new PropertyBinding()
            {
                @SuppressWarnings( "unchecked" )
                public void injectProperty( final Object bean )
                {
                    property.set( bean, (T) ( property.getName() + "Value" ) );
                }
            };
        }
    }

    static final PropertyBinder namedPropertyBinder = new NamedPropertyBinder();

    Injector injector;

    @Override
    public void setUp()
    {
        injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindListener( new AbstractMatcher<TypeLiteral<?>>()
                {
                    public boolean matches( final TypeLiteral<?> type )
                    {
                        return Base.class.isAssignableFrom( type.getRawType() );
                    }
                }, new BeanListener( new BeanBinder()
                {
                    public <T> PropertyBinder bindBean( final TypeLiteral<T> type, final TypeEncounter<T> encounter )
                    {
                        return type.getRawType().getName().contains( "Bean" ) ? namedPropertyBinder : null;
                    }
                } ) );
            }
        } );
    }

    public void testNoBean()
    {
        final Base base = injector.getInstance( Base.class );
        assertNull( base.a );
    }

    public void testNoBindings()
    {
        final Bean0 bean0 = injector.getInstance( Bean0.class );
        assertNull( bean0.a );
    }

    public void testPropertyBindings()
    {
        final Bean1 bean1 = injector.getInstance( Bean1.class );
        assertEquals( "aValue", bean1.a );
        assertEquals( "bValue", bean1.b );
    }

    public void testSpecialProperties()
    {
        final Bean2 bean2 = injector.getInstance( Bean2.class );
        assertNull( bean2.a );
        assertEquals( "bValue", bean2.b );
        assertEquals( "cValue", bean2.c );
        assertNull( bean2.d );

        try
        {
            PropertyBinder.LAST_BINDING.injectProperty( bean2 );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testBrokenBinding()
    {
        try
        {
            injector.getInstance( Bean3.class );
            fail( "Expected ConfigurationException" );
        }
        catch ( final ConfigurationException e )
        {
            System.out.println( e );
        }
    }

    public void testInjectionOrder()
    {
        assertEquals( "correct order", injector.getInstance( Bean4.class ).a );
    }
}
