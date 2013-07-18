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

import java.util.Map;

import org.sonatype.inject.BeanScanning;

import com.google.inject.Injector;
import com.google.inject.Module;

@Deprecated
public final class Main
{
    public static void main( final String... args )
    {
        org.eclipse.sisu.launch.Main.main( args );
    }

    public static <T> T boot( final Class<T> type, final String... args )
    {
        return org.eclipse.sisu.launch.Main.boot( type, args );
    }

    public static Injector boot( final Map<?, ?> properties, final String... args )
    {
        return org.eclipse.sisu.launch.Main.boot( properties, args );
    }

    public static Module wire( final BeanScanning scanning, final Module... bindings )
    {
        return org.eclipse.sisu.launch.Main.wire( org.eclipse.sisu.BeanScanning.valueOf( scanning.name() ), bindings );
    }
}
