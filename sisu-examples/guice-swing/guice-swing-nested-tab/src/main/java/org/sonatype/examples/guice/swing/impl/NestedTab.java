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
package org.sonatype.examples.guice.swing.impl;

import java.awt.Graphics;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@Named( "Nested" )
final class NestedTab
    extends JPanel
{
    static int instanceCount;

    final JTabbedPane pane = new JTabbedPane();

    @Inject
    Map<String, JPanel> tabs;

    NestedTab()
    {
        setName( "NestedTab instance #" + ++instanceCount );
        add( pane );
    }

    @Override
    public void paint( final Graphics g )
    {
        if ( pane.getTabCount() == 0 )
        {
            pane.setPreferredSize( getSize() );
            for ( final Entry<String, JPanel> e : tabs.entrySet() )
            {
                pane.addTab( e.getKey(), e.getValue() );
            }
        }
        super.paint( g );
    }
}
