package org.codehaus.plexus.component.repository.io;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.ComponentRequirementList;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.io.XmlPlexusConfigurationReader;

/**
 * @author Jason van Zyl
 */
public class PlexusTools
{
    public static PlexusConfiguration buildConfiguration( final String resourceName, final Reader configuration )
        throws PlexusConfigurationException
    {
        try
        {
            final XmlPlexusConfigurationReader reader = new XmlPlexusConfigurationReader();

            final PlexusConfiguration result = reader.read( configuration );

            return result;
        }
        catch ( final PlexusConfigurationException e )
        {
            throw new PlexusConfigurationException( "PlexusConfigurationException building configuration from: "
                + resourceName, e );
        }
        catch ( final IOException e )
        {
            throw new PlexusConfigurationException( "IO error building configuration from: " + resourceName, e );
        }
    }

    public static PlexusConfiguration buildConfiguration( final String configuration )
        throws PlexusConfigurationException
    {
        return buildConfiguration( "<String Memory Resource>", new StringReader( configuration ) );
    }

    public static ComponentDescriptor<?> buildComponentDescriptor( final String configuration, final ClassRealm realm )
        throws PlexusConfigurationException
    {
        return buildComponentDescriptor( buildConfiguration( configuration ), realm );
    }

    public static ComponentDescriptor<?> buildComponentDescriptor( final PlexusConfiguration configuration )
        throws PlexusConfigurationException
    {
        return buildComponentDescriptorImpl( configuration, null );
    }

    public static ComponentDescriptor<?> buildComponentDescriptor( final PlexusConfiguration configuration,
                                                                   final ClassRealm realm )
        throws PlexusConfigurationException
    {
        if ( realm == null )
        {
            throw new NullPointerException( "realm is null" );
        }

        return buildComponentDescriptorImpl( configuration, realm );
    }

    private static ComponentDescriptor<?> buildComponentDescriptorImpl( final PlexusConfiguration configuration,
                                                                        final ClassRealm realm )
        throws PlexusConfigurationException
    {
        final String implementation = configuration.getChild( "implementation" ).getValue();
        if ( implementation == null )
        {
            throw new PlexusConfigurationException( "implementation is null" );
        }

        ComponentDescriptor<?> cd;
        try
        {
            if ( realm != null )
            {
                final Class<?> implementationClass = realm.loadClass( implementation );
                cd = new ComponentDescriptor( implementationClass, realm );
            }
            else
            {
                cd = new ComponentDescriptor();
                cd.setImplementation( implementation );
            }
        }
        catch ( final Throwable e )
        {
            throw new PlexusConfigurationException( "Can not load implementation class " + implementation
                + " from realm " + realm, e );
        }

        cd.setRole( configuration.getChild( "role" ).getValue() );

        cd.setRoleHint( configuration.getChild( "role-hint" ).getValue() );

        cd.setVersion( configuration.getChild( "version" ).getValue() );

        cd.setComponentType( configuration.getChild( "component-type" ).getValue() );

        cd.setInstantiationStrategy( configuration.getChild( "instantiation-strategy" ).getValue() );

        cd.setLifecycleHandler( configuration.getChild( "lifecycle-handler" ).getValue() );

        cd.setComponentProfile( configuration.getChild( "component-profile" ).getValue() );

        cd.setComponentComposer( configuration.getChild( "component-composer" ).getValue() );

        cd.setComponentConfigurator( configuration.getChild( "component-configurator" ).getValue() );

        cd.setComponentFactory( configuration.getChild( "component-factory" ).getValue() );

        cd.setDescription( configuration.getChild( "description" ).getValue() );

        cd.setAlias( configuration.getChild( "alias" ).getValue() );

        final String s = configuration.getChild( "isolated-realm" ).getValue();

        if ( s != null )
        {
            cd.setIsolatedRealm( s.equals( "true" ) ? true : false );
        }

        // ----------------------------------------------------------------------
        // Here we want to look for directives for inlining external
        // configurations. we probably want to take them from files or URLs.
        // ----------------------------------------------------------------------

        cd.setConfiguration( configuration.getChild( "configuration" ) );

        // ----------------------------------------------------------------------
        // Requirements
        // ----------------------------------------------------------------------

        final PlexusConfiguration[] requirements = configuration.getChild( "requirements" ).getChildren( "requirement" );

        for ( final PlexusConfiguration requirement : requirements )
        {
            ComponentRequirement cr;

            final PlexusConfiguration[] hints = requirement.getChild( "role-hints" ).getChildren( "role-hint" );
            if ( hints != null && hints.length > 0 )
            {
                cr = new ComponentRequirementList();

                final List<String> hintList = new LinkedList<String>();
                for ( final PlexusConfiguration hint : hints )
                {
                    hintList.add( hint.getValue() );
                }

                ( (ComponentRequirementList) cr ).setRoleHints( hintList );
            }
            else
            {
                cr = new ComponentRequirement();

                cr.setRoleHint( requirement.getChild( "role-hint" ).getValue() );
            }

            cr.setRole( requirement.getChild( "role" ).getValue() );

            cr.setOptional( Boolean.parseBoolean( requirement.getChild( "optional" ).getValue() ) );

            cr.setFieldName( requirement.getChild( "field-name" ).getValue() );

            cd.addRequirement( cr );
        }

        return cd;
    }

