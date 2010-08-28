package org.codehaus.plexus.components;

/**
 * @plexus.component
 */
public class DefaultB
    implements B
{
    public void hello()
    {
        System.out.println( "Hello World!" );
    }
}
