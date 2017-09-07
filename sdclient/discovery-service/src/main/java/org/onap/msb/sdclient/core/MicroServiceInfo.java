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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @ClassName: MicroServiceInfo Bean
 * @Description:P2PRouteInfo信息实体类
 * @author 10188044
 * @date 2016-1-19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroServiceInfo extends Service<Node> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Set<Node> nodes;

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }



}
