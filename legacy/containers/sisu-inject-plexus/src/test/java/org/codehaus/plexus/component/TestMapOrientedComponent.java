package org.codehaus.plexus.component;

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

import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.component.repository.ComponentRequirement;

public class TestMapOrientedComponent
    implements MapOrientedComponent
{

    public static final String ROLE = TestMapOrientedComponent.class.getName();

    private final Map context = new TreeMap();

    public void addComponentRequirement( final ComponentRequirement requirementDescriptor, final Object requirementValue )
    {
        context.put( requirementDescriptor.getFieldName(), requirementValue );
    }

    public void setComponentConfiguration( final Map componentConfiguration )
    {
        context.putAll( componentConfiguration );
    }

    public Map getContext()
    {
        return context;
    }

}
