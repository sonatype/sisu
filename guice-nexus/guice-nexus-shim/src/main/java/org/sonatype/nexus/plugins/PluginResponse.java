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

import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * TODO.
 */
public final class PluginResponse
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final GAVCoordinate coordinate;

    private final PluginDescriptor descriptor;

    private final PluginActivationResult result;

    private final boolean successful;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PluginResponse( final GAVCoordinate coordinate, final PluginDescriptor descriptor,
                    final PluginActivationResult result, final boolean successful )
    {
        this.coordinate = coordinate;
        this.descriptor = descriptor;
        this.result = result;
        this.successful = successful;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public GAVCoordinate getPluginCoordinates()
    {
        return coordinate;
    }

    public PluginDescriptor getPluginDescriptor()
    {
        return descriptor;
    }

    public PluginActivationResult getAchievedGoal()
    {
        return result;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public String formatAsString( final boolean detailed )
    {
        return "TODO: formatAsString(" + detailed + ")";// TODO
    }
}
