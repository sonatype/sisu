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
                writeIndex();

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
