package org.codehaus.plexus.configuration.io;

import java.io.StringReader;

import org.codehaus.plexus.configuration.ConfigurationTestHelper;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import junit.framework.TestCase;

public class XmlPlexusConfigurationReaderTest
    extends TestCase
{
    public void testRead()
        throws Exception
    {
        final StringReader sr = new StringReader( ConfigurationTestHelper.getXmlConfiguration() );

        final XmlPlexusConfigurationReader reader = new XmlPlexusConfigurationReader();

        final PlexusConfiguration c = reader.read( sr );

        ConfigurationTestHelper.testConfiguration( c );
    }

}
