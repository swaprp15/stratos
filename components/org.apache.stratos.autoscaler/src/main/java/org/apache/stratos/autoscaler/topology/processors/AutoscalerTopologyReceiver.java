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

package org.apache.stratos.autoscaler.topology.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.autoscaler.ClusterContext;
import org.apache.stratos.autoscaler.ClusterMonitor;
import org.apache.stratos.autoscaler.rule.AutoscalerRuleEvaluator;
import org.apache.stratos.autoscaler.util.AutoscalerUtil;
import org.apache.stratos.messaging.domain.topology.Cluster;
import org.apache.stratos.messaging.domain.topology.Member;
import org.apache.stratos.messaging.domain.topology.Service;
import org.apache.stratos.messaging.event.Event;
import org.apache.stratos.messaging.listener.topology.ClusterCreatedEventListener;
import org.apache.stratos.messaging.listener.topology.ClusterRemovedEventListener;
import org.apache.stratos.messaging.listener.topology.CompleteTopologyEventListener;
import org.apache.stratos.messaging.listener.topology.MemberActivatedEventListener;
import org.apache.stratos.messaging.listener.topology.ServiceRemovedEventListener;
import org.apache.stratos.messaging.event.topology.*;
import org.apache.stratos.messaging.message.processor.topology.TopologyEventProcessorChain;
import org.apache.stratos.messaging.message.receiver.topology.TopologyEventMessageDelegator;
import org.apache.stratos.messaging.message.receiver.topology.TopologyManager;

import java.util.Collection;

/**
 * Load balancer topology receiver.
 */
public class AutoscalerTopologyReceiver implements Runnable {

    private static final Log log = LogFactory.getLog(AutoscalerTopologyReceiver.class);

    private TopologyReceiver topologyReceiver;
    private boolean terminated;

    public AutoscalerTopologyReceiver() {
        this.topologyReceiver = new TopologyReceiver(createMessageDelegator());
    }

    @Override
    public void run() {
        Thread thread = new Thread(topologyReceiver);
        thread.start();
        if(log.isInfoEnabled()) {
            log.info("Load balancer topology receiver thread started");
        }

        // Keep the thread live until terminated
        while (!terminated);
        if(log.isInfoEnabled()) {
            log.info("Load balancer topology receiver thread terminated");
        }
    }

    private TopologyEventMessageDelegator createMessageDelegator() {
        TopologyEventProcessorChain processorChain = createEventProcessorChain();
        final TopologyEventMessageDelegator messageDelegator = new TopologyEventMessageDelegator(processorChain);
        messageDelegator.addCompleteTopologyEventListener(new CompleteTopologyEventListener() {
            @Override
            protected void onEvent(Event event) {
                try {
                    TopologyManager.acquireReadLock();
                    for(Service service : TopologyManager.getTopology().getServices()) {
                        for(Cluster cluster : service.getClusters()) {
                                addClusterToContext(cluster);
                        }
                    }
                }
                finally {
                    TopologyManager.releaseReadLock();
                }
                // Complete topology is only consumed once, remove listener
                messageDelegator.removeCompleteTopologyEventListener(this);
            }

        });
        return messageDelegator;
    }

    private TopologyEventProcessorChain createEventProcessorChain() {
        // Listen to topology events that affect clusters
        TopologyEventProcessorChain processorChain = new TopologyEventProcessorChain();
        processorChain.addEventListener(new ClusterCreatedEventListener() {
            @Override
            protected void onEvent(Event event) {
                try {
                    ClusterCreatedEvent e = (ClusterCreatedEvent) event;
                    TopologyManager.acquireReadLock();
                    Service service = TopologyManager.getTopology().getService(e.getServiceName());
                    Cluster cluster = service.getCluster(e.getClusterId());
                    addClusterToContext(cluster);
                }
                finally {
                    TopologyManager.releaseReadLock();
                }
            }

        });
        
        processorChain.addEventListener(new ClusterRemovedEventListener() {
            @Override
            protected void onEvent(Event event) {
                try {
                    ClusterRemovedEvent e = (ClusterRemovedEvent) event;
                    TopologyManager.acquireReadLock();
                    
                    removeClusterFromContext(e.getClusterId());
                }
                finally {
                    TopologyManager.releaseReadLock();
                }
            }

        });
        
        processorChain.addEventListener(new MemberActivatedEventListener() {
            @Override
            protected void onEvent(Event event) {
//                try {
//                    TopologyManager.acquireReadLock();
//
//                    // Add cluster to the context when its first member is activated
//                    MemberActivatedEvent memberActivatedEvent = (MemberActivatedEvent)event;
//                    Cluster cluster = findCluster(memberActivatedEvent.getClusterId());
//                    if(cluster == null) {
//                        if(log.isErrorEnabled()) {
//                            log.error(String.format("Cluster not found in topology: [cluster] %s", memberActivatedEvent.getClusterId()));
//                        }
//                    }
//                    addClusterToContext(cluster);
//                }
//                finally {
//                    TopologyManager.releaseReadLock();
//                }
            }
        });
        processorChain.addEventListener(new ServiceRemovedEventListener() {
            @Override
            protected void onEvent(Event event) {
//                try {
//                    TopologyManager.acquireReadLock();
//
//                    // Remove all clusters of given service from context
//                    ServiceRemovedEvent serviceRemovedEvent = (ServiceRemovedEvent)event;
//                    for(Service service : TopologyManager.getTopology().getServices()) {
//                        for(Cluster cluster : service.getClusters()) {
//                            removeClusterFromContext(cluster.getHostName());
//                        }
//                    }
//                }
//                finally {
//                    TopologyManager.releaseReadLock();
//                }
            }
        });
        return processorChain;
    }

    private void addClusterToContext(Cluster cluster) {
        ClusterContext ctxt = AutoscalerUtil.getClusterContext(cluster);
        AutoscalerRuleEvaluator ruleCtxt = AutoscalerRuleEvaluator.getInstance();
        ClusterMonitor monitor =
                                 new ClusterMonitor(cluster.getClusterId(), ctxt,
                                                    ruleCtxt.getStatefulSession());
        Thread th = new Thread(monitor);
        th.start();
        AutoscalerRuleEvaluator.getInstance().addMonitor(monitor);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Cluster monitor has been added: [cluster] %s",
                                    cluster.getClusterId()));
        }
    }

    private void removeClusterFromContext(String clusterId) {
        ClusterMonitor monitor = AutoscalerRuleEvaluator.getInstance().removeMonitor(clusterId);
        monitor.destroy();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Cluster monitor has been removed: [cluster] %s ", clusterId));
            }
    }

    private Cluster findCluster(String clusterId) {
        if(clusterId == null) {
            return null;
        }

        Collection<Service> services = TopologyManager.getTopology().getServices();
        for (Service service : services) {
            for (Cluster cluster : service.getClusters()) {
                if (clusterId.equals(cluster.getClusterId())) {
                    return cluster;
                }
            }
        }
        return null;
    }

    /**
     * Terminate load balancer topology receiver thread.
     */
    public void terminate() {
        topologyReceiver.terminate();
        terminated = true;
    }
}
