package org.codehaus.plexus.test.cycle;

import org.codehaus.plexus.PlexusTestCase;

public class LockComponentTest
    extends PlexusTestCase
{
    public void testLockComponent()
        throws Exception
    {
        lookup( EagerComponent.class );
    }
}
