/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.scanners;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

public class SimpleScanningExample
{
    public SimpleScanningExample()
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        new PlexusXmlScanner( null, null, null ).scan( space, true );
    }
}
