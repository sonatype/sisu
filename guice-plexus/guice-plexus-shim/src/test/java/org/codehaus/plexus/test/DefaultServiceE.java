package org.codehaus.plexus.test;

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

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;

/**
 * A simple native plexus component implementing the manual configuration phase.
 */
public class DefaultServiceE
    extends AbstractLogEnabled
    implements ServiceE, Contextualizable, Initializable, Startable
{
    public boolean enableLogging;

    public boolean configured;

    public boolean contextualize;

    public boolean initialize;

    public boolean start;

    public boolean stop;

    public boolean serviced;

    @Override
    public void enableLogging( final Logger logger )
    {
        enableLogging = true;
    }

    public void contextualize( final Context context )
        throws ContextException
    {
        contextualize = true;
    }

    public void initialize()
    {
        initialize = true;
    }

    public void start()
    {
        start = true;
    }

    public void stop()
    {
        stop = true;
    }
}
