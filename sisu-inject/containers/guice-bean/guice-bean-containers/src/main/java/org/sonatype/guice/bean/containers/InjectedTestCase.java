/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.containers;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanScanning;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 * Abstract JUnit3 {@link TestCase} that automatically binds and injects itself.
 */
public abstract class InjectedTestCase
    extends TestCase
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String basedir;

    @Inject
    private MutableBeanLocator locator;

    // ----------------------------------------------------------------------
    // Setup
    // ----------------------------------------------------------------------

    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new WireModule( new SetUpModule(), new SpaceModule( space(), scanning() ) ) );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        locator.clear();
    }

    final class SetUpModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install( InjectedTestCase.this );

            final Properties properties = new Properties();
            properties.put( "basedir", getBasedir() );
            InjectedTestCase.this.configure( properties );

            bind( ParameterKeys.PROPERTIES ).toInstance( properties );

            requestInjection( InjectedTestCase.this );
        }
    }

    public ClassSpace space()
    {
        return new URLClassSpace( getClass().getClassLoader() );
    }

    public BeanScanning scanning()
    {
        return BeanScanning.CACHE;
    }

    // ----------------------------------------------------------------------
    // Container configuration methods
    // ----------------------------------------------------------------------

    /**
     * Custom injection bindings.
     * 
     * @param binder The Guice binder
     */
    public void configure( final Binder binder )
    {
        // place any per-test bindings here...
    }

    /**
     * Custom property values.
     * 
     * @param properties The test properties
     */
    public void configure( final Properties properties )
    {
        // put any per-test properties here...
    }

    // ----------------------------------------------------------------------
    // Container lookup methods
    // ----------------------------------------------------------------------

    public final <T> T lookup( final Class<T> type )
    {
        return lookup( Key.get( type ) );
    }

    public final <T> T lookup( final Class<T> type, final String name )
    {
        return lookup( type, Names.named( name ) );
    }

    public final <T> T lookup( final Class<T> type, final Class<? extends Annotation> qualifier )
    {
        return lookup( Key.get( type, qualifier ) );
    }

    public final <T> T lookup( final Class<T> type, final Annotation qualifier )
    {
        return lookup( Key.get( type, qualifier ) );
    }

    // ----------------------------------------------------------------------
    // Container resource methods
    // ----------------------------------------------------------------------

    public final String getBasedir()
    {
        if ( null == basedir )
        {
            basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );
        }
        return basedir;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private final <T> T lookup( final Key<T> key )
    {
        final Iterator<? extends Entry<Annotation, T>> i = locator.locate( key ).iterator();
        return i.hasNext() ? i.next().getValue() : null;
    }
}
