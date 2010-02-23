package org.sonatype.inject;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Example of implementation of the contract in singular mode. The contract is unambigous, since only one interface is
 * implemented, and this will be "default" implementation, since the class name makes it feel so.
 * 
 * @author cstamas
 */
@Named
@Singleton
public class DefaultMySimpleComponent
    implements MySimpleComponent
{
    public String helloWorld()
    {
        return "hello world!";
    }
}
