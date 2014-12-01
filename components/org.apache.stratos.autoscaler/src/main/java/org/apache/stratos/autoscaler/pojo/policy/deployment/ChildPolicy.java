/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.stratos.autoscaler.pojo.policy.deployment;

import org.apache.stratos.autoscaler.pojo.policy.deployment.partition.network.ChildLevelNetworkPartition;

import java.io.Serializable;

/**
 * This will keep the Children's policy in an application
 */
public class ChildPolicy implements Serializable {
    private String id;

    private ChildLevelNetworkPartition[] childLevelNetworkPartitions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChildLevelNetworkPartition[] getChildLevelNetworkPartitions() {
        return childLevelNetworkPartitions;
    }

    public void setChildLevelNetworkPartitions(ChildLevelNetworkPartition[] childLevelNetworkPartitions) {
        this.childLevelNetworkPartitions = childLevelNetworkPartitions;
    }

    public ChildLevelNetworkPartition getChildLevelNetworkPartition(String id) {
        for(ChildLevelNetworkPartition partition : childLevelNetworkPartitions) {
            if(partition.getId().equals(id)) {
                return partition;
            }
        }

        return null;
    }
}
