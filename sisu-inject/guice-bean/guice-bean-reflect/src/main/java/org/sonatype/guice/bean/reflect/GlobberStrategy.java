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
        public final Object compile( final String glob )
        {
            return null;
        }

        @Override
        public final boolean matches( final Object globPattern, final String filename )
        {
            return true;
        }
    },
    SUFFIX
    {
        @Override
        public final Object compile( final String glob )
        {
            return glob.substring( 1 ); // remove leading asterisk
        }

        @Override
        public final boolean matches( final Object globPattern, final String filename )
        {
            return filename.endsWith( (String) globPattern ); // no need for basename(...)
        }
    },
    PREFIX
    {
        @Override
        public final Object compile( final String glob )
        {
            return glob.substring( 0, glob.length() - 1 ); // remove trailing asterisk
        }

        @Override
        public final boolean matches( final Object globPattern, final String filename )
        {
            return basename( filename ).startsWith( (String) globPattern );
        }
    },
    EXACT
    {
        @Override
        public final Object compile( final String glob )
        {
            return glob;
        }

        @Override
        public final boolean matches( final Object globPattern, final String filename )
        {
            return ( (String) globPattern ).equals( basename( filename ) );
        }
    },
    PATTERN
    {
        @Override
        public final Object compile( final String glob )
        {
            return Pattern.compile( glob.replaceAll( "[^*]+", "\\\\Q$0\\\\E" ).replaceAll( "\\*+", ".*" ) );
        }

        @Override
        public final boolean matches( final Object globPattern, final String filename )
        {
            return ( (Pattern) globPattern ).matcher( basename( filename ) ).matches();
        }
    };

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Compiles the given plain-text glob into an optimized pattern.
     * 
     * @param glob The plain-text glob
     * @return Compiled glob pattern
     */
    public abstract Object compile( final String glob );

    /**
     * Attempts to match the given compiled glob pattern against a filename.
     * 
     * @param globPattern The compiled glob pattern
     * @param filename The candidate filename
     * @return {@code true} if the pattern matches; otherwise {@code false}
     */
    public abstract boolean matches( final Object globPattern, final String filename );

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