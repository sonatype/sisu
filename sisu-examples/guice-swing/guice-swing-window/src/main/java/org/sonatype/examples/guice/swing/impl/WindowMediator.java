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

import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;

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
