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

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.Parameters;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;

/**
 * Bootstrap class that creates a static {@link Injector} by scanning the current class-path for beans.
 */
public final class Main
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<String, String> properties;

    private final String[] args;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Main( final Map<String, String> properties, final String... args )
    {
        this.properties = Collections.unmodifiableMap( properties );
        this.args = args;
    }

    // ----------------------------------------------------------------------
    // Public entry points
    // ----------------------------------------------------------------------

    public static void main( final String... args )
    {
        boot( System.getProperties(), args );
    }

    public static <T> T boot( final Class<T> type, final String... args )
    {
        return boot( System.getProperties(), args ).getInstance( type );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public static Injector boot( final Properties properties, final String... args )
    {
        return boot( (Map) properties, args );
    }

    public static Injector boot( final Map<String, String> properties, final String... args )
    {
        final ClassSpace space = new URLClassSpace( Main.class.getClassLoader() );
        final Module[] modules = { new Main( properties, args ), new SpaceModule( space ) };
        final Injector injector = Guice.createInjector( new WireModule( modules ) );

        SisuContainer.context( new SisuStaticContext( injector ) );

        return injector;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        binder.bind( ParameterKeys.PROPERTIES ).toInstance( properties );
        binder.bind( ShutdownThread.class ).asEagerSingleton();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Provides
    @Parameters
    String[] parameters()
    {
        return args.clone();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    static final class ShutdownThread
        extends Thread
    {
        private final MutableBeanLocator locator;

        @Inject
        ShutdownThread( final MutableBeanLocator locator )
        {
            this.locator = locator;

            Runtime.getRuntime().addShutdownHook( this );
        }

        @Override
        public void run()
        {
            locator.clear();
        }
    }
}
