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
package org.sonatype.guice.bean.scanners;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Binder;
import com.google.inject.Key;

public class QualifiedBeanRegistry
{
    private final List<Class<?>> beans = new ArrayList<Class<?>>();

    // @SuppressWarnings( "unchecked" )
    // private final Map<Key, Constructor> constructorMap = new HashMap<Key, Constructor>();

    public void add( @SuppressWarnings( "unused" ) final Key<?> key, final Class<?> beanType )
    {
        beans.add( beanType );

        // final Constructor<?> ctor = (Constructor<?>) InjectionPoint.forConstructorOf( bean ).getMember();
        // constructorMap.put( Key.get( type( bean ), qualify( bean, qualifier ) ), ctor );
    }

    // @SuppressWarnings( "unchecked" )
    public void apply( final Binder binder )
    {
        new QualifiedClassBinder( binder ).bind( beans );

        // for ( final Entry<Key, Constructor> entry : constructorMap.entrySet() )
        // {
        // binder.bind( entry.getKey() ).toConstructor( entry.getValue() );
        // }
    }
}
