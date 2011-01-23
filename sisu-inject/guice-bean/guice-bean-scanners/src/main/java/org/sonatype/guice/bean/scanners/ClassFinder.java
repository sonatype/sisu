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
package org.sonatype.guice.bean.scanners;

import java.net.URL;
import java.util.Enumeration;

import org.sonatype.guice.bean.reflect.ClassSpace;

/**
 * Finds (and optionally filters) {@link Class} resources from {@link ClassSpace}s.
 */
public interface ClassFinder
{
    /**
     * Searches the given {@link ClassSpace} for {@link Class} resources.
     * 
     * @param space The space to search
     * @return Sequence of Class URLs
     */
    Enumeration<URL> findClasses( ClassSpace space );
}
