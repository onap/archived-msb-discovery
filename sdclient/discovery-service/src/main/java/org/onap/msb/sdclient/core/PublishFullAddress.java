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

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.onap.msb.sdclient.wrapper.consul.model.health.ServiceHealth;

import com.google.common.base.Objects;

public class PublishFullAddress implements Serializable {
  private static final long serialVersionUID = 1L;


  @ApiModelProperty(value = "Service Publish IP")
  private String ip;  

  @ApiModelProperty(value = "Service Publish Domain")
  private String domain;
  
  @ApiModelProperty(value = "Service Publish Port", required = true)
  private String port;
  
  @ApiModelProperty(value = "Service Publish URL,start with /",example = "/api/serviceName/v1", required = true)
  private String publish_url;
  
  @ApiModelProperty(value = "[visual Range]outSystem:0,inSystem:1", allowableValues = "0,1", example = "1", required = true)
  private String visualRange;
  
  @ApiModelProperty(value = "Service Publish Protocol",allowableValues = "http,https",example = "https", required = true)
  private String publish_protocol;

  public String getPublish_protocol() {
    return publish_protocol;
  }

  public void setPublish_protocol(String publish_protocol) {
    this.publish_protocol = publish_protocol;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getPublish_url() {
    return publish_url;
  }

  public void setPublish_url(String publish_url) {
    this.publish_url = publish_url;
  }

  public String getVisualRange() {
    return visualRange;
  }

  public void setVisualRange(String visualRange) {
    this.visualRange = visualRange;
  }

  public String getIp() {
      return ip;
  }

  public void setIp(String ip) {
      this.ip = ip;
  }

  public String getPort() {
      return port;
  }

  public void setPort(String port) {
      this.port = port;
  }
  
  public PublishFullAddress(){
    
  }
  
  public PublishFullAddress(String ip,String port,String publish_url,String visualRange,String publish_protocol ){
    this.ip=ip;
    this.port=port;
    this.publish_url=publish_url;
    this.visualRange=visualRange;
    this.publish_protocol=publish_protocol;
  }
  
  @Override
  public boolean equals(Object other)
  {
      if(this == other)
          return true;
      if(other instanceof PublishFullAddress)
      {
        PublishFullAddress that = (PublishFullAddress)other;
          return Objects.equal(ip, that.ip) 
                 && Objects.equal(domain, that.domain)
                 && Objects.equal(port, that.port)
                 && Objects.equal(publish_url, that.publish_url)
                 && Objects.equal(visualRange, that.visualRange)
                 && Objects.equal(publish_protocol, that.publish_protocol);
      } else
      {
          return false;
      }
  }
  
  @Override
  public int hashCode() {
      return Objects.hashCode(ip, domain,port,publish_url,visualRange,publish_protocol);
  }
  
  @Override
  public String toString() {
    // TODO Auto-generated method stub
    if(StringUtils.isNotBlank(this.domain)){
      return (new StringBuilder().append(this.publish_protocol).append("://").append(this.domain).append(":").append(this.port).append(this.publish_url)).toString();
    }
    else {
      return (new StringBuilder().append(this.publish_protocol).append("://").append(this.ip).append(":").append(this.port).append(this.publish_url)).toString();
    }

  }
  

}
