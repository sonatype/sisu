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
package org.sonatype.inject.guice;

import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.sonatype.guice.bean.containers.SisuActivator;

import com.google.inject.Injector;

public final class SisuBundleContext
    extends AbstractSisuContext
{
    private final Bundle bundle;

    public SisuBundleContext( final Bundle bundle )
    {
        this.bundle = bundle;
    }

    public SisuBundleContext( final BundleContext context )
    {
        this.bundle = context.getBundle();
    }

    public void configure( final Map<String, String> properties )
    {
        // FIXME
    }

    @Override
    protected Injector injector()
    {
        return SisuActivator.getInjector( bundle );
    }
}
