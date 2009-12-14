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
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.sonatype.guice.plexus.config.Hints;

public final class ComponentDescriptor<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String role;

    private String hint = Hints.DEFAULT_HINT;

    private String instantiationStrategy;

    private String implementation;

    private ClassRealm classRealm;

    private Class<? extends T> roleClass;

    private Class<? extends T> implementationClass;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getDescription()
    {
        return null;
    }

    public void setRole( final String role )
    {
        this.role = role;
    }

    public void setRoleHint( final String hint )
    {
        this.hint = Hints.canonicalHint( hint );
    }

    public void setInstantiationStrategy( final String instantiationStrategy )
    {
        this.instantiationStrategy = instantiationStrategy;
    }

    public String getRole()
    {
        return role;
    }

    public String getRoleHint()
    {
        return hint;
    }

    public String getInstantiationStrategy()
    {
        return instantiationStrategy;
    }

    @SuppressWarnings( "unchecked" )
    public Class<T> getRoleClass()
    {
        if ( null == roleClass )
        {
            try
            {
                roleClass = classRealm.loadClass( role );
            }
            catch ( final Throwable e ) // NOPMD
            {
                // ignore
            }
        }
        return null == roleClass ? (Class) Object.class : roleClass;
    }

    public void setImplementation( final String implementation )
    {
        this.implementation = implementation;
    }

    public String getImplementation()
    {
        return implementation;
    }

    @SuppressWarnings( "unchecked" )
    public Class<? extends T> getImplementationClass()
    {
        if ( null == implementationClass )
        {
            try
            {
                implementationClass = classRealm.loadClass( implementation );
            }
            catch ( final Throwable e ) // NOPMD
            {
                // ignore
            }
        }
        return null == implementationClass ? (Class) Object.class : implementationClass;
    }

    @SuppressWarnings( "unused" )
    public void addRequirement( final ComponentRequirement requirement )
    {
    }

    @SuppressWarnings( "unused" )
    public void setConfiguration( final PlexusConfiguration configuration )
    {
    }

    @SuppressWarnings( "unused" )
    public void setComponentSetDescriptor( final ComponentSetDescriptor setDescriptor )
    {
    }

    public void setRealm( final ClassRealm classRealm )
    {
        this.classRealm = classRealm;
    }

    public ClassRealm getRealm()
    {
        return classRealm;
    }
}
