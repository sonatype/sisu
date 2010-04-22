package org.codehaus.plexus.component.configurator.converters.composite;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ComponentValueSetter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;

/**
 * @author <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 * @version $Id: ObjectWithFieldsConverter.java 8512 2009-10-21 23:15:04Z bentmann $
 */
public class ObjectWithFieldsConverter
    extends AbstractConfigurationConverter
{
    public boolean canConvert( Class type )
    {
        boolean retValue = true;

        if ( Dictionary.class.isAssignableFrom( type ) )
        {
            retValue = false;
        }

        else if ( Map.class.isAssignableFrom( type ) )
        {
            retValue = false;
        }
        else if ( Collection.class.isAssignableFrom( type ) )
        {
            retValue = false;
        }

        return retValue;
    }

    public Object fromConfiguration( ConverterLookup converterLookup,
                                     PlexusConfiguration configuration,
                                     Class type,
                                     Class baseType,
                                     ClassLoader classLoader,
                                     ExpressionEvaluator expressionEvaluator,
                                     ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object retValue = fromExpression( configuration, expressionEvaluator, type );

        if ( retValue == null )
        {
            try
            {
                // it is a "composite" - we compose it from its children. It does not have a value of its own
                Class implementation = getClassForImplementationHint( type, configuration, classLoader );

                if ( type == implementation && type.isInterface() && configuration.getChildCount() <= 0 )
                {
                    return null;
                }

                retValue = instantiateObject( implementation );

                processConfiguration( converterLookup, retValue, classLoader, configuration, expressionEvaluator, listener );
            }
            catch ( ComponentConfigurationException e )
            {
                if ( e.getFailedConfiguration() == null )
                {
                    e.setFailedConfiguration( configuration );
                }

                throw e;
            }
        }
        return retValue;
    }


    public void processConfiguration( ConverterLookup converterLookup,
                                      Object object,
                                      ClassLoader classLoader,
                                      PlexusConfiguration configuration )
        throws ComponentConfigurationException
    {
        processConfiguration( converterLookup, object, classLoader, configuration, null );
    }

    public void processConfiguration( ConverterLookup converterLookup,
                                      Object object,
                                      ClassLoader classLoader,
                                      PlexusConfiguration configuration,
                                      ExpressionEvaluator expressionEvaluator )
        throws ComponentConfigurationException
    {
        processConfiguration( converterLookup, object, classLoader, configuration, expressionEvaluator, null );
    }

    public void processConfiguration( ConverterLookup converterLookup,
                                      Object object,
                                      ClassLoader classLoader,
                                      PlexusConfiguration configuration,
                                      ExpressionEvaluator expressionEvaluator,
                                      ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        int items = configuration.getChildCount();

        for ( int i = 0; i < items; i++ )
        {
            PlexusConfiguration childConfiguration = configuration.getChild( i );

            String elementName = childConfiguration.getName();

            ComponentValueSetter valueSetter = new ComponentValueSetter( fromXML( elementName ), object, converterLookup, listener );

            valueSetter.configure( childConfiguration, classLoader, expressionEvaluator );
        }
    }
}
