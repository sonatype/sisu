/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.reflect;

import java.rmi.UnexpectedException;

public class LoggingExample
{
    public LoggingExample()
    {
        Logs.debug( "", null, null );
        Logs.debug( "", "a", "b" );
        Logs.debug( "{", null, null );
        Logs.debug( "}", "a", "b" );
        Logs.debug( "}{", null, null );
        Logs.debug( "{}", "a", "b" );
        Logs.debug( "}{}", null, null );
        Logs.debug( "{{}", "a", "b" );
        Logs.debug( "{}{", null, null );
        Logs.debug( "{}}", "a", "b" );
        Logs.debug( "{{}}", null, null );
        Logs.debug( "}{}{", "a", "b" );
        Logs.debug( "{}{}", null, null );
        Logs.debug( "{}{}", "a", "b" );
        Logs.debug( "{{}}{{}}", null, null );
        Logs.debug( "{}-{}", "a", "b" );

        Logs.warn( "oops", null, new UnexpectedException( "doh!" ) );
    }
}
