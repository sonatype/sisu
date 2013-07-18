/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.examples.peaberry.test.impl;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.sisu.EagerSingleton;
import org.ops4j.peaberry.ServiceUnavailableException;
import org.sonatype.examples.peaberry.test.Scramble;

@Named
@EagerSingleton
class ServiceTest
{
    private static final String TEXT = "This is a simple test of peaberry.";

    @Inject
    ServiceTest( final Scramble scramble )
    {
        // quick'n'dirty test thread
        new Thread( new Runnable()
        {
            public void run()
            {
                while ( true )
                {
                    try
                    {
                        System.out.println( '[' + scramble.process( TEXT ) + ']' );
                    }
                    catch ( final ServiceUnavailableException e )
                    {
                        System.err.println( "No scrambler service!" );
                    }
                    try
                    {
                        Thread.sleep( 2000 );
                    }
                    catch ( final InterruptedException e )
                    {
                        // wake-up
                    }
                }
            }
        } ).start();
    }
}
