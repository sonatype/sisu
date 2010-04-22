package org.codehaus.plexus.component.configurator.converters;

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
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;


public interface ConfigurationConverter
{
    boolean canConvert( Class type );

    /**
     * @param converterLookup Repository of available converters
     * @param configuration
     * @param type the type of object to read
     * @param baseType the type of object the the source is
     * @param classLoader ClassLoader which should be used for loading classes
     * @param expressionEvaluator the expression evaluator to use for expressions
     * @return the object
     * @throws ComponentConfigurationException
     * @todo a better way, instead of baseType, would be to pass in a factory for new classes that could be based from the given package
     */
    Object fromConfiguration( ConverterLookup converterLookup, PlexusConfiguration configuration, Class type,
                              Class baseType, ClassLoader classLoader, ExpressionEvaluator expressionEvaluator )
        throws ComponentConfigurationException;

    /**
     * @param converterLookup Repository of available converters
     * @param configuration
     * @param type the type of object to read
     * @param baseType the type of object the the source is
     * @param classLoader ClassLoader which should be used for loading classes
     * @param expressionEvaluator the expression evaluator to use for expressions
     * @return the object
     * @throws ComponentConfigurationException
     * @todo a better way, instead of baseType, would be to pass in a factory for new classes that could be based from the given package
     */
    Object fromConfiguration( ConverterLookup converterLookup, PlexusConfiguration configuration, Class type,
                              Class baseType, ClassLoader classLoader, ExpressionEvaluator expressionEvaluator,
                              ConfigurationListener listener )
        throws ComponentConfigurationException;
}
