package org.codehaus.plexus.plugins;

import org.codehaus.plexus.components.A;
import org.codehaus.plexus.components.C;

/**
 * @plexus.component role-hint="alt"
 */
public class AltPlugin1
    implements Plugin1
{
    /** @plexus.requirement */
    private A a;
    
    /** @plexus.requirement */    
    private C c;
    
    public void hello()
    {
        System.out.println( "Hello World!" );
    }
}
