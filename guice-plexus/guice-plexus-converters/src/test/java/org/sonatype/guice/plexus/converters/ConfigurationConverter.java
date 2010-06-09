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
package org.sonatype.guice.plexus.converters;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.sonatype.guice.plexus.config.PlexusBeanConverter;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeConverterBinding;

public class ConfigurationConverter
    extends AbstractMatcher<TypeLiteral<?>>
    implements TypeConverter, Module
{
    private List<TypeConverterBinding> otherConverterBindings;

    @Inject
    private PlexusBeanConverter beanConverter;

    private boolean available = true;

    public void configure( final Binder binder )
    {
        binder.convertToTypes( this, this );
        binder.requestInjection( this );
    }

    public boolean matches( final TypeLiteral<?> type )
    {
        for ( final TypeConverterBinding b : otherConverterBindings )
        {
            if ( b.getTypeMatcher().matches( type ) )
            {
                return false;
            }
        }
        return available;
    }

    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        available = false;
        try
        {
            return beanConverter.convert( toType, value );
        }
        finally
        {
            available = true;
        }
    }

    @Inject
    void setTypeConverterBindings( final Injector injector )
    {
        otherConverterBindings = new ArrayList<TypeConverterBinding>();
        for ( final TypeConverterBinding b : injector.getTypeConverterBindings() )
        {
            if ( false == b.getTypeConverter() instanceof ConfigurationConverter )
            {
                otherConverterBindings.add( b );
            }
        }
    }
}
