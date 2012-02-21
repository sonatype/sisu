/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *   Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.scanners;

import java.net.URL;
import java.util.Enumeration;

import org.eclipse.sisu.reflect.ClassSpace;

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
