package org.codehaus.plexus.component.configurator.converters.basic;

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

/**
 * @author Benjamin Bentmann
 */
@SuppressWarnings( "rawtypes" )
public class EnumConverter
    extends AbstractConfigurationConverter
{

    public boolean canConvert( final Class type )
    {
        return type.isEnum();
    }

    @SuppressWarnings( "unchecked" )
    public Object fromConfiguration( final ConverterLookup converterLookup, final PlexusConfiguration configuration,
                                     final Class type, final Class baseType, final ClassLoader classLoader,
                                     final ExpressionEvaluator expressionEvaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        if ( configuration.getChildCount() > 0 )
        {
            throw new ComponentConfigurationException( "When configuring a basic element the configuration cannot "
                + "contain any child elements. " + "Configuration element '" + configuration.getName() + "'." );
        }

        Object retValue = fromExpression( configuration, expressionEvaluator );

        if ( retValue instanceof String )
        {
            try
            {
                retValue = Enum.valueOf( type, (String) retValue );
            }
            catch ( final RuntimeException e )
            {
                throw new ComponentConfigurationException( "Cannot assign value " + retValue + " to property "
                    + configuration.getName() + " of " + baseType.getName() + ": " + e.getMessage(), e );
            }
        }

        return retValue;
    }

}
