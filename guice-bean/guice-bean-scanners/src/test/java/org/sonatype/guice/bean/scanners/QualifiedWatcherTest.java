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
package org.sonatype.guice.bean.scanners;

import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.Mediator;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Jsr330;

public class QualifiedWatcherTest
    extends TestCase
{
    @Named
    static abstract class Item
    {
    }

    static abstract class SomeItem
        extends Item
    {
    }

    static class ItemA
        extends SomeItem
    {
    }

    static class ItemB
        extends SomeItem
    {
    }

    static class ItemC
        extends SomeItem
    {
    }

    @Named
    static class ItemWatcher
    {
        Map<Named, Item> items = new HashMap<Named, Item>();
    }

    @Named
    static class ItemMediator
        implements Mediator<Named, Item, ItemWatcher>
    {
        public void add( final Entry<Named, Item> bean, final ItemWatcher watcher )
            throws Exception
        {
            watcher.items.put( bean.getKey(), bean.getValue() );
        }

        public void remove( final Entry<Named, Item> bean, final ItemWatcher watcher )
            throws Exception
        {
            if ( watcher.items.get( bean.getKey() ) == bean.getValue() )
            {
                watcher.items.remove( bean.getKey() );
            }
        }
    }

    @Inject
    private ItemWatcher itemWatcher;

    @Inject
    private Injector injector;

    @Override
    protected void setUp()
        throws Exception
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );
        Guice.createInjector( new QualifiedScannerModule( space ) ).injectMembers( this );
    }

    public void testQualifiedWatcher()
    {
        assertEquals( 3, itemWatcher.items.size() );

        assertTrue( itemWatcher.items.get( Jsr330.named( ItemA.class.getName() ) ) instanceof ItemA );
        assertTrue( itemWatcher.items.get( Jsr330.named( ItemB.class.getName() ) ) instanceof ItemB );
        assertTrue( itemWatcher.items.get( Jsr330.named( ItemC.class.getName() ) ) instanceof ItemC );

        injector.getInstance( MutableBeanLocator.class ).remove( injector );

        assertEquals( 0, itemWatcher.items.size() );
    }
}
