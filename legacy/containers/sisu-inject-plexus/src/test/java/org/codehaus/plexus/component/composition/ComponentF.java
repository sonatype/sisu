package org.codehaus.plexus.component.composition;

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

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mmaczka@interia.p">Michal Maczka</a>
 * @version $Id: ComponentF.java 4779 2006-11-23 04:09:31Z jvanzyl $
 */
public class ComponentF
{
    private ComponentA componentA;

    private ComponentB componentB;

    private ComponentC[] componentC;

    private List componentD;

    private Map componentE;

    public ComponentA getComponentA()
    {
        return componentA;
    }

    public void setComponentA( final ComponentA componentA )
    {
        this.componentA = componentA;
    }

    public ComponentB getComponentB()
    {
        return componentB;
    }

    public void setComponentB( final ComponentB componentB )
    {
        this.componentB = componentB;
    }

    public ComponentC[] getComponentC()
    {
        return componentC;
    }

    public void setComponentC( final ComponentC[] componentC )
    {
        this.componentC = componentC;
    }

    public List getComponentD()
    {
        return componentD;
    }

    public void setComponentD( final List componentD )
    {
        this.componentD = componentD;
    }

    public Map getComponentE()
    {
        return componentE;
    }

    public void setComponentE( final Map componentE )
    {
        this.componentE = componentE;
    }
}
