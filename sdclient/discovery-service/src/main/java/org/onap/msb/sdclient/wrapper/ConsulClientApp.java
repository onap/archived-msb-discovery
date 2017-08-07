/**
 * Copyright 2016-2017 ZTE, Inc. and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.msb.sdclient.wrapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onap.msb.sdclient.core.KeyVaulePair;
import org.onap.msb.sdclient.core.MicroServiceFullInfo;
import org.onap.msb.sdclient.core.NodeInfo;
import org.onap.msb.sdclient.wrapper.consul.Consul;
import org.onap.msb.sdclient.wrapper.consul.HealthClient;
import org.onap.msb.sdclient.wrapper.consul.cache.HealthCache;
import org.onap.msb.sdclient.wrapper.consul.model.health.Service;
import org.onap.msb.sdclient.wrapper.consul.model.health.ServiceHealth;
import org.onap.msb.sdclient.wrapper.util.DiscoverUtil;
import org.onap.msb.sdclient.wrapper.util.JacksonJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulClientApp {

    private final Consul consul;
    private final HealthClient healthClient;
//    private AtomicReference<List<HealthCache>> cacheList = new AtomicReference<List<HealthCache>>(
//            new ArrayList<HealthCache>());


    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulClientApp.class);

    public ConsulClientApp(String ip, int port) {
        URL url = null;
        try {
            url = new URL("http", ip, port, "");
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            LOGGER.error("start  ConsulClientApp throw exception", e1);
            throw new RuntimeException(e1);
        }
        this.consul = Consul.builder().withUrl(url).build(); // connect to Consul on localhost
        this.healthClient = consul.healthClient();
    }

    public Consul getConsul() {
        return consul;
    }



   

    /**
     * @Title startHealthNodeListen
     * @Description TODO(开启某个服务的node变化监听,只返回健康状态服务)
     * @param serviceName
     * @return
     * @return HealthCache
     */
    public HealthCache startHealthNodeListen(final String serviceName) {
        final HealthCache healthCache = HealthCache.newCache(healthClient, serviceName, 30);
        healthCache.addListener(new HealthCache.Listener<String, ServiceHealth>() {
            @Override
            public void notify(Map<String, ServiceHealth> newValues) {
                // do Something with updated server map
                LOGGER.info(serviceName + "--new node notify--");
                
                             
                if (newValues.isEmpty()) {
                    LOGGER.warn(serviceName + "--nodeList is Empty--");
                    PublishAddressWrapper.publishApigateWayList.remove(serviceName);
                   
                      try {
                        healthCache.stop();
                        LOGGER.info(serviceName + " Node Listen stopped");
                      } catch (Exception e) {
                        LOGGER.error(serviceName + " Node Listen stop throw exception", e);
                      }
                    
                    return;
                } 
                //服务发现变化
                List<MicroServiceFullInfo> nodeAddressList=new ArrayList<MicroServiceFullInfo>(); 
                for (Map.Entry<String, ServiceHealth> entry : newValues.entrySet()) {

                    MicroServiceFullInfo microServiceInfo = new MicroServiceFullInfo();
                    
                    ServiceHealth value = (ServiceHealth) entry.getValue();
                    Service service = value.getService();
                    
                    NodeInfo node = new NodeInfo();
                    node.setIp(service.getAddress());
                    node.setPort(String.valueOf(service.getPort()));
                    Set<NodeInfo> nodes = new HashSet<NodeInfo>();
                    nodes.add(node);
                    microServiceInfo.setNodes(nodes);


                    microServiceInfo.setServiceName(serviceName);
                    
                    try {
                      List<String> tagList = service.getTags();


                      for (String tag : tagList) {

                        if (tag.startsWith("\"ns\"")) {
                          String ms_ns_json = tag.split("\"ns\":")[1];
                          Map<String, String> nsMap =
                              (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_ns_json, Map.class);

                          if (nsMap.get("namespace") != null) {
                            microServiceInfo.setNamespace(nsMap.get("namespace"));
                          }

                          continue;
                        }

                        if (tag.startsWith("\"labels\"")) {
                          String ms_labels_json = "{"+tag.split("\"labels\":\\{")[1];
                          Map<String, String> labelMap  =
                              (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_labels_json, Map.class);

                          List<String> nodeLabels = new ArrayList<String>();
                          for (Map.Entry<String, String> labelEntry : labelMap.entrySet()) {
                            if ("visualRange".equals(labelEntry.getKey())) {
                              microServiceInfo.setVisualRange(labelEntry.getValue());
                            } else if ("network_plane_type".equals(labelEntry.getKey())) {
                              microServiceInfo.setNetwork_plane_type( labelEntry.getValue());
                            } else {
                              nodeLabels.add(labelEntry.getKey() + ":" + labelEntry.getValue());
                            }

                          }
                          
                          microServiceInfo.setLabels(nodeLabels);
                          continue;
                        }
                        
                        if (tag.startsWith("\"metadata\"")) {
                          String ms_metadata_json = "{"+tag.split("\"metadata\":\\{")[1];
                          Map<String, String> metadataMap =
                              (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_metadata_json, Map.class);

                          List<KeyVaulePair> ms_metadata = new ArrayList<KeyVaulePair>();
                       

                          for (Map.Entry<String, String> metadataEntry : metadataMap.entrySet()) {
                            KeyVaulePair keyVaulePair = new KeyVaulePair();
                            keyVaulePair.setKey(metadataEntry.getKey());
                            keyVaulePair.setValue(metadataEntry.getValue());
                            ms_metadata.add(keyVaulePair);
                          }
                          microServiceInfo.setMetadata(ms_metadata);
                          continue;
                        }
                    
                      }


                    } catch (Exception e) {
                      LOGGER.error(serviceName + " read tag  throw exception", e);
                    }
                   
                    nodeAddressList.add(microServiceInfo);
                }
                
                PublishAddressWrapper.publishApigateWayList.put(serviceName, nodeAddressList);
             
            }
        });
        try {
            LOGGER.info(serviceName + " Node Listen start");
//            cacheList.get().add(healthCache);
            healthCache.start();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOGGER.error(serviceName + " Node Listen start throw exception", e);
        }

        return healthCache;
    }

  
    
    public static void main(String[] args) {
        ConsulClientApp consulTest = new ConsulClientApp("127.0.0.1", 8500);
        // 监听服务变化
        consulTest.startHealthNodeListen("apigateway");


    }


}
