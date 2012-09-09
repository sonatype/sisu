package org.codehaus.plexus.configuration.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class XmlPlexusConfigurationReader
    implements PlexusConfigurationReader
{

    public PlexusConfiguration read( final InputStream inputStream )
        throws IOException, PlexusConfigurationException
    {
        return read( new InputStreamReader( inputStream ) );
    }

    public PlexusConfiguration read( final Reader reader )
        throws IOException, PlexusConfigurationException
    {
        try
        {
            final Xpp3Dom dom = Xpp3DomBuilder.build( reader );

            return new XmlPlexusConfiguration( dom );
        }
        catch ( final XmlPullParserException e )
        {
            throw new PlexusConfigurationException( "Failed to parse configuration resource!\nError was: \'"
                + e.getLocalizedMessage() + "\'", e );
        }
    }
}
