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

import org.sonatype.guice.bean.reflect.DeclaredMembers.View;

/**
 * {@link Iterable} that iterates over potential bean properties in a class hierarchy.
 */
public final class BeanProperties
    implements Iterable<BeanProperty<Object>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Member> members;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanProperties( final Class<?> clazz )
    {
        if ( clazz.isAnnotationPresent( IgnoreSetters.class ) )
        {
            members = new DeclaredMembers( clazz, View.FIELDS );
        }
        else
        {
            members = new DeclaredMembers( clazz, View.METHODS, View.FIELDS );
        }
    }

    BeanProperties( final Iterable<Member> members )
    {
        this.members = members;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<BeanProperty<Object>> iterator()
    {
        return new BeanPropertyIterator<Object>( members );
    }
}
