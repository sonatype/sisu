/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.binders;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.EntryListAdapter;
import org.sonatype.guice.bean.locators.EntryMapAdapter;
import org.sonatype.guice.bean.locators.EntrySetAdapter;
import org.sonatype.guice.bean.locators.NamedIterableAdapter;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Parameters;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeConverterBinding;

// ----------------------------------------------------------------------
// BeanLocator-backed Providers that can provide dynamic bean lookups
// ----------------------------------------------------------------------

/**
 * Provides a {@link Iterable} sequence of {@link BeanEntry}s.
 */
final class BeanEntryProvider<K extends Annotation, V>
    implements Provider<Iterable<BeanEntry<K, V>>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private BeanLocator locator;

    private final Key<V> key;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanEntryProvider( final Key<V> key )
    {
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterable<BeanEntry<K, V>> get()
    {
        return locator.locate( key );
    }
}

//----------------------------------------------------------------------

/**
 * Provides a {@link List} of qualified beans.
 */
final class BeanListProvider<K extends Annotation, V>
    implements Provider<List<V>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private BeanLocator locator;

    private final Key<V> key;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanListProvider( final Key<V> key )
    {
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public List<V> get()
    {
        return new EntryListAdapter<Annotation, V>( locator.locate( key ) );
    }
}

//----------------------------------------------------------------------

/**
 * Provides a {@link Set} of qualified beans.
 */
final class BeanSetProvider<K extends Annotation, V>
    implements Provider<Set<V>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private BeanLocator locator;

    private final Key<V> key;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanSetProvider( final Key<V> key )
    {
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Set<V> get()
    {
        return new EntrySetAdapter<Annotation, V>( locator.locate( key ) );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a {@link Map} of qualified beans.
 */
@SuppressWarnings( { "unchecked", "rawtypes" } )
final class BeanMapProvider<K extends Annotation, V>
    implements Provider<Map<K, V>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private BeanLocator locator;

    private final Key<V> key;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanMapProvider( final Key<V> key )
    {
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<K, V> get()
    {
        return new EntryMapAdapter( locator.locate( key ) );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a {@link Map} of named beans.
 */
@SuppressWarnings( { "unchecked", "rawtypes" } )
final class NamedBeanMapProvider<V>
    implements Provider<Map<String, V>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private BeanLocator locator;

    private final Key key;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    NamedBeanMapProvider( final TypeLiteral<?> valueType )
    {
        this.key = Key.get( valueType, Named.class );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<String, V> get()
    {
        return new EntryMapAdapter( new NamedIterableAdapter( locator.locate( key ) ) );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a single qualified bean.
 */
final class BeanProvider<V>
    implements Provider<V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private BeanLocator locator;

    private final Key<V> key;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanProvider( final Key<V> key )
    {
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public V get()
    {
        return get( locator, key );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    static <T> T get( final BeanLocator locator, final Key<T> key )
    {
        final Iterator<? extends Entry<Annotation, T>> i = locator.locate( key ).iterator();
        return i.hasNext() ? i.next().getValue() : null; // TODO: dynamic proxy??
    }
}

// ----------------------------------------------------------------------

/**
 * Lazy cache of known {@link TypeConverter}s.
 */
@Singleton
final class TypeConverterMap
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private Injector injector;

    private final Map<TypeLiteral<?>, TypeConverter> converterMap =
        new ConcurrentHashMap<TypeLiteral<?>, TypeConverter>();

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    TypeConverter getTypeConverter( final TypeLiteral<?> type )
    {
        if ( converterMap.containsKey( type ) )
        {
            return converterMap.get( type );
        }
        TypeConverter converter = null;
        for ( final TypeConverterBinding b : injector.getTypeConverterBindings() )
        {
            if ( b.getTypeMatcher().matches( type ) )
            {
                converter = b.getTypeConverter();
                break;
            }
        }
        converterMap.put( type, converter );
        return converter;
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a single bean; the name used to lookup/convert the bean is selected at runtime.
 */
final class PlaceholderBeanProvider<V>
    implements Provider<V>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int EXPRESSION_RECURSION_LIMIT = 8;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    @Parameters
    @SuppressWarnings( "rawtypes" )
    private Map properties;

    @Inject
    private TypeConverterMap converterMap;

    @Inject
    private BeanLocator locator;

    private final Key<V> placeholderKey;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlaceholderBeanProvider( final Key<V> key )
    {
        placeholderKey = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public V get()
    {
        final String template = ( (Named) placeholderKey.getAnnotation() ).value();
        final TypeLiteral<V> expectedType = placeholderKey.getTypeLiteral();

        // ---------------- INTERPOLATION ----------------

        final Class<?> clazz = expectedType.getRawType();
        Object value = interpolate( template, clazz );
        if ( false == value instanceof String )
        {
            return (V) value; // found non-String mapping
        }

        // ------------------- LOOKUP --------------------

        final Key<V> lookupKey = Key.get( expectedType, Names.named( (String) value ) );
        if ( String.class != clazz )
        {
            final V bean = BeanProvider.get( locator, lookupKey );
            if ( null != bean )
            {
                return bean; // found non-String binding
            }
        }

        // ----------------- CONVERSION ------------------

        if ( template == value )
        {
            // same reference, so no interpolation occurred; is this perhaps a Guice constant?
            value = normalize( BeanProvider.get( locator, lookupKey.ofType( String.class ) ) );
        }
        if ( value instanceof String )
        {
            if ( String.class == clazz )
            {
                return (V) value; // no conversion required
            }
            final TypeConverter converter = converterMap.getTypeConverter( expectedType );
            if ( null != converter )
            {
                return (V) converter.convert( (String) value, expectedType );
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static <T> T normalize( final T value )
    {
        return "null".equals( value ) ? null : value;
    }

    private Object interpolate( final String template, final Class<?> clazz )
    {
        final StringBuilder buf;
        if ( template.contains( "${" ) )
        {
            buf = new StringBuilder( template );
        }
        else if ( properties.containsKey( template ) )
        {
            // handle situations where someone missed out the main brackets
            buf = new StringBuilder( "${" ).append( template ).append( '}' );
        }
        else
        {
            return template; // nothing to interpolate, maintain reference
        }
        int x = 0, y, expressionEnd = 0, expressionNum = 0;
        while ( ( x = buf.indexOf( "${", x ) ) >= 0 && ( y = buf.indexOf( "}", x ) + 1 ) > 0 )
        {
            if ( y > expressionEnd ) // making progress
            {
                expressionNum = 0;
                expressionEnd = y;
            }
            final String key = buf.substring( x + 2, y - 1 );
            final int anchor = key.indexOf( ":-" );
            Object value = properties.get( anchor < 0 ? key : key.substring( 0, anchor ) );
            if ( value == null && anchor >= 0 )
            {
                value = key.substring( anchor + 2 );
            }
            if ( expressionNum++ >= EXPRESSION_RECURSION_LIMIT )
            {
                throw new ProvisionException( "Recursive configuration: " + template + " stopped at: " + buf );
            }
            final int len = buf.length();
            if ( 0 == x && len == y && String.class != clazz && clazz.isInstance( value ) )
            {
                return value; // found compatible (non-String) instance in the properties!
            }
            buf.replace( x, y, String.valueOf( value ) );
            expressionEnd += buf.length() - len;
        }
        return normalize( buf.toString() );
    }
}

// ----------------------------------------------------------------------
