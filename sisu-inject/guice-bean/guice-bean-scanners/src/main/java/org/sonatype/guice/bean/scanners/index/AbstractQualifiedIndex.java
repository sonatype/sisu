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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractQualifiedIndex
{
    private final Map<Object, Set<String>> index = new HashMap<Object, Set<String>>();

    private final Map<Object, BufferedWriter> writers = new HashMap<Object, BufferedWriter>();

    protected final void updateIndex( final Object key, final Object line )
    {
        if ( !writers.containsKey( key ) )
        {
            prepareWriter( key );
        }
        Set<String> table = index.get( key );
        if ( null == table )
        {
            table = new LinkedHashSet<String>();
            index.put( key, table );
        }
        table.add( line.toString() );
    }

    private final void prepareWriter( final Object key )
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter( getWriter( "META-INF/sisu/" + key ) );
        }
        catch ( final Throwable e )
        {
            warn( e.toString() );
        }
        writers.put( key, writer );
    }

    protected final void saveIndex()
    {
        for ( final Object key : index.keySet() )
        {
            final BufferedWriter writer = writers.get( key );
            if ( null == writer )
            {
                continue;
            }
            try
            {
                try
                {
                    for ( final String line : index.get( key ) )
                    {
                        writer.write( line );
                        writer.newLine();
                    }
                }
                finally
                {
                    writer.close();
                }
            }
            catch ( final Throwable e )
            {
                warn( e.toString() );
            }
        }
    }

    protected abstract void info( final String message );

    protected abstract void warn( final String message );

    protected abstract Writer getWriter( final String path )
        throws IOException;
}
