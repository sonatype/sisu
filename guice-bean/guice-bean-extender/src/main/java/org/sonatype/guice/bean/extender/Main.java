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
package org.sonatype.guice.bean.extender;

import java.net.URLClassLoader;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.QualifiedScannerModule;

import com.google.inject.InjectorBuilder;
import com.google.inject.Stage;

public final class Main
{
    public static void main( final String[] args )
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) Main.class.getClassLoader() );
        final InjectorBuilder injectorBuilder = new InjectorBuilder();
        injectorBuilder.stage( Stage.PRODUCTION ); // TODO: hack to get eager singletons
        injectorBuilder.addModules( new QualifiedScannerModule( space ) );
        injectorBuilder.build();
    }
}
