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
import org.sonatype.inject.BeanScanning;

@Deprecated
public class SpaceModule
    extends org.eclipse.sisu.space.SpaceModule
{
    public SpaceModule( final ClassSpace space )
    {
        super( space, org.eclipse.sisu.BeanScanning.ON );
    }

    public SpaceModule( final ClassSpace space, final BeanScanning scanning )
    {
        super( space, org.eclipse.sisu.BeanScanning.valueOf( scanning.name() ) );
    }
}
