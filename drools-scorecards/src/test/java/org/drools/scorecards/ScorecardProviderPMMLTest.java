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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.compiler.compiler.ScoreCardFactory;
import org.drools.compiler.compiler.ScoreCardProvider;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.ruleunit.RuleUnitDescr;
import org.drools.core.ruleunit.RuleUnitRegistry;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.pmml.PMML4Data;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.rule.DataSource;
import org.kie.api.runtime.rule.RuleUnit;
import org.kie.api.runtime.rule.RuleUnitExecutor;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.ScoreCardConfiguration;
import org.kie.internal.utils.KieHelper;
import org.kie.pmml.pmml_4_2.model.PMML4UnitImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.internal.builder.ScoreCardConfiguration.SCORECARD_INPUT_TYPE;

public class ScorecardProviderPMMLTest {

    private ScoreCardProvider scoreCardProvider;

    @Before
    public void setUp() {
        scoreCardProvider = ScoreCardFactory.getScoreCardProvider();
        assertNotNull(scoreCardProvider);
    }

    @Test
    @Ignore
    public void testDrlGeneration() throws IOException {
        try (final InputStream is = ScorecardProviderPMMLTest.class.getResourceAsStream("/SimpleScorecard.pmml")) {
            assertNotNull(is);

            final ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
            scconf.setInputType(SCORECARD_INPUT_TYPE.PMML);
            final String drl = scoreCardProvider.loadFromInputStream(is, scconf);
            assertNotNull(drl);
            assertTrue(drl.length() > 0);
        }
    }

    @Test
    public void testKnowledgeBaseWithExecution() {
        final KieBase kbase = new KieHelper().addFromClassPath("/SimpleScorecard.pmml").build();
        final RuleUnitExecutor executor = RuleUnitExecutor.create().bind(kbase);
        assertNotNull(executor);

        final DataSource<PMMLRequestData> data = executor.newDataSource("request");
        final DataSource<PMML4Result> resultData = executor.newDataSource("results");
        final DataSource<PMML4Data> pmmlData = executor.newDataSource("pmmlData");

        final PMMLRequestData request = new PMMLRequestData("123", "SampleScore");
        request.addRequestParam("age", 33.0);
        request.addRequestParam("occupation", "PROGRAMMER");
        request.addRequestParam("residenceState", "KN");
        request.addRequestParam("validLicense", true);

        data.insert(request);

        final PMML4Result resultHolder = new PMML4Result("123");
        resultData.insert(resultHolder);

        final List<String> possiblePackages = calculatePossiblePackageNames("Sample Score", "org.drools.scorecards.example");
        final Class<? extends RuleUnit> ruleUnitClass = getStartingRuleUnit("RuleUnitIndicator", (InternalKnowledgeBase) kbase, possiblePackages);
        final int executions = executor.run(ruleUnitClass);
        assertTrue(executions > 0);

        final Double calculatedScore = resultHolder.getResultValue("Scorecard_calculatedScore", "value", Double.class).orElse(null);
        assertNotNull(calculatedScore);
        assertEquals(56.0, calculatedScore, 1e-6);
    }

    protected Class<? extends RuleUnit> getStartingRuleUnit(final String startingRule, final InternalKnowledgeBase ikb, final List<String> possiblePackages) {
        final RuleUnitRegistry unitRegistry = ikb.getRuleUnitRegistry();
        final Map<String, InternalKnowledgePackage> pkgs = ikb.getPackagesMap();
        RuleImpl ruleImpl = null;
        for (final String pkgName : possiblePackages) {
            if (pkgs.containsKey(pkgName)) {
                final InternalKnowledgePackage pkg = pkgs.get(pkgName);
                ruleImpl = pkg.getRule(startingRule);
                if (ruleImpl != null) {
                    final RuleUnitDescr descr = unitRegistry.getRuleUnitFor(ruleImpl).orElse(null);
                    if (descr != null) {
                        return descr.getRuleUnitClass();
                    }
                }
            }
        }
        return null;
    }

    protected List<String> calculatePossiblePackageNames(final String modelId, final String... knownPackageNames) {
        final List<String> packageNames = new ArrayList<>();
        final String javaModelId = modelId.replaceAll("\\s", "");
        if (knownPackageNames != null && knownPackageNames.length > 0) {
            for (final String knownPkgName : knownPackageNames) {
                packageNames.add(knownPkgName + "." + javaModelId);
            }
        }
        final String basePkgName = PMML4UnitImpl.DEFAULT_ROOT_PACKAGE + "." + javaModelId;
        packageNames.add(basePkgName);
        return packageNames;
    }
}