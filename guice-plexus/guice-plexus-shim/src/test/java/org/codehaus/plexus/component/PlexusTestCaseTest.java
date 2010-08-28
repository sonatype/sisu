package org.codehaus.plexus.component;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;

public class PlexusTestCaseTest
    extends PlexusTestCase
{

    private static final String CUSTOM_PROPERTY = "custom.property";

    private static final String CUSTOM_VALUE = "custom.value";

    @Override
    protected void customizeContext( final Context context )
    {
        super.customizeContext( context );

        context.put( CUSTOM_PROPERTY, CUSTOM_VALUE );
    }

    public void testCustomizeContext()
        throws ContextException
    {
        final String value = (String) getContainer().getContext().get( CUSTOM_PROPERTY );

        assertEquals( CUSTOM_VALUE, value );
    }
}
