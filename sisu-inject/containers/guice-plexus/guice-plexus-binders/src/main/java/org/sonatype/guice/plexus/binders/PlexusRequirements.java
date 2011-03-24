/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.plexus.binders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Provider;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.locators.EntryListAdapter;
import org.sonatype.guice.bean.locators.EntryMapAdapter;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.ProvisionException;
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

    private final Provider<PlexusBeanLocator> locatorProvider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusRequirements( final TypeEncounter<?> encounter )
    {
        locatorProvider = encounter.getProvider( PlexusBeanLocator.class );
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
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public <T> Provider<T> lookup( final Requirement requirement, final BeanProperty<T> property )
    {
        try
        {
            // deduce lookup from metadata + property details
            final TypeLiteral<T> expectedType = property.getType();
            final TypeLiteral<T> roleType = (TypeLiteral<T>) Roles.roleType( requirement, expectedType );
            final Class<?> rawType = expectedType.getRawType();

            final String[] hints = Hints.canonicalHints( requirement );

            if ( Map.class == rawType )
            {
                return new RequirementMapProvider( locatorProvider, roleType, hints );
            }
            else if ( List.class == rawType )
            {
                return new RequirementListProvider( locatorProvider, roleType, hints );
            }

            return new RequirementProvider( locatorProvider, roleType, hints );
        }
        catch ( final Throwable e )
        {
            // defer until later...
            return new Provider<T>()
            {
                public T get()
                {
                    throw new ProvisionException( "Error in requirement: " + property, e );
                }
            };
        }
    }

    // ----------------------------------------------------------------------
    // Implementation classes
    // ----------------------------------------------------------------------

    /**
     * Abstract {@link Provider} that locates Plexus beans on-demand.
     */
    private static abstract class AbstractRequirementProvider<S, T>
        implements Provider<S>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Provider<PlexusBeanLocator> locatorProvider;

        final TypeLiteral<T> type;

        private final String[] hints;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        AbstractRequirementProvider( final Provider<PlexusBeanLocator> locatorProvider, final TypeLiteral<T> type,
                                     final String[] hints )
        {
            this.locatorProvider = locatorProvider;

            this.type = type;
            this.hints = hints;
        }

        // ----------------------------------------------------------------------
        // Locally-shared methods
        // ----------------------------------------------------------------------

        final Iterable<? extends Entry<String, T>> locate()
        {
            return locatorProvider.get().locate( type, hints );
        }
    }

    /**
     * {@link Provider} of Plexus requirement maps.
     */
    private static final class RequirementMapProvider<T>
        extends AbstractRequirementProvider<Map<String, T>, T>
    {
        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        RequirementMapProvider( final Provider<PlexusBeanLocator> locatorProvider, final TypeLiteral<T> type,
                                final String[] hints )
        {
            super( locatorProvider, type, hints );
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public Map<String, T> get()
        {
            return new EntryMapAdapter<String, T>( locate() );
        }
    }

    /**
     * {@link Provider} of Plexus requirement lists.
     */
    private static final class RequirementListProvider<T>
        extends AbstractRequirementProvider<List<T>, T>
    {
        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        RequirementListProvider( final Provider<PlexusBeanLocator> locatorProvider, final TypeLiteral<T> type,
                                 final String[] hints )
        {
            super( locatorProvider, type, hints );
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public List<T> get()
        {
            return new EntryListAdapter<String, T>( locate() );
        }
    }

    /**
     * {@link Provider} of a single Plexus requirement.
     */
    private static final class RequirementProvider<T>
        extends AbstractRequirementProvider<T, T>
    {
        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        RequirementProvider( final Provider<PlexusBeanLocator> locatorProvider, final TypeLiteral<T> type,
                             final String[] hints )
        {
            super( locatorProvider, type, hints );
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public T get()
        {
            // pick first bean: supports both specific and wildcard lookup
            final Iterator<? extends Entry<String, T>> i = locate().iterator();
            if ( i.hasNext() )
            {
                return i.next().getValue();
            }
            return Roles.throwMissingComponentException( type, null );
        }
    }
}
