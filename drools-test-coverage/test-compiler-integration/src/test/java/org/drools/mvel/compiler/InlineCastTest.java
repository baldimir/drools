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
package org.drools.mvel.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieBaseUtil;
import org.drools.testcoverage.common.util.TestParametersUtil2;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

import static org.assertj.core.api.Assertions.assertThat;

public class InlineCastTest {

    public static Stream<KieBaseTestConfiguration> parameters() {
        return TestParametersUtil2.getKieBaseCloudConfigurations(true).stream();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCast(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        String str = "import org.drools.mvel.compiler.*;\n" +
                "rule R1 when\n" +
                "   Person( name == \"mark\", address#LongAddress.country == \"uk\" )\n" +
                "then\n" +
                "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        Person mark2 = new Person("mark");
        ksession.insert(mark2);

        Person mark3 = new Person("mark");
        mark3.setAddress(new Address());
        ksession.insert(mark3);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastWithBinding(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        String str = "import org.drools.mvel.compiler.*;\n" +
                "rule R1 when\n" +
                "   Person( name == \"mark\", $country : address#LongAddress.country == \"uk\" )\n" +
                "then\n" +
                "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        Person mark2 = new Person("mark");
        ksession.insert(mark2);

        Person mark3 = new Person("mark");
        mark3.setAddress(new Address());
        ksession.insert(mark3);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastOnlyBinding(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        String str = "import org.drools.mvel.compiler.*;\n" +
                "rule R1 when\n" +
                "   Person( name == \"mark\", $country : address#LongAddress.country )\n" +
                "then\n" +
                "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        Person mark2 = new Person("mark");
        ksession.insert(mark2);

        Person mark3 = new Person("mark");
        mark3.setAddress(new Address());
        ksession.insert(mark3);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastWithFQN(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        String str = "import org.drools.mvel.compiler.Person;\n" +
                "rule R1 when\n" +
                "   Person( name == \"mark\", address#org.drools.mvel.compiler.LongAddress.country == \"uk\" )\n" +
                "then\n" +
                "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastOnRightOperand(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        String str = "import org.drools.mvel.compiler.*;\n" +
                "rule R1 when\n" +
                "   $person : Person( )\n" +
                "   String( this == $person.address#LongAddress.country )\n" +
                "then\n" +
                "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);
        ksession.insert("uk");

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastOnRightOperandWithFQN(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        String str = "import org.drools.mvel.compiler.Person;\n" +
                "rule R1 when\n" +
                "   $person : Person( )\n" +
                "   String( this == $person.address#org.drools.mvel.compiler.LongAddress.country )\n" +
                "then\n" +
                "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);
        ksession.insert("uk");

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInferredCast(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        String str = "import org.drools.mvel.compiler.*;\n" +
                "rule R1 when\n" +
                "   Person( name == \"mark\", address instanceof LongAddress, address.country == \"uk\" )\n" +
                "then\n" +
                "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        Person mark2 = new Person("mark");
        ksession.insert(mark2);

        Person mark3 = new Person("mark");
        mark3.setAddress(new Address());
        ksession.insert(mark3);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineTypeCast(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        // DROOLS-136
        String str = "import org.drools.mvel.compiler.*;\n" +
                     "rule R1 when\n" +
                     " Person( name == \"mark\", address#LongAddress )\n" +
                     "then\n" +
                     "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        Person mark2 = new Person("mark");
        ksession.insert(mark2);

        Person mark3 = new Person("mark");
        mark3.setAddress(new Address());
        ksession.insert(mark3);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastWithNestedAccces(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        // DROOLS-127
        String str = "import org.drools.mvel.compiler.*;\n" +
                     "rule R1 when\n" +
                     "   Person( name == \"mark\", address#LongAddress.country.length == 2 )\n" +
                     "then\n" +
                     "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        Person mark2 = new Person("mark");
        ksession.insert(mark2);

        Person mark3 = new Person("mark");
        mark3.setAddress(new Address());
        ksession.insert(mark3);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastWithNestedAcccesAndNullSafeDereferencing(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        String str = "import org.drools.mvel.compiler.*;\n" +
                     "rule R1 when\n" +
                     " Person( name == \"mark\", address#LongAddress.country!.length == 2 )\n" +
                     "then\n" +
                     "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        Person mark2 = new Person("mark");
        ksession.insert(mark2);

        Person mark3 = new Person("mark");
        mark3.setAddress(new LongAddress( null ) );
        ksession.insert(mark3);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastWithNestedAcccesAndNullSafeDereferencing2(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        String str = "import org.drools.mvel.compiler.*;\n" +
                     "rule R1 when\n" +
                     " Person( " +
                     " name == \"mark\", " +
                     " name == \"john\" || address#LongAddress.country!.length == 2 )\n" +
                     "then\n" +
                     "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();

        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        Person mark2 = new Person("mark");
        ksession.insert(mark2);

        Person mark3 = new Person("mark");
        mark3.setAddress(new LongAddress( null ) );
        ksession.insert(mark3);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testSuperclass(KieBaseTestConfiguration kieBaseTestConfiguration) {
        String drl = "package org.drools.mvel.compiler.integrationtests\n"
                     + "import org.drools.mvel.compiler.*;\n"
                     + "rule R1\n"
                     + " when\n"
                     + " Person( address#LongAddress.country str[startsWith] \"United\" )\n"
                     + " then\n"
                     + "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, drl);
        KieSession ksession = kbase.newKieSession();
        try {
            Person mark1 = new Person("mark");
            mark1.setAddress(new Address());
            ksession.insert(mark1);

            Person mark2 = new Person("mark");
            mark2.setAddress(new LongAddress("United Kingdom"));
            ksession.insert(mark2);

            Person mark3 = new Person("mark");
            mark3.setAddress(new LongAddress("Czech Republic"));
            ksession.insert(mark3);

            assertThat(ksession.fireAllRules()).as("wrong number of rules fired").isEqualTo(1);
        } finally {
            ksession.dispose();
        }
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testGroupedAccess(KieBaseTestConfiguration kieBaseTestConfiguration) {
        String drl = "package org.drools.mvel.compiler.integrationtests\n"
                     + "import org.drools.mvel.compiler.*;\n"
                     + "rule R1\n"
                     + " when\n"
                     + " Person( address#LongAddress.(country == \"United States\" || country == \"United Kingdom\") )\n"
                     + " then\n"
                     + "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, drl);
        KieSession ksession = kbase.newKieSession();
        try {
            Person mark1 = new Person("mark");
            mark1.setAddress(new LongAddress("United States"));
            ksession.insert(mark1);

            Person mark2 = new Person("mark");
            mark2.setAddress(new LongAddress("United Kingdom"));
            ksession.insert(mark2);

            Person mark3 = new Person("mark");
            mark3.setAddress(new LongAddress("Czech Republic"));
            ksession.insert(mark3);

            assertThat(ksession.fireAllRules()).as("wrong number of rules fired").isEqualTo(2);
        } finally {
            ksession.dispose();
        }
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testMatchesOperator(KieBaseTestConfiguration kieBaseTestConfiguration) {
        // BZ-971008
        String drl = "package org.drools.mvel.compiler.integrationtests\n"
                     + "import org.drools.mvel.compiler.*;\n"
                     + "rule R1\n"
                     + " when\n"
                     + " Person( address#LongAddress.country matches \"[Uu]nited.*\" )\n"
                     + " then\n"
                     + "end\n";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, drl);
        KieSession ksession = kbase.newKieSession();
        try {
            Person mark1 = new Person("mark");
            mark1.setAddress(new LongAddress("United States"));
            ksession.insert(mark1);

            Person mark2 = new Person("mark");
            mark2.setAddress(new LongAddress("United Kingdom"));
            ksession.insert(mark2);

            Person mark3 = new Person("mark");
            mark3.setAddress(new LongAddress("Czech Republic"));
            ksession.insert(mark3);

            assertThat(ksession.fireAllRules()).as("wrong number of rules fired").isEqualTo(2);
        } finally {
            ksession.dispose();
        }
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastWithThis(KieBaseTestConfiguration kieBaseTestConfiguration) {
        String drl = "package org.drools.mvel.compiler.integrationtests "
                     + "import org.drools.compiler.*; "
                     + "rule R1 "
                     + " when "
                     + " Object( this#String matches \"[Uu]nited.*\" ) "
                     + " then "
                     + "end ";

        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, drl);
        KieSession ksession = kbase.newKieSession();
        try {
            ksession.insert( "United States" );
            ksession.insert( "United Kingdom" );
            ksession.insert( "Italy" );
            assertThat(ksession.fireAllRules()).as("wrong number of rules fired").isEqualTo(2);
        } finally {
            ksession.dispose();
        }
    }
    
    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testInlineCastWithFQNAndMethodInvocation(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        // DROOLS-1337
        String str =
                "import org.drools.mvel.compiler.Person;\n" +
                "global java.util.List list;\n" +
                "rule R1 when\n" +
                "   Person( name == \"mark\", $x : address#org.drools.mvel.compiler.LongAddress.country.substring(1) )\n" +
                "then\n" +
                "   list.add($x);" +
                "end\n";
 
        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, str);
        KieSession ksession = kbase.newKieSession();
 
        List<String> list = new ArrayList<String>();
        ksession.setGlobal( "list", list );
 
        Person mark1 = new Person("mark");
        mark1.setAddress(new LongAddress("uk"));
        ksession.insert(mark1);

        assertThat(ksession.fireAllRules()).isEqualTo(1);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo("k");
 
        ksession.dispose();
    }
}
