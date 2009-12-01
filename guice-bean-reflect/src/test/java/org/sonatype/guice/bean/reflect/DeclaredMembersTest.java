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
package org.sonatype.guice.bean.reflect;

import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class DeclaredMembersTest
    extends TestCase
{
    interface A
    {
        char a = 'a';

        void a();
    }

    static class B
        implements A
    {
        public B()
        {
        }

        char b = 'b';

        public void a()
        {
        }
    }

    interface C
        extends A
    {
        char c = 'c';

        void c();
    }

    static class D
        extends B
        implements C
    {
        public D()
        {
        }

        char d = 'd';

        public void c()
        {
        }
    }

    public void testNullClass()
    {
        final Iterator<Member> i = new DeclaredMembers( null ).iterator();

        assertFalse( i.hasNext() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testJavaClass()
    {
        final Iterator<Member> i = new DeclaredMembers( List.class ).iterator();

        assertFalse( i.hasNext() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }
    }

    public void testReadOnlyIterator()
    {
        final Iterator<Member> i = new DeclaredMembers( D.class ).iterator();

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testInterfaceHierarchy()
        throws NoSuchMethodException, NoSuchFieldException
    {
        final Member[] elements = { C.class.getDeclaredMethod( "c" ), C.class.getDeclaredField( "c" ) };

        int i = 0;
        for ( final Member e : new DeclaredMembers( C.class ) )
        {
            assertEquals( elements[i++], e );
        }
        assertEquals( 2, i );
    }

    public void testClassHierarchy()
        throws NoSuchMethodException, NoSuchFieldException
    {
        final Member[] elements =
            { /* D.class.getDeclaredConstructor(), */D.class.getDeclaredMethod( "c" ), D.class.getDeclaredField( "d" ),
            /* B.class.getDeclaredConstructor(), */B.class.getDeclaredMethod( "a" ), B.class.getDeclaredField( "b" ) };

        int i = 0;
        for ( final Member e : new DeclaredMembers( D.class ) )
        {
            assertEquals( elements[i++], e );
        }
        assertEquals( /* 6 including constructors */4, i );
    }
}
