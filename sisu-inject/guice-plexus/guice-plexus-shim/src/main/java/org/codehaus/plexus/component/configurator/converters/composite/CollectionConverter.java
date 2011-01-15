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

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ParameterizedConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 * @version $Id: CollectionConverter.java 8004 2009-01-04 18:39:40Z bentmann $
 */
@SuppressWarnings( { "unchecked", "rawtypes" } )
public class CollectionConverter
    extends AbstractConfigurationConverter
    implements ParameterizedConfigurationConverter
{

    public boolean canConvert( final Class type )
    {
        return Collection.class.isAssignableFrom( type ) && !Map.class.isAssignableFrom( type );
    }

    public Object fromConfiguration( final ConverterLookup converterLookup, final PlexusConfiguration configuration,
                                     final Class type, final Class baseType, final ClassLoader classLoader,
                                     final ExpressionEvaluator expressionEvaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        return fromConfiguration( converterLookup, configuration, type, null, baseType, classLoader,
                                  expressionEvaluator, listener );
    }

    public Object fromConfiguration( final ConverterLookup converterLookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Type[] typeArguments, final Class<?> baseType,
                                     final ClassLoader classLoader, final ExpressionEvaluator expressionEvaluator,
                                     final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object retValue = fromExpression( configuration, expressionEvaluator );

        if ( retValue != null )
        {
            if ( retValue instanceof Object[] )
            {
                final Collection<Object> collection = newCollection( configuration, type, classLoader );
                Collections.addAll( collection, (Object[]) retValue );
                retValue = collection;
            }
            else if ( retValue instanceof String
                && ( !retValue.equals( configuration.getValue() ) || "".equals( retValue ) ) )
            {
                /*
                 * NOTE: String input is supported for expressions only to avoid inconsistent merge behavior when
                 * configuration DOMs mix cdata value with element content.
                 */

                final PlexusConfiguration syntheticConfiguration =
                    toConfiguration( configuration.getName(), (String) retValue );

                retValue =
                    fromChildren( converterLookup, syntheticConfiguration, type, typeArguments, baseType, classLoader,
                                  expressionEvaluator, listener );
            }
            else
            {
                failIfNotTypeCompatible( retValue, type, configuration );
            }
        }
        else
        {
            retValue =
                fromChildren( converterLookup, configuration, type, typeArguments, baseType, classLoader,
                              expressionEvaluator, listener );
        }

        return retValue;
    }

    private PlexusConfiguration toConfiguration( final String name, final String value )
    {
        final PlexusConfiguration configuration = new XmlPlexusConfiguration( name );

        final String[] tokens = value != null && value.length() > 0 ? value.split( ",", -1 ) : new String[0];

        for ( final String token : tokens )
        {
            // use a name that can't accidentally clash with any existing bean class
            configuration.addChild( "#", token );
        }

        return configuration;
    }

    private Object fromChildren( final ConverterLookup converterLookup, final PlexusConfiguration configuration,
                                 final Class<?> type, final Type[] typeArguments, final Class<?> baseType,
                                 final ClassLoader classLoader, final ExpressionEvaluator expressionEvaluator,
                                 final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        final Collection<Object> retValue = newCollection( configuration, type, classLoader );

        final Class<?> elementType = getElementType( typeArguments );

        for ( int i = 0; i < configuration.getChildCount(); i++ )
        {
            final PlexusConfiguration c = configuration.getChild( i );

            final Class<?> childType = getChildType( c, elementType, baseType, classLoader );

            final ConfigurationConverter converter = converterLookup.lookupConverterForType( childType );

            final Object object =
                converter.fromConfiguration( converterLookup, c, childType, baseType, classLoader, expressionEvaluator,
                                             listener );

            retValue.add( object );
        }

        return retValue;
    }

    private Class<?> getElementType( final Type[] typeArguments )
    {
        Class<?> elementType = null;

        if ( typeArguments != null && typeArguments.length > 0 )
        {
            final Type typeArgument = typeArguments[0];

            if ( typeArgument instanceof Class && !Object.class.equals( typeArgument ) )
            {
                elementType = (Class<?>) typeArgument;
            }
        }

        return elementType;
    }

    private Class<?> getChildType( final PlexusConfiguration childConfiguration, final Class<?> elementType,
                                   final Class<?> baseType, final ClassLoader classLoader )
        throws ComponentConfigurationException
    {
        final String configEntry = childConfiguration.getName();

        final String name = fromXML( configEntry );

        Class<?> childType = getClassForImplementationHint( null, childConfiguration, classLoader );

        if ( childType == null && name.indexOf( '.' ) > 0 )
        {
            try
            {
                childType = classLoader.loadClass( name );
            }
            catch ( final ClassNotFoundException e )
            {
                // not found, continue processing
            }
        }

        if ( childType == null )
        {
            // Some classloaders don't create Package objects for classes
            // so we have to resort to slicing up the class name

            final String baseTypeName = baseType.getName();

            final int lastDot = baseTypeName.lastIndexOf( '.' );

            String className;

            if ( lastDot == -1 )
            {
                className = name;
            }
            else
            {
                final String basePackage = baseTypeName.substring( 0, lastDot );

                className = basePackage + "." + StringUtils.capitalizeFirstLetter( name );
            }

            try
            {
                childType = classLoader.loadClass( className );
            }
            catch ( final ClassNotFoundException e )
            {
                if ( elementType != null )
                {
                    childType = elementType;
                }
                else if ( childConfiguration.getChildCount() == 0 )
                {
                    // If no children, try a String.
                    childType = String.class;
                }
                else
                {
                    throw new ComponentConfigurationException( "Error loading class '" + className + "'", e );
                }
            }
        }

        return childType;
    }

    private Collection<Object> newCollection( final PlexusConfiguration configuration, final Class<?> type,
                                              final ClassLoader classLoader )
        throws ComponentConfigurationException
    {
        Object collection;

        final Class<?> implementation = getClassForImplementationHint( type, configuration, classLoader );

        // we can have 2 cases here:
        // - provided collection class which is not abstract
        // like Vector, ArrayList, HashSet - so we will just instantantiate it
        // - we have an abtract class so we have to use default collection type

        if ( Modifier.isAbstract( implementation.getModifiers() ) )
        {
            collection = getDefaultCollection( implementation );
        }
        else
        {
            try
            {
                collection = instantiateObject( implementation );
            }
            catch ( final ComponentConfigurationException e )
            {
                if ( e.getFailedConfiguration() == null )
                {
                    e.setFailedConfiguration( configuration );
                }

                throw e;
            }
        }

        try
        {
            return Collection.class.cast( collection );
        }
        catch ( final ClassCastException e )
        {
            throw new ComponentConfigurationException( configuration, "The class " + implementation.getName()
                + " used to configure the property '" + configuration.getName() + "' is not a collection", e );
        }
    }

    protected Collection getDefaultCollection( final Class collectionType )
    {
        Collection retValue = null;

        if ( List.class.isAssignableFrom( collectionType ) )
        {
            retValue = new ArrayList();
        }
        else if ( SortedSet.class.isAssignableFrom( collectionType ) )
        {
            retValue = new TreeSet();
        }
        else if ( Set.class.isAssignableFrom( collectionType ) )
        {
            retValue = new HashSet();
        }

        return retValue;
    }

}
