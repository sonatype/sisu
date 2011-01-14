/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.locators;

import java.util.Iterator;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.RankedBindingsTest.Bean;
import org.sonatype.guice.bean.locators.RankedBindingsTest.BeanImpl;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.DeferredProvider;
import org.sonatype.guice.bean.reflect.LoadedClass;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.UntargettedBinding;
import com.google.inject.util.Providers;

public class ImplementationVisitorTest
    extends TestCase
{
    Injector injector;

    @Override
    public void setUp()
        throws Exception
    {
        injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "linked" ) ).to( BeanImpl.class );

                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "ctor" ) ).toConstructor( BeanImpl.class.getDeclaredConstructor() );
                }
                catch ( final NoSuchMethodException e )
                {
                }

                bind( Bean.class ).annotatedWith( Names.named( "instance" ) ).toInstance( new BeanImpl() );

                bind( Bean.class ).annotatedWith( Names.named( "deferred" ) ).toProvider( new LoadedClass<Bean>(
                                                                                                                 BeanImpl.class ) );

                install( new PrivateModule()
                {
                    @Override
                    protected void configure()
                    {
                        bind( Bean.class ).annotatedWith( Names.named( "exposed" ) ).to( BeanImpl.class );
                        expose( Bean.class ).annotatedWith( Names.named( "exposed" ) );
                    }
                } );

                bind( Bean.class ).annotatedWith( Names.named( "provider" ) ).toProvider( Providers.of( new BeanImpl() ) );

                bind( Bean.class ).annotatedWith( Names.named( "broken" ) ).toProvider( new DeferredProvider<Bean>()
                {
                    public Bean get()
                    {
                        throw new TypeNotPresentException( "", null );
                    }

                    public DeferredClass<Bean> getImplementationClass()
                    {
                        throw new TypeNotPresentException( "", null );
                    }
                } );

            }
        } );
    }

    public void testImplementationVisitor()
    {
        assertEquals( BeanImpl.class, new UntargettedBinding<BeanImpl>()
        {
            public Key<BeanImpl> getKey()
            {
                return Key.get( BeanImpl.class );
            }

            public Provider<BeanImpl> getProvider()
            {
                return null;
            }

            public <V> V acceptTargetVisitor( final BindingTargetVisitor<? super BeanImpl, V> visitor )
            {
                return visitor.visit( this );
            }

            public <V> V acceptScopingVisitor( final BindingScopingVisitor<V> visitor )
            {
                return null;
            }

            public Object getSource()
            {
                return null;
            }

            public <T> T acceptVisitor( final ElementVisitor<T> visitor )
            {
                return null;
            }

            public void applyTo( final Binder binder )
            {
            }
        }.acceptTargetVisitor( ImplementationVisitor.THIS ) );

        final Iterator<Binding<Bean>> itr = injector.findBindingsByType( TypeLiteral.get( Bean.class ) ).iterator();

        assertEquals( BeanImpl.class, itr.next().acceptTargetVisitor( ImplementationVisitor.THIS ) ); // linked
        assertEquals( BeanImpl.class, itr.next().acceptTargetVisitor( ImplementationVisitor.THIS ) ); // ctor
        assertEquals( BeanImpl.class, itr.next().acceptTargetVisitor( ImplementationVisitor.THIS ) ); // instance
        assertEquals( BeanImpl.class, itr.next().acceptTargetVisitor( ImplementationVisitor.THIS ) ); // deferred
        assertEquals( BeanImpl.class, itr.next().acceptTargetVisitor( ImplementationVisitor.THIS ) ); // exposed

        assertNull( itr.next().acceptTargetVisitor( ImplementationVisitor.THIS ) ); // provider instance
        assertNull( itr.next().acceptTargetVisitor( ImplementationVisitor.THIS ) ); // broken provider

        assertFalse( itr.hasNext() );
    }
}
