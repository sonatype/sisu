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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter extends AbstractBasicConverter
{
    /***
     * @todo DateFormat is not thread safe!
     */
    private static final DateFormat[] formats = {
        new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S a" ),
        new SimpleDateFormat( "yyyy-MM-dd HH:mm:ssa" )
    };

    public boolean canConvert( Class type )
    {
        return type.equals( Date.class );
    }

    public Object fromString( String str )
    {
        for ( int i = 0; i < formats.length; i++ )
        {
            try
            {
                return formats[i].parse( str );
            }
            catch ( ParseException e )
            {
                // no worries, let's try the next format.
            }
        }

        return null;
    }

    public String toString( Object obj )
    {
        Date date = (Date) obj;
        return formats[0].format( date );
    }

}
