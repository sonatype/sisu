package org.codehaus.plexus.component.factory.nonjava;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.factory.ComponentFactory;

/** @author Jason van Zyl */
public class NonJavaComponentFactoryTest
    extends PlexusTestCase
{
    public void testNonJavaComponentFactory()
        throws Exception
    {
        final ComponentFactory factory = lookup( ComponentFactory.class, "nonjava" );

        assertNotNull( factory );
    }
}
