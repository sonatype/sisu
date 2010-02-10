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
package org.sonatype.nexus.plugins.repository;

import java.util.Collections;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Default {@link PluginRepositoryManager} implementation.
 */
@Component( role = PluginRepositoryManager.class )
final class DefaultPluginRepositoryManager
    implements PluginRepositoryManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Requirement( role = NexusPluginRepository.class )
    private Map<String, NexusPluginRepository> repositories;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public NexusPluginRepository getNexusPluginRepository( final String id )
    {
        return repositories.get( id );
    }

    public Map<GAVCoordinate, PluginMetadata> findAvailablePlugins()
    {
        return Collections.emptyMap(); // TODO
    }

    public PluginRepositoryArtifact resolveArtifact( final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public PluginRepositoryArtifact resolveDependencyArtifact( final PluginRepositoryArtifact dependant,
                                                               final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public PluginMetadata getPluginMetadata( final GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public int compareTo( final NexusPluginRepository rhs )
    {
        throw new UnsupportedOperationException(); // TODO
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------
}
