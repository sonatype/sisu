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
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;

/**
 * {@link PlexusComponentScanner} that uses runtime annotations to discover Plexus components.
 */
public final class AnnotatedPlexusComponentScanner
    implements PlexusComponentScanner
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<Component, DeferredClass<?>> scan( final ClassSpace space, final boolean localSearch )
        throws IOException
    {
        final PlexusTypeVisitor visitor = new PlexusTypeVisitor( new PlexusTypeBinder( space ) );
        new ClassSpaceScanner( space ).accept( visitor );
        return visitor.getComponents();
    }
}
