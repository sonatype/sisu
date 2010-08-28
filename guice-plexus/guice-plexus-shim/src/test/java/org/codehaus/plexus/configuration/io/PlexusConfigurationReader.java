package org.codehaus.plexus.configuration.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;

public interface PlexusConfigurationReader
{
    public PlexusConfiguration read( Reader reader )
        throws IOException, PlexusConfigurationException;

    public PlexusConfiguration read( InputStream inputStream )
        throws IOException, PlexusConfigurationException;
}
