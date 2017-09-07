/**
 * Copyright 2016-2017 ZTE, Inc. and others.
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

import com.google.common.base.Objects;

import io.swagger.annotations.ApiModelProperty;

public class PublishAddress implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Service Publish IP")
    private String ip;

    @ApiModelProperty(value = "Service Publish Port", required = true)
    private String port;

    @ApiModelProperty(value = "Service Publish URL,start with /", example = "/api/serviceName/v1", required = true)
    private String publish_url;

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

    public PublishAddress() {

    }

    public PublishAddress(String ip, String port, String publish_url) {
        this.ip = ip;
        this.port = port;
        this.publish_url = publish_url;
    }

    public String getPublish_url() {
        return publish_url;
    }

    public void setPublish_url(String publish_url) {
        this.publish_url = publish_url;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other instanceof PublishAddress) {
            PublishAddress that = (PublishAddress) other;
            return Objects.equal(ip, that.ip) && Objects.equal(port, that.port)
                            && Objects.equal(publish_url, that.publish_url);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ip, port, publish_url);
    }

    @Override
    public String toString() {
        return this.ip + ":" + this.port + this.publish_url;
    }

}
