/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.shim;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.LoggerManager;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.binders.PlexusXmlBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.lifecycles.PlexusLifecycleManager;
import org.sonatype.guice.plexus.locators.DefaultPlexusBeanLocator;
import org.sonatype.inject.Parameters;

import com.google.inject.AbstractModule;

public final class PlexusSpaceModule
    extends AbstractModule
{
    private final ClassSpace space;

    public PlexusSpaceModule( final ClassSpace space )
    {
        this.space = space;
    }

    @Override
    protected void configure()
    {
        final Context context = new ParameterizedContext();
        bind( Context.class ).toInstance( context );

        final Provider<?> slf4jLoggerFactoryProvider = space.deferLoadClass( "org.slf4j.ILoggerFactory" ).asProvider();
        requestInjection( slf4jLoggerFactoryProvider );

        bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
        bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
        bind( PlexusContainer.class ).to( PseudoPlexusContainer.class );

        final PlexusBeanManager manager =
            new PlexusLifecycleManager( binder().getProvider( Context.class ),
                                        binder().getProvider( LoggerManager.class ), //
                                        slf4jLoggerFactoryProvider ); // SLF4J (optional)

        bind( PlexusBeanManager.class ).toInstance( manager );

        install( new PlexusBindingModule( manager, new PlexusXmlBeanModule( space, new ContextMapAdapter( context ) ) ) );
    }

    static final class ParameterizedContext
        extends DefaultContext
    {
        @Inject
        protected void setParameters( @Parameters final Map<?, ?> parameters )
        {
            contextData.putAll( parameters );
        }
    }
}
