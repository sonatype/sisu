package org.codehaus.plexus.component.configurator.converters;

/*
 * Copyright 2005-2007 Codehaus Foundation.
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.StringUtils;

/** @author <a href="mailto:kenney@codehaus.org">Kenney Westerhof</a> */
@SuppressWarnings( "rawtypes" )
public class ComponentValueSetter
{
    private final Object object;

    private final String fieldName;

    private final ConverterLookup lookup;

    private Method setter;

    private Class setterParamType;

    private ConfigurationConverter setterTypeConverter;

    private Field field;

    private Class fieldType;

    private ConfigurationConverter fieldTypeConverter;

    private final ConfigurationListener listener;

    public ComponentValueSetter( final String fieldName, final Object object, final ConverterLookup lookup )
        throws ComponentConfigurationException
    {
        this( fieldName, object, lookup, null );
    }

    public ComponentValueSetter( final String fieldName, final Object object, final ConverterLookup lookup,
                                 final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        this.fieldName = fieldName;
        this.object = object;
        this.lookup = lookup;
        this.listener = listener;

        if ( object == null )
        {
            throw new ComponentConfigurationException( "Component is null" );
        }

        initSetter();

        initField();

        if ( setter == null && field == null )
        {
            throw new ComponentConfigurationException( "Cannot find setter, adder nor field in "
                + object.getClass().getName() + " for '" + fieldName + "'" );
        }

        if ( setterTypeConverter == null && fieldTypeConverter == null )
        {
            throw new ComponentConfigurationException( "Cannot find converter for " + setterParamType.getName()
                + ( fieldType != null && !fieldType.equals( setterParamType ) ? " or " + fieldType.getName() : "" ) );
        }
    }

    private void initSetter()
    {
        setter = ReflectionUtils.getSetter( fieldName, object.getClass() );

        if ( setter == null )
        {
            setter = getAdder( fieldName, object.getClass() );

            if ( setter == null )
            {
                return;
            }
        }

        setterParamType = setter.getParameterTypes()[0];

        try
        {
            setterTypeConverter = lookup.lookupConverterForType( setterParamType );
        }
        catch ( final ComponentConfigurationException e )
        {
            // ignore, handle later
        }
    }

    private static Method getAdder( final String fieldName, final Class clazz )
    {
        final Method[] methods = clazz.getMethods();

        final String adderName = "add" + StringUtils.capitalizeFirstLetter( fieldName );

        for ( final Method method : methods )
        {
            if ( adderName.equals( method.getName() ) && !Modifier.isStatic( method.getModifiers() )
                && method.getParameterTypes().length == 1 )
            {
                return method;
            }
        }

        return null;
    }

    private void initField()
    {
        field = ReflectionUtils.getFieldByNameIncludingSuperclasses( fieldName, object.getClass() );

        if ( field == null )
        {
            return;
        }

        fieldType = field.getType();

        try
        {
            fieldTypeConverter = lookup.lookupConverterForType( fieldType );
        }
        catch ( final ComponentConfigurationException e )
        {
            // ignore, handle later
        }
    }

    private void setValueUsingField( final Object value )
        throws ComponentConfigurationException
    {
        try
        {
            final boolean wasAccessible = field.isAccessible();

            if ( !wasAccessible )
            {
                field.setAccessible( true );
            }

            if ( listener != null )
            {
                listener.notifyFieldChangeUsingReflection( fieldName, value, object );
            }

            field.set( object, value );

            if ( !wasAccessible )
            {
                field.setAccessible( false );
            }
        }
        catch ( final IllegalAccessException e )
        {
            throw new ComponentConfigurationException( "Cannot access field: " + field, e );
        }
        catch ( final IllegalArgumentException e )
        {
            throw new ComponentConfigurationException( "Cannot assign value '" + value + "' (type: " + value.getClass()
                + ") to " + field, e );
        }
    }

    private void setValueUsingSetter( final Object value )
        throws ComponentConfigurationException
    {
        if ( setterParamType == null || setter == null )
        {
            throw new ComponentConfigurationException( "No setter found" );
        }

        final String exceptionInfo =
            object.getClass().getName() + "." + setter.getName() + "( " + setterParamType.getClass().getName() + " )";

        if ( listener != null )
        {
            listener.notifyFieldChangeUsingSetter( fieldName, value, object );
        }

        try
        {
            setter.invoke( object, new Object[] { value } );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ComponentConfigurationException( "Cannot access method: " + exceptionInfo, e );
        }
        catch ( final IllegalArgumentException e )
        {
            throw new ComponentConfigurationException( "Invalid parameter supplied while setting '" + value + "' to "
                + exceptionInfo, e );
        }
        catch ( final InvocationTargetException e )
        {
            throw new ComponentConfigurationException(
                                                       "Setter " + exceptionInfo
                                                           + " threw exception when called with parameter '" + value
                                                           + "': " + e.getTargetException().getMessage(), e );
        }
    }

    public void configure( final PlexusConfiguration config, final ClassLoader classLoader,
                           final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        Object value = null;

        // try setter converter + method first

        if ( setterTypeConverter != null )
        {
            try
            {
                value =
                    setterTypeConverter.fromConfiguration( lookup, config, setterParamType, object.getClass(),
                                                           classLoader, evaluator, listener );

                if ( value != null )
                {
                    setValueUsingSetter( value );

                    return;
                }
            }
            catch ( final ComponentConfigurationException e )
            {
                if ( fieldTypeConverter == null
                    || fieldTypeConverter.getClass().equals( setterTypeConverter.getClass() ) )
                {
                    throw e;
                }
            }
        }

        // try setting field using value found with method
        // converter, if present.

        ComponentConfigurationException savedEx = null;

        if ( value != null )
        {
            try
            {
                setValueUsingField( value );
                return;
            }
            catch ( final ComponentConfigurationException e )
            {
                savedEx = e;
            }
        }

        // either no value or setting went wrong. Try
        // new converter.

        value =
            fieldTypeConverter.fromConfiguration( lookup, config, fieldType, object.getClass(), classLoader, evaluator,
                                                  listener );

        if ( value != null )
        {
            setValueUsingField( value );
        }
        // FIXME: need this?
        else if ( savedEx != null )
        {
            throw savedEx;
        }
    }

}
