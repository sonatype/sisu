/**
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus.component;

import com.google.inject.Key;

public final class CastUtils
{
    // ----------------------------------------------------------------------
    // Static utility methods
    // ----------------------------------------------------------------------

    public static boolean isAssignableFrom( final Class<?> expected, final Class<?> actual )
    {
        if ( null == actual )
        {
            return false;
        }
        if ( null == expected )
        {
            return true;
        }
        if ( !expected.isPrimitive() )
        {
            return expected.isAssignableFrom( actual );
        }

        // shortcut: use temporary Key as quick way to auto-box primitive types
        return Key.get( expected ).getTypeLiteral().getRawType().equals( actual );
    }
}
