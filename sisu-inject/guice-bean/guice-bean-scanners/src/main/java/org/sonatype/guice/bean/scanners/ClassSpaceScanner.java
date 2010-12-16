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

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.guice.bean.reflect.Streams;
import org.sonatype.guice.bean.scanners.asm.ClassReader;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;

/**
 * ASM-style scanner that makes a {@link ClassSpaceVisitor} visit an existing {@link ClassSpace}.
 */
public final class ClassSpaceScanner
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int ASM_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ClassSpaceScanner( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Makes the given {@link ClassSpaceVisitor} visit the {@link ClassSpace} of this scanner.
     * 
     * @param visitor The class space visitor
     */
    public void accept( final ClassSpaceVisitor visitor )
    {
        visitor.visit( space );
        final Enumeration<URL> e = space.findEntries( null, "*.class", true );
        while ( e.hasMoreElements() )
        {
            final URL url = e.nextElement();
            final ClassVisitor cv = visitor.visitClass( url );
            if ( null != cv )
            {
                accept( cv, url );
            }
        }
        visitor.visitEnd();
    }

    /**
     * Makes the given {@link ClassVisitor} visit the class contained in the resource {@link URL}.
     * 
     * @param visitor The class space visitor
     * @param url The class resource URL
     */
    public static void accept( final ClassVisitor visitor, final URL url )
    {
        if ( null == url )
        {
            return; // nothing to visit
        }
        try
        {
            final InputStream in = url.openStream();
            try
            {
                new ClassReader( in ).accept( visitor, ASM_FLAGS );
            }
            finally
            {
                in.close();
            }
        }
        catch ( final Throwable e )
        {
            Logs.debug( visitor.getClass(), "Problem scanning resource: {} cause: {}", url, e );
        }
    }
}
