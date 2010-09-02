/**
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus.component.repository.exception;

public final class ComponentLookupException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String LS = System.getProperty( "line.separator" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String role;

    private final String hint;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public ComponentLookupException( final String message, final String role, final String hint )
    {
        super( message );
        this.role = role;
        this.hint = hint;
    }

    public ComponentLookupException( final Throwable cause, final String role, final String hint )
    {
        super( cause );
        this.role = role;
        this.hint = hint;
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + LS + "      role: " + role + LS + "  roleHint: " + hint;
    }
}
