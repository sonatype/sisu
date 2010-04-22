package org.codehaus.plexus.component.configurator.converters.special;

import org.codehaus.classworlds.ClassRealmAdapter;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * ConfigurationConverter to set up ClassRealm component fields.
 *
 * @author <a href="mailto:kenney@neonics.com">Kenney Westerhof</a>
 */
public class ClassRealmConverter
    extends AbstractConfigurationConverter
{
    public static final String ROLE = ConfigurationConverter.class.getName();

    private ClassRealm classRealm;

    /**
     * Constructs this ClassRealmConverter with the given ClassRealm.
     * If there's a way to automatically configure this component
     * using the current classrealm, this method can go away.
     *
     * @param classRealm
     */
    public ClassRealmConverter( ClassRealm classRealm )
    {
        setClassRealm( classRealm );
    }

    public void setClassRealm( ClassRealm classRealm )
    {
        this.classRealm = classRealm;
    }

    public boolean canConvert( Class type )
    {
        // backwards compatibility for old ClassWorld fields
        return org.codehaus.classworlds.ClassRealm.class.isAssignableFrom( type )
            || ClassRealm.class.isAssignableFrom( type );
    }

    public Object fromConfiguration( ConverterLookup converterLookup, PlexusConfiguration configuration, Class type,
                                     Class baseType, ClassLoader classLoader, ExpressionEvaluator expressionEvaluator,
                                     ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object retValue = fromExpression( configuration, expressionEvaluator, type );

        if ( retValue == null )
        {
            retValue = classRealm;
        }

        // backwards compatibility for old ClassWorld fields
        if ( retValue instanceof ClassRealm && org.codehaus.classworlds.ClassRealm.class.isAssignableFrom( type ) )
        {
            retValue = ClassRealmAdapter.getInstance( (ClassRealm) retValue );
        }

        return retValue;
    }

}
