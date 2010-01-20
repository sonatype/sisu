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
package org.codehaus.plexus.component.repository;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

public class ComponentDescriptor<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String componentConfigurator;

    private String componentFactory;

    private String implementation;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unused" )
    public final void setRole( final String role )
    {
    }

    @SuppressWarnings( "unused" )
    public final void setRoleHint( final String hint )
    {
    }

    @SuppressWarnings( "unused" )
    public final void setInstantiationStrategy( final String instantiationStrategy )
    {
    }

    @SuppressWarnings( "unused" )
    public final void setDescription( final String description )
    {
    }

    @SuppressWarnings( "unused" )
    public final void setComponentComposer( final String componentComposer )
    {
    }

    public final void setComponentConfigurator( final String componentConfigurator )
    {
        this.componentConfigurator = componentConfigurator;
    }

    public final void setComponentFactory( final String componentFactory )
    {
        this.componentFactory = componentFactory;
    }

    public final void setImplementation( final String implementation )
    {
        this.implementation = implementation;
    }

    public final String getComponentConfigurator()
    {
        return componentConfigurator;
    }

    public final String getComponentFactory()
    {
        return componentFactory;
    }

    public final String getImplementation()
    {
        return implementation;
    }

    @SuppressWarnings( "unused" )
    public final void addRequirement( final ComponentRequirement requirement )
    {
    }

    @SuppressWarnings( "unused" )
    public final void setRealm( final ClassRealm classRealm )
    {
    }
}
