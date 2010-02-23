package org.sonatype.inject;

import java.io.File;
import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Example of MyArchiver implementation named "zip". It's contract will be discovered by scanning it's class hierarchy
 * and looking for class having {@link Contract} annotation.
 * 
 * @author cstamas
 */
@Singleton
@Named( "zip" )
public class MyArchiverZip
    implements MyArchiver
{
    public boolean canExtractFile( File archive )
    {
        return archive.getName().endsWith( ".zip" );
    }

    public void extractArchiveFile( File archive )
        throws IOException
    {
        // unzip it!
    }
}
