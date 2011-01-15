package org.codehaus.plexus.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockLoggerFactory
    implements ILoggerFactory
{
    public Logger getLogger( final String name )
    {
        return LoggerFactory.getLogger( "::" + name + "::" );
    }
}
