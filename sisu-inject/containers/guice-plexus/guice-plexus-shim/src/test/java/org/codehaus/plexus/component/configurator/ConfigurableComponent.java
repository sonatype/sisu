package org.codehaus.plexus.component.configurator;

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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * @author Jason van Zyl
 * @version $Id: ConfigurableComponent.java 7783 2008-10-13 21:56:51Z bentmann $
 */
public class ConfigurableComponent
{
    private boolean booleanValue;

    private byte byteValue;

    private short shortValue;

    private int intValue;

    private float floatValue;

    private long longValue;

    private double doubleValue;

    private char charValue;

    private String stringValue;

    private File fileValue;

    private URI uriValue;

    private URL urlValue;

    private List importantThings;

    private PlexusConfiguration configuration;

    public boolean getBooleanValue()
    {
        return booleanValue;
    }

    public int getByteValue()
    {
        return byteValue;
    }

    public int getShortValue()
    {
        return shortValue;
    }

    public int getIntValue()
    {
        return intValue;
    }

    public float getFloatValue()
    {
        return floatValue;
    }

    public long getLongValue()
    {
        return longValue;
    }

    public double getDoubleValue()
    {
        return doubleValue;
    }

    public char getCharValue()
    {
        return charValue;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public File getFileValue()
    {
        return fileValue;
    }

    public URI getUriValue()
    {
        return uriValue;
    }

    public URL getUrlValue()
    {
        return urlValue;
    }

    public List getImportantThings()
    {
        return importantThings;
    }

    public PlexusConfiguration getConfiguration()
    {
        return configuration;
    }
}
