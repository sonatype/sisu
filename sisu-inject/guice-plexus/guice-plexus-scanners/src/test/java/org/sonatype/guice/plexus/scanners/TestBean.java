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

package org.sonatype.guice.plexus.scanners;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.plexus.config.Strategies;

@Component( role = Runnable.class, hint = "test", instantiationStrategy = Strategies.PER_LOOKUP, description = "Some Test", isolatedRealm = true )
public class TestBean
    implements Runnable
{
    public void run()
    {
    }
}
