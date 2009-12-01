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
import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.config.Hints;

/**
 * Runtime implementation of Plexus @{@link Requirement} annotation.
 */
public final class RequirementImpl
    implements Requirement
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final DeferredClass<?> role;

    private final boolean optional;

    private final String hint;

    private final String[] hints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public RequirementImpl( final DeferredClass<?> role, final boolean optional, final String... hints )
    {
        if ( null == role || null == hints || Arrays.asList( hints ).contains( null ) )
        {
            throw new IllegalArgumentException( "@Requirement cannot contain null values" );
        }

        this.role = role;
        this.optional = optional;

        if ( hints.length == 1 )
        {
            hint = hints[0];
            this.hints = Hints.NO_HINTS;
        }
        else
        {
            hint = "";
            this.hints = hints.clone();
        }
    }

    // ----------------------------------------------------------------------
    // Annotation properties
    // ----------------------------------------------------------------------

    public Class<?> role()
    {
        return role.get();
    }

    public boolean optional()
    {
        return optional;
    }

    public String hint()
    {
        return hint;
    }

    public String[] hints()
    {
        return hints.clone();
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

        if ( rhs instanceof Requirement )
        {
            final Requirement req = (Requirement) rhs;

            return role().equals( req.role() ) && optional == req.optional() && hint.equals( req.hint() )
                && Arrays.equals( hints, req.hints() );
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return ( 127 * "role".hashCode() ^ role().hashCode() )
            + ( 127 * "optional".hashCode() ^ Boolean.valueOf( optional ).hashCode() )
            + ( 127 * "hint".hashCode() ^ hint.hashCode() ) + ( 127 * "hints".hashCode() ^ Arrays.hashCode( hints ) );
    }

    @Override
    public String toString()
    {
        return String.format( "@%s(hints=%s, optional=%b, role=%s, hint=%s)", Requirement.class.getName(),
                              Arrays.toString( hints ), Boolean.valueOf( optional ), role(), hint );
    }

    public Class<? extends Annotation> annotationType()
    {
        return Requirement.class;
    }
}