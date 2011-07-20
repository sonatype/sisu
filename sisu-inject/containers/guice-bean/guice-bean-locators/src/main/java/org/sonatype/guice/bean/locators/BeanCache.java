package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.sonatype.inject.BeanEntry;

import com.google.inject.Binding;

@SuppressWarnings( { "rawtypes", "unchecked" } )
class BeanCache<Q extends Annotation, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final AtomicReference<Object> cache = new AtomicReference();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final BeanEntry<Q, T> get( final Binding<T> binding )
    {
        final Object o = cache.get();
        if ( null == o )
        {
            return null;
        }
        else if ( o instanceof LazyBeanEntry )
        {
            final LazyBeanEntry entry = (LazyBeanEntry) o;
            return binding == entry.binding ? entry : null;
        }
        return ( (Map<Binding, BeanEntry>) o ).get( binding );
    }

    public final BeanEntry<Q, T> create( final Q qualifier, final Binding<T> binding, final int rank )
    {
        final BeanEntry newBean = new LazyBeanEntry( qualifier, binding, rank );

        Object oldCache;
        Object newCache;

        BeanEntry oldBean;

        do
        {
            oldCache = cache.get();
            if ( null == oldCache )
            {
                newCache = newBean;
            }
            else
            {
                if ( oldCache instanceof LazyBeanEntry )
                {
                    newCache = new IdentityHashMap();
                    final LazyBeanEntry entry = (LazyBeanEntry) oldCache;
                    ( (Map) newCache ).put( entry.binding, entry );
                }
                else
                {
                    newCache = ( (IdentityHashMap) oldCache ).clone();
                }
                oldBean = ( (Map<Binding, BeanEntry>) newCache ).put( binding, newBean );
                if ( null != oldBean )
                {
                    return oldBean;
                }
            }
        }
        while ( !flush( oldCache, newCache ) );

        return newBean;
    }

    public final void remove( final Binding<T> binding )
    {
        Object oldCache;
        Map newCache;

        do
        {
            oldCache = cache.get();
            if ( null == oldCache )
            {
                // ignore
            }
            else if ( oldCache instanceof LazyBeanEntry )
            {
                if ( binding == ( (LazyBeanEntry) oldCache ).binding )
                {
                    newCache = null;
                    continue;
                }
            }
            else if ( null != ( newCache = (Map) ( (IdentityHashMap) oldCache ).clone() ).remove( binding ) )
            {
                if ( newCache.isEmpty() )
                {
                    newCache = null;
                }
                continue;
            }
            return;
        }
        while ( !flush( oldCache, newCache ) );
    }

    public final void retainAll( final RankedList<Binding<T>> bindings )
    {
        Object oldCache;
        Map newCache;

        do
        {
            oldCache = cache.get();
            if ( null == oldCache )
            {
                // ignore
            }
            else if ( bindings.isEmpty() )
            {
                newCache = null;
                continue;
            }
            else if ( oldCache instanceof LazyBeanEntry )
            {
                if ( bindings.indexOfThis( ( (LazyBeanEntry) oldCache ).binding ) < 0 )
                {
                    newCache = null;
                    continue;
                }
            }
            else
            {
                boolean modified = false;
                newCache = (Map) ( (IdentityHashMap) oldCache ).clone();
                for ( final Iterator<Binding> itr = newCache.keySet().iterator(); itr.hasNext(); )
                {
                    if ( bindings.indexOfThis( itr.next() ) < 0 )
                    {
                        modified = true;
                        itr.remove();
                    }
                }
                if ( modified )
                {
                    if ( newCache.isEmpty() )
                    {
                        newCache = null;
                    }
                    continue;
                }
            }
            return;
        }
        while ( !flush( oldCache, newCache ) );
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    boolean flush( final Object oldCache, final Object newCache )
    {
        return cache.compareAndSet( oldCache, newCache );
    }
}
