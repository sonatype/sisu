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
package org.sonatype.guice.bean.scanners.index;

import java.io.File;

import org.sonatype.guice.bean.reflect.ClassSpace;

@Deprecated
public final class SisuIndex
{
    private final org.eclipse.sisu.space.SisuIndex delegate;

    public SisuIndex( final File targetDirectory )
    {
        delegate = new org.eclipse.sisu.space.SisuIndex( targetDirectory );
    }

    public static void main( final String[] args )
    {
        org.eclipse.sisu.space.SisuIndex.main( args );
    }

    public void index( final ClassSpace space )
    {
        delegate.index( space );
    }
}
