/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.scanners;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;

/**
 * {@link PlexusComponentScanner} that uses runtime annotations to discover Plexus components.
 */
public final class AnnotatedPlexusComponentScanner
    implements PlexusComponentScanner
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int CLASS_READER_FLAGS =
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<Component, DeferredClass<?>> scan( final ClassSpace space, final boolean localSearch )
        throws IOException
    {
        return visitClassResources( space, new PlexusComponentClassVisitor( space ) ).getComponents();
    }

    /**
     * Visits class resources in the given space with the given class visitor.
     * 
     * @param space The class space
     * @param visitor The class visitor
     * @return Current class visitor
     */
    public static <T extends ClassVisitor> T visitClassResources( final ClassSpace space, final T visitor )
        throws IOException
    {
        final Enumeration<URL> e = space.findEntries( null, "*.class", true );
        while ( e.hasMoreElements() )
        {
            visitClassResource( e.nextElement(), visitor );
        }
        return visitor;
    }

    /**
     * Visits the given class resource with the given class visitor.
     * 
     * @param url The class resource URL
     * @param visitor The class visitor
     */
    public static void visitClassResource( final URL url, final ClassVisitor visitor )
        throws IOException
    {
        final InputStream in = url.openStream();
        try
        {
            new ClassReader( in ).accept( visitor, CLASS_READER_FLAGS );
        }
        finally
        {
            IOUtil.close( in );
        }
    }
}