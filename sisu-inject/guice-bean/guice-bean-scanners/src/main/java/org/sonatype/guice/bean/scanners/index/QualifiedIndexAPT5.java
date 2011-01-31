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

package org.sonatype.guice.bean.scanners.index;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.inject.Qualifier;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;

public final class QualifiedIndexAPT5
    implements AnnotationProcessorFactory
{
    public AnnotationProcessor getProcessorFor( final Set<AnnotationTypeDeclaration> annotations,
                                                final AnnotationProcessorEnvironment environment )
    {
        return new Processor( annotations, environment );
    }

    private static class Processor
        extends AbstractQualifiedIndex
        implements AnnotationProcessor, RoundCompleteListener
    {
        private final Set<AnnotationTypeDeclaration> annotations;

        private final AnnotationProcessorEnvironment environment;

        Processor( final Set<AnnotationTypeDeclaration> annotations, final AnnotationProcessorEnvironment environment )
        {
            this.annotations = annotations;
            this.environment = environment;
            environment.addListener( this );
        }

        public void process()
        {
            for ( final AnnotationTypeDeclaration anno : annotations )
            {
                if ( null != anno.getAnnotation( Qualifier.class ) )
                {
                    for ( final Declaration decl : environment.getDeclarationsAnnotatedWith( anno ) )
                    {
                        if ( decl instanceof ClassDeclaration )
                        {
                            updateIndex( anno.getQualifiedName(), ( (ClassDeclaration) decl ).getQualifiedName() );
                        }
                    }
                }
            }
        }

        public void roundComplete( final RoundCompleteEvent event )
        {
            if ( event.getRoundState().finalRound() )
            {
                saveIndex();

                environment.removeListener( this );
            }
        }

        @Override
        protected void info( final String msg )
        {
            environment.getMessager().printNotice( msg );
        }

        @Override
        protected void warn( final String msg )
        {
            environment.getMessager().printWarning( msg );
        }

        @Override
        protected Writer getWriter( final String path )
            throws IOException
        {
            return environment.getFiler().createTextFile( Filer.Location.CLASS_TREE, "", new File( path ), "UTF-8" );
        }
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
