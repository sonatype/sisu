/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanMediator;
import org.sonatype.inject.EagerSingleton;
import org.sonatype.inject.NamedBeanMediator;

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
    static class ItemA
        extends SomeItem
    {
    }

    @Marked( 0 )
    @javax.inject.Named
    static class ItemB
        extends SomeItem
    {
    }

    @EagerSingleton
    @javax.inject.Named
    static class ItemC
        extends SomeItem
    {
        static boolean initialized;

        public ItemC()
        {
            initialized = true;
        }
    }

    @Marked( 1 )
    static class ItemD
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
        implements NamedBeanMediator<Item, NamedItemWatcher>
    {
        public void add( final String name, final Provider<Item> bean, final NamedItemWatcher watcher )
            throws Exception
        {
            assertNull( watcher.items.put( name, bean.get() ) );
        }

        public void remove( final String name, final Provider<Item> bean, final NamedItemWatcher watcher )
            throws Exception
        {
            assertEquals( watcher.items.remove( name ), bean.get() );
        }
    }

    @javax.inject.Named
    static class MarkedItemMediator
        implements BeanMediator<Marked, Item, MarkedItemWatcher>
    {
        public void add( final Marked mark, final Provider<Item> bean, final MarkedItemWatcher watcher )
            throws Exception
        {
            assertNull( watcher.items.put( Integer.valueOf( mark.value() ), bean.get() ) );
        }

        public void remove( final Marked mark, final Provider<Item> bean, final MarkedItemWatcher watcher )
            throws Exception
        {
            assertEquals( watcher.items.remove( Integer.valueOf( mark.value() ) ), bean.get() );
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
        final ClassSpace space = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );
        Guice.createInjector( new BeanSpaceModule( space ) ).injectMembers( this );
    }

    public void testNamedWatcher()
    {
        assertTrue( ItemC.initialized );

        assertEquals( 3, namedItemWatcher.items.size() );
        assertEquals( 2, markedItemWatcher.items.size() );

        assertTrue( namedItemWatcher.items.get( ItemA.class.getName() ) instanceof ItemA );
        assertTrue( namedItemWatcher.items.get( ItemB.class.getName() ) instanceof ItemB );
        assertTrue( namedItemWatcher.items.get( ItemC.class.getName() ) instanceof ItemC );

        assertNotSame( namedItemWatcher.items.get( ItemA.class.getName() ),
                       injector.getInstance( Key.get( Item.class, Names.named( ItemA.class.getName() ) ) ) );

        assertSame( namedItemWatcher.items.get( ItemC.class.getName() ),
                    injector.getInstance( Key.get( Item.class, Names.named( ItemC.class.getName() ) ) ) );

        assertTrue( markedItemWatcher.items.get( Integer.valueOf( 0 ) ) instanceof ItemB );
        assertTrue( markedItemWatcher.items.get( Integer.valueOf( 1 ) ) instanceof ItemD );

        injector.getInstance( MutableBeanLocator.class ).remove( injector );

        assertEquals( 0, namedItemWatcher.items.size() );
        assertEquals( 0, markedItemWatcher.items.size() );
    }
}
