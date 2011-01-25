/**
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.scanners.apt;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

@SuppressWarnings( "restriction" )
public final class NamedProcessorFactory
    implements AnnotationProcessorFactory
{
    public AnnotationProcessor getProcessorFor( final Set<AnnotationTypeDeclaration> annotations,
                                                final AnnotationProcessorEnvironment environment )
    {
        return new NamedProcessor5( annotations, environment );
    }

    public Collection<String> supportedAnnotationTypes()
    {
        return Collections.singleton( "*" );
    }

    public Collection<String> supportedOptions()
    {
        return Collections.emptyList();
    }
}
