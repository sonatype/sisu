/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.binders;

import java.util.Collections;
import java.util.Map;

import javax.inject.Qualifier;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.bean.scanners.QualifiedTypeVisitor;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Guice {@link Module} that automatically binds types annotated with {@link Qualifier} annotations.
 */
public final class SpaceModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Map<String, String> DEFAULT_PROPERTIES = Collections.emptyMap();

    private static final String[] DEFAULT_ARGUMENTS = {};

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SpaceModule( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        new ClassSpaceScanner( space ).accept( new QualifiedTypeVisitor( new QualifiedTypeBinder( binder ) ) );

        binder.bind( ParameterKeys.PROPERTIES ).toInstance( DEFAULT_PROPERTIES );
        binder.bind( ParameterKeys.ARGUMENTS ).toInstance( DEFAULT_ARGUMENTS );
    }
}
