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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.sonatype.guice.bean.reflect.ClassSpace;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Guice {@link Module} that discovers and auto-wires all qualified beans from a {@link ClassSpace}.
 */
public final class QualifiedScannerModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public QualifiedScannerModule( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        final Enumeration<URL> e = findClasses( space );
        final QualifiedClassVisitor visitor = new QualifiedClassVisitor( space );
        while ( e.hasMoreElements() )
        {
            try
            {
                visitor.scan( e.nextElement() );
            }
            catch ( final Throwable t )
            {
                binder.addError( t );
            }
        }
        new QualifiedClassBinder( binder ).bind( visitor.getQualifiedTypes() );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Scans for all class-files in the given {@link ClassSpace}.
     * 
     * @param space The containing class space
     * @return Enumeration of all class-files in the class space
     */
    private static Enumeration<URL> findClasses( final ClassSpace space )
    {
        try
        {
            return space.findEntries( "", "*.class", true );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( e.toString() );
        }
    }
}
