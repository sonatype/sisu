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
package org.sonatype.guice.swing.example;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.QualifiedScannerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public final class SwingExample
{
    @Named
    static class DefaultRunnable
        implements Runnable
    {
        @Inject
        Map<String, JPanel> tabMap;

        public void run()
        {
            final JFrame frame = new JFrame( "GuiceBeanExample" );
            frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            frame.setLocation( 100, 50 );
            frame.setName( "Main window" );
            addTabPane( frame, tabMap );
            frame.pack();
            frame.setVisible( true );
        }
    }

    static void addTabPane( final Container container, final Map<String, JPanel> tabs )
    {
        System.out.println( "Adding tabs to " + container.getName() );

        final JTabbedPane pane = new JTabbedPane();
        pane.setPreferredSize( new Dimension( 600, 400 ) );
        container.add( pane, BorderLayout.CENTER );

        for ( final Entry<String, JPanel> e : tabs.entrySet() )
        {
            final JPanel panel = e.getValue();
            if ( container != panel )
            {
                pane.addTab( e.getKey(), panel );
            }
        }
    }

    public static void main( final String[] args )
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) SwingExample.class.getClassLoader() );
        final Injector injector = Guice.createInjector( new QualifiedScannerModule( space ) );
        SwingUtilities.invokeLater( injector.getInstance( Runnable.class ) );
    }
}
