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

import java.net.URL;

import org.sonatype.guice.bean.reflect.ClassSpace;

/**
 * ASM-style visitor that visits a {@link ClassSpace}. The methods of this interface must be called in the following
 * order: {@code visit ( visitClass )* visitEnd}.
 */
public interface ClassSpaceVisitor
{
    /**
     * Visits the start of the class space.
     * 
     * @param space The class space
     */
    void visit( final ClassSpace space );

    /**
     * Visits a class resource in the class space.
     * 
     * @param url The class resource URL
     * @return Class visitor; {@code null} if this visitor is not interested in visiting the class
     */
    ClassVisitor visitClass( final URL url );

    /**
     * Visits the end of the class space.
     */
    void visitEnd();
}
