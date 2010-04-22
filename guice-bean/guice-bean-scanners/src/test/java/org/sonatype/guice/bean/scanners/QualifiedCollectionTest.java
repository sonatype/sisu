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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Jsr330;

public class QualifiedCollectionTest
    extends TestCase
{
    static abstract class Widget
    {
    }

    @Named
    static class ButtonWidget
        extends Widget
    {
    }

    @Named
    static class MenuWidget
        extends Widget
    {
    }

    @Named
    static class DefaultWidget
        extends Widget
    {
    }

    @Named
    static class ScrollBarWidget
        extends Widget
    {
    }

    @Named
    static class WindowWidget
    {
        @Inject
        List<Widget> widgets;

        @Inject
        Map<Named, Widget> namedWidgets;

        @Inject
        Map<String, Widget> hintedWidgets;
    }

    @Inject
    private WindowWidget windowWidget;

    @Override
    protected void setUp()
        throws Exception
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );
        final Injector injector = Guice.createInjector( new QualifiedScannerModule( space ) );
        injector.injectMembers( this );
    }

    public void testQualifiedList()
    {
        final Iterator<Widget> it = windowWidget.widgets.iterator();
        assertTrue( it.hasNext() );
        assertEquals( DefaultWidget.class, it.next().getClass() );
        assertTrue( it.hasNext() );
        it.next();
        assertTrue( it.hasNext() );
        it.next();
        assertTrue( it.hasNext() );
        it.next();
        assertFalse( it.hasNext() );

        final List<Widget> tempList = new ArrayList<Widget>( windowWidget.widgets );
        assertEquals( DefaultWidget.class, tempList.remove( 0 ).getClass() );
        for ( final Iterator<Widget> i = tempList.iterator(); i.hasNext(); )
        {
            if ( ButtonWidget.class.equals( i.next().getClass() ) )
            {
                i.remove();
                break;
            }
        }
        for ( final Iterator<Widget> i = tempList.iterator(); i.hasNext(); )
        {
            if ( MenuWidget.class.equals( i.next().getClass() ) )
            {
                i.remove();
                break;
            }
        }
        for ( final Iterator<Widget> i = tempList.iterator(); i.hasNext(); )
        {
            if ( ScrollBarWidget.class.equals( i.next().getClass() ) )
            {
                i.remove();
                break;
            }
        }
        assertTrue( tempList.isEmpty() );
    }

    public void testQualifiedMap()
    {
        final Map<Named, Widget> widgetMap = windowWidget.namedWidgets;

        Entry<Named, Widget> mapping;
        final Iterator<Entry<Named, Widget>> it = widgetMap.entrySet().iterator();
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertEquals( "default", mapping.getKey().value() );
        assertEquals( DefaultWidget.class, mapping.getValue().getClass() );
        assertTrue( it.hasNext() );
        it.next();
        assertTrue( it.hasNext() );
        it.next();
        assertTrue( it.hasNext() );
        it.next();
        assertFalse( it.hasNext() );

        assertEquals( ButtonWidget.class, widgetMap.get( Jsr330.named( ButtonWidget.class.getName() ) ).getClass() );
        assertEquals( MenuWidget.class, widgetMap.get( Jsr330.named( MenuWidget.class.getName() ) ).getClass() );
        assertEquals( ScrollBarWidget.class, widgetMap.get( Jsr330.named( ScrollBarWidget.class.getName() ) )
                                                      .getClass() );
    }

    public void testQualifiedHints()
    {
        final Map<String, Widget> widgetMap = windowWidget.hintedWidgets;

        Entry<String, Widget> mapping;
        final Iterator<Entry<String, Widget>> it = widgetMap.entrySet().iterator();
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertEquals( "default", mapping.getKey() );
        assertEquals( DefaultWidget.class, mapping.getValue().getClass() );
        assertTrue( it.hasNext() );
        it.next();
        assertTrue( it.hasNext() );
        it.next();
        assertTrue( it.hasNext() );
        it.next();
        assertFalse( it.hasNext() );

        assertEquals( ButtonWidget.class, widgetMap.get( ButtonWidget.class.getName() ).getClass() );
        assertEquals( MenuWidget.class, widgetMap.get( MenuWidget.class.getName() ).getClass() );
        assertEquals( ScrollBarWidget.class, widgetMap.get( ScrollBarWidget.class.getName() ).getClass() );
    }
}
