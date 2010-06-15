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
package org.sonatype.guice.bean.scanners;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Qualifier;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.sonatype.guice.bean.reflect.ClassSpace;

/**
 * Caching {@link ClassVisitor} that maintains a map of known {@link Qualifier} annotations.
 */
final class QualifierCache
    extends EmptyClassVisitor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String QUALIFIER_DESC = Type.getDescriptor( Qualifier.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private final Map<String, Class> cachedResults = new HashMap<String, Class>();

    private boolean isQualified;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to load the potential {@link Qualifier} annotation and return its class.
     * 
     * @param space The class space
     * @param desc The annotation descriptor
     * @return Qualified annotation class; {@code null} if the annotation is not a qualifier
     */
    @SuppressWarnings( "unchecked" )
    public Class<Annotation> qualify( final ClassSpace space, final String desc )
    {
        if ( !cachedResults.containsKey( desc ) )
        {
            isQualified = false;

            final String name = desc.substring( 1, desc.length() - 1 );
            ClassSpaceScanner.accept( this, space.getResource( name + ".class" ) );

            cachedResults.put( desc, isQualified ? space.loadClass( name.replace( '/', '.' ) ) : null );
        }
        return cachedResults.get( desc );
    }

    @Override
    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        isQualified |= QUALIFIER_DESC.equals( desc );
        return null;
    }
}
