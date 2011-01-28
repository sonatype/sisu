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
public final class DeclaredMembers
    implements Iterable<Member>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<?> clazz;

    private final View[] views;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DeclaredMembers( final Class<?> clazz, final View... views )
    {
        this.clazz = clazz;
        this.views = views.length == 0 ? View.values() : views;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<Member> iterator()
    {
        return new MemberIterator( clazz, views );
    }

    // ----------------------------------------------------------------------
    // Implementation types
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

        private static final Member[] NO_MEMBERS = new Member[0];

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private Class<?> clazz;

        private final View[] views;

        private int viewIndex;

        private Member[] members = NO_MEMBERS;

        private int memberIndex;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        MemberIterator( final Class<?> clazz, final View[] views )
        {
            this.clazz = filterClass( clazz );
            this.views = views;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            while ( memberIndex <= 0 )
            {
                if ( viewIndex >= views.length )
                {
                    // no more views, time to move up hierarchy
                    clazz = filterClass( clazz.getSuperclass() );
                    viewIndex = 0;
                }

                if ( null == clazz )
                {
                    return false;
                }

                try
                {
                    // load each view in turn to get next members
                    members = views[viewIndex].members( clazz );
                    memberIndex = members.length;
                    viewIndex++;
                }
                catch ( final Throwable e )
                {
                    // truncate
                    clazz = null;
                    viewIndex = 0;
                    return false;
                }
            }

            return true;
        }

        public Member next()
        {
            if ( hasNext() )
            {
                // initialized by hasNext()
                return members[--memberIndex];
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
            // deliberately ignore all java.* classes because we won't be injecting them
            return null == clazz || clazz.getName().startsWith( "java." ) ? null : clazz;
        }
    }

    /**
     * {@link Enum} implementation that provides different views of a class's members.
     */
    public static enum View
    {
        CONSTRUCTORS
        {
            @Override
            final Member[] members( final Class<?> clazz )
            {
                return clazz.getDeclaredConstructors();
            }
        },
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
