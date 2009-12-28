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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import junit.framework.TestCase;

public class FindEntriesTest
    extends TestCase
{
    public static void main( final String[] args )
        throws IOException
    {
        final String[] jars = System.getProperty( "java.class.path" ).split( File.pathSeparator );
        final URL[] urls = new URL[jars.length];
        for ( int i = 0; i < jars.length; i++ )
        {
            urls[i] = new URL( "file:" + jars[i] );
        }

        final ClassSpace space = new URLClassSpace( URLClassLoader.newInstance( urls ) );
        final Enumeration<URL> e = space.findEntries( "META-INF", "MANIFEST.MF", false );
        while ( e.hasMoreElements() )
        {
            System.out.println( "=== " + e.nextElement() );
        }
    }
}
