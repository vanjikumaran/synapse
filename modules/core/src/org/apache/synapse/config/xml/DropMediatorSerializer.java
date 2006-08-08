/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.builtin.DropMediator;

public class DropMediatorSerializer extends AbstractMediatorSerializer
    implements MediatorSerializer {

    private static final Log log = LogFactory.getLog(DropMediatorSerializer.class);

    public OMElement serializeMediator(OMElement parent, Mediator m) {

        if (!(m instanceof DropMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        DropMediator mediator = (DropMediator) m;
        OMElement drop = fac.createOMElement("drop", synNS);

        if (parent != null) {
            parent.addChild(drop);
        }
        return drop;
    }

    public String getMediatorClassName() {
        return DropMediator.class.getName();
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
