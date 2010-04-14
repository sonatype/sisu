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

import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

@Singleton
@Named( "default" )
final class Window
    implements Runnable
{
    final JTabbedPane pane = new JTabbedPane();

    Window()
    {
        SwingUtilities.invokeLater( this );
    }

    public void run()
    {
        final JFrame frame = new JFrame( "Guice Swing Example" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.add( pane, BorderLayout.CENTER );
        frame.setLocation( 100, 50 );
        frame.setSize( 600, 400 );
        frame.setName( "Window" );
        frame.setVisible( true );
    }

    public void add( final String name, final JPanel panel )
    {
        pane.addTab( name, panel );
    }

    public void remove( final String name )
    {
        pane.removeTabAt( pane.indexOfTab( name ) );
    }
}
