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
package org.sonatype.guice.plexus.locators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.sonatype.inject.BeanEntry;

import com.google.inject.name.Named;

final class RealmFilter<T>
    implements Iterable<BeanEntry<Named, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Iterable<BeanEntry<Named, T>> beans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RealmFilter( final Iterable<BeanEntry<Named, T>> beans )
    {
        this.beans = beans;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<BeanEntry<Named, T>> iterator()
    {
        final Set<String> realmNames = ClassRealmUtils.visibleRealmNames( ClassRealmUtils.contextRealm() );
        if ( null != realmNames && realmNames.size() > 0 )
        {
            return new FilteredItr( realmNames );
        }
        return beans.iterator();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    final class FilteredItr
        implements Iterator<BeanEntry<Named, T>>
    {
        private final Iterator<BeanEntry<Named, T>> itr = beans.iterator();

        private final Set<String> realmNames;

        private BeanEntry<Named, T> nextBean;

        public FilteredItr( final Set<String> realmNames )
        {
            this.realmNames = realmNames;
        }

        public boolean hasNext()
        {
            if ( null != nextBean )
            {
                return true;
            }
            while ( itr.hasNext() )
            {
                nextBean = itr.next();
                final String source = nextBean.getBinding().getSource().toString();
                if ( !source.startsWith( "ClassRealm" ) || realmNames.contains( source ) )
                {
                    return true;
                }
            }
            nextBean = null;
            return false;
        }

        public BeanEntry<Named, T> next()
        {
            if ( hasNext() )
            {
                // populated by hasNext()
                final BeanEntry<Named, T> bean = nextBean;
                nextBean = null;
                return bean;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
