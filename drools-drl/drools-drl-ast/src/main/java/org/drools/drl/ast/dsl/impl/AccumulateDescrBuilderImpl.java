/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.drools.drl.ast.dsl.impl;

import java.util.List;

import org.drools.drl.ast.descr.AccumulateDescr;
import org.drools.drl.ast.descr.AndDescr;
import org.drools.drl.ast.descr.BaseDescr;
import org.drools.drl.ast.descr.ConditionalElementDescr;
import org.drools.drl.ast.descr.ExprConstraintDescr;
import org.drools.drl.ast.descr.PatternDescr;
import org.drools.drl.ast.dsl.AccumulateDescrBuilder;
import org.drools.drl.ast.dsl.CEDescrBuilder;
import org.drools.drl.ast.dsl.DescrBuilder;
import org.drools.drl.ast.dsl.PatternDescrBuilder;

/**
 * An implementation for the CollectDescrBuilder
 */
public class AccumulateDescrBuilderImpl<P extends DescrBuilder< ?, ? >> extends BaseDescrBuilderImpl<P, AccumulateDescr>
    implements
        AccumulateDescrBuilder<P> {

    public AccumulateDescrBuilderImpl(P parent) {
        this( parent, new AccumulateDescr() );
    }

    protected AccumulateDescrBuilderImpl(P parent, AccumulateDescr accumulateDescr) {
        super( parent, accumulateDescr );
    }

    /**
     * {@inheritDoc}
     */
    public PatternDescrBuilder<AccumulateDescrBuilder<P>> pattern( String type ) {
        PatternDescrBuilder<AccumulateDescrBuilder<P>> pattern = new PatternDescrBuilderImpl<>( this, type );
        descr.setInputPattern( pattern.getDescr() );
        return pattern;
    }

    /**
     * {@inheritDoc}
     */
    public PatternDescrBuilder<AccumulateDescrBuilder<P>> pattern() {
        PatternDescrBuilder<AccumulateDescrBuilder<P>> pattern = new PatternDescrBuilderImpl<>( this );
        descr.setInputPattern( pattern.getDescr() );
        return pattern;
    }

    public CEDescrBuilder<AccumulateDescrBuilder<P>, AndDescr> source() {
        CEDescrBuilder<AccumulateDescrBuilder<P>, AndDescr> and = new CEDescrBuilderImpl<>( this, new AndDescr() );
        descr.setInput( and.getDescr() );
        return and;
    }

    public AccumulateDescrBuilder<P> function( String name,
                                               String bind,
                                               boolean unif,
                                               String... parameters ) {
        descr.addFunction( name,
                           bind,
                           unif,
                           parameters );
        return this;
    }

    public AccumulateDescrBuilder<P> init( String block ) {
        descr.setInitCode( block );
        return this;
    }

    public AccumulateDescrBuilder<P> action( String block ) {
        descr.setActionCode( block );
        return this;
    }

    public AccumulateDescrBuilder<P> reverse( String block ) {
        descr.setReverseCode( block );
        return this;
    }

    public AccumulateDescrBuilder<P> result( String expr ) {
        descr.setResultCode( expr );
        return this;
    }

    @Override
    public AccumulateDescrBuilder<P> constraint( String constr ) {
        if ( parent instanceof PatternDescrBuilder ) {
            ( (PatternDescrBuilder) parent ).constraint( constr );
        } else if ( parent instanceof CEDescrBuilder ) {
            List<? extends BaseDescr> args = ((ConditionalElementDescr) parent.getDescr()).getDescrs();
            ( (PatternDescr) args.get( args.size() - 1 ) ).addConstraint( new ExprConstraintDescr( constr ) );
        }
        return this;
    }
}
