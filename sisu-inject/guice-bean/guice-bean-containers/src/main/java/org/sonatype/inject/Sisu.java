/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.inject;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Properties;

import org.sonatype.guice.bean.containers.SisuContainer;

import com.google.inject.Key;
import com.google.inject.name.Names;

public final class Sisu
{
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void configure( final Class<?> type, final Properties properties )
    {
        configure( type, (Map) properties );
    }

    public void configure( final Class<?> type, final Map<String, String> properties )
    {
        SisuContainer.configure( type, properties );
    }

    public static <T> T lookup( final Class<T> type )
    {
        return SisuContainer.lookup( Key.get( type ) );
    }

    public static <T> T lookup( final Class<T> type, final String name )
    {
        return SisuContainer.lookup( Key.get( type, Names.named( name ) ) );
    }

    public static <T> T lookup( final Class<T> type, final Annotation qualifier )
    {
        return SisuContainer.lookup( Key.get( type, qualifier ) );
    }

    public static <T> T lookup( final Class<T> type, final Class<? extends Annotation> qualifier )
    {
        return SisuContainer.lookup( Key.get( type, qualifier ) );
    }

    public static void inject( final Object that )
    {
        SisuContainer.inject( that );
    }
}
