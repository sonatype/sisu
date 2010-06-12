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
package org.sonatype.guice.bean.binders;

import org.sonatype.guice.bean.converters.FileTypeConverter;
import org.sonatype.guice.bean.converters.URLTypeConverter;
import org.sonatype.guice.bean.locators.BeanLocator;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

/**
 * Guice {@link Module} that automatically adds {@link BeanLocator}-backed bindings for non-local bean dependencies.
 */
public final class BeanImportModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Module[] modules;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanImportModule( final Module... modules )
    {
        this.modules = modules.clone();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        binder.install( new FileTypeConverter() );
        binder.install( new URLTypeConverter() );

        final ElementAnalyzer analyzer = new ElementAnalyzer( binder );
        for ( final Module m : modules )
        {
            for ( final Element e : Elements.getElements( m ) )
            {
                e.acceptVisitor( analyzer );
            }
        }
        new ImportBinder( binder.withSource( BeanLocator.HIDDEN_SOURCE ) ).bind( analyzer.getImportedKeys() );
    }
}
