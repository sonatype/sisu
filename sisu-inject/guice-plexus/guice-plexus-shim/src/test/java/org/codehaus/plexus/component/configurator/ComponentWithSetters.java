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

import java.util.List;

import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * @author Jason van Zyl
 * @version $Id: ComponentWithSetters.java 7089 2007-11-25 15:19:06Z jvanzyl $
 */
public class ComponentWithSetters
{
    private int intValueVariable;

    private float floatValueVariable;

    private long longValueVariable;

    private double doubleValueVariable;

    private String stringValueVariable;

    private List importantThingsVariable;

    private PlexusConfiguration configurationVariable;

    public int getIntValue()
    {
        return intValueVariable;
    }

    public float getFloatValue()
    {
        return floatValueVariable;
    }

    public long getLongValue()
    {
        return longValueVariable;
    }

    public double getDoubleValue()
    {
        return doubleValueVariable;
    }

    public String getStringValue()
    {
        return stringValueVariable;
    }

    public List getImportantThings()
    {
        return importantThingsVariable;
    }

    public PlexusConfiguration getConfiguration()
    {
        return configurationVariable;
    }

    // ----------------------------------------------------------------------
    // setters
    // ----------------------------------------------------------------------

    boolean intValueSet;

    boolean floatValueSet;

    boolean longValueSet;

    boolean doubleValueSet;

    boolean stringValueSet;

    boolean importantThingsValueSet;

    boolean configurationValueSet;

    public void setIntValue( final int intValue )
    {
        intValueVariable = intValue;

        intValueSet = true;
    }

    public void setFloatValue( final float floatValue )
    {
        floatValueVariable = floatValue;

        floatValueSet = true;
    }

    public void setLongValue( final long longValue )
    {
        longValueVariable = longValue;

        longValueSet = true;
    }

    public void setDoubleValue( final double doubleValue )
    {
        doubleValueVariable = doubleValue;

        doubleValueSet = true;
    }

    public void setStringValue( final String stringValue )
    {
        stringValueVariable = stringValue;

        stringValueSet = true;
    }

    public void setImportantThings( final List importantThings )
    {
        importantThingsVariable = importantThings;

        importantThingsValueSet = true;
    }

    public void setConfiguration( final PlexusConfiguration configuration )
    {
        configurationVariable = configuration;

        configurationValueSet = true;
    }
}
