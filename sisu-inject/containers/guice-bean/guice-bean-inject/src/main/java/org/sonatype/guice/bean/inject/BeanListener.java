/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.guice.bean.reflect.BeanProperties;
import org.sonatype.guice.bean.reflect.BeanProperty;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * {@link TypeListener} that listens for bean types and arranges for their properties to be injected.
 */
public final class BeanListener
    implements TypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanBinder beanBinder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanListener( final BeanBinder beanBinder )
    {
        this.beanBinder = beanBinder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <B> void hear( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
    {
        final PropertyBinder propertyBinder = beanBinder.bindBean( type, encounter );
        if ( null == propertyBinder )
        {
            return; // no properties to bind
        }

        final List<PropertyBinding> bindings = new ArrayList<PropertyBinding>();
        final Set<String> visited = new HashSet<String>();

        for ( final BeanProperty<?> property : new BeanProperties( type.getRawType() ) )
        {
            final String name = property.getName();
            if ( visited.add( name ) )
            {
                try
                {
                    final PropertyBinding binding = propertyBinder.bindProperty( property );
                    if ( binding == PropertyBinder.LAST_BINDING )
                    {
                        break; // no more bindings
                    }
                    if ( binding != null )
                    {
                        bindings.add( binding );
                    }
                    else
                    {
                        visited.remove( name );
                    }
                }
                catch ( final RuntimeException e )
                {
                    encounter.addError( new ProvisionException( "Error binding: " + property, e ) );
                }
                catch ( final LinkageError e )
                {
                    encounter.addError( new ProvisionException( "Error binding: " + property, e ) );
                }
            }
        }

        if ( bindings.size() > 0 )
        {
            encounter.register( new BeanInjector<B>( bindings ) );
        }
    }
}
