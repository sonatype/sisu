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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

public final class Custom3TestCase
    extends InjectedTestCase
{
    @Override
    public Map<String, ?> properties()
    {
        final Map<String, String> properties = new HashMap<String, String>();

        properties.put( "hint", "NameTag" );
        properties.put( "port", "8080" );

        return properties;
    }

    @Inject
    @Named( "${hint}" )
    Foo bean;

    @Inject
    @Named( "${port}" )
    int port;

    public void testPerTestCaseCustomization()
    {
        assertTrue( bean instanceof NamedAndTaggedFoo );

        assertEquals( 8080, port );
    }
}
