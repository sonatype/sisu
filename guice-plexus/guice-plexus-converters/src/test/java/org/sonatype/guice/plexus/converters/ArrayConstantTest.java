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

import java.net.URI;
import java.util.Arrays;

import javax.inject.Named;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.util.Jsr330;

public class ArrayConstantTest
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

                bind( "Custom", "<items><item implementation='java.lang.Boolean'>true</item>"
                    + "<item implementation='java.net.URI'>file:temp</item>"
                    + "<item implementation='java.lang.Float'>8.1</item></items>" );

                bind( "Text", "<items><item>1</item><item>2</item><item>3</item></items>" );

                bind( "Numbers", "<items><item>4</item><item>5</item><item>6</item></items>" );

                bind( "Multi", "<as><a><b>1</b><b>2</b></a><a><b>3</b><b>4</b></a><a><b>5</b><b>6</b></a></as>" );

                install( new PlexusXmlBeanConverter() );
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Empty" )
    char[] empty;

    @Inject
    @Named( "Custom" )
    Object[] custom;

    @Inject
    @Named( "Text" )
    String[] text;

    @Inject
    @Named( "Numbers" )
    int[] numbers;

    @Inject
    @Named( "Multi" )
    Integer[][] multi1;

    @Inject
    @Named( "Multi" )
    double[][] multi2;

    public void testEmptyArray()
    {
        assertEquals( 0, empty.length );
    }

    @SuppressWarnings( "boxing" )
    public void testCustomArray()
    {
        assertTrue( Arrays.equals( new Object[] { true, URI.create( "file:temp" ), 8.1f }, custom ) );
    }

    public void testStringArray()
    {
        assertTrue( Arrays.equals( new String[] { "1", "2", "3" }, text ) );
    }

    public void testPrimitiveArray()
    {
        assertTrue( Arrays.equals( new int[] { 4, 5, 6 }, numbers ) );
    }

    @SuppressWarnings( "boxing" )
    public void testMultiArrays()
    {
        assertTrue( Arrays.deepEquals( new Integer[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } }, multi1 ) );
        assertTrue( Arrays.deepEquals( new double[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } }, multi2 ) );
    }
}
