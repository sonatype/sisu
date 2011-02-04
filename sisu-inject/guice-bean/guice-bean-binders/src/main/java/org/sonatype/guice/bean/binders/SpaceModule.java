/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.binders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Qualifier;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.bean.scanners.ClassSpaceVisitor;
import org.sonatype.guice.bean.scanners.QualifiedTypeVisitor;
import org.sonatype.guice.bean.scanners.index.SisuIndexFinder;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

/**
 * Guice {@link Module} that automatically binds types annotated with {@link Qualifier} annotations.
 */
public class SpaceModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static Map<String, List<Element>> cachedElementsMap;

    final ClassSpace space;

    private final BeanScanning scanning;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SpaceModule( final ClassSpace space )
    {
        this( space, BeanScanning.ON );
    }

    public SpaceModule( final ClassSpace space, final BeanScanning scanning )
    {
        this.space = space;
        this.scanning = scanning;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void configure( final Binder binder )
    {
        binder.bind( ClassSpace.class ).toInstance( space );

        final ClassSpaceScanner scanner;
        switch ( scanning )
        {
            default:
            case ON:
                scanner = new ClassSpaceScanner( space );
                break;
            case INDEX:
                scanner = new ClassSpaceScanner( new SisuIndexFinder(), space );
                break;
            case CACHE:
                binder.install( Elements.getModule( cachedScan() ) );
                return;
            case OFF:
                return;
        }

        scanner.accept( visitor( binder ) );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected ClassSpaceVisitor visitor( final Binder binder )
    {
        return new QualifiedTypeVisitor( new QualifiedTypeBinder( binder ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private final synchronized List<Element> cachedScan()
    {
        final String key = space.toString();
        if ( null == cachedElementsMap )
        {
            cachedElementsMap = new HashMap<String, List<Element>>();
        }
        List<Element> elements = cachedElementsMap.get( key );
        if ( null == elements )
        {
            elements = Elements.getElements( new Module()
            {
                public void configure( final Binder binder )
                {
                    new ClassSpaceScanner( space ).accept( visitor( binder ) );
                }
            } );
            cachedElementsMap.put( key, elements );
        }
        return elements;
    }
}
