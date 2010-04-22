package org.codehaus.plexus.component.configurator;

import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 *
 * 
 * @author Jason van Zyl
 *
 * @version $Id: ComponentConfigurationException.java 7089 2007-11-25 15:19:06Z jvanzyl $
 */
public class ComponentConfigurationException
    extends Exception
{
    private PlexusConfiguration failedConfiguration;

    public ComponentConfigurationException( String message )
    {
        super( message );
    }

    public ComponentConfigurationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ComponentConfigurationException( Throwable cause )
    {
        super( cause );
    }
    
    public ComponentConfigurationException( PlexusConfiguration failedConfiguration, String message )
    {
        super( message );
        this.failedConfiguration = failedConfiguration;
    }

    public ComponentConfigurationException( PlexusConfiguration failedConfiguration, String message, Throwable cause )
    {
        super( message, cause );
        this.failedConfiguration = failedConfiguration;
    }

    public ComponentConfigurationException( PlexusConfiguration failedConfiguration, Throwable cause )
    {
        super( cause );
        this.failedConfiguration = failedConfiguration;
    }
    
    public void setFailedConfiguration( PlexusConfiguration failedConfiguration )
    {
        this.failedConfiguration = failedConfiguration;
    }
    
    public PlexusConfiguration getFailedConfiguration()
    {
        return failedConfiguration;
    }
}
