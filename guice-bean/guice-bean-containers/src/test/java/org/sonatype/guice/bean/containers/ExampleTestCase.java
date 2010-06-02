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

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.Key;
import com.google.inject.name.Names;

public final class ExampleTestCase
    extends InjectedTestCase
{
    @Inject
    @Named( "basedir" )
    String basedir;

    public void testBasedir()
    {
        assertEquals( basedir, getBasedir() );
        assertTrue( new File( getBasedir(), "target/test-classes/inject.properties" ).isFile() );
    }

    @Inject
    Foo bean;

    @Inject
    Map<String, Foo> beans;

    public void testInjection()
    {
        assertTrue( bean instanceof DefaultFoo );

        assertEquals( 3, beans.size() );

        assertTrue( beans.get( "default" ) instanceof DefaultFoo );
        assertTrue( beans.get( NamedFoo.class.getName() ) instanceof NamedFoo );
        assertTrue( beans.get( "NameTag" ) instanceof NamedAndTaggedFoo );

        assertTrue( bean == beans.get( "default" ) );
    }

    public void testContainerLookup()
    {
        assertTrue( lookup( Foo.class ) instanceof DefaultFoo );
        assertTrue( lookup( Key.get( Foo.class, Named.class ) ) instanceof DefaultFoo );
        assertTrue( lookup( Foo.class, "NameTag" ) instanceof NamedAndTaggedFoo );
        assertTrue( lookup( Key.get( Foo.class, Names.named( "NameTag" ) ) ) instanceof NamedAndTaggedFoo );
        assertTrue( lookup( Key.get( Foo.class, Tag.class ) ).getClass().isAnnotationPresent( Tag.class ) );
        assertTrue( lookup( Key.get( Foo.class, new TagImpl( "A" ) ) ) instanceof TaggedFoo );
        assertNull( lookup( Key.get( Foo.class, new TagImpl( "X" ) ) ) );
        assertNull( lookup( Integer.class ) );
    }
}
