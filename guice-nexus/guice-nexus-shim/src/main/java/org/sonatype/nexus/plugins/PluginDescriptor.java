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

    public String formatAsString( final boolean detailed )
    {
        return detailed ? "" : "";
        /****
         * buf.append( LS ); buf.append( "       Detailed report about the plugin \"" +
         * descriptor.getPluginCoordinates() + "\":" ); buf.append( LS ).append( LS ); buf.append( "         Source: \""
         * + descriptor.getSource() + "\":" + LS ); buf.append( "         Plugin defined these components:\n" ); for (
         * ComponentDescriptor<?> component : descriptor.getComponents() ) { final String hint =
         * component.getRoleHint(); buf.append( "         * FQN of Type \"" + component.getRole() ); if (
         * !Hints.isDefaultHint( hint ) ) { buf.append( "\", named as \"" + hint ); } buf.append(
         * "\", with implementation \"" + component.getImplementation() + "\"" + LS ); } final Map<String,
         * RepositoryTypeDescriptor> repositoryTypes = descriptor.getPluginRepositoryTypes(); if (
         * !pluginRepositoryTypes.isEmpty() ) { buf.append( LS ); buf.append(
         * "         Plugin defined these custom repository types:" ); buf.append( LS ); for ( final
         * RepositoryTypeDescriptor type : repositoryTypes.values() ) { buf.append( "         * FQN of Type \"" +
         * type.getRole() + "\", to be published at path \"" + type.getPrefix() + "\"" ); buf.append( LS ); } } final
         * List<PluginStaticResourceModel> staticResourceModels = descriptor.getPluginStaticResourceModels(); if (
         * !staticResourceModels.isEmpty() ) { buf.append( LS ); buf.append(
         * "         Plugin contributed these static resources:" ); buf.append( LS ); for ( final
         * PluginStaticResourceModel model : staticResourceModels ) { buf.append( "         * Resource path \"" +
         * model.getResourcePath() + "\", to be published at path \"" + model.getPublishedPath() + "\", content type \""
         * + model.getContentType() + "\"" ); buf.append( LS ); } }
         ****/
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
