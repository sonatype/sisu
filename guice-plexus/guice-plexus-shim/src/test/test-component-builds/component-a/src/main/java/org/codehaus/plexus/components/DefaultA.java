package org.codehaus.plexus.components;

/**
 * @plexus.component
 */
public class DefaultA
    implements A
{
    public void hello()
    {
        System.out.println( "Hello World!" );
    }
}
