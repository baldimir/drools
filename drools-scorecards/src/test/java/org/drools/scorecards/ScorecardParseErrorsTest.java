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

import org.junit.Test;

import static org.drools.scorecards.ScorecardCompiler.DrlType.INTERNAL_DECLARED_TYPES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ScorecardParseErrorsTest {

    @Test
    public void testErrorCount() throws IOException {
        final ScorecardCompiler scorecardCompiler = new ScorecardCompiler(INTERNAL_DECLARED_TYPES);
        try (final InputStream inputStream = PMMLDocumentTest.class.getResourceAsStream("/scoremodel_errors.xls")) {
            final boolean compileResult = scorecardCompiler.compileFromExcel(inputStream);
            assertFalse(compileResult);
            assertEquals(4, scorecardCompiler.getScorecardParseErrors().size());
            assertEquals("$C$4", scorecardCompiler.getScorecardParseErrors().get(0).getErrorLocation());
            assertEquals("Scorecard Package is missing", scorecardCompiler.getScorecardParseErrors().get(0).getErrorMessage());
        }
    }

    @Test
    public void testWrongData() throws IOException {
        final ScorecardCompiler scorecardCompiler = new ScorecardCompiler();
        try (final InputStream inputStream = PMMLDocumentTest.class.getResourceAsStream("/scoremodel_errors.xls")) {
            final boolean compileResult = scorecardCompiler.compileFromExcel(inputStream, "scorecards_wrongData");
            assertFalse(compileResult);
            assertEquals(4, scorecardCompiler.getScorecardParseErrors().size());
            assertEquals("$D$10", scorecardCompiler.getScorecardParseErrors().get(0).getErrorLocation());
            assertEquals("$D$19", scorecardCompiler.getScorecardParseErrors().get(1).getErrorLocation());
            assertEquals("$C$8", scorecardCompiler.getScorecardParseErrors().get(2).getErrorLocation());
            assertEquals("$C$28", scorecardCompiler.getScorecardParseErrors().get(3).getErrorLocation());
        }
    }

    @Test
    public void testMissingDataType() throws IOException {
        final ScorecardCompiler scorecardCompiler = new ScorecardCompiler(ScorecardCompiler.DrlType.INTERNAL_DECLARED_TYPES);
        try (final InputStream inputStream = PMMLDocumentTest.class.getResourceAsStream("/scoremodel_errors.xls")) {
            final boolean compileResult = scorecardCompiler.compileFromExcel(inputStream, "missingDataType");
            assertFalse(compileResult);
            assertEquals(2, scorecardCompiler.getScorecardParseErrors().size());
            assertEquals("$C$8", scorecardCompiler.getScorecardParseErrors().get(0).getErrorLocation());
            assertEquals("$C$16", scorecardCompiler.getScorecardParseErrors().get(1).getErrorLocation());
        }
    }

    @Test
    public void testMissingAttributes() throws IOException {
        final ScorecardCompiler scorecardCompiler = new ScorecardCompiler(ScorecardCompiler.DrlType.INTERNAL_DECLARED_TYPES);
        try (final InputStream inputStream = PMMLDocumentTest.class.getResourceAsStream("/scoremodel_errors.xls")) {
            final boolean compileResult = scorecardCompiler.compileFromExcel(inputStream, "incomplete_noAttr");
            assertFalse(compileResult);
        }
    }
}
