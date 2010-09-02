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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

// various qualified types for testing

interface Foo
{
}

@Qualifier
@Retention( RetentionPolicy.RUNTIME )
@interface Tag
{
    String value();
}

@Named
class NamedFoo
    implements Foo
{
}

@Tag( "A" )
class TaggedFoo
    implements Foo
{
}

@Tag( "B" )
@Named( "NameTag" )
class NamedAndTaggedFoo
    implements Foo
{
}

@Named
@Singleton
class DefaultFoo
    implements Foo
{
}

class TagImpl
    implements Tag
{
    private final String value;

    public TagImpl( final String value )
    {
        this.value = value;
    }

    public String value()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return 127 * "value".hashCode() ^ value.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof Tag )
        {
            return value.equals( ( (Tag) rhs ).value() );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "@" + Tag.class.getName() + "(value=" + value + ")";
    }

    public Class<? extends Annotation> annotationType()
    {
        return Tag.class;
    }
}
