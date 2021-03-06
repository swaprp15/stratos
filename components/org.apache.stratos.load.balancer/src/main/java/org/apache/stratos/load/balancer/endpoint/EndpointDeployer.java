/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.load.balancer.endpoint;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;

import java.util.Properties;

/**
 * Responsible for deploying Synapse artifacts.
 */
public class EndpointDeployer extends org.apache.synapse.deployers.EndpointDeployer {

    MediationPersistenceManager mpm;

    @Override
    public void init(ConfigurationContext configCtx) {
        super.init(configCtx);
        this.mpm = ServiceBusUtils.getMediationPersistenceManager(configCtx.getAxisConfiguration());
    }

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {
        String epName = super.deploySynapseArtifact(artifactConfig, fileName, properties);
        if (mpm != null) {
            mpm.saveItemToRegistry(epName, ServiceBusConstants.ITEM_TYPE_ENDPOINT);
        }
        return epName;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {
        String epName = super.updateSynapseArtifact(
                artifactConfig, fileName, existingArtifactName, properties);
        if (mpm != null) {
            mpm.saveItemToRegistry(epName, ServiceBusConstants.ITEM_TYPE_ENDPOINT);
        }
        return epName;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {
        super.undeploySynapseArtifact(artifactName);
        if (mpm != null) {
            mpm.deleteItemFromRegistry(artifactName, ServiceBusConstants.ITEM_TYPE_ENDPOINT);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {
        super.restoreSynapseArtifact(artifactName);
        if (mpm != null) {
            mpm.saveItemToRegistry(artifactName, ServiceBusConstants.ITEM_TYPE_ENDPOINT);
        }
    }
}
