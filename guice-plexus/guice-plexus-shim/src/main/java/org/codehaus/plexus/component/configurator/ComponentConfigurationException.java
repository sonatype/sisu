package org.codehaus.plexus.component.configurator;

import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * @author Jason van Zyl
 * @version $Id: ComponentConfigurationException.java 7089 2007-11-25 15:19:06Z jvanzyl $
 */
public class ComponentConfigurationException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    private PlexusConfiguration failedConfiguration;

    public ComponentConfigurationException( final String message )
    {
        super( message );
    }

    public ComponentConfigurationException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

    public ComponentConfigurationException( final Throwable cause )
    {
        super( cause );
    }

    public ComponentConfigurationException( final PlexusConfiguration failedConfiguration, final String message )
    {
        super( message );
        this.failedConfiguration = failedConfiguration;
    }

    public ComponentConfigurationException( final PlexusConfiguration failedConfiguration, final String message,
                                            final Throwable cause )
    {
        super( message, cause );
        this.failedConfiguration = failedConfiguration;
    }

    public ComponentConfigurationException( final PlexusConfiguration failedConfiguration, final Throwable cause )
    {
        super( cause );
        this.failedConfiguration = failedConfiguration;
    }

    public void setFailedConfiguration( final PlexusConfiguration failedConfiguration )
    {
        this.failedConfiguration = failedConfiguration;
    }

    public PlexusConfiguration getFailedConfiguration()
    {
        return failedConfiguration;
    }
}
