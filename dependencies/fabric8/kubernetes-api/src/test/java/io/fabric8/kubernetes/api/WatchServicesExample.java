/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.fabric8.kubernetes.api;

import io.fabric8.kubernetes.api.model.Service;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class WatchServicesExample {

    public static void main(String... args) throws Exception {
        KubernetesClient kube = new KubernetesClient();
        System.out.println("Connecting to kubernetes on: " + kube.getAddress());
        WebSocketClient client = kube.watchServices("jimmi", null, new ExampleWatcher());
        Thread.sleep(10000l);
        client.stop();
    }

    static class ExampleWatcher extends AbstractWatcher<Service> {
        @Override
        public void eventReceived(Action action, Service object) {
            System.out.println(action + ": " + object);
        }
    }

}
