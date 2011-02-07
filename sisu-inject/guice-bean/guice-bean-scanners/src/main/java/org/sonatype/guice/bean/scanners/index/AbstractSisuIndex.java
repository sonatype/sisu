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

/**
 * Skeleton class that can generate {@code META-INF/sisu} index files.
 */
public abstract class AbstractSisuIndex
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String META_INF_SISU = "META-INF/sisu/";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<Object, Set<String>> index = new HashMap<Object, Set<String>>();

    private final Map<Object, BufferedWriter> writers = new HashMap<Object, BufferedWriter>();

    // ----------------------------------------------------------------------
    // Common methods
    // ----------------------------------------------------------------------

    /**
     * Adds a new annotated class entry to the index.
     * 
     * @param anno The annotation name
     * @param clazz The class name
     */
    protected synchronized final void addIndexEntry( final Object anno, final Object clazz )
    {
        if ( !writers.containsKey( anno ) )
        {
            prepareWriter( anno ); // need to do this early on for Java 5 APT
        }
        if ( writers.get( anno ) != null )
        {
            Set<String> table = index.get( anno );
            if ( null == table )
            {
                table = new LinkedHashSet<String>();
                index.put( anno, table );
            }
            table.add( clazz.toString() );
        }
    }

    /**
     * Writes the current index as a series of tables.
     */
    protected synchronized final void saveIndex()
    {
        for ( final Object anno : index.keySet().toArray() )
        {
            // one index file per annotation class name...
            final BufferedWriter writer = writers.remove( anno );
            try
            {
                // one line per implementation class name...
                for ( final String clazz : index.remove( anno ) )
                {
                    writer.write( clazz );
                    writer.newLine();
                }
            }
            catch ( final Throwable e )
            {
                warn( e.toString() );
            }
            finally
            {
                try
                {
                    writer.close();
                }
                catch ( final Throwable e )
                {
                    warn( e.toString() );
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Reports an informational message.
     * 
     * @param message The message
     */
    protected abstract void info( final String message );

    /**
     * Reports a warning message.
     * 
     * @param message The message
     */
    protected abstract void warn( final String message );

    /**
     * Creates a new writer for the given output path.
     * 
     * @param path The output path
     * @return The relevant writer
     */
    protected abstract Writer getWriter( final String path )
        throws IOException;

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Prepares a new writer for the given annotation.
     * 
     * @param anno The annotation name
     */
    private final void prepareWriter( final Object anno )
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter( getWriter( META_INF_SISU + anno ) );
        }
        catch ( final Throwable e )
        {
            warn( e.toString() );
        }
        writers.put( anno, writer );
    }
}
