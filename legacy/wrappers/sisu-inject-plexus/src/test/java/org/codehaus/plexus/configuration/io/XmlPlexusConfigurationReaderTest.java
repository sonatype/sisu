package org.codehaus.plexus.configuration.io;

import java.io.StringReader;

import junit.framework.TestCase;

import org.codehaus.plexus.configuration.ConfigurationTestHelper;
import org.codehaus.plexus.configuration.PlexusConfiguration;

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
