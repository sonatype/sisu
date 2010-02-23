package org.sonatype.inject;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Example of a class not using interface, but directly implementing a component. Since it has no superclasses, it is
 * enough to make it "named".
 * <p>
 * Equivalent annotation in Plexus:
 * 
 * <pre>
 * @Component( role = ArchiverManager.class )
 * </pre>
 * 
 * Equivalent code in Guice:
 * 
 * <pre>
 * bind( ArchiveManager.class );
 * </pre>
 * 
 * @author cstamas
 */
@Named( "default" )
@Singleton
public class ArchiverManager
{
    @Inject
    private Map<String, MyArchiver> archivers;

    // some implementation follows...
}
