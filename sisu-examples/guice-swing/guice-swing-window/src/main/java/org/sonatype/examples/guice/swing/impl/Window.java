/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.examples.guice.swing.impl;

import java.awt.BorderLayout;

import javax.inject.Named;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.eclipse.sisu.EagerSingleton;

@EagerSingleton
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
