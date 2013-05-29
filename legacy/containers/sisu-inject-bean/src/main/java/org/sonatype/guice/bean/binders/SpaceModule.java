/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.guice.bean.binders;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.bean.scanners.ClassSpaceVisitor;
import org.sonatype.inject.BeanScanning;

import com.google.inject.Binder;
import com.google.inject.Module;

@Deprecated
public class SpaceModule
    implements Module
{
    private final ClassSpace space;

    private final org.eclipse.sisu.BeanScanning scanning;

    public SpaceModule( final ClassSpace space )
    {
        this( space, BeanScanning.ON );
    }

    public SpaceModule( final ClassSpace space, final BeanScanning scanning )
    {
        this.space = space;
        this.scanning = org.eclipse.sisu.BeanScanning.valueOf( scanning.name() );
    }

    public void configure( final Binder binder )
    {
        binder.install( new org.eclipse.sisu.space.SpaceModule( space, scanning )
        {
            @Override
            protected org.eclipse.sisu.space.ClassSpaceVisitor visitor( final Binder _binder )
            {
                final ClassSpaceVisitor v = SpaceModule.this.visitor( _binder );
                return null != v ? ClassSpaceScanner.adapt( v ) : super.visitor( _binder );
            }
        } );
    }

    protected ClassSpaceVisitor visitor( @SuppressWarnings( "unused" ) final Binder binder )
    {
        return null;
    }
}
