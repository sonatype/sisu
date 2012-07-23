package org.codehaus.plexus.logging;

import javax.inject.Inject;

import org.codehaus.plexus.PlexusTestCase;

public class CustomLoggerFactoryTest
    extends PlexusTestCase
{
    static class ComponentWithSLF4J
    {
        @Inject
        org.slf4j.Logger loggerDependency;

        org.slf4j.Logger loggerRequirement;
    }

    public void testCustomLoggerName()
        throws Exception
    {
        final String customName = "::" + ComponentWithSLF4J.class.getName() + "::";

        final ComponentWithSLF4J component = lookup( ComponentWithSLF4J.class );

        assertEquals( customName, component.loggerDependency.getName() );
        assertEquals( customName, component.loggerRequirement.getName() );
    }
}
