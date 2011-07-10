/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.binders;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.guice.bean.inject.BeanBinder;
import org.sonatype.guice.bean.inject.PropertyBinder;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeEncounter;

/**
 * {@link BeanBinder} that binds bean properties according to Plexus metadata.
 */
final class PlexusBeanBinder
    implements BeanBinder, ProvisionListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final ThreadLocal<List<Object>> PENDING = new ThreadLocal<List<Object>>()
    {
        @Override
        protected java.util.List<Object> initialValue()
        {
            return new ArrayList<Object>();
        }
    };

    private final PlexusBeanManager manager;

    private final PlexusBeanSource[] sources;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusBeanBinder( final PlexusBeanManager manager, final List<PlexusBeanSource> sources )
    {
        this.manager = manager;
        this.sources = sources.toArray( new PlexusBeanSource[sources.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <B> PropertyBinder bindBean( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
    {
        final Class<?> clazz = type.getRawType();
        for ( final PlexusBeanSource source : sources )
        {
            // use first source that has metadata for the given implementation
            final PlexusBeanMetadata metadata = source.getBeanMetadata( clazz );
            if ( metadata != null )
            {
                return new PlexusPropertyBinder( manager, encounter, metadata );
            }
        }
        return null; // no need to auto-bind
    }

    public <T> void onProvision( final ProvisionInvocation<T> pi )
    {
        final List<Object> pending = PENDING.get();
        final boolean isRoot = pending.isEmpty();
        if ( isRoot )
        {
            pending.add( null ); // must add NULL place-holder before scheduling starts
        }

        final Object bean = pi.provision(); // this may involve more onProvision calls

        boolean scheduleBean = true;
        for ( int i = pending.size() - 1; i >= 0; i-- )
        {
            if ( pending.get( i ) == bean )
            {
                scheduleBean = false; // make sure we never schedule the same bean twice
                break;
            }
        }
        if ( scheduleBean )
        {
            pending.add( bean );
        }

        if ( isRoot )
        {
            try
            {
                // process in order of creation; but skip the NULL place-holder at the start
                for ( int i = 1; i < pending.size(); i++ )
                {
                    manager.manage( pending.get( i ) ); // possibly more onProvision calls
                }
            }
            finally
            {
                pending.clear();
            }
        }
    }
}
