/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * Binding {@link Key} for implementations that act as "wild-cards", meaning they match against any assignable type.<br>
 * Wild-card keys use the fully-qualified name as the key's qualifier; the real qualifier is recorded separately.
 */
public final class WildcardKey
    extends Key<Object> // all wild-card keys have Object as their type
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Annotation qualifier;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public WildcardKey( final Class<?> type, final Annotation qualifier )
    {
        // make unique per-implementation name
        super( Names.named( type.getName() ) );

        this.qualifier = qualifier;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * @return Real qualifier associated with the implementation
     */
    public Annotation getQualifier()
    {
        return qualifier;
    }
}
