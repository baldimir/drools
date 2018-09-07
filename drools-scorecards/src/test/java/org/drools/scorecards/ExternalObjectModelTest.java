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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.pmml_4_2.descr.Extension;
import org.dmg.pmml.pmml_4_2.descr.Output;
import org.dmg.pmml.pmml_4_2.descr.OutputField;
import org.dmg.pmml.pmml_4_2.descr.PMML;
import org.dmg.pmml.pmml_4_2.descr.Scorecard;
import org.drools.scorecards.example.Applicant;
import org.drools.scorecards.pmml.ScorecardPMMLUtils;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.ScoreCardConfiguration;
import org.kie.internal.io.ResourceFactory;
import org.kie.pmml.pmml_4_2.PMML4Compiler;
import org.kie.pmml.pmml_4_2.PMML4ExecutionHelper;
import org.kie.pmml.pmml_4_2.PMML4ExecutionHelper.PMML4ExecutionHelperFactory;
import org.kie.pmml.pmml_4_2.extensions.PMMLExtensionNames;

import static org.drools.scorecards.ScorecardCompiler.DrlType.EXTERNAL_OBJECT_MODEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExternalObjectModelTest {
    private ScorecardCompiler scorecardCompiler;

    @Before
    public void setUp() {
        scorecardCompiler = new ScorecardCompiler(EXTERNAL_OBJECT_MODEL);
    }

    @Test
    public void testPMMLCustomOutput() throws IOException {
        PMML pmmlDocument = null;
        String drl = null;
        try (final InputStream inputStream = PMMLDocumentTest.class.getResourceAsStream("/scoremodel_externalmodel.xls")) {
            if (scorecardCompiler.compileFromExcel(inputStream)) {
                pmmlDocument = scorecardCompiler.getPMMLDocument();
                assertNotNull(pmmlDocument);
                PMML4Compiler.dumpModel(pmmlDocument, System.out);
                drl = scorecardCompiler.getDRL();
                assertTrue(drl != null && !drl.isEmpty());
            } else {
                fail("failed to parse scoremodel Excel.");
            }
        }

        for (final Object serializable : pmmlDocument.getAssociationModelsAndBaselineModelsAndClusteringModels()){
            if (serializable instanceof Scorecard){
                final Scorecard scorecard = (Scorecard)serializable;
                for (final Object obj :scorecard.getExtensionsAndCharacteristicsAndMiningSchemas()){
                    if ( obj instanceof Output) {
                        final Output output = (Output)obj;
                        final List<OutputField> outputFields = output.getOutputFields();
                        assertEquals(1, outputFields.size());
                        final OutputField outputField = outputFields.get(0);
                        assertNotNull(outputField);
                        assertEquals("totalScore", outputField.getName());
                        assertEquals("Final Score", outputField.getDisplayName());
                        assertEquals("double", outputField.getDataType().value());
                        assertEquals("predictedValue", outputField.getFeature().value());
                        final Extension extension = ScorecardPMMLUtils.getExtension(outputField.getExtensions(), PMMLExtensionNames.EXTERNAL_CLASS );
                        assertNotNull(extension);
                        assertEquals("org.drools.scorecards.example.Applicant",extension.getValue());
                        return;
                    }
                }
            }
        }
        fail();
    }

    @Test
    public void testWithInitialScore() {
        final Map<String,List<Object>> externalData = new HashMap<>();
        final List<Object> applicantValues = new ArrayList<>();

        final Resource resource = ResourceFactory.newClassPathResource("scoremodel_externalmodel.xls");
        assertNotNull(resource);
        final ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
        scconf.setUsingExternalTypes(true);
        scconf.setWorksheetName("scorecards_initialscore");
        resource.setConfiguration(scconf);
        resource.setResourceType(ResourceType.SCARD);
        final PMML4ExecutionHelper helper = PMML4ExecutionHelperFactory.getExecutionHelper("Sample Score", resource, null, false);
        helper.addExternalDataSource("externalBeanApplicant");
        helper.addPossiblePackageName("org.drools.scorecards.example");

        Applicant applicant = new Applicant();
        applicant.setAge(10.0);
        applicantValues.add(applicant);
        externalData.put("externalBeanApplicant", applicantValues);

        PMMLRequestData request = new PMMLRequestData("123","Sample Score");
        PMML4Result resultHolder = helper.submitRequest(request, externalData);

        //occupation = 0, age = 30, validLicence -1, initialScore=100
        checkResults(129.0,resultHolder);

        applicant = new Applicant();
        applicant.setOccupation("SKYDIVER");
        applicant.setAge(0);
        applicantValues.clear();
        applicantValues.add(applicant);

        request = new PMMLRequestData("234", "Sample Score");
        resultHolder = helper.submitRequest(request, externalData);

        //occupation = -10, age = +10, validLicense = -1, initialScore=100;
        checkResults(99.0, resultHolder);

        applicant = new Applicant();
        applicant.setResidenceState("AP");
        applicant.setOccupation("TEACHER");
        applicant.setAge(20);
        applicant.setValidLicense(true);
        applicantValues.clear();
        applicantValues.add(applicant);

        request = new PMMLRequestData("345", "Sample Score");
        resultHolder = helper.submitRequest(request, externalData);

        //occupation = +10, age = +40, state = -10, validLicense = 1, initialScore=100
        checkResults(141.0, resultHolder);
    }

    @Test
    public void testWithReasonCodes() {
        final Map<String,List<Object>> externalData = new HashMap<>();
        final List<Object> applicantValues = new ArrayList<>();

        final Resource resource = ResourceFactory.newClassPathResource("scoremodel_externalmodel.xls");
        assertNotNull(resource);
        final ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
        scconf.setUsingExternalTypes(true);
        scconf.setWorksheetName("scorecards_reasoncode");
        resource.setConfiguration(scconf);
        resource.setResourceType(ResourceType.SCARD);
        final PMML4ExecutionHelper helper = PMML4ExecutionHelperFactory.getExecutionHelper("Sample Score", resource, null, false);
        helper.addExternalDataSource("externalBeanApplicant");
        helper.addPossiblePackageName("org.drools.scorecards.example");

        Applicant applicant = new Applicant();
        applicant.setAge(10);
        applicantValues.add(applicant);
        externalData.put("externalBeanApplicant", applicantValues);

        PMMLRequestData request = new PMMLRequestData("123","Sample Score");
        PMML4Result resultHolder = helper.submitRequest(request, externalData);

        //occupation = 0, age = 30, validLicence -1, initialScore=100
        checkResults(129.0,"VL0099",Arrays.asList("VL0099", "AGE02"),resultHolder);

        applicant = new Applicant();
        applicant.setOccupation("SKYDIVER");
        applicant.setAge(0);
        applicantValues.clear();
        applicantValues.add(applicant);
        request = new PMMLRequestData("234","Sample Score");
        resultHolder = helper.submitRequest(request, externalData);

        //occupation = -10, age = +10, validLicense = -1, initialScore=100;
        checkResults(99.0,"OC0099",Arrays.asList("OC0099", "VL0099", "AGE01"),resultHolder);

        applicant = new Applicant();
        applicant.setResidenceState("AP");
        applicant.setOccupation("TEACHER");
        applicant.setAge(20);
        applicant.setValidLicense(true);
        applicantValues.clear();
        applicantValues.add(applicant);
        request = new PMMLRequestData("234","Sample Score");
        resultHolder = helper.submitRequest(request, externalData);

        //occupation = +10, age = +40, state = -10, validLicense = 1, initialScore=100
        checkResults(141.0,"RS001",Arrays.asList("RS001", "VL001", "OC0099", "AGE03"),resultHolder);
    }


    private void checkResults(final Double expectedTotalScore, final PMML4Result resultHolder) {
        assertEquals("OK",resultHolder.getResultCode());
        final Double totalScore = resultHolder.getResultValue("TotalScore", "value", Double.class).orElse(null);
        assertNotNull(totalScore);
        assertEquals(expectedTotalScore, totalScore,1e-6);
    }

    private void checkResults(final Double expectedTotalScore, final String expectedReasonCode, final List<String> expectedRanking, final PMML4Result resultHolder) {
        final Double totalScore = resultHolder.getResultValue("TotalScore", "value", Double.class).orElse(null);
        assertNotNull(totalScore);
        assertEquals(expectedTotalScore, totalScore, 1e-6);
        final String reasonCode = resultHolder.getResultValue("ReasonCodes", "value", String.class).orElse(null);
        assertEquals( expectedReasonCode, reasonCode );
        final Map reasonCodesMap = (Map)resultHolder.getResultValue("ScoreCard", "ranking");
        assertNotNull( reasonCodesMap );
        assertEquals( expectedRanking, new ArrayList( reasonCodesMap.keySet() ) );
    }
}
