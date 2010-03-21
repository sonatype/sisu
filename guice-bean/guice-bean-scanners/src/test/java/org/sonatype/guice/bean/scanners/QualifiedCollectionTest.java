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

public class QualifiedCollectionTest
    extends TestCase
{
    @Named
    static abstract class Widget
    {
    }

    static class ButtonWidget
        extends Widget
    {
    }

    static class MenuWidget
        extends Widget
    {
    }

    static class DefaultWidget
        extends Widget
    {
    }

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
        assertEquals( ButtonWidget.class, it.next().getClass() );
        assertTrue( it.hasNext() );
        assertEquals( MenuWidget.class, it.next().getClass() );
        assertTrue( it.hasNext() );
        assertEquals( ScrollBarWidget.class, it.next().getClass() );
        assertFalse( it.hasNext() );
    }

    public void testQualifiedMap()
    {
        Entry<Named, Widget> mapping;
        final Iterator<Entry<Named, Widget>> it = windowWidget.namedWidgets.entrySet().iterator();
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertNull( mapping.getKey() );
        assertEquals( DefaultWidget.class, mapping.getValue().getClass() );
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertEquals( ButtonWidget.class.getName(), mapping.getKey().value() );
        assertEquals( ButtonWidget.class, mapping.getValue().getClass() );
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertEquals( MenuWidget.class.getName(), mapping.getKey().value() );
        assertEquals( MenuWidget.class, mapping.getValue().getClass() );
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertEquals( ScrollBarWidget.class.getName(), mapping.getKey().value() );
        assertEquals( ScrollBarWidget.class, mapping.getValue().getClass() );
        assertFalse( it.hasNext() );
    }

    public void testQualifiedHints()
    {
        Entry<String, Widget> mapping;
        final Iterator<Entry<String, Widget>> it = windowWidget.hintedWidgets.entrySet().iterator();
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertNull( mapping.getKey() );
        assertEquals( DefaultWidget.class, mapping.getValue().getClass() );
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertEquals( ButtonWidget.class.getName(), mapping.getKey() );
        assertEquals( ButtonWidget.class, mapping.getValue().getClass() );
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertEquals( MenuWidget.class.getName(), mapping.getKey() );
        assertEquals( MenuWidget.class, mapping.getValue().getClass() );
        assertTrue( it.hasNext() );
        mapping = it.next();
        assertEquals( ScrollBarWidget.class.getName(), mapping.getKey() );
        assertEquals( ScrollBarWidget.class, mapping.getValue().getClass() );
        assertFalse( it.hasNext() );
    }
}
