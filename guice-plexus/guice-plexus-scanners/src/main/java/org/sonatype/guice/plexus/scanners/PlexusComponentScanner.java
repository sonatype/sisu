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
import org.sonatype.guice.plexus.config.PlexusBeanSource;

/**
 * Service that scans {@link ClassSpace}s looking for Plexus components and associated metadata.
 */
public interface PlexusComponentScanner
{
    /**
     * Scan the given {@link ClassSpace} for Plexus components; may also gather extra metadata.
     * 
     * @param space The class space to scan
     * @param localSearch If {@code true} only scan local space; otherwise include surrounding spaces
     * @return Map of Plexus component beans
     * @see PlexusBeanSource#findPlexusComponentBeans()
     * @see ClassSpace#findEntries(String, String, boolean)
     * @see ClassSpace#getResources(String)
     */
    Map<Component, DeferredClass<?>> scan( ClassSpace space, boolean localSearch )
        throws IOException;
}