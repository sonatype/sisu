package org.codehaus.plexus.test;

import java.util.concurrent.atomic.AtomicInteger;

public interface StartableComponent
{
    static String ROLE = StartableComponent.class.getName();

    AtomicInteger startGenerator = new AtomicInteger( 1 );

    AtomicInteger stopGenerator = new AtomicInteger( 1 );

    void assertStartOrderCorrect();

    void assertStopOrderCorrect();
}
