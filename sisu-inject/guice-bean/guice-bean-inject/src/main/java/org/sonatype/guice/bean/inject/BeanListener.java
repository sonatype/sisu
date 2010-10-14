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

import java.util.LinkedHashMap;
import java.util.Map;

import org.sonatype.guice.bean.reflect.BeanProperties;
import org.sonatype.guice.bean.reflect.BeanProperty;

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

        final Map<String, PropertyBinding> bindings = new LinkedHashMap<String, PropertyBinding>();
        for ( final BeanProperty<?> property : new BeanProperties( type.getRawType() ) )
        {
            final String name = property.getName();
            if ( bindings.containsKey( name ) )
            {
                continue; // already have a binding for this property
            }
            try
            {
                final PropertyBinding binding = propertyBinder.bindProperty( property );
                if ( binding == PropertyBinder.LAST_BINDING )
                {
                    break; // no more bindings
                }
                if ( binding != null )
                {
                    bindings.put( name, binding );
                }
            }
            catch ( final Throwable e )
            {
                encounter.addError( "Error binding bean property: " + property + " reason: " + e );
            }
        }

        if ( !bindings.isEmpty() )
        {
            encounter.register( new BeanInjector<B>( bindings.values() ) );
        }
    }

    public static boolean isActive()
    {
        return BeanInjector.ACTIVE_COUNT.intValue() > 0;
    }
}
