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
package org.sonatype.guice.bean.locators;

import org.sonatype.guice.bean.locators.NotifyingBeansTest.BrokenNotifier;
import org.sonatype.guice.bean.locators.WatchedBeansTest.Bean;
import org.sonatype.guice.bean.locators.WatchedBeansTest.BeanImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class BrokenNotifierExample
{
    @SuppressWarnings( "unused" )
    public BrokenNotifierExample()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Runnable brokenNotifier = new BrokenNotifier();

        final Iterable<QualifiedBean<Named, Bean>> a =
            locator.locate( Key.get( Bean.class, Named.class ), brokenNotifier );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "A" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "-" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "Z" ) ).to( BeanImpl.class );
            }
        } ) );
    }
}
