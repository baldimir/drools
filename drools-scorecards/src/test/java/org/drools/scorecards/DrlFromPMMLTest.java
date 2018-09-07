/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.scorecards;

import java.io.IOException;
import java.io.InputStream;

import org.dmg.pmml.pmml_4_2.descr.PMML;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.pmml.pmml_4_2.PMML4Compiler;

import static org.drools.scorecards.ScorecardCompiler.DrlType.INTERNAL_DECLARED_TYPES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Ignore
public class DrlFromPMMLTest {

    private String drl;

    @Before
    public void setUp() throws IOException {
        final ScorecardCompiler scorecardCompiler = new ScorecardCompiler(INTERNAL_DECLARED_TYPES);
        try (final InputStream inputStream = PMMLDocumentTest.class.getResourceAsStream("/scoremodel_c.xls")) {
            if (scorecardCompiler.compileFromExcel(inputStream)) {
                final PMML pmmlDocument = scorecardCompiler.getPMMLDocument();
                assertNotNull(pmmlDocument);
                PMML4Compiler.dumpModel(pmmlDocument, System.out);
                drl = scorecardCompiler.getDRL();
            } else {
                fail("failed to parse scoremodel Excel.");
            }
        }
    }

    @Test
    public void testDrlNoNull() {
        assertNotNull(drl);
        assertTrue(drl.length() > 0);
    }

    @Test
    public void testPackage() {
        assertTrue(drl.contains("package org.drools.scorecards.example"));
    }

    @Test
    public void testRuleCount() {
        assertEquals(61, StringUtil.countMatches(drl, "rule \""));
    }

    @Test
    public void testImports() {
        assertEquals(2, StringUtil.countMatches(drl, "import "));
    }

    @Test
    public void testDRLExecution() throws Exception {
        final KieServices ks = KieServices.Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write(ks.getResources().newByteArrayResource(drl.getBytes())
                          .setSourcePath("test_scorecard_rules.drl")
                          .setResourceType(ResourceType.DRL));
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kieBuilder.buildAll();
        final KieContainer kieContainer = ks.newKieContainer(kieBuilder.getKieModule().getReleaseId());

        final KieBase kbase = kieContainer.getKieBase();
        KieSession session = kbase.newKieSession();

        final FactType scorecardType = kbase.getFactType("org.drools.scorecards.example", "SampleScore");
        Object scorecard = scorecardType.newInstance();
        scorecardType.set(scorecard, "age", 10);
        session.insert(scorecard);
        session.fireAllRules();
        session.dispose();
        //occupation = 5, age = 25, validLicence -1
        assertEquals(29.0, scorecardType.get(scorecard, "scorecard__calculatedScore"));

        session = kbase.newKieSession();
        scorecard = scorecardType.newInstance();
        scorecardType.set(scorecard, "occupation", "SKYDIVER");
        scorecardType.set(scorecard, "age", 0);
        session.insert(scorecard);
        session.fireAllRules();
        session.dispose();
        //occupation = -10, age = +10, validLicense = -1;
        assertEquals(-1.0, scorecardType.get(scorecard, "scorecard__calculatedScore"));

        session = kbase.newKieSession();
        scorecard = scorecardType.newInstance();
        scorecardType.set(scorecard, "residenceState", "AP");
        scorecardType.set(scorecard, "occupation", "TEACHER");
        scorecardType.set(scorecard, "age", 20);
        scorecardType.set(scorecard, "validLicense", true);
        session.insert(scorecard);
        session.fireAllRules();
        session.dispose();
        //occupation = +10, age = +40, state = -10, validLicense = 1
        assertEquals(41.0, scorecardType.get(scorecard, "scorecard__calculatedScore"));
    }
}
