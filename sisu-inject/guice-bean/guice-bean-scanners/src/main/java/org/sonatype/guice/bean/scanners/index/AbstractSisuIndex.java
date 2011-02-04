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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractSisuIndex
{
    public static final String META_INF_SISU = "META-INF/sisu/";

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
            writer = new BufferedWriter( getWriter( META_INF_SISU + key ) );
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
