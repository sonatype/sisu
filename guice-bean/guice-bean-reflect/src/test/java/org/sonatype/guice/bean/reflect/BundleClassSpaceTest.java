/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.reflect;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

public class BundleClassSpaceTest
    extends TestCase
{
    private static final URL COMMONS_LOGGING_JAR =
        ZipEntryIteratorTest.class.getClassLoader().getResource( "commons-logging-1.1.1.jar" );

    private Framework framework;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        final Map<String, String> configuration = new HashMap<String, String>();
        configuration.put( Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT );
        configuration.put( Constants.FRAMEWORK_STORAGE, "target/bundlecache" );

        final FrameworkFactory frameworkFactory = new FrameworkFactory();
        framework = frameworkFactory.newFramework( configuration );
        framework.start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        framework.stop();
        super.tearDown();
    }

    public void testClassSpaceResources()
        throws Exception
    {
        final Bundle testBundle = framework.getBundleContext().installBundle( COMMONS_LOGGING_JAR.toString() );
        final ClassSpace space = new BundleClassSpace( testBundle );

        Enumeration<URL> e;

        e = space.getResources( "META-INF/MANIFEST.MF" );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/META-INF/MANIFEST.MF" ) );
        assertFalse( e.hasMoreElements() );

        e = space.findEntries( "META-INF", "*.MF", false );
        assertTrue( e.hasMoreElements() );
        assertTrue( e.nextElement().toString().matches( "bundle://.*/META-INF/MANIFEST.MF" ) );
        assertFalse( e.hasMoreElements() );

        final URL manifestURL = space.getResource( "META-INF/MANIFEST.MF" );
        assertNotNull( manifestURL );
        new Manifest( manifestURL.openStream() );
    }

    public void testDeferredClass()
        throws Exception
    {
        final Bundle testBundle = framework.getBundleContext().installBundle( COMMONS_LOGGING_JAR.toString() );
        final ClassSpace space = new BundleClassSpace( testBundle );

        final String clazzName = "org.apache.commons.logging.Log";
        final DeferredClass<?> clazz = space.deferLoadClass( clazzName );

        assertEquals( clazzName, clazz.getName() );
        assertSame( space.loadClass( clazzName ), clazz.get() );
    }
}
