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

import java.awt.Graphics;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JPanel;

@Named( "Nested" )
final class NestedTab
    extends JPanel
{
    @Inject
    Map<String, JPanel> tabMap;

    private static int instanceCount;

    private boolean initialized;

    @Override
    public void paint( final Graphics g )
    {
        if ( !initialized )
        {
            initialized = true;
            setName( "NestedTab instance #" + ++instanceCount );
            Window.addTabPane( this, tabMap );
        }
        super.paint( g );
    }
}
