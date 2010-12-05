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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.basic.AbstractBasicConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * Sets the default property of a component. The default property is denoted by a method with the signature
 * {@code set(<type> arg)} .
 */
class ComponentDefaultValueSetter
{

    private final Object object;

    private final ConverterLookup lookup;

    private final ConfigurationListener listener;

    private final Method setter;

    public ComponentDefaultValueSetter( final Object object, final ConverterLookup lookup,
                                        final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        this.object = object;
        this.lookup = lookup;
        this.listener = listener;

        this.setter = getDefaultSetter( object );
    }

    private static Method getDefaultSetter( final Object object )
    {
        for ( Method method : object.getClass().getMethods() )
        {
            if ( "set".equals( method.getName() ) && method.getParameterTypes().length == 1
                && !Modifier.isStatic( method.getModifiers() ) )
            {
                return method;
            }
        }
        return null;
    }

    public void configure( final Object value, final PlexusConfiguration config, final ClassLoader classLoader,
                           final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        if ( setter == null )
        {
            throw new ComponentConfigurationException( config, "Cannot configure instance of "
                + object.getClass().getName() + " from " + value );
        }

        Class<?> propertyType = setter.getParameterTypes()[0];

        Object propertyValue;

        if ( propertyType.isInstance( value ) )
        {
            propertyValue = value;
        }
        else
        {
            ConfigurationConverter converter = lookup.lookupConverterForType( propertyType );

            if ( !( converter instanceof AbstractBasicConverter ) )
            {
                throw new ComponentConfigurationException( config, "Cannot configure instance of "
                    + object.getClass().getName() + " from " + value );
            }

            propertyValue =
                converter.fromConfiguration( lookup, config, propertyType, object.getClass(), classLoader, evaluator,
                                             listener );
        }

        if ( listener != null )
        {
            listener.notifyFieldChangeUsingSetter( "", propertyValue, object );
        }

        try
        {
            setter.invoke( object, propertyValue );
        }
        catch ( IllegalAccessException e )
        {
            throw new ComponentConfigurationException( config, "Cannot access method " + setter, e );
        }
        catch ( InvocationTargetException e )
        {
            throw new ComponentConfigurationException( config, "Could not invoke method " + setter + ": "
                + e.getCause(), e.getCause() );
        }
    }

}
