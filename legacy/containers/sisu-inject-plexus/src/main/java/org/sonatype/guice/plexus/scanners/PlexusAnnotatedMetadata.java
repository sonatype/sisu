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
package org.sonatype.guice.plexus.scanners;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.bean.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;

@Deprecated
public final class PlexusAnnotatedMetadata
    implements PlexusBeanMetadata
{
    private final org.eclipse.sisu.plexus.PlexusAnnotatedMetadata delegate;

    public PlexusAnnotatedMetadata( final Map<?, ?> variables )
    {
        delegate = new org.eclipse.sisu.plexus.PlexusAnnotatedMetadata( variables );
    }

    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    public Requirement getRequirement( final BeanProperty<?> property )
    {
        return delegate.getRequirement( property );
    }

    public Configuration getConfiguration( final BeanProperty<?> property )
    {
        return delegate.getConfiguration( property );
    }
}
