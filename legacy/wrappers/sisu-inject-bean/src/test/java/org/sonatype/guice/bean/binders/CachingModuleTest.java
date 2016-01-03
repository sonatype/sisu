/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.binders;

import javax.inject.Named;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanScanning;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import junit.framework.TestCase;

public class CachingModuleTest
    extends TestCase
{
    @Named
    static class CustomModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            requireBinding( CachingModuleTest.class );
            getMembersInjector( CachingModuleTest.class );
        }
    }

    public void testQualifiedModule()
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );

        Guice.createInjector( new SpaceModule( space, BeanScanning.CACHE ) );
        Guice.createInjector( new SpaceModule( space, BeanScanning.CACHE ) );
        Guice.createInjector( new SpaceModule( space, BeanScanning.CACHE ) );
    }
}
