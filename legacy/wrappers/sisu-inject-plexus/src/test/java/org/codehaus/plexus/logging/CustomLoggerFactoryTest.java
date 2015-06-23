package org.codehaus.plexus.logging;

import org.codehaus.plexus.PlexusTestCase;

public class CustomLoggerFactoryTest
    extends PlexusTestCase
{
    static class ComponentWithSLF4J
    {
        org.slf4j.Logger loggerRequirement;
    }

    public void testCustomLoggerName()
        throws Exception
    {
        final String customName = "::" + ComponentWithSLF4J.class.getName() + "::";

        final ComponentWithSLF4J component = lookup( ComponentWithSLF4J.class );

        assertEquals( customName, component.loggerRequirement.getName() );
    }
}
