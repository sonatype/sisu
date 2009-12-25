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
import org.sonatype.guice.plexus.config.PlexusBeanSource;

public class AnnotatedPlexusBeanSourceTest
    extends TestCase
{
    @Component( role = Bean.class )
    static class Bean
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

    public void testNonComponent()
    {
        final PlexusBeanSource source = new AnnotatedPlexusBeanSource( null, null );
        assertTrue( source.findPlexusComponentBeans().isEmpty() );
        assertNull( source.getBeanMetadata( String.class ) );
        assertNotNull( source.getBeanMetadata( Bean.class ) );
        assertFalse( source.getBeanMetadata( Bean.class ).isEmpty() );
    }

    public void testInterpolatedAnnotations()
    {
        Iterator<BeanProperty<Object>> propertyIterator;

        final PlexusBeanSource uninterpolatedSource = new AnnotatedPlexusBeanSource( null, null );
        final PlexusBeanMetadata metadata1 = uninterpolatedSource.getBeanMetadata( Bean.class );

        propertyIterator = new BeanProperties( Bean.class ).iterator();
        final Configuration configuration11 = metadata1.getConfiguration( propertyIterator.next() );
        final Configuration configuration12 = metadata1.getConfiguration( propertyIterator.next() );
        final Configuration configuration13 = metadata1.getConfiguration( propertyIterator.next() );
        final Requirement requirement11 = metadata1.getRequirement( propertyIterator.next() );
        final Requirement requirement12 = metadata1.getRequirement( propertyIterator.next() );
        assertFalse( propertyIterator.hasNext() );

        assertFalse( configuration11 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "1", "BLANK" ), configuration11 );
        assertFalse( configuration12 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "2", "${some.value}" ), configuration12 );
        assertNull( configuration13 );
        assertEquals( new RequirementImpl( Bean.class, true, "mock" ), requirement11 );
        assertNull( requirement12 );

        final Map<?, ?> variables = Collections.singletonMap( "some.value", "INTERPOLATED" );

        final PlexusBeanSource interpolatedSource = new AnnotatedPlexusBeanSource( null, variables );
        final PlexusBeanMetadata metadata2 = interpolatedSource.getBeanMetadata( Bean.class );

        propertyIterator = new BeanProperties( Bean.class ).iterator();
        final Configuration configuration21 = metadata2.getConfiguration( propertyIterator.next() );
        final Configuration configuration22 = metadata2.getConfiguration( propertyIterator.next() );
        final Configuration configuration23 = metadata2.getConfiguration( propertyIterator.next() );
        final Requirement requirement21 = metadata2.getRequirement( propertyIterator.next() );
        final Requirement requirement22 = metadata2.getRequirement( propertyIterator.next() );
        assertFalse( propertyIterator.hasNext() );

        assertFalse( configuration21 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "1", "BLANK" ), configuration21 );
        assertTrue( configuration22 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "2", "INTERPOLATED" ), configuration22 );
        assertNull( configuration23 );
        assertEquals( new RequirementImpl( Bean.class, true, "mock" ), requirement21 );
        assertNull( requirement22 );
    }
}
