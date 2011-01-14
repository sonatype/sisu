package org.codehaus.plexus.personality.plexus.lifecycle.phase;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;

public interface Contextualizable
{
    void contextualize( Context context )
        throws ContextException;
}
