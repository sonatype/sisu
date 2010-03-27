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
package org.sonatype.guice.swing.example.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map.Entry;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.sonatype.guice.bean.locators.Watchable;
import org.sonatype.guice.bean.locators.Watcher;

@Named( "default" )
@Typed( Runnable.class )
final class Window
    implements Runnable, Watcher<Entry<String, JPanel>>
{
    final JTabbedPane pane = new JTabbedPane();

    @Inject
    Watchable<Entry<String, JPanel>> watchable;

    public void run()
    {
        final JFrame frame = new JFrame( "Guice Swing Example" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setSize( new Dimension( 600, 400 ) );
        frame.add( pane, BorderLayout.CENTER );
        frame.setLocation( 100, 50 );
        frame.setName( "Window" );
        frame.setVisible( true );

        watchable.subscribe( this );
    }

    public void add( final Entry<String, JPanel> item )
    {
        pane.addTab( item.getKey(), item.getValue() );
    }

    public void remove( final Entry<String, JPanel> item )
    {
        pane.removeTabAt( pane.indexOfTab( item.getKey() ) );
    }
}
