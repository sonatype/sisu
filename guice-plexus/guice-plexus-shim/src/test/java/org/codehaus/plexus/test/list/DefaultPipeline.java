package org.codehaus.plexus.test.list;

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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Jason van Zyl
 * @version $Id: DefaultPipeline.java 7089 2007-11-25 15:19:06Z jvanzyl $
 */
public class DefaultPipeline
    implements Pipeline
{
    private List valves;

    private Map valveMap;

    public void execute()
    {
        for ( final Iterator i = valves.iterator(); i.hasNext(); )
        {
            ( (Valve) i.next() ).execute();
        }
    }

    public List getValves()
    {
        return valves;
    }

    public Map getValveMap()
    {
        return valveMap;
    }
}
