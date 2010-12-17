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

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.guice.bean.locators.QualifiedBean;

import com.google.inject.name.Named;

final class RealmFilter<T>
    implements Iterable<QualifiedBean<Named, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Iterable<QualifiedBean<Named, T>> beans;

    private ClassRealm cachedContextRealm;

    private Set<String> visibleRealmNames;

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

    public synchronized Iterator<QualifiedBean<Named, T>> iterator()
    {
        final ClassRealm contextRealm = ClassRealmUtils.contextRealm();
        if ( contextRealm != cachedContextRealm )
        {
            visibleRealmNames = ClassRealmUtils.visibleRealmNames( contextRealm );
            cachedContextRealm = contextRealm;
        }
        if ( null == visibleRealmNames || visibleRealmNames.isEmpty() )
        {
            return beans.iterator();
        }
        return new FilteredItr( new HashSet<String>( visibleRealmNames ) );
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
