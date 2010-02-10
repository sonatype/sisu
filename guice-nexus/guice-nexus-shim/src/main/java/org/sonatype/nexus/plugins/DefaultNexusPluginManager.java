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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.sonatype.nexus.plugins.repository.PluginRepositoryManager;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Default {@link NexusPluginManager} implementation backed by a {@link PluginRepositoryManager}.
 */
public final class DefaultNexusPluginManager
    implements NexusPluginManager
{
    @Inject
    private PluginRepositoryManager repositoryManager;

    public Map<GAVCoordinate, PluginDescriptor> getActivatedPlugins()
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public Map<GAVCoordinate, PluginMetadata> getInstalledPlugins()
    {
        return Collections.unmodifiableMap( repositoryManager.findAvailablePlugins() );
    }

    public Map<GAVCoordinate, PluginResponse> getPluginResponses()
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public Collection<PluginManagerResponse> activateInstalledPlugins()
    {
        final List<PluginManagerResponse> result = new ArrayList<PluginManagerResponse>();
        // for ( final GAVCoordinate gav : repositoryManager.findAvailablePlugins().keySet() )
        // {
        // result.add( activatePlugin( gav ) );
        // }
        return result;
    }

    public boolean isActivatedPlugin( final GAVCoordinate gav )
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public PluginManagerResponse activatePlugin( final GAVCoordinate gav )
    {
        throw new UnsupportedOperationException(); // TODO
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
}
