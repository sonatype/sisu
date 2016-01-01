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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Deprecated
public final class Weak
{
    private Weak()
    {
    }

    public static <T> Collection<T> elements()
    {
        return org.eclipse.sisu.inject.Weak.elements();
    }

    public static <T> Collection<T> elements( final int capacity )
    {
        return org.eclipse.sisu.inject.Weak.elements( capacity );
    }

    public static <K, V> Map<K, V> keys()
    {
        return org.eclipse.sisu.inject.Weak.keys();
    }

    public static <K, V> Map<K, V> keys( final int capacity )
    {
        return org.eclipse.sisu.inject.Weak.keys( capacity );
    }

    public static <K, V> ConcurrentMap<K, V> concurrentKeys()
    {
        return org.eclipse.sisu.inject.Weak.concurrentKeys();
    }

    public static <K, V> ConcurrentMap<K, V> concurrentKeys( final int capacity, final int concurrency )
    {
        return org.eclipse.sisu.inject.Weak.concurrentKeys( capacity, concurrency );
    }

    public static <K, V> Map<K, V> values()
    {
        return org.eclipse.sisu.inject.Weak.values();
    }

    public static <K, V> Map<K, V> values( final int capacity )
    {
        return org.eclipse.sisu.inject.Weak.values( capacity );
    }

    public static <K, V> ConcurrentMap<K, V> concurrentValues()
    {
        return org.eclipse.sisu.inject.Weak.concurrentValues();
    }

    public static <K, V> ConcurrentMap<K, V> concurrentValues( final int capacity, final int concurrency )
    {
        return org.eclipse.sisu.inject.Weak.concurrentValues( capacity, concurrency );
    }
}
