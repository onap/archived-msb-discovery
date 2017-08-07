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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import com.google.common.base.Objects;

public class NodeAddress implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(required = true)
    private String ip;
    
    @ApiModelProperty(required = true)
    private String port;
    

    
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);  
    
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
    
    public NodeAddress(String ip,String port){
        this.ip=ip;
        this.port=port;
    }
    
    public void setIPandPort(String ip,String port){
        String oldAddress = this.ip+":"+this.port;
        String newAddress = ip+":"+port;
        this.ip=ip;
        this.port=port;   
        
        changes.firePropertyChange("ip", oldAddress, newAddress);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {  
        changes.addPropertyChangeListener(listener);  
     }  
    
 public void removePropertyChangeListener(PropertyChangeListener listener) {  
        changes.removePropertyChangeListener(listener);  
     }  
    
    public NodeAddress(){
       
    }
    
    @Override
    public boolean equals(Object other)
    {
        if(this == other)
            return true;
        if(other instanceof NodeAddress)
        {
            NodeAddress that = (NodeAddress)other;
            return Objects.equal(ip, that.ip) && Objects.equal(port, that.port);
        } else
        {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(ip, port);
    }
    
    @Override
    public String toString(){
        return this.ip+":"+this.port;
    }

   
    
    
   

}
