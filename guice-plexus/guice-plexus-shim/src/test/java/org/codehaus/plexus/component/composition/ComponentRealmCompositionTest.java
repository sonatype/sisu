package org.codehaus.plexus.component.composition;

import static org.codehaus.plexus.PlexusConstants.PLEXUS_DEFAULT_HINT;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/** @author Jason van Zyl */
public class ComponentRealmCompositionTest
    extends PlexusTestCase
{
    //
    // Component archives
    //
    private static final String PLUGIN_0_JAR = "src/test/test-components/plugin0-1.0-SNAPSHOT.jar";

    private static final String PLUGIN_1_JAR = "src/test/test-components/plugin1-1.0-SNAPSHOT.jar";

    private static final String COMPONENT_A_JAR = "src/test/test-components/component-a-1.0-SNAPSHOT.jar";

    private static final String COMPONENT_B_JAR = "src/test/test-components/component-b-1.0-SNAPSHOT.jar";

    private static final String COMPONENT_C_JAR = "src/test/test-components/component-c-1.0-SNAPSHOT.jar";

    private static final String ARCHIVER_JAR = "src/test/test-components/plexus-archiver-1.0-alpha-8.jar";

    //
    // Component roles
    //
    private static final String PLUGIN_0_ROLE = "org.codehaus.plexus.plugins.Plugin0";

    private static final String PLUGIN_1_ROLE = "org.codehaus.plexus.plugins.Plugin1";

    //
    // Component realms
    //
    private static final String PLUGIN_0_REALM = "plugin0Realm";

    private static final String PLUGIN_1_REALM = "plugin1Realm";

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // Create ClassRealm plugin0 with plugin0 -> A, plugin0 -> B
        createClassRealm( PLUGIN_0_REALM, PLUGIN_0_JAR, COMPONENT_A_JAR, COMPONENT_B_JAR, ARCHIVER_JAR );

        // Create ClassRealm plugin1 with plugin1 -> A, plugin1 -> C
        createClassRealm( PLUGIN_1_REALM, PLUGIN_1_JAR, COMPONENT_A_JAR, COMPONENT_C_JAR, ARCHIVER_JAR );
    }

    /*
     * We are testing that when the same component implementation exists in more then one realm and components depend on
     * those implementations, that the right realm is used to wire up the components. An example of this in practice are
     * Maven plugins where each plugin is loaded into a separate realm and the plugin may have dependencies on other
     * components. We want to make sure that a requirement, say a JarArchiver, for a given component, say the
     * maven-jar-plugin, is wired up with a JarArchiver taken from the same realm as the maven-jar-plugin and not a
     * different realm.
     */

    public void testCompositionWhereTheSameImplementationExistsInDifferentRealms()
        throws Exception
    {
        // Plugin0
        getContainer().lookup( PLUGIN_0_ROLE );

        // Plugin1
        getContainer().lookup( PLUGIN_1_ROLE );

        // Plugin0(alt)
        getContainer().lookup( PLUGIN_0_ROLE, "alt" );

        // Plugin1(alt)
        getContainer().lookup( PLUGIN_1_ROLE, "alt" );
    }

    public void testThatASingletonComponentIntheCoreRealmWhenLookedUpInComponentRealmsYieldsTheSameInstance()
        throws Exception
    {
    }

    public void testMultiRealmLookupMap()
        throws Exception
    {
        final Map<String, Object> plugin0Map = getContainer().lookupMap( PLUGIN_0_ROLE );
        assertNotNull( "plugin0Map is null", plugin0Map );
        assertNotNull( "plugin0Map does not contain a DefaultPlugin0", plugin0Map.get( PLEXUS_DEFAULT_HINT ) );
        assertNotNull( "plugin0Map does not contain a AltPlugin0", plugin0Map.get( "alt" ) );
        assertEquals( "Expected only 2 components in plugin0Map", 2, plugin0Map.size() );

        final Map<String, Object> plugin1Map = getContainer().lookupMap( PLUGIN_1_ROLE );
        assertNotNull( "plugin1Map is null", plugin1Map );
        assertNotNull( "plugin1Map does not contain a DefaultPlugin1", plugin1Map.get( PLEXUS_DEFAULT_HINT ) );
        assertNotNull( "plugin1Map does not contain a AltPlugin1", plugin1Map.get( "alt" ) );
        assertEquals( "Expected only 2 components in plugin1Map", 2, plugin1Map.size() );

    }

    public void testMultiRealmLookupList()
        throws Exception
    {
        final List<Object> plugin0List = getContainer().lookupList( PLUGIN_0_ROLE );
        assertNotNull( "plugin0List is null", plugin0List );
        final Map<String, Object> plugin0Map = mapByClassSimpleName( plugin0List );
        assertNotNull( "plugin0List does not contain a DefaultPlugin0", plugin0Map.get( "DefaultPlugin0" ) );
        assertNotNull( "plugin0List does not contain a AltPlugin0", plugin0Map.get( "AltPlugin0" ) );
        assertEquals( "Expected only 2 components in plugin0Map", 2, plugin0Map.size() );

        final List<Object> plugin1List = getContainer().lookupList( PLUGIN_1_ROLE );
        assertNotNull( "plugin1List is null", plugin1List );
        final Map<String, Object> plugin1Map = mapByClassSimpleName( plugin1List );
        assertNotNull( "plugin1List does not contain a DefaultPlugin1", plugin1Map.get( "DefaultPlugin1" ) );
        assertNotNull( "plugin1List does not contain a AltPlugin1", plugin1Map.get( "AltPlugin1" ) );
        assertEquals( "Expected only 2 components in plugin0Map", 2, plugin1Map.size() );
    }

    private ClassRealm createClassRealm( final String id, final String... jars )
        throws Exception
    {
        // create the realm
        final ClassRealm classRealm = getContainer().createChildRealm( id );

        // populate the realm
        for ( final String jar : jars )
        {
            final File file = new File( jar );
            assertTrue( jar + " is not a file", file.isFile() );

            final URL url = file.toURI().toURL();
            classRealm.addURL( url );
        }

        // descover all component definitions in the realm and register them with the repository
        getContainer().discoverComponents( classRealm );

        return classRealm;
    }

    private Map<String, Object> mapByClassSimpleName( final List<Object> objects )
    {
        final Map<String, Object> map = new TreeMap<String, Object>();
        for ( final Object object : objects )
        {
            map.put( object.getClass().getSimpleName(), object );
        }
        return map;
    }
}
