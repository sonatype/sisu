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

package org.sonatype.guice.bean.containers;

import org.osgi.framework.FrameworkUtil;

import com.google.inject.Injector;

public final class SisuBundleContext
    implements SisuContext
{
    public Injector injector( final Class<?> type )
    {
        return SisuActivator.getInjector( FrameworkUtil.getBundle( type ) );
    }
}
