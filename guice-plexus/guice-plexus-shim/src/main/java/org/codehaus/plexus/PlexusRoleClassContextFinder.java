package org.codehaus.plexus;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * Custom {@link SecurityManager} that can load Plexus roles from the calling class loader.
 */
final class PlexusRoleClassContextFinder
    extends SecurityManager
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String CONTAINER_PACKAGE = "org.codehaus.plexus";

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to load the given role from the first non-container context in the call-stack.
     * 
     * @param role The Plexus role
     * @return Plexus role class loaded from first non-container context; otherwise {@code null}
     */
    Class<?> loadClassFromCaller( final String role )
    {
        try
        {
            for ( final Class<?> clazz : getClassContext() )
            {
                if ( false == clazz.getName().startsWith( CONTAINER_PACKAGE ) )
                {
                    final ClassLoader contextClassLoader = clazz.getClassLoader();
                    if ( contextClassLoader instanceof ClassRealm )
                    {
                        return ( (ClassRealm) contextClassLoader ).loadClassFromSelf( role );
                    }
                }
            }
        }
        catch ( final Throwable e ) // NOPMD
        {
            // drop-through
        }
        return null;
    }
}