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
package org.sonatype.guice.swing.example.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public final class Util
{
    public static void addTabPane( final Container container, final Map<String, JPanel> tabs )
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
}
