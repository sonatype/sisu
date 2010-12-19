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

import org.sonatype.guice.bean.locators.QualifiedBean;

import com.google.inject.name.Named;

final class RealmFilter<T>
    implements Iterable<QualifiedBean<Named, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Iterable<QualifiedBean<Named, T>> beans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RealmFilter( final Iterable<QualifiedBean<Named, T>> beans )
    {
        this.beans = beans;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<QualifiedBean<Named, T>> iterator()
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
        implements Iterator<QualifiedBean<Named, T>>
    {
        private final Iterator<QualifiedBean<Named, T>> itr = beans.iterator();

        private final Set<String> realmNames;

        private QualifiedBean<Named, T> nextBean;

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

        public QualifiedBean<Named, T> next()
        {
            if ( hasNext() )
            {
                // populated by hasNext()
                final QualifiedBean<Named, T> bean = nextBean;
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
