/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.examples.guice.swing.impl;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

@Named
@Singleton
final class WindowMediator
    implements Mediator<Named, JPanel, Window>
{
    public void add( final BeanEntry<Named, JPanel> bean, final Window window )
        throws Exception
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                window.add( bean.getKey().value(), bean.getValue() );
            }
        } );
    }

    public void remove( final BeanEntry<Named, JPanel> bean, final Window window )
        throws Exception
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                window.remove( bean.getKey().value() );
            }
        } );
    }
}
