package org.codehaus.plexus.component.factory.nonjava;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.factory.ComponentFactory;
import org.codehaus.plexus.component.factory.ComponentInstantiationException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

/** @author Jason van Zyl */
public class NonJavaComponentFactory
    implements ComponentFactory
{
    public String getId()
    {
        return "nonjava";
    }

    public Object newInstance( final ComponentDescriptor componentDescriptor, final ClassRealm classRealm,
                               final PlexusContainer container )
        throws ComponentInstantiationException
    {
        return "component";
    }
}
