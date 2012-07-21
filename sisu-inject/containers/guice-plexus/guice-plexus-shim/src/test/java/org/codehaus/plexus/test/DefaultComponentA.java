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

/**
 * @component.role org.codehaus.plexus.test.ComponentA
 * @component.requirement org.codehaus.plexus.test.ComponentB
 * @component.requirement org.codehaus.plexus.test.ComponentC
 * @component.version 1.0
 */
public class DefaultComponentA
    implements ComponentA
{
    private ComponentB componentB;

    private ComponentC componentC;

    /** @default localhost */
    private String host;

    /** @default 10000 */
    private int port;

    public ComponentB getComponentB()
    {
        return componentB;
    }

    public ComponentC getComponentC()
    {
        return componentC;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }
}
