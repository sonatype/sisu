package org.codehaus.plexus.component.repository;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.io.PlexusTools;

import junit.framework.TestCase;

/**
 * @author Jason van Zyl
 * @version $Id: ComponentSetTest.java 8155 2009-04-26 01:22:53Z jvanzyl $
 */
public class ComponentSetTest
    extends TestCase
{
    public void testSimpleComponentResolution()
        throws Exception
    {
        final String xml =
            "<component-set>" + "  <components>" + "    <component>"
                + "      <implementation>java.lang.String</implementation>" + "      <role>c1</role>"
                + "      <role-hint>role-hint</role-hint>"
                + "      <component-profile>component-profile</component-profile>" + "      <requirements>"
                + "        <requirement>" + "          <role>c2</role>" + "        </requirement>"
                + "        <requirement>" + "          <role>c3</role>" + "        </requirement>"
                + "      </requirements>" + "    </component>" + "  </components>" + "  <dependencies>"
                + "    <dependency>" + "      <group-id>plexus</group-id>" + "      <artifact-id>wedgy</artifact-id>"
                + "      <version>1.0</version>" + "    </dependency>" + "  </dependencies>" + "</component-set>";

        final ClassWorld classWorld = new ClassWorld( "test", Thread.currentThread().getContextClassLoader() );
        final ClassRealm realm = classWorld.getRealm( "test" );

        final ComponentSetDescriptor cs = PlexusTools.buildComponentSet( PlexusTools.buildConfiguration( xml ), realm );

        final ComponentDescriptor<?> c1 = cs.getComponents().get( 0 );

        assertEquals( "c1", c1.getRole() );

        assertEquals( "role-hint", c1.getRoleHint() );

        assertEquals( "component-profile", c1.getComponentProfile() );

        final List<ComponentRequirement> requirements = c1.getRequirements();

        assertEquals( 2, requirements.size() );

        boolean containsC2 = false;

        boolean containsC3 = false;

        for ( final ComponentRequirement requirement : requirements )
        {
            if ( requirement.getRole().equals( "c2" ) )
            {
                containsC2 = true;
            }
            else if ( requirement.getRole().equals( "c3" ) )
            {
                containsC3 = true;
            }

        }

        assertTrue( containsC2 );

        assertTrue( containsC3 );

        final ComponentDependency d1 = cs.getDependencies().get( 0 );

        assertEquals( "plexus", d1.getGroupId() );

        assertEquals( "wedgy", d1.getArtifactId() );

        assertEquals( "1.0", d1.getVersion() );
    }
}
