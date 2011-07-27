package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.sonatype.inject.BeanEntry;

import com.google.inject.Binding;

/**
 * Copy-on-write cache mapping {@link Binding}s to {@link BeanEntry}s; optimized for the common case of a single entry.
 * Note: uses {@code ==} instead of {@code equals} to compare {@link Binding}s because we want referential equality.
 */
@SuppressWarnings( { "rawtypes", "unchecked" } )
final class BeanCache<Q extends Annotation, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final AtomicReference<Object> cache = new AtomicReference();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Atomically creates a new {@link BeanEntry} for this exact {@link Binding}.
     * 
     * @param qualifier The qualifier
     * @param binding The binding
     * @param rank The assigned rank
     * @return Associated bean entry
     */
    public BeanEntry<Q, T> create( final Q qualifier, final Binding<T> binding, final int rank )
    {
        final BeanEntry newBean = new LazyBeanEntry( qualifier, binding, rank );

        Object o, n;

        /*
         * Compare-and-swap approach; avoids locking without missing any updates
         */
        do
        {
            o = cache.get();
            if ( null == o )
            {
                n = newBean; // most common case: adding the one (and-only) entry
            }
            else
            {
                final Map<Binding, BeanEntry> map;
                if ( o instanceof LazyBeanEntry ) // promote single entry into map
                {
                    map = new IdentityHashMap(); // must use identity!
                    final LazyBeanEntry entry = (LazyBeanEntry) o;
                    map.put( entry.binding, entry );
                }
                else
                {
                    map = (Map) ( (IdentityHashMap) o ).clone(); // copy-on-write
                }
                final BeanEntry oldBean = map.put( binding, newBean );
                if ( null != oldBean )
                {
                    return oldBean; // map already has an entry; use that instead
                }
                n = map;
            }
        }
        while ( !cache.compareAndSet( o, n ) );

        return newBean;
    }

    /**
     * Retrieves the {@link BeanEntry} associated with this exact {@link Binding}.
     * 
     * @param binding The binding
     * @return Associated bean entry
     */
    public BeanEntry<Q, T> get( final Binding<T> binding )
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

    /**
     * Removes the {@link BeanEntry} associated with this exact {@link Binding}.
     * 
     * @param binding The binding
     */
    public void removeThis( final Binding<T> binding )
    {
        Object o, n;

        /*
         * Compare-and-swap approach; avoids locking without missing any updates
         */
        do
        {
            o = cache.get();
            if ( null == o )
            {
                return;
            }
            else if ( o instanceof LazyBeanEntry )
            {
                if ( binding != ( (LazyBeanEntry) o ).binding )
                {
                    return;
                }
                n = null; // clear single entry
            }
            else
            {
                final Map map = (Map) ( (IdentityHashMap) o ).clone(); // copy-on-write
                if ( null == map.remove( binding ) )
                {
                    return;
                }
                n = map.isEmpty() ? null : map; // avoid leaving empty maps around
            }
        }
        while ( !cache.compareAndSet( o, n ) );
    }
}
