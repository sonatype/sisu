/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.bean.reflect;

import java.util.regex.Pattern;

/**
 * Enumerates various optimized filename globbing strategies.
 */
enum GlobberStrategy
{
    // ----------------------------------------------------------------------
    // Enumerated values
    // ----------------------------------------------------------------------

    ANYTHING
    {
        @Override
        final Object compile( final String glob )
        {
            return null;
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return true;
        }
    },
    SUFFIX
    {
        @Override
        final Object compile( final String glob )
        {
            return glob.substring( 1 ); // remove leading asterisk
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return filename.endsWith( (String) globPattern ); // no need for basename(...)
        }
    },
    PREFIX
    {
        @Override
        final Object compile( final String glob )
        {
            return glob.substring( 0, glob.length() - 1 ); // remove trailing asterisk
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return basename( filename ).startsWith( (String) globPattern );
        }
    },
    EXACT
    {
        @Override
        final Object compile( final String glob )
        {
            return glob;
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return ( (String) globPattern ).equals( basename( filename ) );
        }
    },
    PATTERN
    {
        @Override
        final Object compile( final String glob )
        {
            return Pattern.compile( glob.replaceAll( "[^*]+", "\\\\Q$0\\\\E" ).replaceAll( "\\*+", ".*" ) );
        }

        @Override
        final boolean matches( final Object globPattern, final String filename )
        {
            return ( (Pattern) globPattern ).matcher( basename( filename ) ).matches();
        }
    };

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * Selects the optimal globber strategy for the given plain-text glob.
     * 
     * @param glob The plain-text glob
     * @return Optimal globber strategy
     */
    static final GlobberStrategy selectFor( final String glob )
    {
        if ( null == glob || "*".equals( glob ) )
        {
            return GlobberStrategy.ANYTHING;
        }
        final int firstWildcard = glob.indexOf( '*' );
        if ( firstWildcard < 0 )
        {
            return GlobberStrategy.EXACT;
        }
        final int lastWildcard = glob.lastIndexOf( '*' );
        if ( firstWildcard == lastWildcard )
        {
            if ( firstWildcard == 0 )
            {
                return GlobberStrategy.SUFFIX;
            }
            if ( lastWildcard == glob.length() - 1 )
            {
                return GlobberStrategy.PREFIX;
            }
        }
        return GlobberStrategy.PATTERN;
    }

    /**
     * Compiles the given plain-text glob into an optimized pattern.
     * 
     * @param glob The plain-text glob
     * @return Compiled glob pattern
     */
    abstract Object compile( final String glob );

    /**
     * Attempts to match the given compiled glob pattern against a filename.
     * 
     * @param globPattern The compiled glob pattern
     * @param filename The candidate filename
     * @return {@code true} if the pattern matches; otherwise {@code false}
     */
    abstract boolean matches( final Object globPattern, final String filename );

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Extracts the basename segment from the given filename.
     * 
     * @param filename The filename
     * @return Basename segment
     */
    static final String basename( final String filename )
    {
        return filename.substring( 1 + filename.lastIndexOf( '/' ) );
    }
}