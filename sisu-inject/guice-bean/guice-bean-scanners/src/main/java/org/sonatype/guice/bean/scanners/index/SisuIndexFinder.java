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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.guice.bean.reflect.Streams;
import org.sonatype.guice.bean.scanners.ClassFinder;

/**
 * {@link ClassFinder} that uses the qualified class index to select implementations to scan.
 */
public final class SisuIndexFinder
    implements ClassFinder
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Enumeration<URL> findClasses( final ClassSpace space )
    {
        final List<URL> components = new ArrayList<URL>();
        final Set<String> visited = new HashSet<String>();
        final Enumeration<URL> indices = space.findEntries( AbstractSisuIndex.SISU_INDEX_DIR, "*", false );
        while ( indices.hasMoreElements() )
        {
            final URL url = indices.nextElement();
            try
            {
                final BufferedReader reader = new BufferedReader( new InputStreamReader( Streams.open( url ) ) );
                try
                {
                    // each index file contains a list of classes with that qualifier, one per line
                    for ( String line = reader.readLine(); line != null; line = reader.readLine() )
                    {
                        if ( visited.add( line ) )
                        {
                            final URL clazz = space.getResource( line.replace( '.', '/' ) + ".class" );
                            if ( null != clazz )
                            {
                                components.add( clazz );
                            }
                        }
                    }
                }
                finally
                {
                    reader.close();
                }
            }
            catch ( final Throwable e )
            {
                Logs.warn( "Problem reading: " + url, e );
            }
        }
        return Collections.enumeration( components );
    }
}
