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
package org.sonatype.guice.bean.reflect;

import java.net.URL;

@Deprecated
public final class URLClassSpace
    extends org.eclipse.sisu.space.URLClassSpace
    implements ClassSpace
{
    public URLClassSpace( final ClassLoader loader )
    {
        super( loader );
    }

    public URLClassSpace( final ClassLoader loader, final URL[] path )
    {
        super( loader, path );
    }
}
