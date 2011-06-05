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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.sonatype.guice.bean.locators.BeanLocator;

import com.google.inject.Injector;
import com.google.inject.Key;

public final class SisuGuice
{
    private static final ThreadLocal<BeanLocator> LOCATOR = new InheritableThreadLocal<BeanLocator>();

    @Inject
    public static void setBeanLocator( BeanLocator locator )
    {
        LOCATOR.set( locator );
    }

    public static BeanLocator getBeanLocator()
    {
        return LOCATOR.get();
    }

    public static <T> T lookup( final Key<T> key )
    {
        final BeanLocator locator = LOCATOR.get();
        if ( null != locator )
        {
            final Iterator<? extends Entry<?, T>> i = locator.locate( key ).iterator();
            if ( i.hasNext() )
            {
                return i.next().getValue();
            }
        }
        return null;
    }

    public static Injector adapt( final Injector injector )
    {
        final Class<?>[] api = { Injector.class };
        return (Injector) Proxy.newProxyInstance( api[0].getClassLoader(), api, new InvocationHandler()
        {
            @SuppressWarnings( { "rawtypes", "unchecked" } )
            public Object invoke( final Object proxy, final Method method, final Object[] args )
                throws Throwable
            {
                if ( "getInstance".equals( method.getName() ) )
                {
                    try
                    {
                        final Key key = args[0] instanceof Key ? (Key) args[0] : Key.get( (Class) args[0] );
                        final Iterator<Entry> i = injector.getInstance( BeanLocator.class ).locate( key ).iterator();
                        return i.hasNext() ? i.next().getValue() : null;
                    }
                    catch ( final Throwable e )
                    {
                        // drop through...
                    }
                }
                return method.invoke( injector, args );
            }
        } );
    }
}
