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
package org.drools.commands.runtime.rule;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.drools.base.base.CoreComponentsBuilder;
import org.drools.core.common.DisconnectedFactHandle;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.command.Setter;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.command.RegistryContext;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ModifyCommand implements ExecutableCommand<Object> {

    /**
     * if this is true, modify can be any MVEL expressions. If false, it will only allow literal values.
     * (false should be use when taking input from an untrusted source, such as a web service).
     */
    @XmlAttribute(name="allow-modify-expr")
    public boolean ALLOW_MODIFY_EXPRESSIONS = true;

    @XmlElement(name="fact-handle", required=true)
    private DisconnectedFactHandle factHandle;

    // see getSetters()
    private List<Setter> setters;

    public ModifyCommand() {
        // JAXB Constructor
    }

    public ModifyCommand(FactHandle handle,
                         List<Setter> setters) {
        this.factHandle = DisconnectedFactHandle.newFrom( handle );
        setSetters(setters);
    }

    public FactHandle getFactHandle() {
        return this.factHandle;
    }

    public void setFactHandle(DisconnectedFactHandle factHandle) {
        this.factHandle = factHandle;
    }


    public void setFactHandleFromString(String factHandleId) {
        factHandle = new DisconnectedFactHandle(factHandleId);
    }

    public String getFactHandleFromString() {
        return factHandle.toExternalForm();
    }

    @XmlElement(type=SetterImpl.class)
    public List<Setter> getSetters() {
        if ( this.setters == null ) {
            this.setters = new ArrayList<>();
        }
        checkSetters();
        return this.setters;
    }

    public void setSetters(List<Setter> setters) {
        this.setters = setters;
        if( this.setters != null ) {
            checkSetters();
        }
    }

    private void checkSetters() {
        for( int i = 0; i < setters.size(); ++i ) {
           Setter setter = setters.get(i);
           if( ! (setters instanceof SetterImpl) ) {
              setters.set(i, new SetterImpl(setter.getAccessor(), setter.getValue()));
           }
        }
    }

    private String getMvelExpr() {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append( "with (this) {\n" );
        int i = 0;
        for ( Setter setter : getSetters() ) {
            if ( i++ > 0 ) {
                sbuilder.append( "," );
            }
            if (ALLOW_MODIFY_EXPRESSIONS) {
                sbuilder.append( setter.getAccessor() + " = '" + setter.getValue() + "'\n" );
            } else {
                sbuilder.append( setter.getAccessor() + " = '" + setter.getValue().replace("\"", "") + "'\n" );
            }
        }
        sbuilder.append( "}" );
        return sbuilder.toString();
    }

    public Object execute(Context context) {
        KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
        EntryPoint wmep = ksession.getEntryPoint( factHandle.getEntryPointName() );

        Object object = wmep.getObject( this.factHandle );
        CoreComponentsBuilder.get().getMVELExecutor().eval( getMvelExpr(), object );

        wmep.update( factHandle,
                        object );
        return object;
    }

    public String toString() {
        return "modify() " + getMvelExpr();
    }

    @XmlRootElement(name="setter")
    public static class SetterImpl implements Setter {
        @XmlAttribute
        private String accessor;
        @XmlAttribute
        private String value;

        public SetterImpl() {
            // JAXB Constructor
        }

        public SetterImpl(String accessor, String value) {
            this.accessor = accessor;
            this.value = value;
        }

        public String getAccessor() {
            return accessor;
        }

        public String getValue() {
            return value;
        }
    }
}
