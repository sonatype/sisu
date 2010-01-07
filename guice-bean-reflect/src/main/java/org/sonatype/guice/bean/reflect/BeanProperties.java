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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * {@link Iterable} that iterates over potential bean properties in a class hierarchy.
 */
public final class BeanProperties
    implements Iterable<BeanProperty<Object>>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final Pattern SETTER_PATTERN = Pattern.compile( "set\\p{javaUpperCase}" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Member> members;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanProperties( final Class<?> clazz )
    {
        this( new DeclaredMembers( clazz ) );
    }

    BeanProperties( final Iterable<Member> members )
    {
        this.members = members;
    }

    // ----------------------------------------------------------------------
    // Standard iterable behaviour
    // ----------------------------------------------------------------------

    public Iterator<BeanProperty<Object>> iterator()
    {
        return new BeanPropertyIterator<Object>( members );
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    /**
     * Read-only {@link Iterator} that picks out potential bean properties from members.
     */
    private static final class BeanPropertyIterator<T>
        implements Iterator<BeanProperty<T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Member> i;

        // avoid reporting duplicate properties with same name
        private final Set<String> visited = new HashSet<String>();

        // look-ahead, maintained by hasNext()
        private BeanProperty<T> nextProperty;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BeanPropertyIterator( final Iterable<Member> members )
        {
            i = members.iterator();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            if ( null != nextProperty )
            {
                return true; // no need to check again
            }

            while ( i.hasNext() )
            {
                final Member member = i.next();
                final int modifiers = member.getModifiers();

                // static members can't be properties, abstracts and synthetics are just noise so we ignore them
                if ( Modifier.isStatic( modifiers ) || Modifier.isAbstract( modifiers ) || member.isSynthetic() )
                {
                    continue;
                }

                // ignore any members with @Inject as they will be injected by Guice
                final AnnotatedElement annotatedElement = (AnnotatedElement) member;
                if ( annotatedElement.isAnnotationPresent( com.google.inject.Inject.class )
                    || annotatedElement.isAnnotationPresent( javax.inject.Inject.class ) )
                {
                    continue;
                }

                if ( member instanceof Method )
                {
                    final Method m = (Method) member;
                    if ( m.getParameterTypes().length == 1 && SETTER_PATTERN.matcher( m.getName() ).lookingAt() )
                    {
                        nextProperty = new BeanPropertySetter<T>( m );
                    }
                }
                if ( member instanceof Field && !Modifier.isFinal( modifiers ) )
                {
                    nextProperty = new BeanPropertyField<T>( (Field) member );
                }

                // report one property per name, even if they have different types
                if ( null != nextProperty && visited.add( nextProperty.getName() ) )
                {
                    return true;
                }
                nextProperty = null;
            }

            return false;
        }

        public BeanProperty<T> next()
        {
            if ( hasNext() )
            {
                // look-ahead from hasNext(), remember to reset it
                final BeanProperty<T> tempProperty = nextProperty;
                nextProperty = null;
                return tempProperty;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
