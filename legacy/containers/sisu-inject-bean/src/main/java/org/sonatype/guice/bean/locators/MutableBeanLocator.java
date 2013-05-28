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

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;

@Deprecated
@ImplementedBy( DefaultBeanLocator.class )
public interface MutableBeanLocator
    extends BeanLocator
{
    void add( Injector injector, int rank );

    void remove( Injector injector );

    void clear();
}
