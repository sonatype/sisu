/**
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus.configuration.xml;

import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public final class XmlPlexusConfiguration
    extends DefaultPlexusConfiguration
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public XmlPlexusConfiguration( final String name )
    {
        super( name );
    }

    public XmlPlexusConfiguration( final Xpp3Dom dom )
    {
        super( dom.getName(), dom.getValue() );

        for ( final String attribute : dom.getAttributeNames() )
        {
            setAttribute( attribute, dom.getAttribute( attribute ) );
        }

        for ( final Xpp3Dom child : dom.getChildren() )
        {
            addChild( new XmlPlexusConfiguration( child ) );
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder().append( '<' ).append( getName() );
        for ( final String a : getAttributeNames() )
        {
            buf.append( ' ' ).append( a ).append( "=\"" ).append( getAttribute( a ) ).append( '"' );
        }
        if ( getChildCount() > 0 )
        {
            buf.append( '>' );
            for ( final PlexusConfiguration c : getChildren() )
            {
                buf.append( c );
            }
            buf.append( "</" ).append( getName() ).append( '>' );
        }
        else if ( null != getValue() )
        {
            buf.append( '>' ).append( getValue() ).append( "</" ).append( getName() ).append( '>' );
        }
        else
        {
            buf.append( "/>" );
        }
        return buf.append( '\n' ).toString();
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    @Override
    protected PlexusConfiguration createChild( final String name )
    {
        return new XmlPlexusConfiguration( name );
    }
}
