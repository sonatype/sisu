/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.containers;

import java.net.URLClassLoader;
import java.util.Map;

import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 * Bootstrap class that creates a static {@link Injector} by scanning the current class-path for beans.
 */
public final class Main
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<?, ?> config;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Main( final Map<?, ?> config )
    {
        this.config = config;
    }

    // ----------------------------------------------------------------------
    // Public entry points
    // ----------------------------------------------------------------------

    public static void main( final String[] args )
    {
        boot( System.getProperties() );
    }

    public static <T> T boot( final Class<T> type )
    {
        return boot( Key.get( type ), System.getProperties() );
    }

    public static <T> T boot( final Class<T> type, final String name )
    {
        return boot( Key.get( type, Names.named( name ) ), System.getProperties() );
    }

    public static <T> T boot( final Key<T> key, final Map<?, ?> config )
    {
        return boot( config ).getInstance( key );
    }

    public static Injector boot( final Map<?, ?> config )
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) Main.class.getClassLoader() );
        return Guice.createInjector( new WireModule( new Main( config ), new SpaceModule( space ) ) );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        binder.bind( WireModule.CONFIG_KEY ).toInstance( config );
    }
}
