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
package org.sonatype.guice.bean.binders;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;

public class QualifiedModuleTest
    extends TestCase
{
    @javax.inject.Named
    static class CustomModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bindConstant().annotatedWith( Names.named( "CustomConstant" ) ).to( "CustomValue" );
        }
    }

    @Inject
    @javax.inject.Named( "CustomConstant" )
    private String value;

    @Inject
    private ClassSpace surroundingSpace;

    public void testQualifiedModule()
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        Guice.createInjector( new SpaceModule( space ) ).injectMembers( this );
        assertEquals( surroundingSpace, space );
        assertEquals( "CustomValue", value );
    }
}
