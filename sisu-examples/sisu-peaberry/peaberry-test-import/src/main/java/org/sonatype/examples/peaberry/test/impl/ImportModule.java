package org.sonatype.examples.peaberry.test.impl;

import static org.ops4j.peaberry.Peaberry.service;

import javax.inject.Named;

import org.ops4j.peaberry.Peaberry;
import org.sonatype.examples.peaberry.test.Scramble;

import com.google.inject.AbstractModule;

@Named
class ImportModule
    extends AbstractModule
{
    @Override
    protected void configure()
    {
        install( Peaberry.osgiModule() );
        bind( Scramble.class ).toProvider( service( Scramble.class ).single() );
    }
}
