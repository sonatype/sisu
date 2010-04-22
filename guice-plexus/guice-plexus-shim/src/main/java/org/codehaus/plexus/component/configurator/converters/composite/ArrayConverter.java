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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


/**
 * @author <a href="mailto:kenney@codehaus.org">Kenney Westerhof</a>
 * @version $Id: ArrayConverter.java 8005 2009-01-04 19:41:09Z bentmann $
 */
public class ArrayConverter
    extends AbstractConfigurationConverter
{
    public boolean canConvert( Class type )
    {
        return type.isArray();
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

        List values = new ArrayList();

        for ( int i = 0; i < configuration.getChildCount(); i++ )
        {
            PlexusConfiguration childConfiguration = configuration.getChild( i );

            String configEntry = childConfiguration.getName();

            String name = fromXML( configEntry );
            
            Class childType = getClassForImplementationHint( null, childConfiguration, classLoader );
            
            // check if the name is a fully qualified classname

            if ( childType == null && name.indexOf( '.' ) > 0 )
            {
                try
                {
                    childType = classLoader.loadClass( name );
                }
                catch ( ClassNotFoundException e )
                {
                    // doesn't exist - continue processing
                }
            }

            if ( childType == null )
            {
                // try to find the class in the package of the baseType
                // (which is the component being configured)

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
                    // doesn't exist, continue processing
                }                               
            }

            // finally just try the component type of the array

            if ( childType == null )
            {
                childType = type.getComponentType();
            }

            ConfigurationConverter converter = converterLookup.lookupConverterForType( childType );
            
            Object object = converter.fromConfiguration( converterLookup, 
                                                         childConfiguration, 
                                                         childType, 
                                                         baseType, 
                                                         classLoader,
                                                         expressionEvaluator, 
                                                         listener );

            values.add( object );
        }

        try
        {
            return values.toArray( (Object[]) Array.newInstance( type.getComponentType(), 0 ) );
        }
        catch ( ArrayStoreException e )
        {
            throw new ComponentConfigurationException( "Cannot assign configuration values to array of type "
                + type.getComponentType().getName() + ": " + values );
        }
    }

}
