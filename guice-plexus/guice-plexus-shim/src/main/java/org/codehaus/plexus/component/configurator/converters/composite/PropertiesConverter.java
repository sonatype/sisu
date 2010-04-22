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
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.Properties;

/**
 * Converter for <code>java.util.Properties</code>.
 *
 * @author <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 * @version $Id: PropertiesConverter.java 8516 2009-10-26 12:54:02Z bentmann $
 */
public class PropertiesConverter
    extends AbstractConfigurationConverter
{
    public boolean canConvert( Class type )
    {
        return Properties.class.isAssignableFrom( type );
    }

    public Object fromConfiguration( ConverterLookup converterLookup, PlexusConfiguration configuration, Class type,
                                     Class baseType, ClassLoader classLoader, ExpressionEvaluator expressionEvaluator,
                                     ConfigurationListener listener )
        throws ComponentConfigurationException
    {

        Object retValueInterpolated = fromExpression( configuration, expressionEvaluator, type );
        if ( retValueInterpolated != null )
        {
            return retValueInterpolated;
        }

        String element = configuration.getName();

        Properties retValue = new Properties();

        PlexusConfiguration[] children = configuration.getChildren( "property" );

        if ( children != null && children.length > 0 )
        {
            for ( int i = 0; i < children.length; i++ )
            {
                PlexusConfiguration child = children[i];

                addEntry( retValue, element, child, expressionEvaluator );
            }
        }

        return retValue;
    }

    private void addEntry( Properties properties, String element, PlexusConfiguration property,
                           ExpressionEvaluator expressionEvaluator )
        throws ComponentConfigurationException
    {
        Object name = fromExpression( property.getChild( "name" ), expressionEvaluator );

        if ( name == null )
        {
            String msg =
                "Trying to convert the configuration element: '" + element
                    + "', missing child element 'name' for property.";

            throw new ComponentConfigurationException( msg );
        }

        Object value = fromExpression( property.getChild( "value" ), expressionEvaluator );

        if ( value == null )
        {
            properties.setProperty( name.toString(), "" );
        }
        else
        {
            properties.setProperty( name.toString(), value.toString() );
        }
    }

}
