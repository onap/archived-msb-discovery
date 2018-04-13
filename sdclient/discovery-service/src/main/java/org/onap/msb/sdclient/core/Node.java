/**
 * Copyright 2016-2018 ZTE, Inc. and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onap.msb.sdclient.core;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Node implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(example = "127.0.0.1", required = true)
    private String ip;

    @ApiModelProperty(example = "80", required = true)
    private String port;


    // loadbalance policy parameter
    @ApiModelProperty(value = "lb node params", allowableValues = "weight,max_fails,fail_timeout",
                    example = "weight=5,max_fails=3,fail_timeout=30s")
    private String lb_server_params;

    // health check parameter
    @ApiModelProperty(value = "health check type", allowableValues = "TTL,HTTP,TCP", example = "TTL")
    private String checkType = "";

    @ApiModelProperty(value = "health check URL,applies only to TCP or HTTP", example = "http://localhost:5000/health")
    private String checkUrl = "";

    @ApiModelProperty(value = "TCP or HTTP health check Interval,Unit: second", example = "10s")
    private String checkInterval;

    @ApiModelProperty(value = "TCP or HTTP health check TimeOut,Unit: second", example = "10s")
    private String checkTimeOut;

    @ApiModelProperty(value = "TTL health check Interval,Unit: second", example = "10s")
    private String ttl;
	
    @ApiModelProperty(value = "health check skip TLS verify, applies only to HTTPs", allowableValues = "true, false")
	private Boolean tls_skip_verify = true;

    @ApiModelProperty(value = "Instance HA_role", allowableValues = "active,standby", example = "active")
    private String ha_role = "";



    public String getHa_role() {
        return ha_role;
    }

    public void setHa_role(String ha_role) {
        this.ha_role = ha_role;
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

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public Node() {

    }

    public Node(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public String getLb_server_params() {
        return lb_server_params;
    }

    public void setLb_server_params(String lb_server_params) {
        this.lb_server_params = lb_server_params;
    }


    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getCheckUrl() {
        return checkUrl;
    }

    public void setCheckUrl(String checkUrl) {
        this.checkUrl = checkUrl;
    }

    public String getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(String checkInterval) {
        this.checkInterval = checkInterval;
    }

    public String getCheckTimeOut() {
        return checkTimeOut;
    }

    public void setCheckTimeOut(String checkTimeOut) {
        this.checkTimeOut = checkTimeOut;
    }

    public Boolean getTls_skip_verify() {
        return tls_skip_verify;
    }

    public void setTls_skip_verify(Boolean tls_skip_verify) {
        this.tls_skip_verify = tls_skip_verify;
    }



}
