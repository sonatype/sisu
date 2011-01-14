package org.codehaus.plexus;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.guice.plexus.config.Hints;

public interface PlexusConstants
{
    String PLEXUS_DEFAULT_HINT = Hints.DEFAULT_HINT;

    String PLEXUS_KEY = "plexus";

    String GLOBAL_VISIBILITY = "global";

    String REALM_VISIBILITY = "realm";
}
