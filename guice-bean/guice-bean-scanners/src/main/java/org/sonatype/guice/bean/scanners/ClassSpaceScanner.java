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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.sonatype.guice.bean.reflect.ClassSpace;

public final class ClassSpaceScanner
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int CLASS_READER_FLAGS =
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public static void accept( final ClassSpaceVisitor visitor, final ClassSpace space )
        throws IOException
    {
        visitor.visit( space );
        final Enumeration<URL> e = space.findEntries( null, "*.class", true );
        while ( e.hasMoreElements() )
        {
            accept( visitor.visitClass(), e.nextElement() );
        }
        visitor.visitEnd();
    }

    public static void accept( final ClassVisitor visitor, final URL url )
        throws IOException
    {
        final InputStream in = url.openStream();
        try
        {
            new ClassReader( in ).accept( visitor, CLASS_READER_FLAGS );
        }
        finally
        {
            in.close();
        }
    }
}
