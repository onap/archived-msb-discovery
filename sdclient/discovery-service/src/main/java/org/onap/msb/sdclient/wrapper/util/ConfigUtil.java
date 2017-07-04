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
package org.onap.msb.sdclient.wrapper.util;

import org.apache.commons.lang3.StringUtils;
import org.onap.msb.sdclient.DiscoverAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: ConfigUtil
 * @Description: TODO(服务发现配置项工具类)
 * @author tanghua10186366
 * @date 2017年1月23日
 * 
 */
public class ConfigUtil {

  private static ConfigUtil instance = new ConfigUtil();


  private ConfigUtil() {}

  public static ConfigUtil getInstance() {
    return instance;
  }


  private String tcpudpPortRangeStart= DiscoverUtil.TCP_UDP_PORT_RANGE_START;

  private String tcpudpPortRangeEnd=DiscoverUtil.TCP_UDP_PORT_RANGE_END;

  private String consulAddress = DiscoverUtil.CONSUL_ADDRESSS;

  private String consulRegisterMode=DiscoverUtil.CONSUL_REGISTER_MODE;

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

  public void initConsulClientInfo(DiscoverAppConfig config) {


    String env_CONSUL_IP = System.getenv("CONSUL_IP");


    if (StringUtils.isNotBlank(env_CONSUL_IP)) {
      String consul_port = DiscoverUtil.CONSUL_DEFAULT_PORT;
      try {
        consul_port = config.getConsulAdderss().split(":")[1];
      } catch (Exception e) {
        LOGGER.error("initConsulClientInfo throw err:" + e.getMessage());
      }

      consulAddress = env_CONSUL_IP + ":" + consul_port;
    } else if (StringUtils.isNotBlank(config.getConsulAdderss())) {
      {
        consulAddress = config.getConsulAdderss();
      }


      LOGGER.info("init Discover CONSUL ADDRESSS:" + consulAddress);


    }
  }
  
  public void initTCP_UDP_portRange(){
    
    String env_TCP_UDP_PORT_RANGE_START=System.getenv("TCP_UDP_PORT_RANGE_START");
    String env_TCP_UDP_PORT_RANGE_END=System.getenv("TCP_UDP_PORT_RANGE_END");
    
    
    if(StringUtils.isNotBlank(env_TCP_UDP_PORT_RANGE_START))
    {
      tcpudpPortRangeStart=env_TCP_UDP_PORT_RANGE_START;
    }
   
    
    if(StringUtils.isNotBlank(env_TCP_UDP_PORT_RANGE_END))
    {
      tcpudpPortRangeEnd=env_TCP_UDP_PORT_RANGE_END;
    }
   
    LOGGER.info("init TCP_UDP portRange:"+ tcpudpPortRangeStart+"-"+tcpudpPortRangeEnd);
    
  }
  
  public void initConsulRegisterMode(DiscoverAppConfig config){
    
    String env_CONSUL_REGISTER_MODE=System.getenv("CONSUL_REGISTER_MODE");
    
    if(StringUtils.isNotBlank(env_CONSUL_REGISTER_MODE))
    {
      consulRegisterMode=env_CONSUL_REGISTER_MODE;
    }
    else{
      if(StringUtils.isNotBlank(config.getConsulRegisterMode())){
        consulRegisterMode=config.getConsulRegisterMode();
      }
    }
    
    LOGGER.info("init Consul Register Mode:"+consulRegisterMode);
    
  }
  


  public String getConsulAddress() {
    return consulAddress;
  }
  
  public String getTcpudpPortRangeStart() {
    return tcpudpPortRangeStart;
  }


  public String getTcpudpPortRangeEnd() {
    return tcpudpPortRangeEnd;
  }


  public String getConsulRegisterMode() {
    return consulRegisterMode;
  }

  
  
}
