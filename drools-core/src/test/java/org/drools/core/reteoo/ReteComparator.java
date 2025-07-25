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
package org.drools.core.reteoo;

import java.util.Arrays;
import java.util.Comparator;

import org.drools.base.common.NetworkNode;
import org.drools.core.common.BaseNode;
import org.drools.core.impl.InternalRuleBase;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;

public class ReteComparator {

    private ReteComparator() { }

    public static boolean areEqual(KieBase kbase1, KieBase kbase2) {
        try {
            compare( kbase1, kbase2 );
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static void compare(KieBase kbase1, KieBase kbase2) {
        compare((InternalRuleBase) kbase1, (InternalRuleBase) kbase2);
    }

    public static void compare( KieRuntime session1, KieRuntime session2 ) {
        compare((InternalRuleBase) session1.getKieBase(), (InternalRuleBase) session2.getKieBase());
    }

    public static void compare(KieSession session1, KieSession session2) {
        compare((InternalRuleBase) session1.getKieBase(), (InternalRuleBase) session2.getKieBase());
    }

    public static void compare(InternalRuleBase kBase1, InternalRuleBase kBase2) {
        compare( kBase1.getRete(), kBase2.getRete() );
    }

    public static void compare(Rete rete1, Rete rete2) {
        for (EntryPointNode epn1 : rete1.getEntryPointNodes().values()) {
            EntryPointNode epn2 = rete2.getEntryPointNode( epn1.getEntryPoint() );
            compareNodes( epn1, epn2 );
        }
    }

    private static void compareNodes(BaseNode node1, BaseNode node2) {
        if (!node1.equals( node2 )) {
            throw new RuntimeException( node1 + " and " + node2 + " are not equal as expected" );
        }

        NetworkNode[] sinks1 = node1.getSinks();
        NetworkNode[] sinks2 = node2.getSinks();

        if (sinks1 == null) {
            if (sinks2 == null) {
                return;
            } else {
                throw new RuntimeException( node1 + " has no sinks while " + node2 + " has " + sinks2.length + " sinks" );
            }
        } else if (sinks2 == null) {
            throw new RuntimeException( node1 + " has " + sinks1.length + " sinks while " + node2 + " has 0 sinks" );
        }

        if (sinks1.length != sinks2.length) {
            throw new RuntimeException( node1 + " has " + sinks1.length + " sinks while " + node2 + " has no sinks" );
        }

        Arrays.sort(sinks1, NODE_SORTER);
        Arrays.sort(sinks2, NODE_SORTER);

        for (int i = 0; i < sinks1.length; i++) {
            compareNodes( (BaseNode) sinks1[i], (BaseNode) sinks2[i] );
        }
    }

    public static final NetworkNodeComparator NODE_SORTER = new NetworkNodeComparator();
    public static class NetworkNodeComparator implements Comparator<NetworkNode> {
        @Override
        public int compare( NetworkNode n1, NetworkNode n2 ) {
            return n1.getId() - n2.getId();
        }
    }
}
