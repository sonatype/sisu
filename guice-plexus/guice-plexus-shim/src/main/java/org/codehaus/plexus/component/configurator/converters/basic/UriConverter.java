/*
 * Copyright (C) 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.plexus.component.configurator.converters.basic;

import java.net.URI;
import java.net.URISyntaxException;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;

/**
 * Converter for {@link URI} objects.
 *
 * @version $Id: UriConverter.java 6935 2007-10-06 08:15:56Z user57 $
 */
public class UriConverter
    extends AbstractBasicConverter
{
    public boolean canConvert(final Class type) {
        assert type != null;

        return type.equals(URI.class);
    }

    public Object fromString(final String str) throws ComponentConfigurationException {
        assert str != null;

        try {
            return new URI(str);
        }
        catch (URISyntaxException e) {
            throw new ComponentConfigurationException("Unable to convert to URI: " + str, e);
        }
    }
}