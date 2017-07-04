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
package org.onap.msb.sdclient;

import org.onap.msb.sdclient.resources.MicroServiceResource;
import org.onap.msb.sdclient.wrapper.ConsulClientApp;
import org.onap.msb.sdclient.wrapper.PublishAddressWrapper;
import org.onap.msb.sdclient.wrapper.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.dropwizard.Application;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;

public class DiscoverApp extends Application<DiscoverAppConfig> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DiscoverApp.class);

	public static void main(String[] args) throws Exception {
		new DiscoverApp().run(args);

	}
	
	private DiscoverAppConfig config;

	@Override
	public String getName() {
		return " MicroService Bus ";
	}

	@Override
	public void initialize(Bootstrap<DiscoverAppConfig> bootstrap) {
//		bootstrap.addBundle(new AssetsBundle("/iui-metrics",
//				"/iui/microservices/metrics", "index.html", "iui-metrics"));
//		bootstrap.addBundle(new AssetsBundle("/iui-discover",
//				"/iui/microservices", "index.html", "iui-microservices"));
//		bootstrap.addBundle(new AssetsBundle("/iui-discover", "/iui",
//				"index.html", "iui"));

		
	}

	@Override
	public void run(DiscoverAppConfig configuration, Environment environment) {
		
		environment.jersey().register(new MicroServiceResource());
		
		config=configuration;
		
		initSwaggerConfig(environment, configuration);
		
		ConfigUtil.getInstance().initConsulClientInfo(configuration);		
		
		initApiGateWayServiceListen();
		
		ConfigUtil.getInstance().initTCP_UDP_portRange();
		
		ConfigUtil.getInstance().initConsulRegisterMode(configuration);
	
	}

	

	private void initSwaggerConfig(Environment environment,
	                               DiscoverAppConfig configuration) {

		environment.jersey().register(new ApiListingResource());
		environment.getObjectMapper().setSerializationInclusion(
				JsonInclude.Include.NON_NULL);

		BeanConfig config = new BeanConfig();
		config.setTitle("Service Discovery RESTful API");
		config.setVersion("1.0.0");
		config.setResourcePackage("org.onap.msb.sdclient.resources");
		// 设置swagger里面访问rest api时的basepath
		SimpleServerFactory simpleServerFactory = (SimpleServerFactory) configuration
				.getServerFactory();
		// 必须以"/"开头，结尾可有可无"/"
		String basePath = simpleServerFactory.getApplicationContextPath();
		String rootPath = simpleServerFactory.getJerseyRootPath();

		rootPath = rootPath.substring(0, rootPath.indexOf("/*"));

		basePath = basePath.equals("/") ? rootPath : (new StringBuilder())
				.append(basePath).append(rootPath).toString();

		LOGGER.info("getApplicationContextPath： " + basePath);
		config.setBasePath(basePath);
		config.setScan(true);
	}

	
	
 
	
	
	
	
	
	/** 
	* @Title initApiGateWayServiceListen 
	* @Description TODO(开启对consul中ApiGateWay服务的监听和缓存)       
	* @return void    
	*/
	private void initApiGateWayServiceListen(){
	   
	    String[] consulAddress= ConfigUtil.getInstance().getConsulAddress().split(":");
	    ConsulClientApp consulClientApp = new ConsulClientApp(consulAddress[0],Integer.parseInt(consulAddress[1]));
	    
	    PublishAddressWrapper.getInstance().setConsulClientApp(consulClientApp);
        // 监听服务变化
        //consulClientApp.startHealthNodeListen(DiscoverUtil.APIGATEWAY_SERVINCE_ALL);
        //LOGGER.info("start monitor ApiGateWay service--" + DiscoverUtil.CONSUL_ADDRESSS+"--"+DiscoverUtil.APIGATEWAY_SERVINCE);
	       
	}
	
	
	

}
