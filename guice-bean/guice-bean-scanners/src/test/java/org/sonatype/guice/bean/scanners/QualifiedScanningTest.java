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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLClassLoader;
import java.util.EventListener;
import java.util.Map;
import java.util.RandomAccess;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Qualifier;

import junit.framework.TestCase;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.util.Jsr330;

public class QualifiedScanningTest
    extends TestCase
{
    @Named
    static class DefaultB01
    {
    }

    @Named
    @Typed( {} )
    static class B01
        extends Thread
    {
    }

    @Named
    @Typed( EventListener.class )
    static class DefaultB02
        implements RandomAccess, EventListener
    {
    }

    @Named
    @Typed( EventListener.class )
    static class B02
        implements RandomAccess, EventListener
    {
    }

    @Named
    static class B03
        implements EventListener
    {
    }

    @Named( "TEST" )
    static class DefaultB04
    {
    }

    @Named( "TEST" )
    static class B04
    {
    }

    @Named
    static abstract class AbstractB01
    {
    }

    @Named
    @Typed( EventListener.class )
    static abstract class AbstractB02
        implements RandomAccess, EventListener
    {
    }

    @Named
    static abstract class AbstractB03
        implements EventListener
    {
    }

    @Named( "TEST" )
    static abstract class AbstractB04
        implements EventListener
    {
    }

    static class SubclassB00
        extends B01
    {
    }

    static class SubclassB01
        extends AbstractB01
    {
    }

    static class SubclassB02
        extends AbstractB02
    {
    }

    static class SubclassB03
        extends AbstractB03
    {
    }

    static class SubclassB04
        extends AbstractB04
    {
    }

    @Named( "RENAME" )
    static class SubclassB05
        extends AbstractB04
    {
    }

    @Typed( {} )
    static class SubclassB06
        extends B02
    {
    }

    @Typed( Serializable.class )
    static class SubclassB07
        extends B02
        implements Runnable, Serializable
    {
        private static final long serialVersionUID = 1L;

        public void run()
        {
        }
    }

    static class SubclassB08
        extends B02
        implements Serializable
    {
        private static final long serialVersionUID = 1L;
    }

    @Qualifier
    @Retention( RetentionPolicy.RUNTIME )
    public @interface Legacy
    {
    }

    static class LegacyImpl
        implements Legacy
    {
        public Class<? extends Annotation> annotationType()
        {
            return Legacy.class;
        }
    }

    @Legacy
    static class LegacyRunnable
        implements Runnable
    {
        public void run()
        {
        }
    }

    private Map<Key<?>, Binding<?>> bindings;

    @Override
    protected void setUp()
        throws Exception
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );
        final Injector injector = Guice.createInjector( new QualifiedScannerModule( space ) );
        bindings = injector.getBindings();
    }

    private void checkDefaultBinding( final Class<?> api, final Class<?> imp )
    {
        assertEquals( imp, bindings.get( Key.get( api, Jsr330.named( "default" ) ) ).getProvider().get().getClass() );
        assertEquals( imp, bindings.get( Key.get( api ) ).getProvider().get().getClass() );
    }

    private void checkNamedBinding( final Class<?> api, final String name, final Class<?> imp )
    {
        assertEquals( imp, bindings.get( Key.get( api, Jsr330.named( name ) ) ).getProvider().get().getClass() );
    }

    private void checkLegacyBinding( final Class<?> api, final Class<?> imp )
    {
        assertEquals( imp, bindings.get( Key.get( api, new LegacyImpl() ) ).getProvider().get().getClass() );
    }

    public void testComponentScanning()
    {
        checkDefaultBinding( DefaultB01.class, DefaultB01.class );
        checkNamedBinding( Thread.class, B01.class.getName(), B01.class );
        checkDefaultBinding( EventListener.class, DefaultB02.class );

        checkNamedBinding( EventListener.class, B02.class.getName(), B02.class );
        checkNamedBinding( EventListener.class, B03.class.getName(), B03.class );
        checkNamedBinding( DefaultB04.class, "TEST", DefaultB04.class );
        checkNamedBinding( B04.class, "TEST", B04.class );

        checkNamedBinding( Thread.class, SubclassB00.class.getName(), SubclassB00.class );
        checkNamedBinding( AbstractB01.class, SubclassB01.class.getName(), SubclassB01.class );

        checkNamedBinding( EventListener.class, SubclassB02.class.getName(), SubclassB02.class );
        checkNamedBinding( EventListener.class, SubclassB03.class.getName(), SubclassB03.class );
        checkNamedBinding( EventListener.class, "TEST", SubclassB04.class );
        checkNamedBinding( EventListener.class, "RENAME", SubclassB05.class );
        checkNamedBinding( EventListener.class, SubclassB06.class.getName(), SubclassB06.class );

        checkNamedBinding( Serializable.class, SubclassB07.class.getName(), SubclassB07.class );
        checkNamedBinding( Serializable.class, SubclassB08.class.getName(), SubclassB08.class );

        checkLegacyBinding( Runnable.class, LegacyRunnable.class );
    }

    public void testEmptyMethods()
    {
        final AnnotationVisitor emptyAnnotationVisitor = new EmptyAnnotationVisitor();

        emptyAnnotationVisitor.visit( null, null );
        emptyAnnotationVisitor.visitEnum( null, null, null );
        emptyAnnotationVisitor.visitAnnotation( null, null );
        emptyAnnotationVisitor.visitArray( null );
        emptyAnnotationVisitor.visitEnd();

        final ClassVisitor emptyClassVisitor = new EmptyClassVisitor();

        emptyClassVisitor.visit( 0, 0, null, null, null, null );
        emptyClassVisitor.visitAnnotation( null, false );
        emptyClassVisitor.visitAttribute( null );
        emptyClassVisitor.visitSource( null, null );
        emptyClassVisitor.visitEnd();
    }
}
