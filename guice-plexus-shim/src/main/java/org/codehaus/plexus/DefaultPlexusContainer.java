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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.WeakClassSpace;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.binders.PlexusGuice;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanRegistry;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.scanners.AnnotatedPlexusBeanSource;
import org.sonatype.guice.plexus.scanners.XmlPlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;

/**
 * {@link PlexusContainer} shim that delegates to a Plexus-aware Guice {@link Injector}.
 */
public final class DefaultPlexusContainer
    extends AbstractLogEnabled
    implements PlexusContainer
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String DEFAULT_REALM_NAME = "plexus.core";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final PlexusLifecycleManager lifecycleManager = new PlexusLifecycleManager( this );

    private final ComponentRepository repository;

    private final ClassRealm containerRealm;

    private final URL configurationUrl;

    final Context context;

    @Inject
    Injector injector;

    @Inject( optional = true )
    LoggerManager loggerManager = new ConsoleLoggerManager();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        repository = configuration.getComponentRepository();
        containerRealm = lookupContainerRealm( configuration );
        configurationUrl = lookupConfigurationUrl( configuration );
        context = new DefaultContext( configuration.getContext() );
        context.put( PlexusConstants.PLEXUS_KEY, this );

        final Map<?, ?> contextMap = new ContextMapAdapter( context );
        final ClassSpace space = new WeakClassSpace( containerRealm );

        try
        {
            final PlexusBeanSource xmlSource = new XmlPlexusBeanSource( configurationUrl, space, contextMap );
            final PlexusBeanSource annSource = new AnnotatedPlexusBeanSource( contextMap );

            PlexusGuice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( PlexusContainer.class ).toInstance( DefaultPlexusContainer.this );
                }

                @Provides
                @SuppressWarnings( "unused" )
                private Logger logger()
                {
                    return loggerManager.getLogger( PlexusContainer.class.getName() ); // TODO: per-component name?
                }
            }, new PlexusBindingModule( lifecycleManager, xmlSource, annSource ) );
        }
        catch ( final Exception e )
        {
            throw new PlexusContainerException( "Unable to create Plexus container", e );
        }
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

    public <T> T lookup( final Class<T> type )
        throws ComponentLookupException
    {
        return lookup( type, Hints.DEFAULT_HINT );
    }

    public <T> T lookup( final Class<T> type, final String hint )
        throws ComponentLookupException
    {
        try
        {
            final T instance = injector.getInstance( Roles.componentKey( type, Hints.canonicalHint( hint ) ) );
            PlexusGuice.resumeInjections( injector );
            return instance;
        }
        catch ( final RuntimeException e )
        {
            throw new ComponentLookupException( e.toString(), type.getName(), hint );
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

    public <T> List<T> lookupList( final Class<T> type )
    {
        final List<T> componentList = injector.getInstance( PlexusGuice.registryKey( type ) ).lookupList();
        PlexusGuice.resumeInjections( injector );
        return componentList;
    }

    public Map<String, Object> lookupMap( final String role )
        throws ComponentLookupException
    {
        return lookupMap( loadRoleClass( role ) );
    }

    public <T> Map<String, T> lookupMap( final Class<T> type )
    {
        final Map<String, T> componentMap = injector.getInstance( PlexusGuice.registryKey( type ) ).lookupMap();
        PlexusGuice.resumeInjections( injector );
        return componentMap;
    }

    // ----------------------------------------------------------------------
    // Query methods
    // ----------------------------------------------------------------------

    public boolean hasComponent( final Class<?> type )
    {
        return !injector.getInstance( PlexusGuice.registryKey( type ) ).availableHints().isEmpty();
    }

    public boolean hasComponent( final Class<?> type, final String hint )
    {
        final PlexusBeanRegistry<?> registry = injector.getInstance( PlexusGuice.registryKey( type ) );
        return registry.availableHints().contains( Hints.canonicalHint( hint ) );
    }

    public boolean hasComponent( final Class<?> type, final String role, final String hint )
    {
        return role.equals( type.getName() ) ? hasComponent( type, hint ) : hasComponent( loadRoleClass( role ), hint );
    }

    // ----------------------------------------------------------------------
    // Component descriptor methods
    // ----------------------------------------------------------------------

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String hint )
    {
        return getComponentDescriptor( Object.class, role, hint );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role, final String hint )
    {
        return repository.getComponentDescriptor( type, role, hint );
    }

    @SuppressWarnings( "unchecked" )
    public List<ComponentDescriptor<?>> getComponentDescriptorList( final String role )
    {
        return (List) getComponentDescriptorList( Object.class, role );
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        return repository.getComponentDescriptorList( type, role );
    }

    public <T> void addComponentDescriptor( final ComponentDescriptor<T> descriptor )
        throws CycleDetectedInComponentGraphException
    {
        repository.addComponentDescriptor( descriptor );
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm classRealm )
    {
        // TODO: do we need to do anything here?
        getLogger().warn( "TODO DefaultPlexusContainer.discoverComponents(" + classRealm + ")" );
        return Collections.emptyList();
    }

    // ----------------------------------------------------------------------
    // Class realm methods
    // ----------------------------------------------------------------------

    public ClassRealm getContainerRealm()
    {
        return containerRealm;
    }

    public ClassRealm createChildRealm( final String id )
    {
        try
        {
            return containerRealm.createChildRealm( id );
        }
        catch ( final DuplicateRealmException e )
        {
            return containerRealm.getWorld().getClassRealm( id );
        }
    }

    public void removeComponentRealm( final ClassRealm classRealm )
    {
        repository.removeComponentRealm( classRealm );
    }

    // ----------------------------------------------------------------------
    // Shutdown methods
    // ----------------------------------------------------------------------

    public void release( final Object component )
    {
        // TODO: do we need to do anything here?
        getLogger().warn( "TODO DefaultPlexusContainer.release(" + component + ")" );
    }

    public void dispose()
    {
        lifecycleManager.dispose();

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
                url = getClass().getClassLoader().getResource( StringUtils.stripStart( configurationPath, "/" ) );
                if ( null == url )
                {
                    final File file = new File( configurationPath );
                    if ( file.isFile() )
                    {
                        try
                        {
                            url = file.toURI().toURL();
                        }
                        catch ( final MalformedURLException e )
                        {
                            // drop through and recover
                        }
                    }
                }
                if ( null == url )
                {
                    ConsoleLoggerManager.LOGGER.warn( "Bad container configuration path: " + configurationPath );
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
            return containerRealm.loadClass( role );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new TypeNotPresentException( role, e );
        }
    }
}
