package org.sonatype.guice.plexus.scanners;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.StrongDeferredClass;

final class DisambiguatedClassSpace
    extends ClassLoader
    implements ClassSpace
{
    private static final String PROXY_KEY = "$__plexus";

    private final ClassSpace space;

    private int counter;

    DisambiguatedClassSpace( final ClassSpace space )
    {
        this.space = space;
    }

    public DeferredClass<?> deferLoadClass( final String name )
    {
        return new StrongDeferredClass<Object>( this, encode( name ) );
    }

    public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
        throws ClassNotFoundException
    {
        if ( !name.contains( PROXY_KEY ) )
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
        final String superName = decode( proxyName );

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

    private String encode( final String name )
    {
        return name + PROXY_KEY + ++counter;
    }

    private String decode( final String name )
    {
        return name.substring( 0, name.indexOf( PROXY_KEY ) );
    }
}
