package org.sonatype.guice.plexus.scanners;

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

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;

/**
 * {@link QualifiedTypeListener} that also listens for Plexus components.
 */
public interface PlexusTypeListener
    extends QualifiedTypeListener
{
    /**
     * Invoked when the {@link PlexusTypeListener} finds a Plexus component.
     * 
     * @param component The Plexus component
     * @param implementation The implementation
     * @param source The source of this component
     */
    void hear( Component component, DeferredClass<?> implementation, Object source );
}
