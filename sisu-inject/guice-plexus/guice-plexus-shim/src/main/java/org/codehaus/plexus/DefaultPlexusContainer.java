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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.sonatype.guice.bean.binders.MergedModule;
import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.EntryListAdapter;
import org.sonatype.guice.bean.locators.EntryMapAdapter;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.DeferredProvider;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.plexus.binders.PlexusAnnotatedBeanModule;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.binders.PlexusXmlBeanModule;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBean;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.converters.PlexusDateTypeConverter;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.ClassRealmUtils;
import org.sonatype.guice.plexus.locators.DefaultPlexusBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

/**
 * {@link PlexusContainer} shim that delegates to a Plexus-aware Guice {@link Injector}.
 */
@SuppressWarnings( { "unchecked", "rawtypes" } )
public final class DefaultPlexusContainer
    implements MutablePlexusContainer
{
    static
    {
        System.setProperty( "guice.disable.misplaced.annotation.check", "true" );
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String DEFAULT_REALM_NAME = "plexus.core";

    private static final LoggerManager CONSOLE_LOGGER_MANAGER = new ConsoleLoggerManager();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Set<String> realmIds = new HashSet<String>();

    final Map<ClassRealm, List<ComponentDescriptor<?>>> descriptorMap =
        new ConcurrentHashMap<ClassRealm, List<ComponentDescriptor<?>>>();

    final ThreadLocal<ClassRealm> lookupRealm = new ThreadLocal<ClassRealm>();

    final MutableBeanLocator qualifiedBeanLocator = new DefaultBeanLocator();

    final Context context;

    final Map<String, String> variables;

    final ClassRealm containerRealm;

    final PlexusLifecycleManager lifecycleManager;

    final PlexusBeanLocator plexusBeanLocator;

    private final String componentVisibility;

    private final boolean isClassPathScanningEnabled;

    private final boolean isClassPathCachingEnabled;

    private final boolean isAutoWiringEnabled;

    private final Module bootModule = new BootModule();

    private final Module setupModule = new SetupModule();

    private final Module loggerModule = new LoggerModule();

    private LoggerManager loggerManager = CONSOLE_LOGGER_MANAGER;

    private Logger logger;

    private boolean disposing;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultPlexusContainer()
        throws PlexusContainerException
    {
        this( new DefaultContainerConfiguration() );
    }

    @SuppressWarnings( "finally" )
    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        final URL plexusXml = lookupPlexusXml( configuration );

        context = new DefaultContext( configuration.getContext() );
        context.put( PlexusConstants.PLEXUS_KEY, this );
        variables = (Map) new ContextMapAdapter( context );

        containerRealm = lookupContainerRealm( configuration );
        lifecycleManager = new PlexusLifecycleManager( this, context );

        componentVisibility = configuration.getComponentVisibility();
        isClassPathScanningEnabled = configuration.getClassPathScanning();
        isClassPathCachingEnabled = configuration.getClassPathCaching();
        isAutoWiringEnabled = configuration.getAutoWiring();

        plexusBeanLocator = new DefaultPlexusBeanLocator( qualifiedBeanLocator, componentVisibility );

        realmIds.add( containerRealm.getId() );
        setLookupRealm( containerRealm );

        final List<PlexusBeanModule> beanModules = new ArrayList<PlexusBeanModule>();

        final ClassSpace space = new URLClassSpace( containerRealm );
        beanModules.add( new PlexusXmlBeanModule( space, variables, plexusXml ) );
        if ( isClassPathScanningEnabled )
        {
            beanModules.add( new PlexusAnnotatedBeanModule( space, variables, isClassPathCachingEnabled ) );
        }

        try
        {
            addPlexusInjector( beanModules, bootModule );
        }
        catch ( final RuntimeException e )
        {
            try
            {
                dispose(); // cleanup as much as possible
            }
            finally
            {
                throw e; // always report original failure
            }
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
        return lookup( role, "" );
    }

    public Object lookup( final String role, final String hint )
        throws ComponentLookupException
    {
        return lookup( null, role, hint );
    }

    public <T> T lookup( final Class<T> role )
        throws ComponentLookupException
    {
        return lookup( role, "" );
    }

    public <T> T lookup( final Class<T> role, final String hint )
        throws ComponentLookupException
    {
        return lookup( role, null, hint );
    }

    public <T> T lookup( final Class<T> type, final String role, final String hint )
        throws ComponentLookupException
    {
        try
        {
            return locate( role, type, hint ).iterator().next().getValue();
        }
        catch ( final Throwable e )
        {
            throw new ComponentLookupException( e, null != role ? role : type.getName(), hint );
        }
    }

    public List<Object> lookupList( final String role )
        throws ComponentLookupException
    {
        return new EntryListAdapter<String, Object>( locate( role, null ) );
    }

    public <T> List<T> lookupList( final Class<T> role )
        throws ComponentLookupException
    {
        return new EntryListAdapter<String, T>( locate( null, role ) );
    }

    public Map<String, Object> lookupMap( final String role )
        throws ComponentLookupException
    {
        return new EntryMapAdapter<String, Object>( locate( role, null ) );
    }

    public <T> Map<String, T> lookupMap( final Class<T> role )
        throws ComponentLookupException
    {
        return new EntryMapAdapter<String, T>( locate( null, role ) );
    }

    // ----------------------------------------------------------------------
    // Query methods
    // ----------------------------------------------------------------------

    public boolean hasComponent( final Class<?> role )
    {
        return hasComponent( role, "" );
    }

    public boolean hasComponent( final Class role, final String hint )
    {
        return hasComponent( role, null, hint );
    }

    public boolean hasComponent( final Class type, final String role, final String hint )
    {
        return hasPlexusBeans( locate( role, type, hint ) );
    }

    // ----------------------------------------------------------------------
    // Component descriptor methods
    // ----------------------------------------------------------------------

    public void addComponent( final Object component, final String role )
    {
        try
        {
            addComponent( component, component.getClass().getClassLoader().loadClass( role ), Hints.DEFAULT_HINT );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new TypeNotPresentException( role, e );
        }
    }

    public <T> void addComponent( final T component, final java.lang.Class<?> role, final String hint )
    {
        // this is only used in Maven3 tests, so keep it simple...
        qualifiedBeanLocator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                if ( Hints.isDefaultHint( hint ) )
                {
                    bind( (Class) role ).toInstance( component );
                }
                else
                {
                    bind( (Class) role ).annotatedWith( Names.named( hint ) ).toInstance( component );
                }
            }
        } ) );
    }

    public <T> void addComponentDescriptor( final ComponentDescriptor<T> descriptor )
    {
        ClassRealm realm = descriptor.getRealm();
        if ( null == realm )
        {
            realm = containerRealm;
            descriptor.setRealm( realm );
        }
        List<ComponentDescriptor<?>> descriptors = descriptorMap.get( realm );
        if ( null == descriptors )
        {
            descriptors = new ArrayList<ComponentDescriptor<?>>();
            descriptorMap.put( realm, descriptors );
        }
        descriptors.add( descriptor );
        if ( containerRealm == realm )
        {
            discoverComponents( containerRealm ); // for Maven3 testing
        }
    }

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String hint )
    {
        return getComponentDescriptor( null, role, hint );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role, final String hint )
    {
        final Iterator<PlexusBean<T>> i = locate( role, type, hint ).iterator();
        if ( i.hasNext() )
        {
            final PlexusBean<T> bean = i.next();
            if ( bean.getImplementationClass() != null )
            {
                return newComponentDescriptor( role, bean );
            }
        }
        return null;
    }

    public List getComponentDescriptorList( final String role )
    {
        return getComponentDescriptorList( null, role );
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        final List<ComponentDescriptor<T>> tempList = new ArrayList<ComponentDescriptor<T>>();
        for ( final PlexusBean<T> bean : locate( role, type ) )
        {
            tempList.add( newComponentDescriptor( role, bean ) );
        }
        return tempList;
    }

    public Map getComponentDescriptorMap( final String role )
    {
        return getComponentDescriptorMap( null, role );
    }

    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap( final Class<T> type, final String role )
    {
        final Map<String, ComponentDescriptor<T>> tempMap = new LinkedHashMap<String, ComponentDescriptor<T>>();
        for ( final PlexusBean<T> bean : locate( role, type ) )
        {
            tempMap.put( bean.getKey(), newComponentDescriptor( role, bean ) );
        }
        return tempMap;
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm realm )
    {
        try
        {
            final List<PlexusBeanModule> beanModules = new ArrayList<PlexusBeanModule>();

            final ClassSpace space = new URLClassSpace( realm );
            final List<ComponentDescriptor<?>> descriptors = descriptorMap.remove( realm );
            if ( null != descriptors )
            {
                beanModules.add( new ComponentDescriptorBeanModule( space, descriptors ) );
            }
            if ( realmIds.add( realm.getId() ) )
            {
                beanModules.add( new PlexusXmlBeanModule( space, variables ) );
                if ( isClassPathScanningEnabled )
                {
                    beanModules.add( new PlexusAnnotatedBeanModule( space, variables, isClassPathCachingEnabled ) );
                }
            }
            if ( !beanModules.isEmpty() )
            {
                addPlexusInjector( beanModules );
            }
        }
        catch ( final Throwable e )
        {
            getLogger().warn( realm.toString(), e );
        }

        return null; // no-one actually seems to use or check the returned component list!
    }

    public void addPlexusInjector( final List<PlexusBeanModule> beanModules, final Module... customModules )
    {
        final List<Module> modules = new ArrayList<Module>();

        modules.add( setupModule );
        Collections.addAll( modules, customModules );
        modules.add( new PlexusBindingModule( lifecycleManager, beanModules ) );
        modules.add( loggerModule );

        Guice.createInjector( isAutoWiringEnabled ? new WireModule( modules ) : new MergedModule( modules ) );
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
        final ClassRealm oldRealm = lookupRealm.get();
        lookupRealm.set( realm );
        return oldRealm;
    }

    public ClassRealm getLookupRealm()
    {
        return lookupRealm.get();
    }

    public ClassRealm createChildRealm( final String id )
    {
        try
        {
            return containerRealm.createChildRealm( id );
        }
        catch ( final DuplicateRealmException e1 )
        {
            try
            {
                return getClassWorld().getRealm( id );
            }
            catch ( final NoSuchRealmException e2 )
            {
                return null; // should never happen!
            }
        }
    }

    // ----------------------------------------------------------------------
    // Logger methods
    // ----------------------------------------------------------------------

    public synchronized LoggerManager getLoggerManager()
    {
        return loggerManager;
    }

    @Inject( optional = true )
    public synchronized void setLoggerManager( final LoggerManager loggerManager )
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

    public synchronized Logger getLogger()
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

    public void releaseAll( final Map<String, ?> components )
    {
        for ( final Object o : components.values() )
        {
            release( o );
        }
    }

    public void releaseAll( final List<?> components )
    {
        for ( final Object o : components )
        {
            release( o );
        }
    }

    public void dispose()
    {
        disposing = true;

        lifecycleManager.unmanage();
        containerRealm.setParentRealm( null );
        qualifiedBeanLocator.clear();
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
            throw new PlexusContainerException( "Missing container class realm: " + DEFAULT_REALM_NAME );
        }
        return realm;
    }

    /**
     * Finds container configuration URL, may search the container {@link ClassRealm} and local file-system.
     * 
     * @param configuration The container configuration
     * @return Local or remote URL
     */
    private URL lookupPlexusXml( final ContainerConfiguration configuration )
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
                    getLogger().debug( "Missing container configuration: " + configurationPath );
                }
            }
        }
        return url;
    }

    private <T> Iterable<PlexusBean<T>> locate( final String role, final Class<T> type, final String... hints )
    {
        if ( disposing )
        {
            return Collections.EMPTY_LIST;
        }
        final String[] canonicalHints = Hints.canonicalHints( hints );
        if ( null == role || null != type && type.getName().equals( role ) )
        {
            return plexusBeanLocator.locate( TypeLiteral.get( type ), canonicalHints );
        }
        final Set<Class> candidates = new HashSet<Class>();
        for ( final ClassRealm realm : getVisibleRealms() )
        {
            try
            {
                final Class clazz = realm.loadClass( role );
                if ( candidates.add( clazz ) )
                {
                    final Iterable beans = plexusBeanLocator.locate( TypeLiteral.get( clazz ), canonicalHints );
                    if ( hasPlexusBeans( beans ) )
                    {
                        return beans;
                    }
                }
            }
            catch ( final Throwable e ) // NOPMD
            {
                // continue...
            }
        }
        return Collections.EMPTY_LIST;
    }

    private Collection<ClassRealm> getVisibleRealms()
    {
        final Object[] realms = getClassWorld().getRealms().toArray();
        final Set<ClassRealm> visibleRealms = new LinkedHashSet<ClassRealm>( realms.length );
        final ClassRealm currentLookupRealm = getLookupRealm();
        if ( null != currentLookupRealm )
        {
            visibleRealms.add( currentLookupRealm );
        }
        if ( PlexusConstants.REALM_VISIBILITY.equals( componentVisibility ) )
        {
            final Collection<String> visibleNames = ClassRealmUtils.visibleRealmNames( ClassRealmUtils.contextRealm() );
            if ( !visibleNames.isEmpty() )
            {
                for ( int i = realms.length - 1; i >= 0; i-- )
                {
                    final ClassRealm r = (ClassRealm) realms[i];
                    if ( visibleNames.contains( r.toString() ) )
                    {
                        visibleRealms.add( r );
                    }
                }
                return visibleRealms;
            }
        }
        for ( int i = realms.length - 1; i >= 0; i-- )
        {
            visibleRealms.add( (ClassRealm) realms[i] );
        }
        return visibleRealms;
    }

    private <T> boolean hasPlexusBeans( final Iterable<PlexusBean<T>> beans )
    {
        final Iterator<PlexusBean<T>> i = beans.iterator();
        return i.hasNext() && i.next().getImplementationClass() != null;
    }

    private <T> ComponentDescriptor<T> newComponentDescriptor( final String role, final PlexusBean<T> bean )
    {
        final ComponentDescriptor<T> cd = new ComponentDescriptor<T>();
        cd.setRole( role );
        cd.setRoleHint( bean.getKey() );
        cd.setImplementationClass( bean.getImplementationClass() );
        cd.setDescription( bean.getDescription() );
        return cd;
    }

    final class BootModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            requestInjection( DefaultPlexusContainer.this );
        }
    }

    final class SetupModule
        extends AbstractModule
    {
        final PlexusDateTypeConverter dateConverter = new PlexusDateTypeConverter();

        final PlexusXmlBeanConverter beanConverter = new PlexusXmlBeanConverter();

        @Override
        protected void configure()
        {
            bind( Context.class ).toInstance( context );
            bind( ParameterKeys.PROPERTIES ).toInstance( variables );

            install( dateConverter );

            bind( MutableBeanLocator.class ).toInstance( qualifiedBeanLocator );

            bind( PlexusContainer.class ).to( MutablePlexusContainer.class );
            bind( PlexusBeanConverter.class ).toInstance( beanConverter );
            bind( PlexusBeanLocator.class ).toInstance( plexusBeanLocator );
            bind( PlexusBeanManager.class ).toInstance( lifecycleManager );

            // use provider wrapper to avoid repeated injections later on when configuring plugin injectors
            bind( MutablePlexusContainer.class ).toProvider( Providers.of( DefaultPlexusContainer.this ) );
        }
    }

    final class LoggerModule
        extends AbstractModule
    {
        final LoggerManagerProvider loggerManagerProvider = new LoggerManagerProvider();

        final LoggerProvider loggerProvider = new LoggerProvider();

        @Override
        protected void configure()
        {
            bind( LoggerManager.class ).toProvider( loggerManagerProvider );
            bind( Logger.class ).toProvider( loggerProvider );
        }
    }

    final class LoggerManagerProvider
        implements DeferredProvider<LoggerManager>
    {
        public LoggerManager get()
        {
            return getLoggerManager();
        }

        public DeferredClass<LoggerManager> getImplementationClass()
        {
            return new LoadedClass<LoggerManager>( get().getClass() );
        }
    }

    final class LoggerProvider
        implements DeferredProvider<Logger>
    {
        public Logger get()
        {
            return getLogger();
        }

        public DeferredClass<Logger> getImplementationClass()
        {
            return new LoadedClass<Logger>( get().getClass() );
        }
    }
}
