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
package org.sonatype.guice.plexus.binders;

import javax.inject.Provider;

import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * Creates {@link Provider}s for properties with @{@link Configuration} metadata.
 */
final class PlexusConfigurations
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Provider<PlexusBeanConverter> converter;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusConfigurations( final TypeEncounter<?> encounter )
    {
        converter = encounter.getProvider( PlexusBeanConverter.class );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link Provider} that provides values that match the given property configuration.
     * 
     * @param configuration The Plexus configuration
     * @param property The bean property
     * @return Provider that provides configured values for the given property
     */
    public <T> Provider<T> lookup( final Configuration configuration, final BeanProperty<T> property )
    {
        return new ConfigurationProvider<T>( converter, property.getType(), configuration.value() );
    }

    // ----------------------------------------------------------------------
    // Implementation classes
    // ----------------------------------------------------------------------

    private static final class ConfigurationProvider<T>
        implements Provider<T>
    {
        private Provider<PlexusBeanConverter> provider;

        private PlexusBeanConverter converter;

        private final TypeLiteral<T> type;

        private final String value;

        ConfigurationProvider( final Provider<PlexusBeanConverter> provider, final TypeLiteral<T> type,
                               final String value )
        {
            this.provider = provider;
            this.type = type;
            this.value = value;
        }

        public synchronized T get()
        {
            if ( null == converter )
            {
                // avoid repeated lookup
                converter = provider.get();
                provider = null;
            }
            // cannot cache this, need per-lookup
            return converter.convert( type, value );
        }
    }
}
