/**
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus.component.repository;

public final class ComponentDependency
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String groupId;

    private String artifactId;

    private String version;

    private String type = "jar";

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setGroupId( final String groupId )
    {
        this.groupId = groupId;
    }

    public void setArtifactId( final String artifactId )
    {
        this.artifactId = artifactId;
    }

    public void setVersion( final String version )
    {
        this.version = version;
    }

    public void setType( final String type )
    {
        this.type = type;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return "groupId = " + groupId + ", artifactId = " + artifactId + ", version = " + version + ", type = " + type;
    }
}
