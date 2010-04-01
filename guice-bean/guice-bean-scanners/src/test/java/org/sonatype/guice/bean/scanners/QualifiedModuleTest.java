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

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Jsr330;

public class QualifiedModuleTest
    extends TestCase
{
    @Named
    static class CustomModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bindConstant().annotatedWith( Jsr330.named( "CustomConstant" ) ).to( "CustomValue" );
        }
    }

    @Inject
    @Named( "CustomConstant" )
    private String value;

    @Override
    protected void setUp()
        throws Exception
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );
        Guice.createInjector( new QualifiedScannerModule( space ) ).injectMembers( this );
    }

    public void testQualifiedModule()
    {
        assertEquals( "CustomValue", value );
    }
}
