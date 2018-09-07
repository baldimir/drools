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
import org.drools.scorecards.example.Applicant;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.pmml.PMML4Data;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.rule.DataSource;
import org.kie.api.runtime.rule.RuleUnit;
import org.kie.api.runtime.rule.RuleUnitExecutor;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.ScoreCardConfiguration;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.kie.pmml.pmml_4_2.model.PMML4UnitImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ScorecardProviderTest {

    private ScoreCardProvider scoreCardProvider;

    @Before
    public void setUp() {
        scoreCardProvider = ScoreCardFactory.getScoreCardProvider();
        assertNotNull(scoreCardProvider);
    }

    @Test
    public void testDrlWithoutSheetName() throws IOException {
        try (final InputStream is = ScorecardProviderTest.class.getResourceAsStream("/scoremodel_c.xls")) {
            assertNotNull(is);

            final ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
            final KieBase kbase = scoreCardProvider.getKieBaseFromInputStream(is, scconf);
            assertNotNull(kbase);
        }
    }

    @Test
    public void testDrlWithSheetName() throws IOException {
        try (final InputStream is = ScorecardProviderTest.class.getResourceAsStream("/scoremodel_c.xls")) {
            assertNotNull(is);

            final ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
            scconf.setWorksheetName("scorecards");
            final KieBase kbase = scoreCardProvider.getKieBaseFromInputStream(is, scconf);
            assertNotNull(kbase);
        }
    }

    @Test
    public void testKnowledgeBaseWithExection() throws IOException {
        try (final InputStream is = ScorecardProviderTest.class.getResourceAsStream("/scoremodel_c.xls")) {
            final ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
            scconf.setWorksheetName("scorecards");

            final KieBase kbase = scoreCardProvider.getKieBaseFromInputStream(is, scconf);
            assertNotNull(kbase);
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

            final Double calculatedScore = resultHolder.getResultValue("CalculatedScore", "value", Double.class).orElse(null);
            assertNotNull(calculatedScore);
            assertEquals(56.0, calculatedScore, 1e-6);
        }
    }

    @Test
    public void testDrlGenerationWithExternalTypes() {
        final Resource resource = ResourceFactory.newClassPathResource("scoremodel_externalmodel.xls");
        final ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
        scconf.setWorksheetName("scorecards");
        scconf.setUsingExternalTypes(true);
        resource.setConfiguration(scconf);
        resource.setResourceType(ResourceType.SCARD);
        final KieBase kbase = new KieHelper().addResource(resource, ResourceType.SCARD).build();
        assertNotNull(kbase);
        final RuleUnitExecutor executor = RuleUnitExecutor.create().bind(kbase);

        final DataSource<PMMLRequestData> data = executor.newDataSource("request");
        final DataSource<PMML4Result> resultData = executor.newDataSource("results");
        executor.newDataSource("pmmlData");
        final DataSource<Applicant> applicantData = executor.newDataSource("externalBeanApplicant");

        final PMMLRequestData request = new PMMLRequestData("123", "Sample Score");
        final Applicant applicant = new Applicant();
        applicant.setAge(33.0);
        applicant.setOccupation("PROGRAMMER");
        applicant.setResidenceState("AP");
        applicant.setValidLicense(true);

        final PMML4Result resultHolder = new PMML4Result("123");
        final List<String> possiblePackages = calculatePossiblePackageNames("Sample Score", "org.drools.scorecards.example");
        final Class<? extends RuleUnit> ruleUnitClass = getStartingRuleUnit("RuleUnitIndicator", (InternalKnowledgeBase) kbase, possiblePackages);
        assertNotNull(ruleUnitClass);

        resultData.insert(resultHolder);
        data.insert(request);
        applicantData.insert(applicant);

        final int count = executor.run(ruleUnitClass);
        assertTrue(count > 0);

        final Double calculatedScore = resultHolder.getResultValue("Scorecard__calculatedScore", "value", Double.class).orElse(null);
        assertNotNull(calculatedScore);
        assertEquals(36.0, calculatedScore, 1e-6);
    }

    protected Class<? extends RuleUnit> getStartingRuleUnit(final String startingRule, final InternalKnowledgeBase ikb, final List<String> possiblePackages) {
        final RuleUnitRegistry unitRegistry = ikb.getRuleUnitRegistry();
        final Map<String, InternalKnowledgePackage> pkgs = ikb.getPackagesMap();
        RuleImpl ruleImpl;
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