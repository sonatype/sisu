package org.codehaus.plexus.context;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

public final class ContextException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    public ContextException( final String message )
    {
        super( message );
    }

    public ContextException( final String message, final Throwable detail )
    {
        super( message, detail );
    }
}
