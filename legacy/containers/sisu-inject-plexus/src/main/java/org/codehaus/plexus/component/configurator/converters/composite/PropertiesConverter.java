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

import java.util.Properties;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * Converter for <code>java.util.Properties</code>.
 * 
 * @author <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 * @version $Id: PropertiesConverter.java 8516 2009-10-26 12:54:02Z bentmann $
 */
@SuppressWarnings( "rawtypes" )
public class PropertiesConverter
    extends AbstractConfigurationConverter
{

    public boolean canConvert( final Class type )
    {
        return Properties.class.isAssignableFrom( type );
    }

    public Object fromConfiguration( final ConverterLookup converterLookup, final PlexusConfiguration configuration,
                                     final Class type, final Class baseType, final ClassLoader classLoader,
                                     final ExpressionEvaluator expressionEvaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object retValue = fromExpression( configuration, expressionEvaluator, type );

        if ( retValue == null )
        {
            retValue = fromChildren( configuration, expressionEvaluator, listener );
        }

        return retValue;
    }

    private Object fromChildren( final PlexusConfiguration configuration,
                                 final ExpressionEvaluator expressionEvaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        final String element = configuration.getName();

        final Properties retValue = new Properties();

        for ( int i = 0, n = configuration.getChildCount(); i < n; i++ )
        {
            final PlexusConfiguration childConfiguration = configuration.getChild( i );

            final Object name;
            final PlexusConfiguration value;

            if ( "property".equals( childConfiguration.getName() ) && childConfiguration.getChildCount() > 0 )
            {
                // <property>
                // <name>key</name>
                // <value>val</value>
                // </property>

                name = fromExpression( childConfiguration.getChild( "name" ), expressionEvaluator );

                value = childConfiguration.getChild( "value" );
            }
            else if ( childConfiguration.getChildCount() <= 0 )
            {
                // <key>val</key>

                name = childConfiguration.getName();

                value = childConfiguration;
            }
            else
            {
                continue;
            }

            addEntry( retValue, element, name, value, expressionEvaluator );
        }

        return retValue;
    }

    private void addEntry( final Properties properties, final String element, final Object name,
                           final PlexusConfiguration valueConfiguration, final ExpressionEvaluator expressionEvaluator )
        throws ComponentConfigurationException
    {
        final String key = name != null ? name.toString() : null;

        if ( key == null )
        {
            final String msg = "Missing name for property of configuration element '" + element + "'";

            throw new ComponentConfigurationException( msg );
        }

        final Object value = fromExpression( valueConfiguration, expressionEvaluator );

        if ( value == null )
        {
            properties.setProperty( key, "" );
        }
        else
        {
            properties.setProperty( key, value.toString() );
        }
    }

}
