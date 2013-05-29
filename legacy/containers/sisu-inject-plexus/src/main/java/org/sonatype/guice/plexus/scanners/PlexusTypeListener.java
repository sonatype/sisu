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

import java.lang.annotation.Annotation;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.DeferredClass;

@Deprecated
public interface PlexusTypeListener
{
    void hear( Annotation qualifier, Class<?> qualifiedType, Object source );

    void hear( Component component, DeferredClass<?> implementation, Object source );
}
