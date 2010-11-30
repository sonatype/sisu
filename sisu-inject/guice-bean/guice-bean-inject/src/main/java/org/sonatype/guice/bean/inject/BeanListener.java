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
                }
                catch ( final Throwable e )
                {
                    encounter.addError( new ProvisionException( "Error binding: " + property, e ) );
                }
            }
        }

        if ( !bindings.isEmpty() )
        {
            encounter.register( new BeanInjector<B>( bindings ) );
        }
    }

    /**
     * @return {@code true} if this thread is performing bean injection; otherwise {@code false}
     */
    public static boolean isInjecting()
    {
        return BeanInjector.isInjecting();
    }
}
