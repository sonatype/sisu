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
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.TypeAwareExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * @version $Id: AbstractBasicConverter.java 8514 2009-10-22 10:25:03Z bentmann $
 */
public abstract class AbstractBasicConverter
    extends AbstractConfigurationConverter
{
    protected abstract Object fromString( String str )
        throws ComponentConfigurationException;

    protected Object fromExpression( PlexusConfiguration configuration, ExpressionEvaluator expressionEvaluator, Class type )
        throws ComponentConfigurationException
    {
        Object v = null;

        String value = configuration.getValue( null );

        if ( value != null && value.length() > 0 )
        {
            // Object is provided by an expression
            // This seems a bit ugly... canConvert really should return false in this instance, but it doesn't have the
            //   configuration to know better
            try
            {
                if ( expressionEvaluator instanceof TypeAwareExpressionEvaluator )
                {
                    v = ( (TypeAwareExpressionEvaluator) expressionEvaluator ).evaluate( value, type );
                }
                else
                {
                    v = expressionEvaluator.evaluate( value );
                }
            }
            catch ( ExpressionEvaluationException e )
            {
                String msg = "Error evaluating the expression '" + value + "' for configuration value '" +
                    configuration.getName() + "'";
                throw new ComponentConfigurationException( configuration, msg, e );
            }
        }

        if ( v == null )
        {
            value = configuration.getAttribute( "default-value", null );

            if ( value != null && value.length() > 0 )
            {
                try
                {
                    if ( expressionEvaluator instanceof TypeAwareExpressionEvaluator )
                    {
                        v = ( (TypeAwareExpressionEvaluator) expressionEvaluator ).evaluate( value, type );
                    }
                    else
                    {
                        v = expressionEvaluator.evaluate( value );
                    }
                }
                catch ( ExpressionEvaluationException e )
                {
                    String msg = "Error evaluating the expression '" + value + "' for configuration value '" +
                        configuration.getName() + "'";
                    throw new ComponentConfigurationException( configuration, msg, e );
                }
            }
        }

        /*
         * NOTE: We don't check the type here which would be ugly to do correctly (e.g. value=Short -> type=int), the
         * reflective setter/field injection will fail by itself when the type didn't match.
         */

        return v;
    }

    public Object fromConfiguration( ConverterLookup converterLookup, PlexusConfiguration configuration, Class type,
                                     Class baseType, ClassLoader classLoader, ExpressionEvaluator expressionEvaluator,
                                     ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        if ( configuration.getChildCount() > 0 )
        {
            throw new ComponentConfigurationException( "When configuring a basic element the configuration cannot " +
                "contain any child elements. " + "Configuration element '" + configuration.getName() + "'." );
        }

        Object retValue = fromExpression( configuration, expressionEvaluator, type );

        if ( retValue instanceof String )
        {
            try
            {
                retValue = fromString( (String) retValue );
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
}
