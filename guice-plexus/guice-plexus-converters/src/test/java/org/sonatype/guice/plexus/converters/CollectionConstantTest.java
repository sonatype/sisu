/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.converters;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.util.Jsr330;

public class CollectionConstantTest
    extends TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new AbstractModule()
        {
            private void bind( final String name, final String value )
            {
                bindConstant().annotatedWith( Jsr330.named( name ) ).to( value );
            }

            @Override
            protected void configure()
            {
                bind( "Empty", "<items/>" );

                bind( "Custom", "<items implementation='java.util.LinkedHashSet'>"
                    + "<item implementation='java.io.File'>FOO</item><item>BAR</item></items>" );

                bind( "Animals", "<animals><animal>cat</animal><animal>dog</animal><animal>aardvark</animal></animals>" );

                bind( "Numbers", "<as><a><b>1</b><b>2</b></a><a><b>3</b><b>4</b></a><a><b>5</b><b>6</b></a></as>" );

                install( new XmlTypeConverter() );
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Empty" )
    List<?> empty;

    @Inject
    @Named( "Custom" )
    Set<?> custom;

    @Inject
    @Named( "Animals" )
    Collection<?> animals;

    @Inject
    @Named( "Numbers" )
    Collection<Collection<Integer>> numbers;

    public void testEmptyCollection()
    {
        assertTrue( empty.isEmpty() );
    }

    public void testCustomCollections()
    {
        assertEquals( LinkedHashSet.class, custom.getClass() );
        final Iterator<?> i = custom.iterator();
        assertEquals( new File( "FOO" ), i.next() );
        assertEquals( "BAR", i.next() );
        assertFalse( i.hasNext() );
    }

    public void testStringCollection()
    {
        assertEquals( Arrays.asList( "cat", "dog", "aardvark" ), animals );
    }

    @SuppressWarnings( { "boxing", "unchecked" } )
    public void testPrimitiveCollection()
    {
        assertEquals( Arrays.asList( Arrays.asList( 1, 2 ), Arrays.asList( 3, 4 ), Arrays.asList( 5, 6 ) ), numbers );
    }
}
