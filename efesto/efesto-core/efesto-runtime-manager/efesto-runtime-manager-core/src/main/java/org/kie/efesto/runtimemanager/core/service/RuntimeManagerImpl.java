/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.efesto.runtimemanager.core.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.api.service.RuntimeManager;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.efesto.runtimemanager.api.utils.SPIUtils.getKieRuntimeService;

public class RuntimeManagerImpl implements RuntimeManager {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeManagerImpl.class.getName());

    @Override
    @SuppressWarnings({"unchecked", "raw"})
    public Collection<EfestoOutput> evaluateInput(KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader, EfestoInput... toEvaluate) {
        final Collection<EfestoOutput> toReturn = new LinkedHashSet<>();
        Arrays.stream(toEvaluate)
                .forEach(input -> {
                    Optional<EfestoOutput> output = getOptionalOutput(memoryCompilerClassLoader, input);
                    output.ifPresent(toReturn::add);
                });
        return toReturn;
    }

    private Optional<EfestoOutput> getOptionalOutput(KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader, EfestoInput input) {
        Optional<KieRuntimeService> retrieved = getKieRuntimeService(input, false,
                                                                     memoryCompilerClassLoader);
        if (!retrieved.isPresent()) {
            logger.warn("Cannot find KieRuntimeService for {}", input.getFRI());
            return Optional.empty();
        }
        return retrieved.flatMap(kieRuntimeService -> kieRuntimeService.evaluateInput(input,
                                                                                      memoryCompilerClassLoader));
    }
}