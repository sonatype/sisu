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

import java.util.Arrays;
import java.util.List;

import com.google.inject.Binder;
import com.google.inject.Module;

@Deprecated
public class WireModule
    implements Module
{
    private final Module delegate;

    public WireModule( final Module... modules )
    {
        this( Arrays.asList( modules ) );
    }

    public WireModule( final List<Module> modules )
    {
        delegate = new org.eclipse.sisu.wire.WireModule( modules );
    }

    public void configure( final Binder binder )
    {
        delegate.configure( binder );
    }
}
