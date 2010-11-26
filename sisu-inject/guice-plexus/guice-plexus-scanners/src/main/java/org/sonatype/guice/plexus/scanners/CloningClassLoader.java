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
package org.sonatype.guice.plexus.scanners;

import java.lang.reflect.Modifier;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassWriter;
import org.sonatype.guice.bean.scanners.MethodVisitor;
import org.sonatype.guice.bean.scanners.Opcodes;

final class CloningClassLoader
    extends ClassLoader
{
    private static final String CLONE_MARKER = "$__plexus";

    private final ClassSpace space;

    CloningClassLoader( final ClassSpace space )
    {
        this.space = space;
    }

    @Override
    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
        throws ClassNotFoundException
    {
        if ( !name.contains( CLONE_MARKER ) )
        {
            return space.loadClass( name );
        }
        return super.loadClass( name, resolve );
    }

    static String proxyName( final String realName, final int cloneId )
    {
        final StringBuilder buf = new StringBuilder();
        if ( realName.startsWith( "java" ) )
        {
            buf.append( '$' );
        }
        return buf.append( realName ).append( CloningClassLoader.CLONE_MARKER ).append( cloneId ).toString();
    }

    static String getRealName( final String proxyName )
    {
        final int cloneMarker = proxyName.indexOf( CLONE_MARKER );
        return cloneMarker < 0 ? proxyName : proxyName.substring( '$' == proxyName.charAt( 0 ) ? 1 : 0, cloneMarker );
    }

    @Override
    protected Class<?> findClass( final String name )
        throws ClassNotFoundException
    {
        final String proxyName = name.replace( '.', '/' );
        final String superName = getRealName( proxyName );

        final ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );
        cw.visit( Opcodes.V1_5, Modifier.PUBLIC | Modifier.FINAL, proxyName, null, superName, null );
        final MethodVisitor mv = cw.visitMethod( Modifier.PUBLIC, "<init>", "()V", null, null );

        mv.visitCode();
        mv.visitVarInsn( Opcodes.ALOAD, 0 );
        mv.visitMethodInsn( Opcodes.INVOKESPECIAL, superName, "<init>", "()V" );
        mv.visitInsn( Opcodes.RETURN );
        mv.visitMaxs( 0, 0 );
        mv.visitEnd();
        cw.visitEnd();

        final byte[] buf = cw.toByteArray();

        return defineClass( name, buf, 0, buf.length );
    }

    @Override
    public String toString()
    {
        return space.toString();
    }
}
