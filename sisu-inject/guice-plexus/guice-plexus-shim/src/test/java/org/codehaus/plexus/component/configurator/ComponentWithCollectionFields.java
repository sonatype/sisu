package org.codehaus.plexus.component.configurator;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;

/**
 * @author <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 * @version $Id: ComponentWithCollectionFields.java 8004 2009-01-04 18:39:40Z bentmann $
 */
public class ComponentWithCollectionFields
{
    private Vector vector;

    private HashSet hashSet;

    private List list;

    private List stringList;

    private Set set;

    private SortedSet sortedSet;

    public Vector getVector()
    {
        return vector;
    }

    public HashSet getHashSet()
    {
        return hashSet;
    }

    public List getList()
    {
        return list;
    }

    public List getStringList()
    {
        return stringList;
    }

    public Set getSet()
    {
        return set;
    }

    public SortedSet getSortedSet()
    {
        return sortedSet;
    }
}
