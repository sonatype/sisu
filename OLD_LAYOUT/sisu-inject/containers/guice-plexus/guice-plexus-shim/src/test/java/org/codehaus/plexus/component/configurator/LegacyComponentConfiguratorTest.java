package org.codehaus.plexus.component.configurator;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import junit.framework.TestCase;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * @author Benjamin Bentmann
 */
public class LegacyComponentConfiguratorTest
    extends TestCase
{

    /**
     * A component configurator implementing the legacy Classworlds API.
     */
    static class LegacyComponentConfigurator
        extends AbstractComponentConfigurator
    {

        int called;

        public void configureComponent( final Object component, final PlexusConfiguration configuration,
                                        final ExpressionEvaluator expressionEvaluator,
                                        final org.codehaus.classworlds.ClassRealm containerRealm,
                                        final ConfigurationListener listener )
        {
            called++;
        }
    }

    public void testLegacySupport()
        throws Exception
    {
        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        final LegacyComponentConfigurator configurator = new LegacyComponentConfigurator();

        configurator.configureComponent( new Object(), null, realm );

        assertEquals( 1, configurator.called );
    }

}
