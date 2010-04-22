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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 */
public class UrlConverter
    extends AbstractBasicConverter
{
    public boolean canConvert( Class type )
    {
        return type.equals( URL.class );
    }

    public Object fromString( String str )
        throws ComponentConfigurationException
    {
        try
        {
            return new URL( str );
        }
        catch ( MalformedURLException e )
        {
            throw new ComponentConfigurationException( "Unable to convert '" + str + "' to an URL", e );
        }
    }
}
