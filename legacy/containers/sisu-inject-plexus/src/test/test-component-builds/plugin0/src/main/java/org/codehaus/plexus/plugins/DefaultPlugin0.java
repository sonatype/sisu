package org.codehaus.plexus.plugins;

import org.codehaus.plexus.components.A;
import org.codehaus.plexus.components.B;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

/**
 * @plexus.component
 */
public class DefaultPlugin0
    implements Plugin0, Contextualizable
{
    /** @plexus.requirement */
    private A a;
    
    /** @plexus.requirement */    
    private B b;

    /** @plexus.requirement */
    private ArchiverManager archiverManager;

    public void hello()
    {
        System.out.println( "Hello World!" );
    }

    // ----------------------------------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------------------------------

    public void contextualize( Context context )
        throws ContextException
    {
        PlexusContainer container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );

        try
        {
            archiverManager = (ArchiverManager) container.lookup( ArchiverManager.ROLE );
        }
        catch ( ComponentLookupException e )
        {
            throw new ContextException( "Error retrieving ArchiverManager instance: " + e.getMessage(), e );
        }
    }
}
