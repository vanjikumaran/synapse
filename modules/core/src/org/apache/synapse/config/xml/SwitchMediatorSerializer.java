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
import org.apache.synapse.mediators.filters.SwitchCaseMediator;
import org.apache.synapse.mediators.filters.SwitchMediator;

import java.util.Iterator;

/**
 * <pre>
 * &lt;switch source="xpath"&gt;
 *   &lt;case regex="string"&gt;
 *     mediator+
 *   &lt;/case&gt;+
 *   &lt;default&gt;
 *     mediator+
 *   &lt;/default&gt;?
 * &lt;/switch&gt;
 * </pre>
 */
public class SwitchMediatorSerializer extends AbstractMediatorSerializer
    implements MediatorSerializer {

    private static final Log log = LogFactory.getLog(SwitchMediatorSerializer.class);

    public OMElement serializeMediator(OMElement parent, Mediator m) {

        if (!(m instanceof SwitchMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        SwitchMediator mediator = (SwitchMediator) m;
        OMElement switchMed = fac.createOMElement("switch", synNS);

        if (mediator.getSource() != null) {
            switchMed.addAttribute(fac.createOMAttribute(
                "source", nullNS, mediator.getSource().toString()));
            super.serializeNamespaces(switchMed, mediator.getSource());

        } else {
            handleException("Invalid switch mediator. Source required");
        }

        Iterator iter = mediator.getCases().iterator();
        SwitchCaseMediatorSerializer swcms = new SwitchCaseMediatorSerializer();
        while (iter.hasNext()) {
            swcms.serializeMediator(switchMed, (SwitchCaseMediator) iter.next());
        }

        if (parent != null) {
            parent.addChild(switchMed);
        }
        return switchMed;
    }

    public String getMediatorClassName() {
        return SwitchMediator.class.getName();
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}
