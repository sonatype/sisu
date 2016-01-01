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
package org.sonatype.inject;

import java.lang.annotation.Annotation;

import org.sonatype.guice.bean.containers.SisuGuice;

import com.google.inject.Key;
import com.google.inject.name.Names;

@Deprecated
public final class Sisu
{
    private Sisu()
    {
    }

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
}
