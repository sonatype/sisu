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
package org.sonatype.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Marks dependencies to external runtime parameters, for example:
 * 
 * <pre>
 * &#064;Inject
 * &#064;Parameters
 * String[] args;
 * 
 * &#064;Inject
 * &#064;Parameters
 * Map&lt;String, String&gt; properties;
 * </pre>
 */
@Target( value = { ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Qualifier
public @interface Parameters
{
}
