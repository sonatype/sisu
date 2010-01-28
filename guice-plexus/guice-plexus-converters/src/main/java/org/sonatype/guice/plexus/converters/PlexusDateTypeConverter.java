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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;

/**
 * {@link TypeConverter} {@link Module} that converts Plexus formatted date strings into {@link Date}s.
 */
public final class PlexusDateTypeConverter
    extends AbstractMatcher<TypeLiteral<?>>
    implements TypeConverter, Module
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private final static DateFormat[] PLEXUS_DATE_FORMATS =
        { new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.S a" ), new SimpleDateFormat( "yyyy-MM-dd hh:mm:ssa" ),
            new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" ), new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) };

    private static final String CONVERSION_ERROR = "Cannot convert: \"%s\" to: %s";

    // ----------------------------------------------------------------------
    // Guice binding
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        // we're both matcher and converter
        binder.convertToTypes( this, this );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean matches( final TypeLiteral<?> type )
    {
        return Date.class == type.getRawType();
    }

    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        for ( final DateFormat f : PLEXUS_DATE_FORMATS )
        {
            try
            {
                synchronized ( f ) // formats are not thread-safe!
                {
                    return f.parse( value );
                }
            }
            catch ( final ParseException e )
            {
                continue; // try another format
            }
        }
        throw new IllegalArgumentException( String.format( CONVERSION_ERROR, value, Date.class ) );
    }
}
