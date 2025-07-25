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
package org.kie.api.runtime.rule;

/**
 * An handle to a fact inserted into the working memory
 */
public interface FactHandle {
    Object getObject();

    boolean isNegated();

    boolean isEvent();

    long getId();

    long getRecency();

     <K> K as(Class<K> klass) throws ClassCastException;

    boolean isValid();

    /**
     * The way how the fact to which this FactHandle was assigned
     * has been inserted into the working memory
     */
    enum State {
        ALL,

        /**
         * A fact that has been explicitly stated into the working memory
         */
        STATED,

        /**
         * A fact that has been logically inserted into the working memory
         */
        LOGICAL;

        public boolean isStated() {
            return this != LOGICAL;
        }

        public boolean isLogical() {
            return this != STATED;
        }
    }

    String toExternalForm();
}
