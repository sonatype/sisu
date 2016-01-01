/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.guice.bean.scanners.asm;

@Deprecated
public final class Type
{
    private final org.eclipse.sisu.space.asm.Type delegate;

    private Type( final org.eclipse.sisu.space.asm.Type delegate )
    {
        this.delegate = delegate;
    }

    public String getClassName()
    {
        return delegate.getClassName();
    }

    public static String getDescriptor( final Class<?> c )
    {
        return org.eclipse.sisu.space.asm.Type.getDescriptor( c );
    }

    public static Type getObjectType( final String internalName )
    {
        return new Type( org.eclipse.sisu.space.asm.Type.getObjectType( internalName ) );
    }

    public static org.eclipse.sisu.space.asm.Type adapt( final Type type )
    {
        return type.delegate;
    }
}
