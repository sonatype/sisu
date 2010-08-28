package org.codehaus.plexus.test.map;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: NoComponentsMapTest.java 7828 2008-11-14 22:07:56Z dain $
 */
public class NoComponentsMapTest
    extends PlexusTestCase
{
    public void testNoComponents()
        throws Exception
    {
        ActivityManager manager;

        manager = lookup( ActivityManager.class );

        assertEquals( 0, manager.getActivityCount() );
    }
}
