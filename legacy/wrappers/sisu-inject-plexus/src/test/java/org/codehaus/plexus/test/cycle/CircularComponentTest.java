package org.codehaus.plexus.test.cycle;

import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.PlexusTestCase;

import junit.framework.Assert;

public class CircularComponentTest
    extends PlexusTestCase
{
    public void testCircularComponents()
        throws Exception
    {
        lookup( CycleComponent.class, "A" );
    }

    public void testCircularComponentsInParallel()
        throws Exception
    {
        AtomicInteger passCount = new AtomicInteger();

        // access same cycle in parallel; activation of the cycle should be rooted
        // in the first scoped component rather than the first requested component
        // and should only be visible to other threads after activation

        Thread t1 = new Thread( new ParallelLookup( passCount ) );
        Thread t2 = new Thread( new ParallelLookup( passCount ) );

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Assert.assertEquals( 2, passCount.get() );
    }

    private class ParallelLookup
        implements Runnable
    {
        AtomicInteger passCount;

        public ParallelLookup( AtomicInteger passCount )
        {
            this.passCount = passCount;
        }

        public void run()
        {
            try
            {
                // accesses the same cycle indirectly from an unscoped component
                CycleComponent cycle = lookup( CycleComponent.class, "parallel" );
                // assertion will be thrown if cycle is not activated before use
                cycle.next().next().next();
                passCount.incrementAndGet();
            }
            catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }
    }
}
