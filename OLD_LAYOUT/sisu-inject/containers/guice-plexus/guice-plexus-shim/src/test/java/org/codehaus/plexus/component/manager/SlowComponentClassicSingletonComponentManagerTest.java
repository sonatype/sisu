package org.codehaus.plexus.component.manager;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author Ben Walding
 * @version $Id: SlowComponentClassicSingletonComponentManagerTest.java 7876 2008-11-23 14:39:51Z bentmann $
 */
public class SlowComponentClassicSingletonComponentManagerTest
    extends PlexusTestCase
{
    public void testThreads1()
        throws Exception
    {
        test( 1 );
    }

    /**
     * Tests that multiple concurrent threads don't acquire different components.
     */
    public void testThreads1000()
        throws Exception
    {
        test( 1000 );
    }

    private void test( final int count )
        throws Exception
    {
        final ComponentLookupThread components[] = new ComponentLookupThread[count];
        // Create them
        for ( int i = 0; i < count; i++ )
        {
            components[i] = new ComponentLookupThread( getContainer() );
        }
        // Start them
        for ( int i = 0; i < count; i++ )
        {
            components[i].start();
        }

        // Wait for them to finish
        for ( int i = 0; i < count; i++ )
        {
            components[i].join( 10000 );
        }

        // Get master component
        final SlowComponent masterComponent = lookup( SlowComponent.class );

        // Verify them
        for ( int i = 0; i < count; i++ )
        {
            assertSame( i + ":" + components[i].getComponent() + " == " + masterComponent, masterComponent,
                        components[i].getComponent() );
        }
    }

    class ComponentLookupThread
        extends Thread
    {
        final PlexusContainer container;

        private SlowComponent component;

        public ComponentLookupThread( final PlexusContainer container )
        {
            /*
             * NOTE: A high priority seems to increase the likelihood of exhibiting missing synchronization.
             */
            setPriority( MAX_PRIORITY );
            this.container = container;
        }

        @Override
        public void run()
        {
            try
            {
                // DefaultPlexusContainer.setLookupRealm( lookupRealm );
                final SlowComponent tmpComponent = container.lookup( SlowComponent.class );

                synchronized ( this )
                {
                    component = tmpComponent;
                }
            }
            catch ( final Exception e )
            {
                container.getLookupRealm().display();
                e.printStackTrace();
            }
        }

        public SlowComponent getComponent()
        {
            synchronized ( this )
            {
                return component;
            }
        }
    }
}
