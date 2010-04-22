package org.codehaus.plexus.component.configurator;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.MapOrientedComponent;
import org.codehaus.plexus.component.configurator.converters.composite.MapConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.Map;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

public class MapOrientedComponentConfigurator
    extends AbstractComponentConfigurator
{

    public void configureComponent( Object component, PlexusConfiguration configuration,
                                    ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm,
                                    ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        if ( !( component instanceof MapOrientedComponent ) )
        {
            throw new ComponentConfigurationException(
                "This configurator can only process implementations of " + MapOrientedComponent.class.getName() );
        }

        MapConverter converter = new MapConverter();

        Map context = (Map) converter.fromConfiguration( converterLookup, configuration, null, null,
                                                         containerRealm, expressionEvaluator,
                                                         listener );

        ( (MapOrientedComponent) component ).setComponentConfiguration( context );
    }

}
