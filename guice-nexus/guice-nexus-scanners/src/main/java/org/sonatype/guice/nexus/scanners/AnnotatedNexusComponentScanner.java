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
package org.sonatype.guice.nexus.scanners;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.objectweb.asm.ClassReader;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.scanners.PlexusComponentScanner;

/**
 * {@link PlexusComponentScanner} that uses runtime annotations to discover Nexus components.
 */
public final class AnnotatedNexusComponentScanner
    implements PlexusComponentScanner
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final int CLASS_READER_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<Component, DeferredClass<?>> scan( final ClassSpace space, final boolean localSearch )
        throws IOException
    {
        // for now scan complete plugin class space, TODO: just check managed dependencies
        final NexusComponentClassVisitor visitor = new NexusComponentClassVisitor( space );
        final Enumeration<URL> e = space.findEntries( null, "*.class", true );
        while ( e.hasMoreElements() )
        {
            final InputStream in = e.nextElement().openStream();
            try
            {
                new ClassReader( in ).accept( visitor, CLASS_READER_FLAGS );
            }
            finally
            {
                IOUtil.close( in );
            }
        }
        return visitor.getComponents();
    }
}