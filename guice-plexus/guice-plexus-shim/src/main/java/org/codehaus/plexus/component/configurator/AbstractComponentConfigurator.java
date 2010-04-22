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

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.converters.lookup.DefaultConverterLookup;
import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 * @version $Id: AbstractComponentConfigurator.java 8325 2009-08-07 14:10:05Z bentmann $
 */
public abstract class AbstractComponentConfigurator
    implements ComponentConfigurator
{
    /**
     * This is being instantiated here because there are old component factories (beanshell) that directly access
     * the converterLookup but do not yet state the ConverterLookup as a requirement in the component metadata.
     * Once these are wired up as standard components properly then we won't have to instantiate the
     * converter lookup here and we can let the container do it.
     *
     * @plexus.requirement
     */
    protected ConverterLookup converterLookup = new DefaultConverterLookup();

    public void configureComponent( Object component,
                                    PlexusConfiguration configuration,
                                    ClassRealm containerRealm )
        throws ComponentConfigurationException
    {
        configureComponent( component, configuration, new DefaultExpressionEvaluator(), containerRealm );
    }

    public void configureComponent( Object component,
                                    PlexusConfiguration configuration,
                                    ExpressionEvaluator expressionEvaluator,
                                    ClassRealm containerRealm )
        throws ComponentConfigurationException
    {
        configureComponent( component, configuration, expressionEvaluator, containerRealm, null );
    }

    public void configureComponent( Object component,
                                    PlexusConfiguration configuration,
                                    ExpressionEvaluator expressionEvaluator,
                                    ClassRealm containerRealm,
                                    ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        // ----------------------------------------------------------------------------
        // For compatibility with old ComponentFactories that use old ClassWorlds
        // ----------------------------------------------------------------------------

        Method method;

        try
        {
            method = getClass().getMethod( "configureComponent", new Class[]{Object.class, PlexusConfiguration.class,
                ExpressionEvaluator.class, ClassRealm.class, ConfigurationListener.class} );

            method.invoke( this, new Object[]{component, configuration, expressionEvaluator, containerRealm, listener} );
        }
        catch ( Exception mnfe )
        {
            mnfe.printStackTrace();
        }

        // TODO: here so extended classes without the method continue to work. should be removed
        // this won't hit the method above going into a loop - instead, it will hit the overridden one
        //configureComponent( component, configuration, expressionEvaluator, containerRealm );
    }
}
