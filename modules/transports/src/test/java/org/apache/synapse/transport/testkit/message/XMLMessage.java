/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.transport.testkit.message;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

public class XMLMessage {
    public enum Type {
        SOAP11(SOAP11Constants.SOAP_11_CONTENT_TYPE),
        SOAP12(SOAP12Constants.SOAP_12_CONTENT_TYPE),
        POX("application/xml");
        
        private final String contentType;
        
        private Type(String contentType) {
            this.contentType = contentType;
        }
        
        public String getContentType() {
            return contentType;
        }
    }
    
    private final String contentType;
    private final Type type;
    private final OMElement payload;
    
    public XMLMessage(String contentType, OMElement payload, Type type) {
        this.contentType = contentType;
        this.payload = payload;
        this.type = type;
    }

    public String getContentType() {
        return contentType;
    }

    public Type getType() {
        return type;
    }

    public OMElement getPayload() {
        return payload;
    }
    
    public OMElement getMessageElement() {
        if (type == Type.POX) {
            return payload;
        } else {
            SOAPFactory factory;
            if (type == Type.SOAP12) {
                factory = OMAbstractFactory.getSOAP12Factory();
            } else {
                factory = OMAbstractFactory.getSOAP11Factory();
            }
            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            envelope.getBody().addChild(payload);
            return envelope;
        }
    }
}
