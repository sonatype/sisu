package org.codehaus.plexus;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.test.ComponentA;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: DefaultComponentLookupManagerTest.java 7828 2008-11-14 22:07:56Z dain $
 */
public class DefaultComponentLookupManagerTest
    extends PlexusTestCase
{
    public void testLookupsWithAndWithoutRoleHint()
        throws Exception
    {
        final String resource = getConfigurationName( "components.xml" );

        System.out.println( "resource = " + resource );

        assertNotNull( resource );

        final ContainerConfiguration c =
            new DefaultContainerConfiguration().setName( "test" ).setContainerConfiguration( resource );

        final DefaultPlexusContainer container = new DefaultPlexusContainer( c );

        try
        {
            container.lookup( ComponentA.class );

            fail( "Expected exception" );
        }
        catch ( final ComponentLookupException e )
        {
            // expected
        }
    }
}
