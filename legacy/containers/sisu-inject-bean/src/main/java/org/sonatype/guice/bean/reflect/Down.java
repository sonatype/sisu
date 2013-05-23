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
package org.sonatype.guice.bean.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Deprecated
public final class Down
{
    private Down()
    {
    }

    @SuppressWarnings( "unchecked" )
    public static <S, T extends S> T cast( final Class<T> clazz, final S delegate )
    {
        return (T) Proxy.newProxyInstance( clazz.getClassLoader(), new Class<?>[] { clazz }, new InvocationHandler()
        {
            public Object invoke( final Object proxy, final Method method, final Object[] args )
                throws Throwable
            {
                return method.invoke( delegate, args );
            }
        } );
    }
}
