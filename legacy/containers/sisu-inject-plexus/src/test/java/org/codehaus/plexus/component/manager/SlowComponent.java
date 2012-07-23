package org.codehaus.plexus.component.manager;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;

/**
 * A slow starting component that checks that sleeps during its Start phase. Configuration: delay - number of
 * milliseconds to sleep during start()
 * 
 * @author Ben Walding
 * @version $Id: SlowComponent.java 5451 2007-01-17 02:36:27Z jvanzyl $
 */
public class SlowComponent
    implements Startable
{
    public static final String ROLE = SlowComponent.class.getName();

    /* Number of ms to sleep during start() */
    private long delay;

    public void start()
    {
        try
        {
            Thread.sleep( delay );
        }
        catch ( final InterruptedException e )
        {
        }
    }

    public void stop()
    {
    }
}
