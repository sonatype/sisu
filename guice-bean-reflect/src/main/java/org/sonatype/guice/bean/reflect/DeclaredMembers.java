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
import java.util.NoSuchElementException;

/**
 * {@link Iterable} that iterates over declared members of a class hierarchy.
 */
final class DeclaredMembers
    implements Iterable<Member>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<?> clazz;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DeclaredMembers( final Class<?> clazz )
    {
        this.clazz = clazz;
    }

    // ----------------------------------------------------------------------
    // Standard iterable behaviour
    // ----------------------------------------------------------------------

    public Iterator<Member> iterator()
    {
        return new MemberIterator( clazz );
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    /**
     * Read-only {@link Iterator} that uses rolling {@link View}s to traverse the different members.
     */
    private static final class MemberIterator
        implements Iterator<Member>
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final View[] VIEWS = View.values();

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private Class<?> clazz;

        private int viewIndex;

        private Member[] members = {};

        private int memberIndex;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        MemberIterator( final Class<?> clazz )
        {
            this.clazz = filterClass( clazz );
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            while ( memberIndex >= members.length )
            {
                if ( viewIndex >= VIEWS.length )
                {
                    // no more views, time to move up hierarchy
                    clazz = filterClass( clazz.getSuperclass() );
                    viewIndex = 0;
                }

                if ( null == clazz )
                {
                    return false;
                }

                // load each view in turn to get next members
                members = VIEWS[viewIndex++].members( clazz );
                memberIndex = 0;
            }

            return true;
        }

        public Member next()
        {
            if ( hasNext() )
            {
                // initialized by hasNext()
                return members[memberIndex++];
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        // ----------------------------------------------------------------------
        // Implementation methods
        // ----------------------------------------------------------------------

        private static Class<?> filterClass( final Class<?> clazz )
        {
            return null == clazz || clazz.getName().startsWith( "java." ) ? null : clazz;
        }
    }

    /**
     * {@link Enum} implementation that provides different views of a class's members.
     */
    private static enum View
    {
        // ignore constructors for the moment...
        //
        // CONSTRUCTORS
        // {
        // @Override
        // final Member[] elements( final Class<?> clazz )
        // {
        // return clazz.getDeclaredConstructors();
        // }
        // },
        //
        METHODS
        {
            @Override
            final Member[] members( final Class<?> clazz )
            {
                return clazz.getDeclaredMethods();
            }
        },
        FIELDS
        {
            @Override
            final Member[] members( final Class<?> clazz )
            {
                return clazz.getDeclaredFields();
            }
        };

        abstract Member[] members( final Class<?> clazz );
    }
}
