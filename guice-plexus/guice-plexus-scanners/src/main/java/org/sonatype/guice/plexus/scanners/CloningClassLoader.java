package org.sonatype.guice.plexus.scanners;

import java.lang.reflect.Modifier;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.sonatype.guice.bean.reflect.ClassSpace;

final class CloningClassLoader
    extends ClassLoader
{
    static final String CLONE_MARKER = "$__plexus";

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

    @Override
    protected Class<?> findClass( final String name )
        throws ClassNotFoundException
    {
        final String proxyName = name.replace( '.', '/' );
        final String superName = proxyName.substring( '$' == name.charAt( 0 ) ? 1 : 0, name.indexOf( CLONE_MARKER ) );

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
