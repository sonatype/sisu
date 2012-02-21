/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu;

import java.lang.annotation.Annotation;

import org.eclipse.sisu.containers.SisuGuice;

import com.google.inject.Key;
import com.google.inject.name.Names;

public final class Sisu
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public static <T> T lookup( final Class<T> type )
    {
        return SisuGuice.lookup( Key.get( type ) );
    }

    public static <T> T lookup( final Class<T> type, final String name )
    {
        return SisuGuice.lookup( Key.get( type, Names.named( name ) ) );
    }

    public static <T> T lookup( final Class<T> type, final Annotation qualifier )
    {
        return SisuGuice.lookup( Key.get( type, qualifier ) );
    }

    public static <T> T lookup( final Class<T> type, final Class<? extends Annotation> qualifier )
    {
        return SisuGuice.lookup( Key.get( type, qualifier ) );
    }

    public static void inject( final Object that )
    {
        SisuGuice.inject( that );
    }

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Sisu()
    {
        // static utility class, not allowed to create instances
    }
}
