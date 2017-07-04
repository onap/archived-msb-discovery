/**
 * Copyright 2016 ZTE, Inc. and others.
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
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModelProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Service<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    // 服务名
    @ApiModelProperty(example = "test",required = true)
    private String serviceName;
    // 版本号
    @ApiModelProperty(example = "v1", required = true)
    private String version="";
    // 服务url
    @ApiModelProperty(value = "Target Service URL,start with /",example = "/api/serviceName/v1", required = true)
    private String url="";
    
    // 服务对应协议，比如REST、UI、MQ、FTP、SNMP、TCP、UDP    
    @ApiModelProperty(value = "Service Protocol", allowableValues = "REST,UI, HTTP, TCP,UDP", example = "HTTP",required = true)
    private String protocol = "";
    
    //服务的可见范围   0:系统间   1:系统内  ,可配置多个，以 |分隔
    @ApiModelProperty(value = "[visual Range]interSystem:0,inSystem:1", allowableValues = "0,1", example = "1")
    private String visualRange = "1";
   
    //负载均衡策略类型
    @ApiModelProperty(value = "lb policy", allowableValues = "round-robin,ip_hash", example = "ip_hash")
    private String lb_policy;
    
    //TCP/UDP协议监听端口
    @ApiModelProperty(hidden = true)
    private String publish_port;
    
    //命名空间
    private String namespace="";

    //网络平面
    @ApiModelProperty(hidden = true)
    private String network_plane_type;
    
    @ApiModelProperty(hidden = true)
    private String host="";
    
    @ApiModelProperty(hidden = true)
    private String path="";

  


    @ApiModelProperty(required = true)
    private Set<T> nodes;
    
    //服务自身属性的键值对
    private List<KeyVaulePair> metadata;
    
    //自定义标签
    @ApiModelProperty(value = "custom labels", example = "key1:value1")
    private List<String> labels;
    


    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }
    
    public List<String> getLabels() {
      return labels;
    }

    public void setLabels(List<String> labels) {
      this.labels = labels;
    }

    public Set<T> getNodes() {
        return nodes;
    }

    public void setNodes(Set<T> nodes) {
        this.nodes = nodes;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
   
   

    public List<KeyVaulePair> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<KeyVaulePair> metadata) {
        this.metadata = metadata;
    }
    

    public String getVisualRange() {
        return visualRange;
    }

    public void setVisualRange(String visualRange) {
        this.visualRange = visualRange;
    }

    public String getLb_policy() {
        return lb_policy;
    }

    public void setLb_policy(String lb_policy) {
        this.lb_policy = lb_policy;
    }

    public String getPublish_port() {
        return publish_port;
    }

    public void setPublish_port(String publish_port) {
        this.publish_port = publish_port;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNetwork_plane_type() {
      return network_plane_type;
    }

    public void setNetwork_plane_type(String network_plane_type) {
      this.network_plane_type = network_plane_type;
    }
}
