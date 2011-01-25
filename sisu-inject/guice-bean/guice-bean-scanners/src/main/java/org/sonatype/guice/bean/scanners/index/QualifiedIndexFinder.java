/**
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.scanners.index;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.Streams;
import org.sonatype.guice.bean.scanners.ClassFinder;

public final class QualifiedIndexFinder
    implements ClassFinder
{
    public Enumeration<URL> findClasses( final ClassSpace space )
    {
        final List<URL> components = new ArrayList<URL>();
        final Enumeration<URL> indices = space.findEntries( "META-INF/sisu", "*", false );
        while ( indices.hasMoreElements() )
        {
            try
            {
                final InputStream in = Streams.open( indices.nextElement() );
                try
                {
                    final BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
                    for ( String line = reader.readLine(); line != null; line = reader.readLine() )
                    {
                        components.add( space.getResource( line.replace( '.', '/' ) + ".class" ) );
                    }
                }
                finally
                {
                    in.close();
                }
            }
            catch ( final Throwable e )
            {
                // ignore
            }
        }
        return Collections.enumeration( components );
    }
}
