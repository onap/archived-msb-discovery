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
package org.onap.msb.sdclient.core;

import java.io.Serializable;
import java.util.List;

import org.onap.msb.sdclient.wrapper.util.DiscoverUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogNode implements Serializable{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("Node")
    private  String node=DiscoverUtil.EXTERNAL_NODE_NAME;
    
    @JsonProperty("Address")
    private  String address="127.0.0.1";
    
    @JsonProperty("Service")
    private ConsulService service;

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ConsulService getService() {
        return service;
    }

    public void setService(ConsulService service) {
        this.service = service;
    }
    
    public CatalogNode(){
      
    }
    
    public CatalogNode(ConsulService service){
       this.service = service;
    }
    

}


