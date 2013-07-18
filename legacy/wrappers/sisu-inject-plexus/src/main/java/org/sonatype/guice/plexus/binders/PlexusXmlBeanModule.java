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

import java.net.URL;
import java.util.Map;

import org.eclipse.sisu.inject.Legacy;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

import com.google.inject.Binder;

@Deprecated
public final class PlexusXmlBeanModule
    implements PlexusBeanModule
{
    private static final Legacy<org.eclipse.sisu.plexus.PlexusBeanSource> LEGACY_PLEXUS_BEAN_SOURCE =
        Legacy.<org.eclipse.sisu.plexus.PlexusBeanSource, PlexusBeanSource> as( PlexusBeanSource.class );

    private final org.eclipse.sisu.plexus.PlexusBeanModule delegate;

    public PlexusXmlBeanModule( final ClassSpace space, final Map<?, ?> variables, final URL plexusXml )
    {
        delegate = new org.eclipse.sisu.plexus.PlexusXmlBeanModule( space, variables, plexusXml );
    }

    public PlexusXmlBeanModule( final ClassSpace space, final Map<?, ?> variables )
    {
        delegate = new org.eclipse.sisu.plexus.PlexusXmlBeanModule( space, variables );
    }

    public PlexusBeanSource configure( final Binder binder )
    {
        return LEGACY_PLEXUS_BEAN_SOURCE.proxy( delegate.configure( binder ) );
    }
}
