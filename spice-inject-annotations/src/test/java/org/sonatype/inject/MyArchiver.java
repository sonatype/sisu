package org.sonatype.inject;

import java.io.File;
import java.io.IOException;

/**
 * Example of an interface that is a contract.
 * 
 * @author cstamas
 */
public interface MyArchiver
{
    boolean canExtractFile( File archive );

    void extractArchiveFile( File archive )
        throws IOException;
}
