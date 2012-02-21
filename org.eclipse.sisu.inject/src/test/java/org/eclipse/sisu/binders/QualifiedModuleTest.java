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
package org.eclipse.sisu.binders;

import java.net.URL;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.reflect.URLClassSpace;

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
        final ClassSpace space =
            new URLClassSpace( getClass().getClassLoader(), new URL[] { getClass().getResource( "" ) } );
        Guice.createInjector( new SpaceModule( space ) ).injectMembers( this );
        assertEquals( surroundingSpace, space );
        assertEquals( "CustomValue", value );
    }
}