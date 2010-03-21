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
package org.sonatype.guice.bean.scanners;

import java.net.URLClassLoader;
import java.util.RandomAccess;

import javax.enterprise.inject.Typed;
import javax.inject.Named;

import junit.framework.TestCase;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class QualifiedScannerModuleTest
    extends TestCase
{
    static abstract class AbstractBean
    {
    }

    @Typed
    static abstract class AbstractDefaultTypedBean
    {
    }

    @Typed( {} )
    static abstract class AbstractEmptyTypedBean
    {
    }

    @Named
    @Typed( { Runnable.class } )
    static abstract class AbstractTypedBean
    {
    }

    static abstract class AbstractBeanWithInterface
        implements RandomAccess
    {
    }

    @Typed
    static abstract class AbstractDefaultTypedBeanWithInterface
        implements RandomAccess
    {
    }

    @Typed( {} )
    static abstract class AbstractEmptyTypedBeanWithInterface
        implements RandomAccess
    {
    }

    @Typed( { Runnable.class } )
    static abstract class AbstractTypedBeanWithInterface
        implements RandomAccess
    {
    }

    static class DefaultBean
        extends AbstractTypedBean
    {
    }

    public void testComponentScanning()
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );
        final Injector injector = Guice.createInjector( new QualifiedScannerModule( space ) );
        for ( final Binding<?> b : injector.getBindings().values() )
        {
            System.out.println( b.getKey() + " ---> " + b.getProvider() );
        }
    }
}
