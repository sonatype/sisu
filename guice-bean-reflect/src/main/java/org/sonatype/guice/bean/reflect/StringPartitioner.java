/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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

final class StringPartitioner
{
    private final char marker;

    private final String prefix;

    private final String suffix;

    private final String divider;

    StringPartitioner( final char marker, final String prefix, final String suffix, final String divider )
    {
        this.marker = marker;

        this.prefix = prefix;
        this.suffix = suffix;

        this.divider = divider;
    }

    String partition( final String text )
    {
        char nextChar;
        char prevChar = marker;

        final StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < text.length(); i++ )
        {
            nextChar = text.charAt( i );
            if ( marker == nextChar )
            {
                if ( marker != prevChar )
                {
                    buf.append( suffix ).append( divider );
                }
                else if ( 0 == i )
                {
                    buf.append( divider );
                }
            }
            else
            {
                if ( marker == prevChar )
                {
                    buf.append( prefix );
                }
                buf.append( nextChar );
            }

            prevChar = nextChar;
        }

        if ( marker != prevChar )
        {
            buf.append( suffix );
        }

        return buf.toString();
    }
}