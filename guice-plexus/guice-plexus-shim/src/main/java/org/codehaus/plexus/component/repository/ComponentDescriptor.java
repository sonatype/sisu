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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.guice.plexus.config.Hints;

public class ComponentDescriptor<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String role;

    private String hint = Hints.DEFAULT_HINT;

    private String description;

    private String instantiationStrategy;

    private String implementation;

    private ClassRealm classRealm;

    private Class<?> implementationClass = Object.class;

    private String componentConfigurator;

    private String componentFactory;

    private List<ComponentRequirement> requirements = Collections.emptyList();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ComponentDescriptor()
    {
        // nothing to set
    }

    public ComponentDescriptor( final Class<T> implementationClass, final ClassRealm classRealm )
    {
        setImplementationClass( implementationClass );
        this.classRealm = classRealm;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void setRole( final String role )
    {
        this.role = role;
    }

    public final void setRoleHint( final String hint )
    {
        this.hint = Hints.canonicalHint( hint );
    }

    public final void setDescription( final String description )
    {
        this.description = description;
    }

    public final void setInstantiationStrategy( final String instantiationStrategy )
    {
        this.instantiationStrategy = instantiationStrategy;
    }

    public final void setImplementation( final String implementation )
    {
        this.implementation = implementation;
        implementationClass = Object.class;
    }

    @SuppressWarnings( "unchecked" )
    public final void setImplementationClass( final Class implementationClass )
    {
        this.implementationClass = implementationClass;
        implementation = implementationClass.getName();
    }

    public final void setComponentConfigurator( final String componentConfigurator )
    {
        this.componentConfigurator = componentConfigurator;
    }

    public final void setComponentFactory( final String componentFactory )
    {
        this.componentFactory = componentFactory;
    }

    public final void addRequirement( final ComponentRequirement requirement )
    {
        if ( requirements.isEmpty() )
        {
            requirements = new ArrayList<ComponentRequirement>();
        }
        requirements.add( requirement );
    }

    public String getRole()
    {
        return role;
    }

    @SuppressWarnings( "unchecked" )
    public Class<T> getRoleClass()
    {
        try
        {
            return classRealm.loadClass( getRole() );
        }
        catch ( final Throwable e )
        {
            return (Class) Object.class;
        }
    }

    public String getRoleHint()
    {
        return hint;
    }

    public final String getDescription()
    {
        return description;
    }

    public final String getInstantiationStrategy()
    {
        return instantiationStrategy;
    }

    public final void setRealm( final ClassRealm classRealm )
    {
        implementationClass = Object.class;
        this.classRealm = classRealm;
    }

    public final String getImplementation()
    {
        return implementation;
    }

    public final ClassRealm getRealm()
    {
        return classRealm;
    }

    @SuppressWarnings( "unchecked" )
    public final Class<T> getImplementationClass()
    {
        if ( Object.class == implementationClass && null != classRealm )
        {
            try
            {
                implementationClass = classRealm.loadClass( implementation );
            }
            catch ( final Throwable e ) // NOPMD
            {
                // follow Plexus and fall back to Object.class
            }
        }
        return (Class) implementationClass;
    }

    public final String getComponentConfigurator()
    {
        return componentConfigurator;
    }

    public final String getComponentFactory()
    {
        return componentFactory;
    }

    public final List<ComponentRequirement> getRequirements()
    {
        return Collections.unmodifiableList( requirements );
    }

    public final String getHumanReadableKey()
    {
        return "role: '" + getRole() + "', implementation: '" + implementation + "', role hint: '" + getRoleHint()
            + "'";
    }

    @Override
    public final String toString()
    {
        return getClass().getName() + " [role: '" + getRole() + "', hint: '" + getRoleHint() + "', realm: "
            + classRealm + "]";
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof ComponentDescriptor<?> )
        {
            final ComponentDescriptor<?> rhsDescriptor = (ComponentDescriptor<?>) rhs;
            return equals( getRole(), rhsDescriptor.getRole() ) && equals( getRoleHint(), rhsDescriptor.getRoleHint() )
                && equals( classRealm, rhsDescriptor.classRealm );
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return ( ( 17 * 31 + hash( getRole() ) ) * 31 + hash( getRoleHint() ) ) * 31 + hash( classRealm );
    }

    @SuppressWarnings( "unused" )
    public final void setComponentComposer( final String componentComposer )
    {
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static final <T> boolean equals( final T lhs, final T rhs )
    {
        return null != lhs ? lhs.equals( rhs ) : null == rhs;
    }

    private static final int hash( final Object obj )
    {
        return null != obj ? obj.hashCode() : 0;
    }
}
