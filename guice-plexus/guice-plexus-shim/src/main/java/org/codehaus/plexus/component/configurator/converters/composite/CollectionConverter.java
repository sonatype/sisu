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
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.StringUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * @author <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 * @version $Id: CollectionConverter.java 8004 2009-01-04 18:39:40Z bentmann $
 */
public class CollectionConverter
    extends AbstractConfigurationConverter
{
    public boolean canConvert( Class type )
    {
        return Collection.class.isAssignableFrom( type ) && !Map.class.isAssignableFrom( type );
    }

    public Object fromConfiguration( ConverterLookup converterLookup, PlexusConfiguration configuration, Class type,
                                     Class baseType, ClassLoader classLoader, ExpressionEvaluator expressionEvaluator,
                                     ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object retValue = fromExpression( configuration, expressionEvaluator, type );
        if ( retValue != null )
        {
            return retValue;
        }

        Class implementation = getClassForImplementationHint( null, configuration, classLoader );

        if ( implementation != null )
        {
            retValue = instantiateObject( implementation );
        }
        else
        {
            // we can have 2 cases here:
            //  - provided collection class which is not abstract
            //     like Vector, ArrayList, HashSet - so we will just instantantiate it
            // - we have an abtract class so we have to use default collection type
            int modifiers = type.getModifiers();

            if ( Modifier.isAbstract( modifiers ) )
            {
                retValue = getDefaultCollection( type );
            }
            else
            {
                try
                {
                    retValue = type.newInstance();
                }
                catch ( IllegalAccessException e )
                {
                    String msg = "An attempt to convert configuration entry " + configuration.getName() + "' into " +
                        type + " object failed: " + e.getMessage();

                    throw new ComponentConfigurationException( msg, e );
                }
                catch ( InstantiationException e )
                {
                    String msg = "An attempt to convert configuration entry " + configuration.getName() + "' into " +
                        type + " object failed: " + e.getMessage();

                    throw new ComponentConfigurationException( msg, e );
                }
            }
        }
        // now we have collection and we have to add some objects to it

        for ( int i = 0; i < configuration.getChildCount(); i++ )
        {
            PlexusConfiguration c = configuration.getChild( i );
            //Object o = null;

            String configEntry = c.getName();

            String name = fromXML( configEntry );

            Class childType = getClassForImplementationHint( null, c, classLoader );

            if ( childType == null && name.indexOf( '.' ) > 0 )
            {
                try
                {
                    childType = classLoader.loadClass( name );
                }
                catch ( ClassNotFoundException e )
                {
                    // not found, continue processing
                }
            }

            if ( childType == null )
            {
                // Some classloaders don't create Package objects for classes
                // so we have to resort to slicing up the class name

                String baseTypeName = baseType.getName();

                int lastDot = baseTypeName.lastIndexOf( '.' );

                String className;

                if ( lastDot == -1 )
                {
                    className = name;
                }
                else
                {
                    String basePackage = baseTypeName.substring( 0, lastDot );

                    className = basePackage + "." + StringUtils.capitalizeFirstLetter( name );
                }

                try
                {
                    childType = classLoader.loadClass( className );
                }
                catch ( ClassNotFoundException e )
                {
                    if ( c.getChildCount() == 0 )
                    {
                        // If no children, try a String.
                        // TODO: If we had generics we could try that instead - or could the component descriptor list an impl?
                        childType = String.class;
                    }
                    else
                    {
                        throw new ComponentConfigurationException( "Error loading class '" + className + "'", e );
                    }
                }
            }

            ConfigurationConverter converter = converterLookup.lookupConverterForType( childType );

            Object object = converter.fromConfiguration( converterLookup, c, childType, baseType, classLoader,
                                                         expressionEvaluator, listener );

            Collection collection = (Collection) retValue;
            collection.add( object );
        }

        return retValue;
    }

    protected Collection getDefaultCollection( Class collectionType )
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
