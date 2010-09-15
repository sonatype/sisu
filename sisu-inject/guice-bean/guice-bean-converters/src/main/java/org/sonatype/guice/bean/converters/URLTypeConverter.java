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
package org.sonatype.guice.bean.converters;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

/**
 * {@link TypeConverter} {@link Module} that converts constants to {@link URL}s.
 */
public final class URLTypeConverter
    extends AbstractTypeConverter<URL>
{
    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        try
        {
            return new URL( value );
        }
        catch ( final MalformedURLException e )
        {
            throw new ProvisionException( e.toString() );
        }
    }
}
