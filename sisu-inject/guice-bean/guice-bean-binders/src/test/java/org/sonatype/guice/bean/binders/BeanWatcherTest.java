/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.bean.binders;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Qualifier;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.EagerSingleton;
import org.sonatype.inject.Mediator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class BeanWatcherTest
    extends TestCase
{
    @Qualifier
    @Retention( RetentionPolicy.RUNTIME )
    public @interface Marked
    {
        int value();
    }

    static abstract class Item
    {
    }

    static abstract class SomeItem
        extends Item
    {
    }

    @javax.inject.Named
    static class AItem
        extends SomeItem
    {
    }

    @Marked( 0 )
    @javax.inject.Named
    static class BItem
        extends SomeItem
    {
    }

    @EagerSingleton
    @javax.inject.Named
    static class CItem
        extends SomeItem
    {
        static boolean initialized;

        public CItem()
        {
            initialized = true;
        }
    }

    @Marked( 1 )
    static class DItem
        extends SomeItem
    {
    }

    @javax.inject.Named
    static class NamedItemWatcher
    {
        Map<String, Item> items = new HashMap<String, Item>();
    }

    @javax.inject.Named
    static class MarkedItemWatcher
    {
        Map<Integer, Item> items = new HashMap<Integer, Item>();
    }

    @javax.inject.Named
    static class NamedItemMediator
        implements Mediator<javax.inject.Named, Item, NamedItemWatcher>
    {
        public void add( final BeanEntry<javax.inject.Named, Item> bean, final NamedItemWatcher watcher )
            throws Exception
        {
            assertNull( watcher.items.put( bean.getKey().value(), bean.getValue() ) );
        }

        public void remove( final BeanEntry<javax.inject.Named, Item> bean, final NamedItemWatcher watcher )
            throws Exception
        {
            assertEquals( watcher.items.remove( bean.getKey().value() ), bean.getValue() );
        }
    }

    @javax.inject.Named
    static class MarkedItemMediator
        implements Mediator<Marked, Item, MarkedItemWatcher>
    {
        public void add( final BeanEntry<Marked, Item> bean, final MarkedItemWatcher watcher )
            throws Exception
        {
            assertNull( watcher.items.put( Integer.valueOf( bean.getKey().value() ), bean.getValue() ) );
        }

        public void remove( final BeanEntry<Marked, Item> bean, final MarkedItemWatcher watcher )
            throws Exception
        {
            assertEquals( watcher.items.remove( Integer.valueOf( bean.getKey().value() ) ), bean.getValue() );
        }
    }

    @Inject
    private NamedItemWatcher namedItemWatcher;

    @Inject
    private MarkedItemWatcher markedItemWatcher;

    @Inject
    private Injector injector;

    @Override
    protected void setUp()
        throws Exception
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        Guice.createInjector( new SpaceModule( space ) ).injectMembers( this );
    }

    public void testNamedWatcher()
    {
        assertTrue( CItem.initialized );

        assertEquals( 4, namedItemWatcher.items.size() );
        assertEquals( 2, markedItemWatcher.items.size() );

        assertTrue( namedItemWatcher.items.get( AItem.class.getName() ) instanceof AItem );
        assertTrue( namedItemWatcher.items.get( BItem.class.getName() ) instanceof BItem );
        assertTrue( namedItemWatcher.items.get( CItem.class.getName() ) instanceof CItem );
        assertTrue( namedItemWatcher.items.get( DItem.class.getName() ) instanceof DItem );

        assertNotSame( namedItemWatcher.items.get( AItem.class.getName() ),
                       injector.getInstance( Key.get( Item.class, Names.named( AItem.class.getName() ) ) ) );

        assertSame( namedItemWatcher.items.get( CItem.class.getName() ),
                    injector.getInstance( Key.get( Item.class, Names.named( CItem.class.getName() ) ) ) );

        assertTrue( markedItemWatcher.items.get( Integer.valueOf( 0 ) ) instanceof BItem );
        assertTrue( markedItemWatcher.items.get( Integer.valueOf( 1 ) ) instanceof DItem );

        injector.getInstance( MutableBeanLocator.class ).remove( injector );

        assertEquals( 0, namedItemWatcher.items.size() );
        assertEquals( 0, markedItemWatcher.items.size() );
    }
}
