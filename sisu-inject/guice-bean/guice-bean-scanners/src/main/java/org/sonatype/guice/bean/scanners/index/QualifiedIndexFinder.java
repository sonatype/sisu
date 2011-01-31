/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

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
