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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.guice.bean.scanners.QualifiedTypeVisitor;

public final class QualifiedIndexCmd
    extends AbstractQualifiedIndex
    implements QualifiedTypeListener
{
    public static void main( final String[] args )
    {
        final QualifiedIndexCmd indexer = new QualifiedIndexCmd();
        final ClassSpace space = new URLClassSpace( QualifiedIndexCmd.class.getClassLoader() );
        new ClassSpaceScanner( space ).accept( new QualifiedTypeVisitor( indexer ) );
        indexer.writeIndex();
    }

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        updateIndex( qualifier.annotationType().getName(), qualifiedType.getName() );
    }

    @Override
    protected void info( final String message )
    {
        System.out.println( "[INFO] " + message );
    }

    @Override
    protected void warn( final String message )
    {
        System.out.println( "[WARN] " + message );
    }

    @Override
    protected Writer getWriter( final String path )
        throws IOException
    {
        final File index = new File( path );
        index.getParentFile().mkdirs();
        return new FileWriter( index );
    }
}
