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
package org.codehaus.plexus.logging;

import org.sonatype.guice.bean.reflect.IgnoreSetters;
import org.sonatype.guice.plexus.config.Hints;

@IgnoreSetters
public abstract class AbstractLoggerManager
    implements LoggerManager
{
    public final Logger getLoggerForComponent( final String role )
    {
        return getLoggerForComponent( role, Hints.DEFAULT_HINT );
    }

    public final void returnComponentLogger( final String role )
    {
        returnComponentLogger( role, Hints.DEFAULT_HINT );
    }
}
