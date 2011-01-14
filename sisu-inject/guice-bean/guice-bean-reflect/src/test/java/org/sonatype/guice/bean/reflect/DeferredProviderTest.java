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
package org.sonatype.guice.bean.reflect;

import javax.inject.Inject;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

public class DeferredProviderTest
    extends TestCase
{
    interface A
    {
    }

    interface B
    {
    }

    interface C
    {
    }

    static class AImpl
        implements A
    {
    }

    static class BImpl
        implements B
    {
        @Inject
        A a;
    }

    static class CImpl
        implements C
    {
        @Inject
        B b;
    }

    public void testRootDeferredProvider()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( C.class ).toProvider( new LoadedClass<C>( CImpl.class ).asProvider() );
                bind( B.class ).to( BImpl.class );
                bind( A.class ).to( AImpl.class );
            }
        } ).getInstance( C.class );
    }

    public void testChildDeferredProvider()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( A.class ).to( AImpl.class );
            }
        } ).createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( B.class ).to( BImpl.class );
            }
        } ).createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( C.class ).toProvider( new LoadedClass<C>( CImpl.class ).asProvider() );
            }
        } ).getInstance( C.class );
    }

    public void testBrokenDeferredProvider()
    {
        try
        {
            Guice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( C.class ).toProvider( new LoadedClass<C>( CImpl.class ).asProvider() );
                    bind( CImpl.class ).toProvider( new Provider<CImpl>()
                    {
                        public CImpl get()
                        {
                            throw new ProvisionException( "Broken Provider" );
                        }
                    } );
                }
            } ).getInstance( C.class );

            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        try
        {
            Guice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( C.class ).toProvider( new LoadedClass<C>( CImpl.class ).asProvider() );
                    bind( CImpl.class ).toProvider( new Provider<CImpl>()
                    {
                        public CImpl get()
                        {
                            throw new LinkageError( "Broken Provider" );
                        }
                    } );
                }
            } ).getInstance( C.class );

            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        final ClassSpace space = new URLClassSpace( C.class.getClassLoader(), null );
        try
        {
            Guice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( C.class ).toProvider( new NamedClass<C>( space, CImpl.class.getName() ).asProvider() );
                    bind( CImpl.class ).toProvider( new Provider<CImpl>()
                    {
                        public CImpl get()
                        {
                            throw new ProvisionException( "Broken Provider" );
                        }
                    } );
                }
            } ).getInstance( C.class );

            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }
    }

    public void testDeferredImplementationClass()
    {
        final ClassSpace space = new URLClassSpace( C.class.getClassLoader(), null );

        final DeferredClass<C> clazz1 = new NamedClass<C>( space, CImpl.class.getName() );
        final DeferredClass<C> clazz2 = new LoadedClass<C>( CImpl.class );

        final DeferredProvider<C> provider1 = clazz1.asProvider();
        final DeferredProvider<C> provider2 = clazz2.asProvider();

        assertSame( clazz1, provider1.getImplementationClass() );
        assertSame( clazz2, provider2.getImplementationClass() );

        assertTrue( provider1.toString().contains( clazz1.toString() ) );
        assertTrue( provider2.toString().contains( clazz2.toString() ) );
    }
}
