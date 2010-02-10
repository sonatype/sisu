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
package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.repository.PluginRepositoryManager;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Default {@link NexusPluginManager} implementation backed by a {@link PluginRepositoryManager}.
 */
@Component( role = NexusPluginManager.class )
public final class DefaultNexusPluginManager
    implements NexusPluginManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PluginRepositoryManager repositoryManager;

    private final Map<GAVCoordinate, PluginDescriptor> activePlugins = new HashMap<GAVCoordinate, PluginDescriptor>();

    private final Map<GAVCoordinate, PluginResponse> pluginResponses = new HashMap<GAVCoordinate, PluginResponse>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    DefaultNexusPluginManager( final PluginRepositoryManager repositoryManager )
    {
        this.repositoryManager = repositoryManager;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<GAVCoordinate, PluginDescriptor> getActivatedPlugins()
    {
        return new HashMap<GAVCoordinate, PluginDescriptor>( activePlugins );
    }

    public Map<GAVCoordinate, PluginMetadata> getInstalledPlugins()
    {
        return repositoryManager.findAvailablePlugins();
    }

    public Map<GAVCoordinate, PluginResponse> getPluginResponses()
    {
        return new HashMap<GAVCoordinate, PluginResponse>( pluginResponses );
    }

    public Collection<PluginManagerResponse> activateInstalledPlugins()
    {
        final List<PluginManagerResponse> result = new ArrayList<PluginManagerResponse>();
        for ( final GAVCoordinate gav : repositoryManager.findAvailablePlugins().keySet() )
        {
            result.add( activatePlugin( gav ) );
        }
        return result;
    }

    public boolean isActivatedPlugin( final GAVCoordinate gav )
    {
        return activePlugins.containsKey( gav );
    }

    public PluginManagerResponse activatePlugin( final GAVCoordinate gav )
    {
        return new PluginManagerResponse( gav, PluginActivationRequest.ACTIVATE ); // TODO
    }

    public PluginManagerResponse deactivatePlugin( final GAVCoordinate gav )
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public boolean installPluginBundle( final URL bundle )
        throws IOException
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public boolean uninstallPluginBundle( final GAVCoordinate gav )
        throws IOException
    {
        throw new UnsupportedOperationException(); // TODO
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

}
