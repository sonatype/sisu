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
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;

import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.ImplementedBy;
import com.google.inject.Key;

@Deprecated
@ImplementedBy( MutableBeanLocator.class )
public interface BeanLocator
{
    <Q extends Annotation, T> Iterable<BeanEntry<Q, T>> locate( Key<T> key );

    <Q extends Annotation, T, W> void watch( Key<T> key, Mediator<Q, T, W> mediator, W watcher );
}
