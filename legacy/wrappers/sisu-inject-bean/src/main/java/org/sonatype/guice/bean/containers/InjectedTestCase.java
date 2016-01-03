/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.guice.bean.containers;

import java.lang.annotation.Annotation;
import java.util.Properties;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanScanning;

import com.google.inject.Binder;
import com.google.inject.Module;

import junit.framework.TestCase;

@Deprecated
public abstract class InjectedTestCase
    extends TestCase
    implements Module
{
    private final org.eclipse.sisu.launch.InjectedTest delegate = new org.eclipse.sisu.launch.InjectedTest()
    {
        @Override
        public org.eclipse.sisu.space.ClassSpace space()
        {
            return InjectedTestCase.this.space();
        }

        @Override
        public org.eclipse.sisu.space.BeanScanning scanning()
        {
            return org.eclipse.sisu.space.BeanScanning.valueOf( InjectedTestCase.this.scanning().name() );
        }

        @Override
        public void configure( final Binder binder )
        {
            binder.install( InjectedTestCase.this );
            binder.requestInjection( InjectedTestCase.this );
        }

        @Override
        public void configure( final Properties properties )
        {
            InjectedTestCase.this.configure( properties );
        }
    };

    @Override
    protected void setUp()
        throws Exception
    {
        delegate.setUp();
    }

    @Override
    protected void tearDown()
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
