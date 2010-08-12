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
package org.sonatype.guice.plexus.scanners;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperties;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;

public class PlexusAnnotatedBeanMetadataTest
    extends TestCase
{
    @Component( role = Bean.class )
    protected static class Bean
    {
        @Configuration( name = "1", value = "BLANK" )
        String fixed;

        @Configuration( name = "2", value = "${some.value}" )
        String variable;

        String dummy1;

        @Requirement( role = Bean.class, hint = "mock", optional = true )
        Bean self;

        String dummy2;
    }

    @SuppressWarnings( "deprecation" )
    public void testRawAnnotations()
    {
        final PlexusBeanMetadata metadata = new PlexusAnnotatedMetadata( null );
        assertFalse( metadata.isEmpty() );

        final Iterator<BeanProperty<Object>> propertyIterator = new BeanProperties( Bean.class ).iterator();
        final Requirement requirement2 = metadata.getRequirement( propertyIterator.next() );
        final Requirement requirement1 = metadata.getRequirement( propertyIterator.next() );
        final Configuration configuration3 = metadata.getConfiguration( propertyIterator.next() );
        final Configuration configuration2 = metadata.getConfiguration( propertyIterator.next() );
        final Configuration configuration1 = metadata.getConfiguration( propertyIterator.next() );
        assertFalse( propertyIterator.hasNext() );

        assertFalse( configuration1 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "1", "BLANK" ), configuration1 );
        assertFalse( configuration2 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "2", "${some.value}" ), configuration2 );
        assertNull( configuration3 );
        assertEquals( new RequirementImpl( Bean.class, true, "mock" ), requirement1 );
        assertNull( requirement2 );
    }

    @SuppressWarnings( "deprecation" )
    public void testInterpolatedAnnotations()
    {
        final Map<?, ?> variables = Collections.singletonMap( "some.value", "INTERPOLATED" );

        final PlexusBeanMetadata metadata = new PlexusAnnotatedMetadata( variables );
        assertFalse( metadata.isEmpty() );

        final Iterator<BeanProperty<Object>> propertyIterator = new BeanProperties( Bean.class ).iterator();
        final Requirement requirement2 = metadata.getRequirement( propertyIterator.next() );
        final Requirement requirement1 = metadata.getRequirement( propertyIterator.next() );
        final Configuration configuration3 = metadata.getConfiguration( propertyIterator.next() );
        final Configuration configuration2 = metadata.getConfiguration( propertyIterator.next() );
        final Configuration configuration1 = metadata.getConfiguration( propertyIterator.next() );
        assertFalse( propertyIterator.hasNext() );

        assertFalse( configuration1 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "1", "BLANK" ), configuration1 );
        assertTrue( configuration2 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "2", "INTERPOLATED" ), configuration2 );
        assertNull( configuration3 );
        assertEquals( new RequirementImpl( Bean.class, true, "mock" ), requirement1 );
        assertNull( requirement2 );
    }
}
