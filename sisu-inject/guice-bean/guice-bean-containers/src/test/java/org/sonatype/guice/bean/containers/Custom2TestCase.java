/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.bean.containers;

import javax.inject.Inject;

import com.google.inject.Binder;

public final class Custom2TestCase
    extends InjectedTestCase
{
    @Override
    public void configure( final Binder binder )
    {
        // override automatic binding
        binder.bind( Foo.class ).to( TaggedFoo.class );
    }

    @Inject
    Foo bean;

    public void testPerTestCaseCustomization()
    {
        assertTrue( bean instanceof TaggedFoo );
    }
}
