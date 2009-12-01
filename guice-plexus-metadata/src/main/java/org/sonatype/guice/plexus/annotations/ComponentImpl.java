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
package org.sonatype.guice.plexus.annotations;

import java.lang.annotation.Annotation;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.DeferredClass;

/**
 * Partial runtime implementation of Plexus @{@link Component} annotation, supporting the most common attributes.
 */
public final class ComponentImpl
    implements Component
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    // computed hashCode representing hard-coded attributes
    private static final int HASH_CODE_OFFSET = 0x69E113F9;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final DeferredClass<?> role;

    private final String hint;

    private final String instantiationStrategy;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ComponentImpl( final DeferredClass<?> role, final String hint, final String instantiationStrategy )
    {
        if ( null == role || null == hint || null == instantiationStrategy )
        {
            throw new IllegalArgumentException( "@Component cannot contain null values" );
        }

        this.role = role;
        this.hint = hint;
        this.instantiationStrategy = instantiationStrategy;
    }

    // ----------------------------------------------------------------------
    // Annotation properties
    // ----------------------------------------------------------------------

    public Class<?> role()
    {
        return role.get();
    }

    public String hint()
    {
        return hint;
    }

    public String instantiationStrategy()
    {
        return instantiationStrategy;
    }

    public boolean isolatedRealm()
    {
        return false;
    }

    public String alias()
    {
        return "";
    }

    public String composer()
    {
        return "";
    }

    public String configurator()
    {
        return "";
    }

    public String description()
    {
        return "";
    }

    public String factory()
    {
        return "";
    }

    public String lifecycleHandler()
    {
        return "";
    }

    public String profile()
    {
        return "";
    }

    public String type()
    {
        return "";
    }

    public String version()
    {
        return "";
    }

    // ----------------------------------------------------------------------
    // Standard annotation behaviour
    // ----------------------------------------------------------------------

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }

        if ( rhs instanceof Component )
        {
            final Component cmp = (Component) rhs;

            if ( role().equals( cmp.role() ) && hint.equals( cmp.hint() )
                && instantiationStrategy.equals( cmp.instantiationStrategy() ) )
            {
                // optimization: we hard-code all these attributes to be empty
                final String hardCodedAttributes =
                    cmp.alias() + cmp.composer() + cmp.configurator() + cmp.description() + cmp.factory()
                        + cmp.lifecycleHandler() + cmp.profile() + cmp.type() + cmp.version();

                return hardCodedAttributes.length() == 0 && !cmp.isolatedRealm();
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return HASH_CODE_OFFSET + ( 127 * "role".hashCode() ^ role().hashCode() )
            + ( 127 * "hint".hashCode() ^ hint.hashCode() )
            + ( 127 * "instantiationStrategy".hashCode() ^ instantiationStrategy.hashCode() );
    }

    @Override
    public String toString()
    {
        return String.format( "@%s(isolatedRealm=false, composer=, configurator=, alias=, description=, "
            + "instantiationStrategy=%s, factory=, hint=%s, type=, lifecycleHandler=, version=, "
            + "profile=, role=%s)", Component.class.getName(), instantiationStrategy, hint, role() );
    }

    public Class<? extends Annotation> annotationType()
    {
        return Component.class;
    }
}