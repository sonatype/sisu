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

import java.util.Map.Entry;

import javax.inject.Named;
import javax.swing.JPanel;

import org.sonatype.guice.bean.locators.Mediator;

@Named
final class WindowMediator
    implements Mediator<Named, JPanel, Window>
{
    public void add( final Entry<Named, JPanel> bean, final Window window )
    {
        window.add( bean.getKey().value(), bean.getValue() );
    }

    public void remove( final Entry<Named, JPanel> bean, final Window window )
    {
        window.remove( bean.getKey().value() );
    }
}
