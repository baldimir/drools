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
package org.drools.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.commands.impl.ContextImpl;
import org.drools.commands.runtime.ExecutionResultImpl;
import org.kie.api.KieBase;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.RequestContext;
import org.kie.internal.command.ContextManager;

public class RequestContextImpl extends ContextImpl implements RequestContext {

    private Context appContext;
    private Context conversationContext;

    private ConversationContextManager cvnManager;

    private Map<String, Object> output = new HashMap<>();

    private Object result;

    private String lastSet;

    private Exception exception;

    public RequestContextImpl() {
        register( ExecutionResults.class, new ExecutionResultImpl() );
    }

    public RequestContextImpl(long requestId, ContextManager ctxManager, ConversationContextManager cvnManager) {
        super(Long.toString(requestId), ctxManager);
        this.cvnManager = cvnManager;
        register( ExecutionResults.class, new ExecutionResultImpl() );
    }

    public Context getApplicationContext() {
        return appContext;
    }

    public void setApplicationContext(Context appContext) {
        this.appContext = appContext;
    }

    public Context getConversationContext() {
        return conversationContext;
    }

    public void setConversationContext(Context conversationContext ) {
        this.conversationContext = conversationContext;
    }

    public ConversationContextManager getConversationManager() {
        return cvnManager;
    }

    @Override
    public Object get(String identifier) {
        if(identifier == null || identifier.equals("")){
            return null;
        }

        Object object = null;

        if ( has(identifier)) {
            object = super.get( identifier );
        } else if (conversationContext != null && conversationContext.has(identifier)) {
            object = conversationContext.get( identifier );
        } else if (appContext != null && appContext.has(identifier)) {
            object = appContext.get( identifier );
        }

        return object;
    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public void setResult( Object result ) {
        this.result = result;
    }

    @Override
    public RequestContext with( KieBase kieBase ) {
        register( KieBase.class, kieBase );
        return this;
    }

    @Override
    public RequestContext with( KieSession kieSession ) {
        register( KieSession.class, kieSession );
        return this;
    }

    public String getLastSet() {
        return lastSet;
    }

    public void setLastSetOrGet(String lastSet) {
        this.lastSet = lastSet;
    }

    @Override
    public Map<String, Object> getOutputs() {
        return Collections.unmodifiableMap(output);
    }

    @Override
    public void setOutput(String identifier, Object value) {
        output.put(identifier, value);
    }

    @Override
    public void removeOutput(String identifier) {
        output.remove(identifier);
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "ContextImpl{" +
               "name='" + getName() + '\'' +
               '}';
    }
}
