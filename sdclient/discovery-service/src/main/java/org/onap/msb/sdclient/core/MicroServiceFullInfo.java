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
import java.util.Set;

import com.google.common.base.Objects;


public class MicroServiceFullInfo extends Service<NodeInfo> implements Serializable {
    private static final long serialVersionUID = 1L;

    // 状态 0:不可用，待审核 1：可用,审核通过 2:审核失败
    private String status = "1";

    private boolean is_manual = false; // 手动注册：true; 自动注册：false

    private Set<NodeInfo> nodes;


    public Set<NodeInfo> getNodes() {
        return nodes;
    }

    public void setNodes(Set<NodeInfo> nodes) {
        this.nodes = nodes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public boolean isIs_manual() {
        return is_manual;
    }

    public void setIs_manual(boolean is_manual) {
        this.is_manual = is_manual;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getServiceName(), getVersion(), getProtocol(), getNamespace(), getUrl(),
                        getVisualRange(), nodes);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other instanceof MicroServiceFullInfo) {
            MicroServiceFullInfo that = (MicroServiceFullInfo) other;
            return Objects.equal(getServiceName(), that.getServiceName())
                            && Objects.equal(getVersion(), that.getVersion())
                            && Objects.equal(getProtocol(), that.getProtocol())
                            && Objects.equal(getNamespace(), that.getNamespace())
                            && Objects.equal(getUrl(), that.getUrl())
                            && Objects.equal(getVisualRange(), that.getVisualRange())
                            && Objects.equal(nodes.hashCode(), that.nodes.hashCode());
        } else {
            return false;
        }
    }



}
