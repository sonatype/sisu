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
package org.sonatype.guice.bean.reflect;

import com.google.inject.Injector;
import com.google.inject.Module;

@Deprecated
public final class Logs
{
    private Logs()
    {
    }

    public static void debug( final String format, final Object arg1, final Object arg2 )
    {
        org.eclipse.sisu.inject.Logs.trace( format, arg1, arg2 );
    }

    public static void warn( final String format, final Object arg1, final Object arg2 )
    {
        org.eclipse.sisu.inject.Logs.warn( format, arg1, arg2 );
    }

    public static void catchThrowable( final Throwable problem )
    {
        org.eclipse.sisu.inject.Logs.catchThrowable( problem );
    }

    public static void throwUnchecked( final Throwable problem )
    {
        org.eclipse.sisu.inject.Logs.throwUnchecked( problem );
    }

    public static String identityToString( final Object object )
    {
        return org.eclipse.sisu.inject.Logs.identityToString( object );
    }

    public static String toString( final Module module )
    {
        return org.eclipse.sisu.inject.Logs.toString( module );
    }

    public static String toString( final Injector injector )
    {
        return org.eclipse.sisu.inject.Logs.toString( injector );
    }
}
