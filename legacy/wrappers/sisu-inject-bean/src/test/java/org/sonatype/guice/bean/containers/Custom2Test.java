/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
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

import javax.inject.Inject;

import org.testng.Assert;

import com.google.inject.Binder;

public final class Custom2Test
    extends InjectedTest
{
    @Override
    public void configure( final Binder binder )
    {
        // override automatic binding
        binder.bind( Foo.class ).to( TaggedFoo.class );
    }

    @Inject
    Foo bean;

    @org.junit.Test
    @org.testng.annotations.Test
    public void testPerTestCaseCustomization()
    {
        Assert.assertTrue( bean instanceof TaggedFoo );
    }
}
