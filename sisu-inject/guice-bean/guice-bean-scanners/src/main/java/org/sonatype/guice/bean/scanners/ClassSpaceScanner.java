/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.scanners;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.Logs;
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

    private final ClassFinder finder;

    private final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ClassSpaceScanner( final ClassSpace space )
    {
        this( null, space );
    }

    public ClassSpaceScanner( final ClassFinder finder, final ClassSpace space )
    {
        this.finder = finder;
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

        final Enumeration<URL> result =
            null != finder ? finder.findClasses( space ) : space.findEntries( null, "*.class", true );

        while ( result.hasMoreElements() )
        {
            final URL url = result.nextElement();
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
