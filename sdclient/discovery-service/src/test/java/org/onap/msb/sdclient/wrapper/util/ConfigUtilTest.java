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
package org.onap.msb.sdclient.wrapper.util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.msb.sdclient.DiscoverAppConfig;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DiscoverAppConfig.class,ConfigUtil.class})
@PowerMockIgnore( {"javax.management.*"})
public class ConfigUtilTest {

	 private static ConfigUtil configUtil =  ConfigUtil.getInstance();
	 private static DiscoverAppConfig discoverConfig;
	 
	 @BeforeClass
	    public static void setUpBeforeClass() throws Exception {
		 discoverConfig=PowerMockito.mock(DiscoverAppConfig.class);
	     PowerMockito.when(discoverConfig.getConsulAdderss()).thenReturn("127.0.0.1:8500");
	     PowerMockito.when(discoverConfig.getConsulRegisterMode()).thenReturn("catalog");

	 }
	 
	 @Test
	    public void testinitConsulClientInfo() {
		 configUtil.initConsulClientInfo(discoverConfig);
		 Assert.assertEquals("127.0.0.1:8500",configUtil.getConsulAddress());
		 
		 PowerMockito.mockStatic(System.class);        
	     PowerMockito.when(System.getenv("CONSUL_IP")).thenReturn("10.74.151.26");
	     configUtil.initConsulClientInfo(discoverConfig);
		 Assert.assertEquals("10.74.151.26:8500",configUtil.getConsulAddress());
		 
	 }
	 
	 @Test
	    public void testinitTCP_UDP_portRange() {
		  PowerMockito.mockStatic(System.class);        
	      PowerMockito.when(System.getenv("TCP_UDP_PORT_RANGE_START")).thenReturn("8500");
	      PowerMockito.when(System.getenv("TCP_UDP_PORT_RANGE_END")).thenReturn("8600");  
	      
	      configUtil.initTCP_UDP_portRange();
	      
	      Assert.assertEquals("8500",configUtil.getTcpudpPortRangeStart());
	      Assert.assertEquals("8600",configUtil.getTcpudpPortRangeEnd());
	 }
	 
	 @Test
	    public void testinitConsulRegisterMode() {
		 
		 configUtil.initConsulRegisterMode(discoverConfig);
		 Assert.assertEquals("catalog",configUtil.getConsulRegisterMode());
		 
		  PowerMockito.mockStatic(System.class);        
	      PowerMockito.when(System.getenv("CONSUL_REGISTER_MODE")).thenReturn("agent");
	      configUtil.initConsulRegisterMode(discoverConfig);
	      Assert.assertEquals("agent",configUtil.getConsulRegisterMode());
	 }
}
