/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
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

                // load each view in turn to get next members
                members = views[viewIndex].members( clazz );
                memberIndex = members.length;

                viewIndex++; // increment here rather than inside array access (workaround JIT bug in 1.6.0_23)
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
