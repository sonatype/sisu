package org.codehaus.plexus.component.configurator.converters.lookup;

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
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.basic.BooleanConverter;
import org.codehaus.plexus.component.configurator.converters.basic.ByteConverter;
import org.codehaus.plexus.component.configurator.converters.basic.CharConverter;
import org.codehaus.plexus.component.configurator.converters.basic.DateConverter;
import org.codehaus.plexus.component.configurator.converters.basic.DoubleConverter;
import org.codehaus.plexus.component.configurator.converters.basic.EnumConverter;
import org.codehaus.plexus.component.configurator.converters.basic.FileConverter;
import org.codehaus.plexus.component.configurator.converters.basic.FloatConverter;
import org.codehaus.plexus.component.configurator.converters.basic.IntConverter;
import org.codehaus.plexus.component.configurator.converters.basic.LongConverter;
import org.codehaus.plexus.component.configurator.converters.basic.ShortConverter;
import org.codehaus.plexus.component.configurator.converters.basic.StringBufferConverter;
import org.codehaus.plexus.component.configurator.converters.basic.StringConverter;
import org.codehaus.plexus.component.configurator.converters.basic.UriConverter;
import org.codehaus.plexus.component.configurator.converters.basic.UrlConverter;
import org.codehaus.plexus.component.configurator.converters.composite.ArrayConverter;
import org.codehaus.plexus.component.configurator.converters.composite.CollectionConverter;
import org.codehaus.plexus.component.configurator.converters.composite.MapConverter;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.composite.PlexusConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.composite.PropertiesConverter;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultConverterLookup
    implements ConverterLookup
{
    private final List<ConfigurationConverter> converters = new ArrayList<ConfigurationConverter>();

    private final List<ConfigurationConverter> customConverters = new CopyOnWriteArrayList<ConfigurationConverter>();

    private final Map<Class<?>, ConfigurationConverter> converterMap = new ConcurrentHashMap<Class<?>, ConfigurationConverter>();

    public DefaultConverterLookup()
    {
        registerDefaultBasicConverters();

        registerDefaultCompositeConverters();
    }

    public synchronized void registerConverter( ConfigurationConverter converter )
    {
        customConverters.add( converter );
    }

    protected void registerDefaultConverter( ConfigurationConverter converter )
    {
        converters.add( converter );
    }

    public ConfigurationConverter lookupConverterForType( Class<?> type )
        throws ComponentConfigurationException
    {
        ConfigurationConverter retValue = converterMap.get( type );

        if ( retValue == null )
        {
            if ( customConverters != null )
            {
                retValue = findConverterForType( customConverters, type );
            }

            if ( retValue == null )
            {
                retValue = findConverterForType( converters, type );
            }

            if ( retValue == null )
            {
                // this is highly irregular
                throw new ComponentConfigurationException( "Configuration converter lookup failed for type: " + type );
            }

            converterMap.put( type, retValue );
        }

        return retValue;
    }

    private ConfigurationConverter findConverterForType( List<ConfigurationConverter> converters, Class<?> type )
    {
        for ( ConfigurationConverter converter : converters )
        {
            if ( converter.canConvert( type ) )
            {
                return converter;
            }
        }

        return null;
    }


    private void registerDefaultBasicConverters()
    {
        registerDefaultConverter( new BooleanConverter() );

        registerDefaultConverter( new ByteConverter() );

        registerDefaultConverter( new CharConverter() );

        registerDefaultConverter( new DoubleConverter() );

        registerDefaultConverter( new FloatConverter() );

        registerDefaultConverter( new IntConverter() );

        registerDefaultConverter( new LongConverter() );

        registerDefaultConverter( new ShortConverter() );

        registerDefaultConverter( new StringBufferConverter() );

        registerDefaultConverter( new StringConverter() );

        registerDefaultConverter( new DateConverter() );

        registerDefaultConverter( new FileConverter() );

        registerDefaultConverter( new UrlConverter() );

        registerDefaultConverter( new UriConverter() );

        registerDefaultConverter( new EnumConverter() );
    }

    private void registerDefaultCompositeConverters()
    {
        registerDefaultConverter( new MapConverter() );

        registerDefaultConverter( new ArrayConverter() );

        registerDefaultConverter( new CollectionConverter() );

        registerDefaultConverter( new PropertiesConverter() );

        registerDefaultConverter( new PlexusConfigurationConverter() );

        // this converter should be always registred as the last one
        registerDefaultConverter( new ObjectWithFieldsConverter() );
    }
}