    public static ComponentSetDescriptor buildComponentSet( final PlexusConfiguration c )
        throws PlexusConfigurationException
    {
        return buildComponentSet( c, null );
    }

    public static ComponentSetDescriptor buildComponentSet( final PlexusConfiguration c, final ClassRealm realm )
        throws PlexusConfigurationException
    {
        final ComponentSetDescriptor csd = new ComponentSetDescriptor();

        // ----------------------------------------------------------------------
        // Components
        // ----------------------------------------------------------------------

        final PlexusConfiguration[] components = c.getChild( "components" ).getChildren( "component" );

        for ( final PlexusConfiguration component : components )
        {
            csd.addComponentDescriptor( buildComponentDescriptorImpl( component, realm ) );
        }

        // ----------------------------------------------------------------------
        // Dependencies
        // ----------------------------------------------------------------------

        final PlexusConfiguration[] dependencies = c.getChild( "dependencies" ).getChildren( "dependency" );

        for ( final PlexusConfiguration d : dependencies )
        {
            final ComponentDependency cd = new ComponentDependency();

            cd.setArtifactId( d.getChild( "artifact-id" ).getValue() );

            cd.setGroupId( d.getChild( "group-id" ).getValue() );

            final String type = d.getChild( "type" ).getValue();
            if ( type != null )
            {
                cd.setType( type );
            }

            cd.setVersion( d.getChild( "version" ).getValue() );

            csd.addDependency( cd );
        }

        return csd;
    }

    public static void writeConfiguration( final PrintStream out, final PlexusConfiguration configuration )
        throws PlexusConfigurationException
    {
        writeConfiguration( out, configuration, "" );
    }

    private static void writeConfiguration( final PrintStream out, final PlexusConfiguration configuration,
                                            final String indent )
        throws PlexusConfigurationException
    {
        out.print( indent + "<" + configuration.getName() );
        final String[] atts = configuration.getAttributeNames();

        if ( atts.length > 0 )
        {
            for ( final String att : atts )
            {
                out.print( "\n" + indent + "  " + att + "='" + configuration.getAttribute( att ) + "'" );
            }
        }

        final PlexusConfiguration[] pc = configuration.getChildren();

        if ( configuration.getValue() != null && configuration.getValue().trim().length() > 0 || pc.length > 0 )
        {
            out.print( ">" + ( configuration.getValue() == null ? "" : configuration.getValue().trim() ) );

            if ( pc.length > 0 )
            {
                out.println();
                for ( final PlexusConfiguration element : pc )
                {
                    writeConfiguration( out, element, indent + "  " );
                }
                out.print( indent );
            }

            out.println( "</" + configuration.getName() + ">" );
        }
        else
        {
            out.println( "/>" );
        }
    }

}
