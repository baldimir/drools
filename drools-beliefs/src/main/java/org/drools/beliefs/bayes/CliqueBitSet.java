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
package org.drools.beliefs.bayes;

import org.drools.util.bitmask.OpenBitSet;

public class CliqueBitSet {
    private org.drools.util.bitmask.OpenBitSet OpenBitSet;

    private int weight;

    public CliqueBitSet(OpenBitSet OpenBitSet, int weight) {
        this.OpenBitSet = OpenBitSet;
        this.weight = weight;
    }

    public OpenBitSet getOpenBitSet() {
        return OpenBitSet;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        CliqueBitSet clique = (CliqueBitSet) o;

        if (weight != clique.weight) { return false; }
        if (!OpenBitSet.equals(clique.OpenBitSet)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        int result = OpenBitSet.hashCode();
        result = 31 * result + weight;
        return result;
    }

    @Override
    public String toString() {
        return "Clique{" +
               "OpenBitSet=" + OpenBitSet +
               ", weight=" + weight +
               '}';
    }
}
