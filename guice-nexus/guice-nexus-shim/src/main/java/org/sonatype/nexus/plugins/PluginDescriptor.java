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
package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Reader;

/**
 * Describes a Nexus plugin; what it contains and what it exports.
 */
public final class PluginDescriptor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final PluginModelXpp3Reader PLUGIN_METADATA_READER = new PluginModelXpp3Reader();

    private static final String[] NO_EXPORTS = new String[0];

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final URL pluginXml;

    private final Map<?, ?> variables;

    private PluginMetadata metadata;

    private String[] exportedClassNames = NO_EXPORTS;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PluginDescriptor( final URL pluginXml, final Map<?, ?> variables )
    {
        this.pluginXml = pluginXml;
        this.variables = variables;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public PluginMetadata getPluginMetadata()
    {
        if ( null == metadata )
        {
            try
            {
                metadata = parsePluginXml( pluginXml, variables );
            }
            catch ( final IOException e )
            {
                throw new RuntimeException( e.toString() );
            }
        }
        return metadata;
    }

    public List<String> getExportedClassnames()
    {
        return Arrays.asList( exportedClassNames );
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    void setExportedClassnames( final String... classNames )
    {
        exportedClassNames = classNames;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Wraps the given {@link InputStream} as a {@link Reader} with XML encoding detection and optional interpolation.
     * 
     * @param in The input stream
     * @param variables The filter variables
     * @return Reader that can automatically detect XML encodings and optionally interpolate variables
     */
    private static Reader filteredXmlReader( final InputStream in, final Map<?, ?> variables )
        throws IOException
    {
        final Reader reader = ReaderFactory.newXmlReader( in );
        if ( null != variables )
        {
            return new InterpolationFilterReader( reader, variables );
        }
        return reader;
    }

    /**
     * Parses a {@code plugin.xml} resource into plugin metadata.
     * 
     * @param url The plugin.xml URL
     * @param variables The filter variables
     * @return Nexus plugin metadata
     */
    private static PluginMetadata parsePluginXml( final URL url, final Map<?, ?> variables )
        throws IOException
    {
        final InputStream in = url.openStream();
        try
        {
            return PLUGIN_METADATA_READER.read( filteredXmlReader( in, variables ) );
        }
        catch ( final XmlPullParserException e )
        {
            throw new IOException( "Problem parsing: " + url + " reason: " + e );
        }
        finally
        {
            IOUtil.close( in );
        }
    }
}
