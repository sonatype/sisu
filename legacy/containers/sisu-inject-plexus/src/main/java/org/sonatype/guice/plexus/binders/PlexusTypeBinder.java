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
package org.sonatype.guice.plexus.binders;

import java.lang.annotation.Annotation;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.scanners.PlexusTypeListener;

import com.google.inject.Binder;

@Deprecated
public final class PlexusTypeBinder
    implements PlexusTypeListener
{
    private final org.eclipse.sisu.plexus.PlexusTypeBinder delegate;

    public PlexusTypeBinder( final Binder binder )
    {
        delegate = new org.eclipse.sisu.plexus.PlexusTypeBinder( binder );
    }

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        delegate.hear( qualifier, qualifiedType, source );
    }

    public void hear( final Component component, final DeferredClass<?> implementation, final Object source )
    {
        delegate.hear( component, implementation, source );
    }
}
