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

import java.util.List;

import com.google.inject.Module;

@Deprecated
public class WireModule
    extends org.eclipse.sisu.wire.WireModule
{
    public WireModule( final Module... modules )
    {
        super( modules );
    }

    public WireModule( final List<Module> modules )
    {
        super( modules );
    }
}
