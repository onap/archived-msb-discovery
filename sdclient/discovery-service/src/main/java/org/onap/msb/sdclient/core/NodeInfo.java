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

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;

public class NodeInfo extends Node {

    private static final long serialVersionUID = 8955786461351557306L;
    
    private String nodeId; //node唯一标识
    
    private String status; //实例健康检查状态
    
  
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date expiration;
    
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date created_at;
    
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date updated_at;
    


    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(getIp(),getPort(),getHa_role(),status );
    }
    
    @Override
    public boolean equals(Object other)
    {
        if(this == other)
            return true;
        if(other instanceof NodeInfo)
        {
          NodeInfo that = (NodeInfo)other;
            return Objects.equal(getIp(), that.getIp()) 
                   && Objects.equal(getPort(), that.getPort());
        } else
        {
            return false;
        }
    }

  
    
}
