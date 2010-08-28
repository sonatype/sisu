package org.codehaus.plexus.components;

/**
 * @plexus.component
 */
public class DefaultC
    implements C
{
    public void hello()
    {
        System.out.println( "Hello World!" );
    }
}
