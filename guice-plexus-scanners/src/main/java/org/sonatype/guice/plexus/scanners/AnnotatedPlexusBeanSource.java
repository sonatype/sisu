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
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

/**
 * {@link PlexusBeanSource} that provides {@link PlexusBeanMetadata} based on runtime annotations.
 */
public final class AnnotatedPlexusBeanSource
    implements PlexusBeanSource, PlexusBeanMetadata
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final Map<?, ?> variables;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public AnnotatedPlexusBeanSource( final ClassSpace space, final Map<?, ?> variables )
    {
        this.space = space;
        this.variables = variables;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean isEmpty()
    {
        return false; // metadata comes from the properties themselves
    }

    public Map<Component, DeferredClass<?>> findPlexusComponentBeans()
    {
        if ( null == space )
        {
            return Collections.emptyMap();
        }

        try
        {
            // TODO: proof-of-concept code, remember to clean up and refactor ;)
            // TODO: scan top-level ExtensionPoint API only once & share results?
            final Enumeration<URL> classpath = space.getResources( "META-INF" );
            while ( classpath.hasMoreElements() )
            {
                final URL entry = classpath.nextElement();
                if ( "jar".equals( entry.getProtocol() ) )
                {
                    final String path = entry.getPath();
                    final URL jar = new URL( path.substring( 0, path.length() - 10 ) );
                    final InputStream in = jar.openStream();
                    try
                    {
                        final JarInputStream jin = new JarInputStream( in );
                        for ( JarEntry e = jin.getNextJarEntry(); e != null; e = jin.getNextJarEntry() )
                        {
                            if ( e.getName().endsWith( ".class" ) )
                            {
                                final boolean[] flag = { false };

                                // TODO: work some magic here...
                                new ClassReader( jin ).accept( new ClassVisitor()
                                {
                                    public void visit( final int arg0, final int arg1, final String arg2,
                                                       final String arg3, final String arg4, final String[] arg5 )
                                    {
                                    }

                                    public AnnotationVisitor visitAnnotation( final String arg0, final boolean arg1 )
                                    {
                                        if ( arg0.endsWith( "/ExtensionPoint;" ) || arg0.endsWith( "/Managed;" ) )
                                        {
                                            flag[0] = true;
                                            System.out.println( "@"
                                                + arg0.substring( 1, arg0.length() - 1 ).replace( '/', '.' ) );
                                        }
                                        return null;
                                    }

                                    public void visitAttribute( final Attribute arg0 )
                                    {
                                    }

                                    public void visitEnd()
                                    {
                                    }

                                    public FieldVisitor visitField( final int arg0, final String arg1,
                                                                    final String arg2, final String arg3,
                                                                    final Object arg4 )
                                    {
                                        return null;
                                    }

                                    public void visitInnerClass( final String arg0, final String arg1,
                                                                 final String arg2, final int arg3 )
                                    {
                                    }

                                    public MethodVisitor visitMethod( final int arg0, final String arg1,
                                                                      final String arg2, final String arg3,
                                                                      final String[] arg4 )
                                    {
                                        return null;
                                    }

                                    public void visitOuterClass( final String arg0, final String arg1, final String arg2 )
                                    {
                                    }

                                    public void visitSource( final String arg0, final String arg1 )
                                    {
                                    }
                                }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES );

                                if ( flag[0] )
                                {
                                    System.out.println( e.getName().replace( '/', '.' ).replaceFirst( "\\.class$", "" ) );
                                }
                            }
                        }
                    }
                    finally
                    {
                        IOUtil.close( in );
                    }
                }
                break; // TODO: caller should remove non-managed elements
            }
            return Collections.emptyMap();
        }
        catch ( final IOException e )
        {
            return Collections.emptyMap();
        }
    }

    public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        return implementation.isAnnotationPresent( Component.class ) ? this : null;
    }

    public Configuration getConfiguration( final BeanProperty<?> property )
    {
        Configuration configuration = property.getAnnotation( Configuration.class );
        if ( configuration != null && variables != null )
        {
            // support runtime interpolation of @Configuration values
            final String value = StringUtils.interpolate( configuration.value(), variables );
            if ( !value.equals( configuration.value() ) )
            {
                configuration = new ConfigurationImpl( configuration.name(), value );
            }
        }
        return configuration;
    }

    public Requirement getRequirement( final BeanProperty<?> property )
    {
        return property.getAnnotation( Requirement.class );
    }
}
