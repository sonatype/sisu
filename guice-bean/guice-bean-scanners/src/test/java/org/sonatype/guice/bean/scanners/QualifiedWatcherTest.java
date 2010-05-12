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

import java.lang.annotation.Annotation;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import junit.framework.TestCase;

import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanMediator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class QualifiedWatcherTest
    extends TestCase
{
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

    @javax.inject.Named
    static class ItemB
        extends SomeItem
    {
    }

    @javax.inject.Named
    static class ItemC
        extends SomeItem
    {
    }

    @javax.inject.Named
    static class ItemWatcher
    {
        Map<Named, Item> items = new HashMap<Named, Item>();
    }

    @javax.inject.Named
    static class ItemMediator
        implements BeanMediator<Named, Item, ItemWatcher>
    {
        public void add( final Named name, final Provider<Item> bean, final ItemWatcher watcher )
            throws Exception
        {
            watcher.items.put( name, bean.get() );
        }

        public void remove( final Named name, final Provider<Item> bean, final ItemWatcher watcher )
            throws Exception
        {
            if ( watcher.items.get( name ) == bean.get() )
            {
                watcher.items.remove( name );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    static class BrokenMediator
        implements BeanMediator
    {
        public void add( final Annotation ann, final Provider bean, final Object watcher )
            throws Exception
        {
        }

        public void remove( final Annotation ann, final Provider bean, final Object watcher )
            throws Exception
        {
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

        assertTrue( itemWatcher.items.get( Names.named( ItemA.class.getName() ) ) instanceof ItemA );
        assertTrue( itemWatcher.items.get( Names.named( ItemB.class.getName() ) ) instanceof ItemB );
        assertTrue( itemWatcher.items.get( Names.named( ItemC.class.getName() ) ) instanceof ItemC );

        injector.getInstance( MutableBeanLocator.class ).remove( injector );

        assertEquals( 0, itemWatcher.items.size() );
    }

    public void testMediatorWithMissingTypeInfo()
    {
        try
        {
            new WatcherListener( BrokenMediator.class );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( final IllegalArgumentException e )
        {
        }
    }
}
