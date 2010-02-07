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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Provider;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.adapters.EntryListAdapter;
import org.sonatype.guice.plexus.adapters.EntryMapAdapter;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * Creates {@link Provider}s for properties with @{@link Requirement} metadata.
 */
final class PlexusRequirements
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Provider<PlexusBeanLocator> locator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusRequirements( final TypeEncounter<?> encounter )
    {
        locator = encounter.getProvider( PlexusBeanLocator.class );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link Provider} that provides Plexus components that match the given property requirement.
     * 
     * @param requirement The Plexus requirement
     * @param property The bean property
     * @return Provider that provides required Plexus components for the given property
     */
    @SuppressWarnings( "unchecked" )
    public <T> Provider<T> lookup( final Requirement requirement, final BeanProperty<T> property )
    {
        // deduce lookup from metadata + property details
        final TypeLiteral expectedType = property.getType();
        final TypeLiteral<T> roleType = (TypeLiteral) Roles.roleType( requirement, expectedType );
        final Class rawType = expectedType.getRawType();

        final String[] hints = Hints.canonicalHints( requirement );

        if ( Map.class == rawType )
        {
            return new RequirementMapProvider( locator, roleType, hints );
        }
        else if ( List.class == rawType )
        {
            return new RequirementListProvider( locator, roleType, hints );
        }

        return new RequirementProvider( locator, roleType, hints );
    }

    // ----------------------------------------------------------------------
    // Implementation classes
    // ----------------------------------------------------------------------

    private static abstract class AbstractRequirementProvider<S, T>
        implements Provider<S>
    {
        private Provider<PlexusBeanLocator> provider;

        private PlexusBeanLocator locator;

        final TypeLiteral<T> type;

        final String[] hints;

        AbstractRequirementProvider( final Provider<PlexusBeanLocator> provider, final TypeLiteral<T> type,
                                     final String... hints )
        {
            this.provider = provider;
            this.type = type;
            this.hints = hints;
        }

        final synchronized Iterable<Entry<String, T>> locate()
        {
            if ( null != provider )
            {
                // avoid repeated lookup
                locator = provider.get();
                provider = null;
            }
            // cannot cache this, need per-lookup
            return locator.locate( type, hints );
        }
    }

    private static final class RequirementMapProvider<T>
        extends AbstractRequirementProvider<Map<String, T>, T>
    {
        RequirementMapProvider( final Provider<PlexusBeanLocator> provider, final TypeLiteral<T> type,
                                final String... hints )
        {
            super( provider, type, hints );
        }

        public Map<String, T> get()
        {
            return new EntryMapAdapter<String, T>( locate() );
        }
    }

    private static final class RequirementListProvider<T>
        extends AbstractRequirementProvider<List<T>, T>
    {
        RequirementListProvider( final Provider<PlexusBeanLocator> provider, final TypeLiteral<T> type,
                                 final String... hints )
        {
            super( provider, type, hints );
        }

        public List<T> get()
        {
            return new EntryListAdapter<String, T>( locate() );
        }
    }

    private static final class RequirementProvider<T>
        extends AbstractRequirementProvider<T, T>
    {
        RequirementProvider( final Provider<PlexusBeanLocator> provider, final TypeLiteral<T> type,
                             final String... hints )
        {
            super( provider, type, hints );
        }

        public T get()
        {
            final Iterator<Entry<String, T>> i = locate().iterator();
            if ( i.hasNext() )
            {
                return i.next().getValue();
            }
            return Roles.throwMissingComponentException( type, null );
        }
    }
}
