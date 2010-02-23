package org.sonatype.inject;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Example of MyArchiver implementation named "tar". It's contract will be discovered by scanning it's class hierarchy
 * and looking for class having {@link Contract} annotation.
 * 
 * @author cstamas
 */
@Singleton
@Named( "tar" )
public class MyArchiverTar
    implements MyArchiver
{
    @Inject
    private MySimpleComponent mySimpleComponent;

    public boolean canExtractFile( File archive )
    {
        return archive.getName().endsWith( ".tar.gz" ) || archive.getName().endsWith( ".tar" );
    }

    public void extractArchiveFile( File archive )
        throws IOException
    {
        // untar!
    }
}
