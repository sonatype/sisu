package org.codehaus.plexus.test.cycle;

import org.codehaus.plexus.PlexusTestCase;

public class CircularComponentTest
    extends PlexusTestCase
{
    public void testCircularComponents()
        throws Exception
    {
        lookup( CycleComponent.class, "C" ); // FIXME: fails if we lookup A
    }
}
