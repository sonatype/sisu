/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.guice.bean.containers;

import java.lang.annotation.Annotation;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanScanning;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.google.inject.Binder;
import com.google.inject.Module;

@Deprecated
public abstract class InjectedTest
    implements Module
{
    private final org.eclipse.sisu.launch.InjectedTest delegate = new org.eclipse.sisu.launch.InjectedTest()
    {
        @Override
        public org.eclipse.sisu.space.ClassSpace space()
        {
            return InjectedTest.this.space();
        }

        @Override
        public org.eclipse.sisu.BeanScanning scanning()
        {
            return org.eclipse.sisu.BeanScanning.valueOf( InjectedTest.this.scanning().name() );
        }

        @Override
        public void configure( final Binder binder )
        {
            binder.install( InjectedTest.this );
            binder.requestInjection( InjectedTest.this );
        }

        @Override
        public void configure( final Properties properties )
        {
            InjectedTest.this.configure( properties );
        }
    };

    @Before
    @BeforeMethod
    public void setUp()
        throws Exception
    {
        delegate.setUp();
    }

    @After
    @AfterMethod
    public void tearDown()
        throws Exception
    {
        delegate.tearDown();
    }

    public ClassSpace space()
    {
        return new URLClassSpace( getClass().getClassLoader() );
    }

    public BeanScanning scanning()
    {
        return BeanScanning.CACHE;
    }

    public void configure( final Binder binder )
    {
    }

    /**
     * @param properties Custom test properties
     */
    public void configure( final Properties properties )
    {
    }

    public final <T> T lookup( final Class<T> type )
    {
        return delegate.lookup( type );
    }

    public final <T> T lookup( final Class<T> type, final String name )
    {
        return delegate.lookup( type, name );
    }

    public final <T> T lookup( final Class<T> type, final Class<? extends Annotation> qualifier )
    {
        return delegate.lookup( type, qualifier );
    }

    public final <T> T lookup( final Class<T> type, final Annotation qualifier )
    {
        return delegate.lookup( type, qualifier );
    }

    public final String getBasedir()
    {
        return delegate.getBasedir();
    }
}
