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
        final QualifiedBeanRegistry registry = new QualifiedBeanRegistry();
        try
        {
            ClassSpaceScanner.accept( new QualifiedBeanScanner( registry ), space );
        }
        catch ( final Throwable e )
        {
            binder.addError( e );
        }
        registry.apply( binder );
    }
}
