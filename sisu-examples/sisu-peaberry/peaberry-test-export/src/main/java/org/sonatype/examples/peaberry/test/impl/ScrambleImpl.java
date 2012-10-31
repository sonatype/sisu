package org.sonatype.examples.peaberry.test.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.examples.peaberry.test.Scramble;

// basic text scrambler implementation
class ScrambleImpl
    implements Scramble
{
    public String process( final String message )
    {
        final List<Character> charList = new ArrayList<Character>();
        for ( final char c : message.toCharArray() )
        {
            charList.add( new Character( c ) );
        }

        Collections.shuffle( charList );

        final char[] mixedChars = new char[message.length()];
        for ( int i = 0; i < mixedChars.length; i++ )
        {
            mixedChars[i] = charList.get( i ).charValue();
        }

        return new String( mixedChars );
    }
}
