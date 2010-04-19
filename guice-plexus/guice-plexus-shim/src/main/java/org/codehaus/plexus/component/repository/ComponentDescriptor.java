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

    @SuppressWarnings( "unchecked" )
    private Class<T> implementationClass = (Class) Object.class;

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
        setRealm( classRealm );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void setRole( final String role )
    {
        this.role = role;
    }

    public final void setRoleClass( final Class<T> roleClass )
    {
        this.role = null != roleClass ? roleClass.getName() : null;
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
        implementationClass = null;
        this.classRealm = classRealm;
    }

    public final String getImplementation()
    {
        return implementation;
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
            catch ( final ClassNotFoundException e ) // NOPMD
            {
                // follow Plexus and fall back to Object.class
            }
        }
        return implementationClass;
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
        return "role: '" + role + "', implementation: '" + implementation + "', role hint: '" + hint + "'";
    }

    @Override
    public final String toString()
    {
        return getClass().getName() + " [role: '" + role + "', hint: '" + hint + "', realm: " + classRealm + "]";
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
            return equals( role, rhsDescriptor.role ) && equals( hint, rhsDescriptor.hint )
                && equals( classRealm, rhsDescriptor.classRealm );
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return ( ( 17 * 31 + hash( role ) ) * 31 + hash( hint ) ) * 31 + hash( classRealm );
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
