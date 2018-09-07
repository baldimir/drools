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

import org.dmg.pmml.pmml_4_2.descr.Extension;
import org.dmg.pmml.pmml_4_2.descr.PMML;
import org.dmg.pmml.pmml_4_2.descr.Scorecard;
import org.drools.core.builder.conf.impl.ScoreCardConfigurationImpl;
import org.drools.scorecards.pmml.ScorecardPMMLExtensionNames;
import org.drools.scorecards.pmml.ScorecardPMMLUtils;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.internal.builder.ScoreCardConfiguration;
import org.kie.internal.io.ResourceFactory;
import org.kie.pmml.pmml_4_2.PMML4ExecutionHelper;
import org.kie.pmml.pmml_4_2.PMML4ExecutionHelper.PMML4ExecutionHelperFactory;
import org.kie.pmml.pmml_4_2.extensions.AggregationStrategy;

import static org.drools.scorecards.ScorecardCompiler.DrlType.INTERNAL_DECLARED_TYPES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ScoringStrategiesTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testScoringExtension() throws IOException {
        final PMML pmmlDocument;
        final ScorecardCompiler scorecardCompiler = new ScorecardCompiler(INTERNAL_DECLARED_TYPES);
        try (final InputStream inputStream = PMMLDocumentTest.class.getResourceAsStream("/scoremodel_scoring_strategies.xls")) {
            if (scorecardCompiler.compileFromExcel(inputStream)) {
                pmmlDocument = scorecardCompiler.getPMMLDocument();
                assertNotNull(pmmlDocument);
                for (final Object serializable : pmmlDocument.getAssociationModelsAndBaselineModelsAndClusteringModels()) {
                    if (serializable instanceof Scorecard) {
                        final Scorecard scorecard = (Scorecard) serializable;
                        assertEquals("Sample Score", scorecard.getModelName());
                        final Extension extension = ScorecardPMMLUtils.getExtension(scorecard.getExtensionsAndCharacteristicsAndMiningSchemas(), ScorecardPMMLExtensionNames.SCORECARD_SCORING_STRATEGY);
                        assertNotNull(extension);
                        assertEquals(extension.getValue(), AggregationStrategy.AGGREGATE_SCORE.toString());
                        return;
                    }
                }
            }
        }
        fail();
    }

    @Test
    public void testAggregate() {

        final double finalScore = executeAndFetchScore("scorecards");
        //age==10 (30), validLicense==FALSE (-1)
        assertEquals(29.0, finalScore, 0.0);
    }

    @Test
    public void testAverage() {

        final double finalScore = executeAndFetchScore("scorecards_avg");
        //age==10 (30), validLicense==FALSE (-1)
        //count = 2
        assertEquals(14.5, finalScore, 0.0);
    }

    @Test
    public void testMinimum() {
        final double finalScore = executeAndFetchScore("scorecards_min");
        //age==10 (30), validLicense==FALSE (-1)
        assertEquals(-1.0, finalScore, 0.0);
    }

    @Test
    public void testMaximum() {

        final double finalScore = executeAndFetchScore("scorecards_max");
        //age==10 (30), validLicense==FALSE (-1)
        assertEquals(30.0, finalScore, 0.0);
    }

    @Test
    public void testWeightedAggregate() {

        final double finalScore = executeAndFetchScore("scorecards_w_aggregate");
        //age==10 (score=30, w=20), validLicense==FALSE (score=-1, w=1)
        assertEquals(599.0, finalScore, 0.0);
    }

    @Test
    public void testWeightedAverage() {

        final double finalScore = executeAndFetchScore("scorecards_w_avg");
        //age==10 (score=30, w=20), validLicense==FALSE (score=-1, w=1)
        assertEquals(299.5, finalScore, 0.0);
    }

    @Test
    public void testWeightedMaximum() {

        final double finalScore = executeAndFetchScore("scorecards_w_max");
        //age==10 (score=30, w=20), validLicense==FALSE (score=-1, w=1)
        assertEquals(600.0, finalScore, 0.0);
    }

    @Test
    public void testWeightedMinimum() {

        final double finalScore = executeAndFetchScore("scorecards_w_min");
        //age==10 (score=30, w=20), validLicense==FALSE (score=-1, w=1)
        assertEquals(-1.0, finalScore, 0.0);
    }

    /* Tests with Initial Score */
    @Test
    public void testAggregateInitialScore() {

        final double finalScore = executeAndFetchScore("scorecards_initial_score");
        //age==10 (30), validLicense==FALSE (-1)
        //initialScore = 100
        assertEquals(129.0, finalScore, 0.0);
    }

    @Test
    public void testAverageInitialScore() {
        final double finalScore = executeAndFetchScore("scorecards_avg_initial_score");
        //age==10 (30), validLicense==FALSE (-1)
        //count = 2
        //initialScore = 100
        assertEquals(114.5, finalScore, 0.0);
    }

    @Test
    public void testMinimumInitialScore() {
        final double finalScore = executeAndFetchScore("scorecards_min_initial_score");
        //age==10 (30), validLicense==FALSE (-1)
        //initialScore = 100
        assertEquals(99.0, finalScore, 0.0);
    }

    @Test
    public void testMaximumInitialScore() {

        final double finalScore = executeAndFetchScore("scorecards_max_initial_score");
        //age==10 (30), validLicense==FALSE (-1)
        //initialScore = 100
        assertEquals(130.0, finalScore, 0.0);
    }

    @Test
    public void testWeightedAggregateInitialScore() {

        final double finalScore = executeAndFetchScore("scorecards_w_aggregate_initial");
        //age==10 (score=30, w=20), validLicense==FALSE (score=-1, w=1)
        //initialScore = 100
        assertEquals(699.0, finalScore, 0.0);
    }

    @Test
    public void testWeightedAverageInitialScore() {

        final double finalScore = executeAndFetchScore("scorecards_w_avg_initial");
        //age==10 (score=30, w=20), validLicense==FALSE (score=-1, w=1)
        //initialScore = 100
        assertEquals(399.5, finalScore, 0.0);
    }

    @Test
    public void testWeightedMaximumInitialScore() {

        final double finalScore = executeAndFetchScore("scorecards_w_max_initial");
        //age==10 (score=30, w=20), validLicense==FALSE (score=-1, w=1)
        //initialScore = 100
        assertEquals(700.0, finalScore, 0.0);
    }

    @Test
    public void testWeightedMinimumInitialScore() {

        final double finalScore = executeAndFetchScore("scorecards_w_min_initial");
        //age==10 (score=30, w=20), validLicense==FALSE (score=-1, w=1)
        //initialScore = 100
        assertEquals(99.0, finalScore, 0.0);
    }

    /* Internal functions */
    private double executeAndFetchScore(final String sheetName) {

        final Resource resource = ResourceFactory.newClassPathResource("scoremodel_scoring_strategies.xls").setResourceType(ResourceType.SCARD);
        final ScoreCardConfiguration resConf = new ScoreCardConfigurationImpl();
        resConf.setWorksheetName(sheetName);
        resource.setConfiguration(resConf);
        final PMML4ExecutionHelper helper = PMML4ExecutionHelperFactory.getExecutionHelper("SampleScore", resource, null);
        helper.addPossiblePackageName("org.drools.scorecards.example");
        final PMMLRequestData request = new PMMLRequestData("123", helper.getModelName());
        request.addRequestParam("age", 10.0);
        request.addRequestParam("validLicense", false);

        final PMML4Result resultHolder = helper.submitRequest(request);
        assertEquals("OK", resultHolder.getResultCode());
        final Double result = resultHolder.getResultValue("Scorecard__calculatedScore", "value", Double.class).orElse(null);
        assertNotNull(result);
        return result;
    }
}
