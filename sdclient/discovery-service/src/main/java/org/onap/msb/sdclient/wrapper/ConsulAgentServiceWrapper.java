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

import org.apache.commons.lang3.StringUtils;
import org.onap.msb.sdclient.core.AgentService;
import org.onap.msb.sdclient.core.Node;
import org.onap.msb.sdclient.wrapper.util.ConfigUtil;
import org.onap.msb.sdclient.wrapper.util.DiscoverUtil;
import org.onap.msb.sdclient.wrapper.util.HttpClientUtil;
import org.onap.msb.sdclient.wrapper.util.JacksonJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsulAgentServiceWrapper {

  private static ConsulAgentServiceWrapper instance = new ConsulAgentServiceWrapper();

  private ConsulAgentServiceWrapper() {}

  public static ConsulAgentServiceWrapper getInstance() {
    return instance;
  }
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulAgentServiceWrapper.class);
  

  public synchronized int  saveService(AgentService agentService) throws Exception{
    String consulRegisterurl =
        (new StringBuilder().append("http://").append(ConfigUtil.getInstance().getConsulAddress())
            .append(DiscoverUtil.CONSUL_AGENT_URL).append("/register")).toString();


    int registerResult =
        HttpClientUtil.httpPostWithJSON(consulRegisterurl,
            JacksonJsonUtil.beanToJson(agentService));
    
    return registerResult;
   
  }
  
  public synchronized int deleteService(String serviceId) throws Exception{
    String consulDelurl =
        (new StringBuilder().append("http://").append(ConfigUtil.getInstance().getConsulAddress())
            .append(DiscoverUtil.CONSUL_AGENT_URL).append("/deregister/").append(serviceId))
            .toString();

    int delResult = HttpClientUtil.httpPostWithJSON(consulDelurl, "");
    
    return delResult;
  }
  
}
