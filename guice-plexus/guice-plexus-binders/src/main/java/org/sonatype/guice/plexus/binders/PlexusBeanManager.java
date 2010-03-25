/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.binders;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.DeferredClass;

/**
 * Service that manages the lifecycle of Plexus beans.
 */
public interface PlexusBeanManager
{
    /**
     * Decides whether the given Plexus component should be registered with the injector.
     * 
     * @param component The Plexus component
     * @param clazz The implementation class
     * @return {@code true} if the component should be registered; otherwise {@code false}
     */
    boolean manage( Component component, DeferredClass<?> clazz );

    /**
     * Decides whether instances of the given Plexus bean type should be reported to this manager.
     * 
     * @param clazz The Plexus bean type
     * @return {@code true} if instances of the bean should be reported; otherwise {@code false}
     */
    boolean manage( Class<?> clazz );

    /**
     * Decides whether the given bean property will be managed by this manager.
     * 
     * @param property The bean property
     * @return Non-null binding if the bean property will be managed; otherwise {@code null}
     */
    PropertyBinding manage( BeanProperty<?> property );

    /**
     * Decides whether the given Plexus bean instance will be managed by this manager.
     * 
     * @param bean The Plexus bean instance
     * @return {@code true} if the bean instance will be managed; otherwise {@code false}
     */
    boolean manage( Object bean );

    /**
     * Creates a new child {@link PlexusBeanManager} with this manager as its parent.
     * 
     * @return Child PlexusBeanManager
     */
    PlexusBeanManager manageChild();

    /**
     * Tells this manager to unmanage the given Plexus bean instance.
     * 
     * @param bean The Plexus bean instance
     * @return {@code true} if the bean instance was unmanaged; otherwise {@code false}
     */
    boolean unmanage( Object bean );

    /**
     * Tells this manager to unmanage all the Plexus bean instances it knows about.
     * 
     * @return {@code true} if any bean instances were unmanaged; otherwise {@code false}
     */
    boolean unmanage();
}
