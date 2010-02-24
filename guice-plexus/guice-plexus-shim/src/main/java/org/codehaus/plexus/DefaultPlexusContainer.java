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
package org.codehaus.plexus;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.plexus.adapters.EntryListAdapter;
import org.sonatype.guice.plexus.adapters.EntryMapAdapter;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.converters.PlexusDateTypeConverter;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.GuiceBeanLocator;
import org.sonatype.guice.plexus.scanners.XmlPlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Jsr330;

/**
 * {@link PlexusContainer} shim that delegates to a Plexus-aware Guice {@link Injector}.
 */
public final class DefaultPlexusContainer
    implements MutablePlexusContainer
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String DEFAULT_REALM_NAME = "plexus.core";

    private static final LoggerManager CONSOLE_LOGGER_MANAGER = new ConsoleLoggerManager();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Context context;

    final Map<?, ?> variables;

    final ClassRealm containerRealm;

    final PlexusLifecycleManager lifecycleManager;

    @Inject
    private Injector injector;

    @Inject
    private GuiceBeanLocator beanLocator;

    private ClassRealm lookupRealm; // TODO: use thread-local

    private LoggerManager loggerManager = CONSOLE_LOGGER_MANAGER;

    private Logger logger;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        final URL configurationUrl = lookupConfigurationUrl( configuration );

        context = new DefaultContext( configuration.getContext() );
        context.put( PlexusConstants.PLEXUS_KEY, this );
        variables = new ContextMapAdapter( context );

        containerRealm = lookupContainerRealm( configuration );
        lifecycleManager = new PlexusLifecycleManager();
        lookupRealm = containerRealm;

        final ClassSpace space = new URLClassSpace( containerRealm );
        final PlexusBeanSource xmlSource = new XmlPlexusBeanSource( space, variables, configurationUrl );

        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                requestInjection( lifecycleManager );

                install( new PlexusDateTypeConverter() );
                install( new PlexusXmlBeanConverter() );

                bind( Context.class ).toInstance( context );
                bind( Map.class ).annotatedWith( Jsr330.named( PlexusConstants.PLEXUS_KEY ) ).toInstance( variables );
                bind( ClassRealm.class ).toInstance( containerRealm );
                bind( PlexusContainer.class ).toInstance( DefaultPlexusContainer.this );
                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
                bind( PlexusBeanLocator.class ).to( GuiceBeanLocator.class );
                bind( PlexusBeanManager.class ).toInstance( lifecycleManager );
            }

            @Provides
            @SuppressWarnings( "unused" )
            protected Logger logger()
            {
                return getLogger();
            }

        }, new PlexusBindingModule( lifecycleManager, xmlSource ) );
    }

    // ----------------------------------------------------------------------
    // Context methods
    // ----------------------------------------------------------------------

    public Context getContext()
    {
        return context;
    }

    // ----------------------------------------------------------------------
    // Lookup methods
    // ----------------------------------------------------------------------

    public Object lookup( final String role )
        throws ComponentLookupException
    {
        return lookup( role, Hints.DEFAULT_HINT );
    }

    public Object lookup( final String role, final String hint )
        throws ComponentLookupException
    {
        return lookup( loadRoleClass( role ), hint );
    }

    public <T> T lookup( final Class<T> role )
        throws ComponentLookupException
    {
        return lookup( role, Hints.DEFAULT_HINT );
    }

    public <T> T lookup( final Class<T> role, final String hint )
        throws ComponentLookupException
    {
        try
        {
            return locate( role, hint ).iterator().next().getValue();
        }
        catch ( final RuntimeException e )
        {
            throw new ComponentLookupException( e.toString(), role.getName(), hint );
        }
    }

    public <T> T lookup( final Class<T> type, final String role, final String hint )
        throws ComponentLookupException
    {
        return role.equals( type.getName() ) ? lookup( type, hint ) : type.cast( lookup( role, hint ) );
    }

    public List<Object> lookupList( final String role )
        throws ComponentLookupException
    {
        return lookupList( loadRoleClass( role ) );
    }

    public <T> List<T> lookupList( final Class<T> role )
    {
        return new EntryListAdapter<String, T>( locate( role ) );
    }

    public Map<String, Object> lookupMap( final String role )
        throws ComponentLookupException
    {
        return lookupMap( loadRoleClass( role ) );
    }

    public <T> Map<String, T> lookupMap( final Class<T> role )
    {
        return new EntryMapAdapter<String, T>( locate( role ) );
    }

    // ----------------------------------------------------------------------
    // Query methods
    // ----------------------------------------------------------------------

    public boolean hasComponent( final Class<?> role )
    {
        return hasComponent( role, Hints.DEFAULT_HINT );
    }

    public boolean hasComponent( final Class<?> role, final String hint )
    {
        return locate( role, hint ).iterator().hasNext();
    }

    public boolean hasComponent( final Class<?> type, final String role, final String hint )
    {
        return role.equals( type.getName() ) ? hasComponent( type, hint ) : hasComponent( loadRoleClass( role ), hint );
    }

    // ----------------------------------------------------------------------
    // Component descriptor methods
    // ----------------------------------------------------------------------

    public <T> void addComponentDescriptor( final ComponentDescriptor<T> descriptor )
    {
        getLogger().warn( "Ignoring " + descriptor );
    }

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String hint )
    {
        return getComponentDescriptor( null, role, hint );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role, final String hint )
    {
        return lifecycleManager.getComponentDescriptor( role, hint );
    }

    @SuppressWarnings( "unchecked" )
    public List getComponentDescriptorList( final String role )
    {
        return getComponentDescriptorList( loadRoleClass( role ), role );
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        final List<ComponentDescriptor<T>> tempList = new ArrayList<ComponentDescriptor<T>>();
        for ( final Entry<String, T> bean : locate( type ) )
        {
            final ComponentDescriptor<T> descriptor = lifecycleManager.getComponentDescriptor( role, bean.getKey() );
            if ( null != descriptor )
            {
                tempList.add( descriptor );
            }
        }
        return tempList;
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm classRealm )
    {
        try
        {
            final ClassSpace space = new URLClassSpace( classRealm );
            final PlexusBeanSource xmlSource = new XmlPlexusBeanSource( space, variables );
            final Module bindings = new PlexusBindingModule( lifecycleManager, xmlSource );

            beanLocator.add( injector.createChildInjector( bindings ) );
        }
        catch ( final Throwable e )
        {
            getLogger().warn( classRealm.toString(), e );
        }

        return null; // no-one actually seems to use or check the returned component list!
    }

    // ----------------------------------------------------------------------
    // Class realm methods
    // ----------------------------------------------------------------------

    public ClassWorld getClassWorld()
    {
        return containerRealm.getWorld();
    }

    public ClassRealm getContainerRealm()
    {
        return containerRealm;
    }

    public ClassRealm setLookupRealm( final ClassRealm realm )
    {
        final ClassRealm oldRealm = lookupRealm;
        lookupRealm = realm;
        return oldRealm;
    }

    public ClassRealm getLookupRealm()
    {
        return lookupRealm;
    }

    // ----------------------------------------------------------------------
    // Logger methods
    // ----------------------------------------------------------------------

    public LoggerManager getLoggerManager()
    {
        return loggerManager;
    }

    @Inject( optional = true )
    public void setLoggerManager( final LoggerManager loggerManager )
    {
        if ( null != loggerManager )
        {
            this.loggerManager = loggerManager;
        }
        else
        {
            this.loggerManager = CONSOLE_LOGGER_MANAGER;
        }
        logger = null; // refresh our local logger
    }

    public Logger getLogger()
    {
        if ( null == logger )
        {
            logger = loggerManager.getLoggerForComponent( PlexusContainer.class.getName(), null );
        }
        return logger;
    }

    // ----------------------------------------------------------------------
    // Shutdown methods
    // ----------------------------------------------------------------------

    public void release( final Object component )
    {
        lifecycleManager.unmanage( component );
    }

    public void dispose()
    {
        lifecycleManager.unmanage();
        containerRealm.setParentRealm( null );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Finds container {@link ClassRealm}, taking existing {@link ClassWorld}s or {@link ClassLoader}s into account.
     * 
     * @param configuration The container configuration
     * @return Container class realm
     */
    private ClassRealm lookupContainerRealm( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        ClassRealm realm = configuration.getRealm();
        if ( null == realm )
        {
            ClassWorld world = configuration.getClassWorld();
            if ( null == world )
            {
                world = new ClassWorld( DEFAULT_REALM_NAME, Thread.currentThread().getContextClassLoader() );
            }
            try
            {
                realm = world.getRealm( DEFAULT_REALM_NAME );
            }
            catch ( final NoSuchRealmException e )
            {
                final Iterator<?> realmIterator = world.getRealms().iterator();
                if ( realmIterator.hasNext() )
                {
                    realm = (ClassRealm) realmIterator.next();
                }
            }
        }
        if ( null == realm )
        {
            throw new PlexusContainerException( "Unable to find class realm: " + DEFAULT_REALM_NAME );
        }
        return realm;
    }

    /**
     * Finds container configuration URL, may search the container {@link ClassRealm} and local file-system.
     * 
     * @param configuration The container configuration
     * @return Local or remote URL
     */
    private URL lookupConfigurationUrl( final ContainerConfiguration configuration )
    {
        URL url = configuration.getContainerConfigurationURL();
        if ( null == url )
        {
            final String configurationPath = configuration.getContainerConfiguration();
            if ( null != configurationPath )
            {
                int index = 0;
                while ( index < configurationPath.length() && configurationPath.charAt( index ) == '/' )
                {
                    index++;
                }

                url = getClass().getClassLoader().getResource( configurationPath.substring( index ) );
                if ( null == url )
                {
                    final File file = new File( configurationPath );
                    if ( file.isFile() )
                    {
                        try
                        {
                            url = file.toURI().toURL();
                        }
                        catch ( final MalformedURLException e ) // NOPMD
                        {
                            // drop through and recover
                        }
                    }
                }
                if ( null == url )
                {
                    getLogger().warn( "Bad container configuration path: " + configurationPath );
                }
            }
        }
        return url;
    }

    /**
     * Loads the named Plexus role from the current container {@link ClassRealm}.
     * 
     * @param role The Plexus role
     * @return Plexus role class
     */
    @SuppressWarnings( "unchecked" )
    private Class<Object> loadRoleClass( final String role )
    {
        try
        {
            return lookupRealm.loadClass( role );
        }
        catch ( final Throwable e )
        {
            throw new TypeNotPresentException( role, e );
        }
    }

    /**
     * Locates instances of the given role, filtered using the given named hints.
     * 
     * @param role The Plexus role
     * @param hints The Plexus hints
     * @return Instances of the given role; ordered according to the given hints
     */
    private <T> Iterable<? extends Entry<String, T>> locate( final Class<T> role, final String... hints )
    {
        return beanLocator.locate( TypeLiteral.get( role ), hints );
    }
}
