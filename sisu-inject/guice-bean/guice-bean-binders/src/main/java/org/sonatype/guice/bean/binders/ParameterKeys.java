/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.binders;

import java.util.Map;

import org.sonatype.inject.Parameters;

import com.google.inject.Key;
import com.google.inject.util.Types;

public interface ParameterKeys
{
    @SuppressWarnings( "unchecked" )
    Key<Map<String, String>> PROPERTIES =
        (Key<Map<String, String>>) Key.get( Types.mapOf( String.class, String.class ), Parameters.class );

    Key<String[]> ARGUMENTS = Key.get( String[].class, Parameters.class );
}
