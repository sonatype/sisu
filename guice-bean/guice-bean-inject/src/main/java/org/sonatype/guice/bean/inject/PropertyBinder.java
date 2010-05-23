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
package org.sonatype.guice.bean.inject;

import org.sonatype.guice.bean.reflect.BeanProperty;

/**
 * Provides custom {@link PropertyBinding}s for bean properties such as fields or setter methods.
 */
public interface PropertyBinder
{
    /**
     * Returns the appropriate {@link PropertyBinding} for the given bean property.
     * 
     * @param property The bean property
     * @return Binding for the given property; {@code null} if no binding is applicable
     */
    <T> PropertyBinding bindProperty( BeanProperty<T> property );

    /**
     * Binders may return {@code LAST_BINDING} to indicate they are done binding a bean.
     */
    PropertyBinding LAST_BINDING = new PropertyBinding()
    {
        public <B> void injectProperty( final B bean )
        {
            throw new UnsupportedOperationException( "LAST_BINDING" );
        }
    };
}
