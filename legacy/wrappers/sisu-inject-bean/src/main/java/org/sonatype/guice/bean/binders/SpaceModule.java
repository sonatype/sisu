/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
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
    private final Module delegate;

    public SpaceModule( final ClassSpace space )
    {
        this( space, BeanScanning.ON );
    }

    public SpaceModule( final ClassSpace space, final BeanScanning scanning )
    {
        final org.eclipse.sisu.space.BeanScanning _scanning =
            org.eclipse.sisu.space.BeanScanning.valueOf( scanning.name() );

        delegate = new org.eclipse.sisu.space.SpaceModule( space, _scanning ).with( new LegacyStrategy() );
    }

    public void configure( final Binder binder )
    {
        delegate.configure( binder );
    }

    protected ClassSpaceVisitor visitor( @SuppressWarnings( "unused" ) final Binder binder )
    {
        return null;
    }

    final class LegacyStrategy
        implements org.eclipse.sisu.space.SpaceModule.Strategy
    {
        public org.eclipse.sisu.space.SpaceVisitor visitor( final Binder binder )
        {
            final ClassSpaceVisitor v = SpaceModule.this.visitor( binder );
            return null != v ? ClassSpaceScanner.adapt( v ) : DEFAULT.visitor( binder );
        }
    }
}
