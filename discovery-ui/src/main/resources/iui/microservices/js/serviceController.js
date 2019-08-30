/*
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

var table;
var vm = avalon
		.define({
			$id : "serviceController",
			$apiGatewayServiceName:"apigateway",
			ifshowApiGatewayService:false,
			tcp_udp_port_range:["40001","40100"],
			apiGatewayService:[],
			apiGatewayService_all:[],
			routeService:[],
			routeService_all:[],
			server_rtn:{
				info_block:false,
				warning_block:false,
				rtn_info:""
			},
			node_rtn:{
				info_block:false,
				warning_block:false,
				rtn_info:""
			},
			if_publish_port:[],
			if_http_publish_port:[],
			$msbProtocol :["REST","HTTP","UI","MQ","FTP","SNMP","TCP","UDP"],
			$healthCheckType:["","TTL","HTTP","TCP"],
			$ha_role_Type:["","active","standby"],
			msbLbPolicyRange:[
			{
					name:$.i18n.prop("org_onap_msb_discover_lbpolicy_round"),
					value:"round-robin"
				},
			  {
					name:"IPHash",
					value:"ip_hash"
				},
				/*{
					name:$.i18n.prop("org_onap_msb_discover_lbpolicy_leastconn"),
					value:"least_conn"
				},*/
				{
					name:$.i18n.prop("org_onap_msb_discover_lbpolicy_clientcustom"),
					value:"client_custom"
				}	
			],
						
			$msbRouteUrl:apiBasePath+'/services?is_manual=true',	
			$msbRouteInstanceUrl :apiBasePath+'/services/{serviceName}/version/{version}',	
			$msbRouteNodesUrl :apiBasePath+'/services/{serviceName}/version/{version}/nodes',
			$apiGatewayInfoUrl :apiBasePath+'/services/apigatewayserviceinfo',
			$tcpudpportrangeUrl:apiBasePath+'/services/tcpudpportrange',
			publishUrl:{
				publishUrl4SysOut:"",
				publishUrl4SysIn:"",
				ifShowPublishUrl4SysOut:false,
				ifShowPublishUrl4SysIn:false

			},	
			
			targetServiceHost:"",
			msbRouteArray :  [],			
			msbRouteInfo : {	
			    oldServiceName:"",	
			    oldVersion:"",	
			    oldPublic_port:"",			
				serviceName : "",
				version:"",
				status:"0",
				nodes:[],	
				url:"",
				protocol:"",
				visualRange:"",
				visualRangeArray:[],
				lb_policy:"",
				publish_port:"",
				namespace:"",
				network_plane_type:"",
				host:"",
				path:"",
				enable_ssl:[]
			},
			nodeInfo:{
				ip:"",
				port:"",
				weight:"",
				max_fails:"",
				fail_timeout:"",
				checkType:"",
				checkUrl:"",
				checkInterval:"",
				checkTimeOut:"",
				ttl:"",
				ha_role:""
			},
			updateServiceInfo:{
				serviceName : "",
				version:"",
				namespace:"",
				authtype:""

			},
			oldNodeInfo:{
				ip:"",
				port:""
			},
			nodeDlgInfo:{
				titleName:"",
				saveType:""
			},
			pageInfo:{
				pageType:"add",	
				pageTitle:"",	

			},														
			initMSBRoute:function(){
				vm.initIUIfori18n();

				var url= window.location.search.substr(1);
		 		vm.pageInfo.pageType=routeUtil.getQueryString(url,"type");

		 		

		 		if(vm.pageInfo.pageType=="add"){
			        vm.addMsbRoute();
			        vm.getApigatewayInfo("all");
			        vm.getApigatewayInfo("");
			        vm.pageInfo.pageTitle=$.i18n.prop("org_onap_msb_discover_form_title_add");

			     }
			     else{
			     	vm.updateServiceInfo.serviceName=routeUtil.getQueryString(url,"serviceName");
		 			vm.updateServiceInfo.version=routeUtil.getQueryString(url,"version");
		 			vm.updateServiceInfo.authtype=routeUtil.getQueryString(url,"authtype");
		 			vm.updateServiceInfo.namespace=routeUtil.getQueryString(url,"namespace");

		 			vm.initUpdateRoute(vm.updateServiceInfo.serviceName,vm.updateServiceInfo.version,vm.updateServiceInfo.namespace); 	
				    vm.getApigatewayInfo("all");
				    vm.getApigatewayInfo(vm.updateServiceInfo.namespace);



				      if(vm.pageInfo.pageType=="update"){
				     	vm.pageInfo.pageTitle=$.i18n.prop("org_onap_msb_discover_form_title_update");			       

				     }
				     else if(vm.pageInfo.pageType=="view"){
				     	vm.pageInfo.pageTitle=$.i18n.prop("org_onap_msb_discover_form_title_view");			      

				     }
			     }

			     vm.getTcpUdpPortRangeUrl();
       
	           
			},
			
        	returnList:function(){
        	
        		window.history.back(-1); 
        	},
        	closePage:function(){
        	
        		window.close(); 
        	},
        	
        	addMsbRoute:function(){

        		vm.msbRouteInfo.serviceName="";	
				vm.msbRouteInfo.version="";
				vm.msbRouteInfo.url="";
				vm.msbRouteInfo.protocol="";
				vm.msbRouteInfo.publish_port="";
				vm.msbRouteInfo.lb_policy="";
				vm.msbRouteInfo.visualRange="";
				vm.msbRouteInfo.visualRangeArray=[],
				vm.msbRouteInfo.nodes=[];					
				vm.msbRouteInfo.status="1";	
				vm.msbRouteInfo.namespace="";
				vm.msbRouteInfo.network_plane_type="";
				vm.msbRouteInfo.host="";
				vm.msbRouteInfo.path="";
				vm.msbRouteInfo.enable_ssl=[];
			
				
				vm.server_rtn.warning_block=false;
				vm.server_rtn.info_block=false;

				$('#labels').val("");
				$('#labels').on('tokenfield:createdtoken', function (e) {
							    var re = /\S+:\S+/
							    var valid = re.test(e.attrs.value)
							    if (!valid) {
							      $(e.relatedTarget).addClass('invalid')
							        }
							    }).tokenfield();
				$('#metadata').val("");
				$('#metadata').on('tokenfield:createdtoken', function (e) {
							    var re = /\S+:\S+/
							    var valid = re.test(e.attrs.value)
							    if (!valid) {
							      $(e.relatedTarget).addClass('invalid')
							        }
							    }).tokenfield();


				$(".form-group").each(function () {
						$(this).removeClass('has-success');
						$(this).removeClass('has-error');
						$(this).find(".help-block[id]").remove();
						$(this).find(".form-tip").removeClass('form-input-focus');
  						//$(this).find(".item-tip").removeClass('item-tip-focus');
					});

        	},
        	initUpdateRoute:function(serviceName,version,namespace){
        		var url= vm.$msbRouteInstanceUrl;
				    var version2=version==""?"null":version;
				    var serviceName2= serviceName.replace(/\//g,"*");
                        url=url.replace("{serviceName}",serviceName2).replace("{version}",version2)+"?ifPassStatus=false&namespace="+namespace;
	
        		$.ajax({
	                "type": 'get',
	                "url":  url,
	                "dataType": "json",
	                success: function (msbRouteInfo) {  
	                        //vm.msbRouteInfo.serviceName=(msbRouteInfo.protocol=="UI"?msbRouteInfo.serviceName.replace("IUI_",""):msbRouteInfo.serviceName);
							vm.msbRouteInfo.serviceName=msbRouteInfo.serviceName;
							vm.msbRouteInfo.oldServiceName=msbRouteInfo.serviceName;
							vm.msbRouteInfo.version= msbRouteInfo.version;
							vm.msbRouteInfo.oldVersion= msbRouteInfo.version
							vm.msbRouteInfo.url=msbRouteInfo.url;
							vm.msbRouteInfo.protocol=msbRouteInfo.protocol;
							vm.msbRouteInfo.visualRange=msbRouteInfo.visualRange;
							vm.msbRouteInfo.visualRangeArray=msbRouteInfo.visualRange.split("|");
							vm.msbRouteInfo.lb_policy=msbRouteInfo.lb_policy==""?"round-robin":msbRouteInfo.lb_policy;
							vm.msbRouteInfo.namespace=msbRouteInfo.namespace;
							vm.msbRouteInfo.network_plane_type=msbRouteInfo.network_plane_type;
							vm.msbRouteInfo.host=msbRouteInfo.host;
							vm.msbRouteInfo.path=msbRouteInfo.path;
							if (msbRouteInfo.enable_ssl == true){
								vm.msbRouteInfo.enable_ssl=["true"];
							}else{
								vm.msbRouteInfo.enable_ssl=[];
							}
													
							  $("#labels").val(msbRouteInfo.labels);
							  $('#labels').on('tokenfield:createdtoken', function (e) {
							    var re = /\S+:\S+/
							    var valid = re.test(e.attrs.value)
							    if (!valid) {
							      $(e.relatedTarget).addClass('invalid')
							        }
							    }).tokenfield();


							  var metadataArray=msbRouteInfo.metadata;
							  var metadatas=[];
							  for(var i=0;i<metadataArray.length;i++){
							  	metadatas.push(metadataArray[i].key+":"+metadataArray[i].value);

							  }

							  $("#metadata").val(metadatas);
							  $('#metadata').on('tokenfield:createdtoken', function (e) {
							    var re = /\S+:\S+/
							    var valid = re.test(e.attrs.value)
							    if (!valid) {
							      $(e.relatedTarget).addClass('invalid')
							        }
							    }).tokenfield();

	                      
	                      if(msbRouteInfo.publish_port!=undefined && msbRouteInfo.publish_port.trim()!=""){
	                      	  if(vm.msbRouteInfo.protocol=="TCP" || vm.msbRouteInfo.protocol=="UDP"){
	                      	  	vm.if_publish_port=["1"];
	                      	  	vm.msbRouteInfo.publish_port=msbRouteInfo.publish_port;
								vm.msbRouteInfo.oldPublish_port=msbRouteInfo.publish_port;
	                      	  }
	                      	  else  if(vm.msbRouteInfo.protocol=="HTTP" || vm.msbRouteInfo.protocol=="REST"|| vm.msbRouteInfo.protocol=="UI" || vm.msbRouteInfo.protocol=="PORTAL"){
	                      	  	vm.if_http_publish_port=["1"];
	                      	  	vm.msbRouteInfo.publish_port=msbRouteInfo.publish_port;
								vm.msbRouteInfo.oldPublish_port=msbRouteInfo.publish_port;
	                      	  }
	                      	  else{
	                      	  	vm.msbRouteInfo.publish_port="";
								vm.msbRouteInfo.oldPublish_port="";
	                      	  }

	                      }

	                

							for(var i=0;i<msbRouteInfo.nodes.length;i++){

								msbRouteInfo.nodes[i].weight="";
						      	msbRouteInfo.nodes[i].max_fails="";
						      	msbRouteInfo.nodes[i].fail_timeout="";

						      var lb_server_params=msbRouteInfo.nodes[i].lb_server_params;
						      if(lb_server_params!=null){
						      	
						      	 var paramsArray=lb_server_params.split(",");
						      	 for(var n=0;n<paramsArray.length;n++){
						      	 	var param=paramsArray[n].split("=");
						      	 	switch(param[0]){
						      	 		case "weight":	msbRouteInfo.nodes[i].weight=param[1]; break;
						      	 		case "max_fails":	msbRouteInfo.nodes[i].max_fails=param[1]; break;
						      	 		case "fail_timeout":	msbRouteInfo.nodes[i].fail_timeout=param[1].replace("s",""); break;
						      	 	}
						      	 	
						      	 }

						      }
						     

							}

								vm.msbRouteInfo.nodes=msbRouteInfo.nodes; 
									routeUtil.changeTargetServiceUrl();
             	
	                },
	                 error: function(XMLHttpRequest, textStatus, errorThrown) {
						   bootbox.alert("get discoverInfo fails："+XMLHttpRequest.responseText);                       
	                       return;
	                 }
	                
	                 
				});

					$(".form-group").each(function () {
						
						$(this).find(".item-tip").addClass("item-tip-focus");
						
					});


        	},        	
        	$showNodeDlg:function() {

        		vm.nodeInfo.ip="";
        		vm.nodeInfo.port="";
        		vm.nodeInfo.weight="";
        		vm.nodeInfo.max_fails="";
        		vm.nodeInfo.fail_timeout="";
        		vm.nodeInfo.checkType="";
        		vm.nodeInfo.checkUrl="";
        		vm.nodeInfo.checkInterval="10";
        		vm.nodeInfo.checkTimeOut="10";
        		vm.nodeInfo.ttl="10";
				vm.nodeInfo.ha_role="";
        		
			

				vm.nodeDlgInfo.saveType = "add";
				vm.nodeDlgInfo.titleName=$.i18n.prop("org_onap_msb_discover_node_title");
				vm.node_rtn.warning_block=false;
				vm.node_rtn.info_block=false;

		$('#advanceDiv').collapse('hide');

				$(".form-group").each(function () {
						$(this).removeClass('has-success');
						$(this).removeClass('has-error');
						$(this).find(".help-block[id]").remove();
						$(this).find(".form-tip").removeClass('form-input-focus');
  						//$(this).find(".item-tip").removeClass('item-tip-focus');
					});

				$("#noderouteDlg").modal("show");
        	},

        	savenodeInfo:function(){
        		nodesuccess.hide();
				nodeerror.hide();
				if (nodeform.valid() == false) {
					return false;
				}

				//健康检查规则效验
				if(vm.nodeInfo.checkType=="TTL"){
					if(vm.nodeInfo.ttl==""){
							vm.node_rtn.warning_block=true;
						    vm.node_rtn.info_block=false; 
						    vm.node_rtn.rtn_info=$.i18n.prop("org_onap_msb_discover_validator_ttl_empty");
							return;
					}
					else{
						
						vm.nodeInfo.checkTimeOut="";
						vm.nodeInfo.checkInterval="";
						vm.nodeInfo.checkUrl="";
					}

				}
				else if(vm.nodeInfo.checkType=="TCP" || vm.nodeInfo.checkType=="HTTP"){
					if(vm.nodeInfo.checkInterval==""){
							vm.node_rtn.warning_block=true;
						    vm.node_rtn.info_block=false; 
						    vm.node_rtn.rtn_info= $.i18n.prop("org_onap_msb_discover_validator_interval_empty");
							return;
					}
					

					if(vm.nodeInfo.checkTimeOut==""){
							vm.node_rtn.warning_block=true;
						    vm.node_rtn.info_block=false; 
						    vm.node_rtn.rtn_info= $.i18n.prop("org_onap_msb_discover_validator_timeOut_empty");
							return;
					}
					

					if(vm.nodeInfo.checkUrl==""){
							vm.node_rtn.warning_block=true;
						    vm.node_rtn.info_block=false; 
						    vm.node_rtn.rtn_info= $.i18n.prop("org_onap_msb_discover_validator_healthurl_empty");
							return;
					}

					vm.nodeInfo.ttl="";
				

				}
				else{
					vm.nodeInfo.checkUrl="";
					vm.nodeInfo.checkInterval="";
					vm.nodeInfo.checkTimeOut="";
					vm.nodeInfo.ttl="";
				}

				

				if(vm.nodeInfo.checkType=="HTTP"){

   					var reg_http_url_match=/^(|http:\/\/)(([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.)(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.){2}([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5]))):(\d{1,5}).*$/;
					if(reg_http_url_match.test(vm.nodeInfo.checkUrl)){
   						/*vm.node_rtn.warning_block=true;
						vm.node_rtn.info_block=false; 
						vm.node_rtn.rtn_info= $.i18n.prop("org_onap_msb_discover_validator_healthurl_http");
						return;*/
						if(vm.nodeInfo.checkUrl.indexOf("http://")!=0){
							vm.nodeInfo.checkUrl="http://"+vm.nodeInfo.checkUrl;
						}
   					}
   					else{
   						if(vm.nodeInfo.checkUrl.indexOf("/")!=0){
							vm.nodeInfo.checkUrl="/"+vm.nodeInfo.checkUrl;
						}
						vm.nodeInfo.checkUrl="http://"+vm.nodeInfo.ip+":"+vm.nodeInfo.port+vm.nodeInfo.checkUrl

   					}
				} 
				else if(vm.nodeInfo.checkType=="TCP"){
					 var reg_tcp_url_match=/^.*:\d{1,5}$/;
   					if(!reg_tcp_url_match.test(vm.nodeInfo.checkUrl)){
   						vm.node_rtn.warning_block=true;
						vm.node_rtn.info_block=false; 
						vm.node_rtn.rtn_info= $.i18n.prop("org_onap_msb_discover_validator_healthurl_tcp");
						return;
   					}
				} 




			

				if(vm.nodeDlgInfo.saveType=="add")
				{

				//判断host是否重复
				for(var i=0;i<vm.msbRouteInfo.nodes.length;i++){
				
						if(vm.msbRouteInfo.nodes[i].ip==vm.nodeInfo.ip && vm.msbRouteInfo.nodes[i].port==vm.nodeInfo.port )
						{
							vm.node_rtn.warning_block=true;
						    vm.node_rtn.info_block=false; 
						    vm.node_rtn.rtn_info=$.i18n.prop('org_onap_msb_discover_err_host_repeat',[vm.nodeInfo.ip],[vm.nodeInfo.port]);
							return;
						}
					
				}


					var node=
					{
						ip:vm.nodeInfo.ip,
						port:vm.nodeInfo.port,
						weight:vm.nodeInfo.weight,
						max_fails:vm.nodeInfo.max_fails,
						fail_timeout:vm.nodeInfo.fail_timeout,
						checkType:vm.nodeInfo.checkType,
						checkUrl:vm.nodeInfo.checkUrl,
						checkInterval:vm.nodeInfo.checkInterval==""?"":vm.nodeInfo.checkInterval+"s",
						checkTimeOut:vm.nodeInfo.checkTimeOut==""?"":vm.nodeInfo.checkTimeOut+"s",
						ttl:vm.nodeInfo.ttl==""?"":vm.nodeInfo.ttl+"s",
						ha_role:vm.nodeInfo.ha_role

					};
						vm.msbRouteInfo.nodes.push(node);
				}
				else if(vm.nodeDlgInfo.saveType=="update")
				{
					if(vm.nodeInfo.ip!=vm.oldNodeInfo.ip || 
               	  vm.nodeInfo.port!=vm.oldNodeInfo.port)  //已修改IP或者port
               	  {

					//判断host是否重复
						for(var i=0;i<vm.msbRouteInfo.nodes.length;i++){
						
								if(vm.msbRouteInfo.nodes[i].ip==vm.nodeInfo.ip && vm.msbRouteInfo.nodes[i].port==vm.nodeInfo.port )
								{
									vm.node_rtn.warning_block=true;
								    vm.node_rtn.info_block=false; 
								    vm.node_rtn.rtn_info= $.i18n.prop('org_onap_msb_discover_err_host_repeat',[vm.nodeInfo.ip],[vm.nodeInfo.port]);
									return;
								}
							
						}
			    }

					 for(var i=0;i<vm.msbRouteInfo.nodes.length;i++){
			                       if(vm.msbRouteInfo.nodes[i].ip == vm.oldNodeInfo.ip && vm.msbRouteInfo.nodes[i].port == vm.oldNodeInfo.port){
			                         
			                            vm.msbRouteInfo.nodes[i].ip=vm.nodeInfo.ip;
			                            vm.msbRouteInfo.nodes[i].port=vm.nodeInfo.port;
			                            vm.msbRouteInfo.nodes[i].weight=vm.nodeInfo.weight;
			                            vm.msbRouteInfo.nodes[i].max_fails=vm.nodeInfo.max_fails;
			                            vm.msbRouteInfo.nodes[i].fail_timeout=vm.nodeInfo.fail_timeout;
			                            vm.msbRouteInfo.nodes[i].checkType=vm.nodeInfo.checkType;
			                            vm.msbRouteInfo.nodes[i].checkUrl=vm.nodeInfo.checkUrl;
			                            vm.msbRouteInfo.nodes[i].checkInterval=vm.nodeInfo.checkInterval==""?"":vm.nodeInfo.checkInterval+"s";
			                            vm.msbRouteInfo.nodes[i].checkTimeOut=vm.nodeInfo.checkTimeOut==""?"":vm.nodeInfo.checkTimeOut+"s";
			                            vm.msbRouteInfo.nodes[i].ttl=vm.nodeInfo.ttl==""?"":vm.nodeInfo.ttl+"s";
			                            vm.msbRouteInfo.nodes[i].ha_role=vm.nodeInfo.ha_role;

			                            

			                           
			                            
			                            break;
			                        }
			               }	


				}

				$('#noderouteDlg').modal('hide');

        	},
        	delnodeInfo:function(ip,port){
        		bootbox.confirm($.i18n.prop('org_onap_msb_discover_err_host_del_ask',[ip],[port]),function(result){
				if(result){
					
					 for(var i=0;i<vm.msbRouteInfo.nodes.length;i++){
			                       if(vm.msbRouteInfo.nodes[i].ip == ip && vm.msbRouteInfo.nodes[i].port == port){
			                            vm.msbRouteInfo.nodes.splice(i, 1);
			                            break;
			                        }
			               }	

			     

			       }
	
	  		 	});


        	},
        	updatenodeInfo:function(node){
        		vm.nodeInfo.ip=node.ip;
        		vm.oldNodeInfo.ip=node.ip;
        		
        		vm.nodeInfo.port=node.port;
        		vm.oldNodeInfo.port=node.port;

        		vm.nodeInfo.weight=node.weight;
        		vm.nodeInfo.max_fails=node.max_fails;
        		vm.nodeInfo.fail_timeout=node.fail_timeout;

        		vm.nodeInfo.checkType=node.checkType;
        		vm.nodeInfo.checkUrl=node.checkUrl;
        		if(node.checkInterval!=undefined) vm.nodeInfo.checkInterval=node.checkInterval.replace("s","");
        		if(node.checkTimeOut!=undefined) vm.nodeInfo.checkTimeOut=node.checkTimeOut.replace("s","");
        		if(node.ttl!=undefined) vm.nodeInfo.ttl=node.ttl.replace("s","");
        		vm.nodeInfo.ha_role=node.ha_role;

        		
			

				vm.nodeDlgInfo.saveType = "update";
				vm.nodeDlgInfo.titleName=$.i18n.prop('org_onap_msb_discover_node_title_update');
				vm.node_rtn.warning_block=false;
				vm.node_rtn.info_block=false;

		$('#advanceDiv').collapse('hide');

				$(".form-group").each(function () {
						$(this).removeClass('has-success');
						$(this).removeClass('has-error');
						$(this).find(".help-block[id]").remove();					
						$(this).find(".form-tip").removeClass('form-input-focus');
					});

				$("#noderouteDlg").modal("show");

        	},
        	resetRoute:function(){
        		if(vm.pageInfo.pageType=="add"){
			        vm.addMsbRoute();
			     }
			     else if(vm.pageInfo.pageType=="update"){
			       vm.initUpdateRoute(vm.updateServiceInfo.serviceName,vm.updateServiceInfo.version,vm.updateServiceInfo.namespace); 			
			     }
			     
        	},
        	getTcpUdpPortRangeUrl:function(){
        		$.ajax({
	                "type": 'get',
	                "url":  vm.$tcpudpportrangeUrl,
	                "dataType": "json",
	                success: function (tcp_udp_port_range) {  
	                   vm.tcp_udp_port_range=tcp_udp_port_range;
	                   $("input[name='publish_port']").attr("placeholder","Range:"+tcp_udp_port_range[0]+"-"+tcp_udp_port_range[1]);


	                },
	                 error: function(XMLHttpRequest, textStatus, errorThrown) {
						   console.log("get TcpUdpPortRangeUrl fails："+XMLHttpRequest.responseText);                       
	                    
	                 }

				});


        	},        	
        	getApigatewayInfo : function(namespace) {

        		// if(namespace=="") namespace="default";
        		if(namespace!="all"){
        			vm.apiGatewayService=[];
        			vm.routeService=[];
        		}


        		 var  url=vm.$apiGatewayInfoUrl+"?namespace="+namespace;

				 $.ajax({
	                "type": 'get',
	                "url":  url,
	                "async":false,
	                "dataType": "json",
	                success: function (resp) {  
	                   var apiGatewayService=[],routeService=[];
	                   var apiGatewayInfoList=resp==null?[]:resp;

	                	for(var i=0;i<apiGatewayInfoList.length;i++){
	                		if(apiGatewayInfoList[i].visualRange=="0"){
	                			routeService.push(apiGatewayInfoList[i]);
	                		}
	                		else if(apiGatewayInfoList[i].visualRange=="1"){
								apiGatewayService.push(apiGatewayInfoList[i])
	                		}
	                	}

	                	if(namespace=="all"){
	                		vm.apiGatewayService_all=apiGatewayService;
	                   		vm.routeService_all=routeService;
	                	}
	                	else{
	                		vm.apiGatewayService=apiGatewayService;
	                   		vm.routeService=routeService;
	                	}
	                   
	                   

	                },
	                 error: function(XMLHttpRequest, textStatus, errorThrown) {
						   console.log("get apigatewayInfo for "+namespace+" fails："+XMLHttpRequest.responseText);                       
	                     
	                 }
	                
	                 
				});


				

				routeUtil.changeTargetServiceUrl();

        		

        	},
        	initIUIfori18n:function(){
        		vm.msbLbPolicyRange=[
					{
						name:$.i18n.prop("org_onap_msb_discover_lbpolicy_round"),
						value:"round-robin"
					},
				 	 {
						name:"IPHash",
						value:"ip_hash"
					},
					/*{
						name:$.i18n.prop("org_onap_msb_discover_lbpolicy_leastconn"),
						value:"least_conn"
					},*/
					{
						name:$.i18n.prop("org_onap_msb_discover_lbpolicy_clientcustom"),
						value:"client_custom"
					}	
				];

        	},
			savemsbRoute : function() {

				msbsuccess.hide();
				msberror.hide();
				if (msbform.valid() == false) {
					return false;
				}

				if(vm.msbRouteInfo.nodes.length==0){
					vm.server_rtn.warning_block=true;
					vm.server_rtn.info_block=false; 
					vm.server_rtn.rtn_info= $.i18n.prop('org_onap_msb_discover_err_host_leastone');
					return;
				}

				if(vm.msbRouteInfo.visualRangeArray.length==0){
					vm.server_rtn.warning_block=true;
					vm.server_rtn.info_block=false; 
					vm.server_rtn.rtn_info= $.i18n.prop('org_onap_msb_discover_err_visualrange_empty');
					return;
				}


				/*if(vm.msbRouteInfo.namespace=="default" || vm.msbRouteInfo.namespace=="all" ){
					vm.server_rtn.warning_block=true;
					vm.server_rtn.info_block=false; 
					vm.server_rtn.rtn_info= $.i18n.prop('org_onap_msb_discover_err_namespace_err',[vm.msbRouteInfo.namespace]);
					return;
				}*/


				if(vm.msbRouteInfo.protocol=="REST" || vm.msbRouteInfo.protocol=="HTTP"||vm.msbRouteInfo.protocol=="UI"||vm.msbRouteInfo.protocol=="PORTAL"){
					if(vm.msbRouteInfo.url==""){
						vm.server_rtn.warning_block=true;
						vm.server_rtn.info_block=false; 
						vm.server_rtn.rtn_info=$.i18n.prop('org_onap_msb_discover_err_url_empty',[vm.msbRouteInfo.protocol]);
						return;

					}
				}

				//正则判断标签格式正确性
				var labelArray=[];
				 if($('#labels').val().trim()!=""){
				 	 labelArray=$('#labels').val().split(",");
				 	for(var i=0;i<labelArray.length;i++){
				 		labelArray[i]=labelArray[i].trim();
				 		if(/\S+:\S+/.test(labelArray[i])==false){
				 		vm.server_rtn.warning_block=true;
						vm.server_rtn.info_block=false; 
						vm.server_rtn.rtn_info=$.i18n.prop('org_onap_msb_discover_err_label_format');
						return;

				 		}

				 	}
				 
				 }

				 var metadataArray=[],metadata=[];
				 if($('#metadata').val().trim()!=""){
				 	 metadataArray=$('#metadata').val().split(",");
				 	for(var i=0;i<metadataArray.length;i++){
				 		metadataArray[i]=metadataArray[i].trim();

				 		if(/\S+:\S+/.test(metadataArray[i])==false){
				 		vm.server_rtn.warning_block=true;
						vm.server_rtn.info_block=false; 
						vm.server_rtn.rtn_info=$.i18n.prop('org_onap_msb_discover_err_metadata_format');
						return;
				 		}
				 		var metadata_key_value=metadataArray[i].split(":");
				 		var metadata_obj=new Object();
				 		metadata_obj.key=metadata_key_value[0];
				 		metadata_obj.value=metadata_key_value[1];
				 		metadata.push(metadata_obj);

				 	}
				 
				 }

       
    
			



				vm.server_rtn.warning_block=false;
				vm.server_rtn.info_block=true;

				//var msbUrl=vm.msbRouteInfo.url=="/"?"":vm.msbRouteInfo.url;
				
				var nodes=[];
				for(var i=0;i<vm.msbRouteInfo.nodes.length;i++){


			      var lb_server_params="";
			      if(vm.msbRouteInfo.nodes[i].weight!=""){
			      	lb_server_params+="weight="+vm.msbRouteInfo.nodes[i].weight+","
			      }

			      if(vm.msbRouteInfo.nodes[i].max_fails!=""){
			      	lb_server_params+="max_fails="+vm.msbRouteInfo.nodes[i].max_fails+","
			      }

			      if(vm.msbRouteInfo.nodes[i].fail_timeout!=""){
			      	lb_server_params+="fail_timeout="+vm.msbRouteInfo.nodes[i].fail_timeout+"s";
			      }



			        

					var node={
						ip:vm.msbRouteInfo.nodes[i].ip,
						port:vm.msbRouteInfo.nodes[i].port,
						lb_server_params: lb_server_params,
						checkType:vm.msbRouteInfo.nodes[i].checkType,
						checkUrl:vm.msbRouteInfo.nodes[i].checkUrl,
						checkInterval:vm.msbRouteInfo.nodes[i].checkInterval,
						checkTimeOut:vm.msbRouteInfo.nodes[i].checkTimeOut,
						ttl:vm.msbRouteInfo.nodes[i].ttl,
						ha_role:vm.msbRouteInfo.nodes[i].ha_role

					};
					nodes.push(node);
				}

				//TCP|UDP协议对外端口判断
				/*if((vm.msbRouteInfo.protocol=="TCP" || vm.msbRouteInfo.protocol=="UDP") && vm.if_publish_port.length==1){




					if(vm.pageInfo.pageType=="add" || (vm.pageInfo.pageType=="update" && vm.msbRouteInfo.oldPublic_port!=vm.msbRouteInfo.publish_port)){

					//唯一性判断
					for(var i=0;i<vm.msbRouteArray.length;i++){
				
						if(vm.msbRouteArray[i].publish_port==vm.msbRouteInfo.publish_port)
						{
							vm.server_rtn.warning_block=true;
						    vm.server_rtn.info_block=false; 
						    vm.server_rtn.rtn_info= "发布端口("+vm.msbRouteInfo.publish_port+")已使用";
							return;
						}
					
				     }
				  }
					

				}
				else{
				   vm.msbRouteInfo.publish_port="";	
				}*/


				//var newServiceName=(vm.msbRouteInfo.protocol=="UI"?("IUI_"+vm.msbRouteInfo.serviceName):vm.msbRouteInfo.serviceName);
				var newServiceName=vm.msbRouteInfo.serviceName;

				var enable_ssl = false;
				if (vm.msbRouteInfo.enable_ssl.length > 0){
					enable_ssl = true;
				}


				var data= JSON.stringify({
							 "serviceName": newServiceName,
							  "version": vm.msbRouteInfo.version,
							  "url": vm.msbRouteInfo.url,							 
							  "nodes": nodes,
							  "protocol":vm.msbRouteInfo.protocol,
							  "visualRange":vm.msbRouteInfo.visualRangeArray.join("|"),
							  "lb_policy":vm.msbRouteInfo.lb_policy,
							  "publish_port":vm.msbRouteInfo.publish_port,
							  "namespace":vm.msbRouteInfo.namespace,
							  "network_plane_type":vm.msbRouteInfo.network_plane_type,
							  "host":vm.msbRouteInfo.host,
							  "path":vm.msbRouteInfo.path,
							  "enable_ssl":enable_ssl,
							  "labels":	labelArray,
							  "metadata":metadata
						});

				

					//判断服务名是否重复
				if(vm.pageInfo.pageType=="add"){

					var url= vm.$msbRouteInstanceUrl;

				 var version2=vm.msbRouteInfo.version==""?"null":vm.msbRouteInfo.version;
                 url=url.replace("{serviceName}",newServiceName).replace("{version}",version2)+"?ifPassStatus=false&namespace="+vm.msbRouteInfo.namespace;

				$.ajax({
	                "type": 'get',
	                "url":  url,
	                "dataType": "json",
	                "async": false,
	                success: function (serviceInfo) {  
	                    
	                     		vm.server_rtn.warning_block=true;
							    vm.server_rtn.info_block=false; 
							    vm.server_rtn.rtn_info=$.i18n.prop('org_onap_msb_discover_err_service_repeat',[vm.msbRouteInfo.serviceName],[vm.msbRouteInfo.protocol],[vm.msbRouteInfo.namespace]);
								
             	
	                },
	                 error: function(XMLHttpRequest, textStatus, errorThrown) {
	                 	  if(XMLHttpRequest.status==404){
	                 	  	    $.ajax({
					                "type": 'POST',
					                "url":  vm.$msbRouteUrl,
					                "data" : data,
					                "dataType": "json",
					                "contentType":"application/json",
					                success: function (resp) {  
					                	

										 window.location.href="index.html" 

										  //table.destroy();
										  //vm.initMSBRoute();
					 	
					                },
					                 error: function(XMLHttpRequest, textStatus, errorThrown) {

										   vm.server_rtn.warning_block=true;
									       vm.server_rtn.info_block=false; 
									       vm.server_rtn.rtn_info= "msb save fails："+XMLHttpRequest.responseText;                
					                      
					                 }
					            });

							}
							else{
						   		bootbox.alert("get service["+vm.msbRouteInfo.serviceName+"]fails："+XMLHttpRequest.responseText);                       
	                       }
	                 }
	                
	                 
				});


                
                 
               }
                else if(vm.pageInfo.pageType=="update")
				{
					
				  var url= vm.$msbRouteInstanceUrl+"?is_manual=true&namespace="+vm.msbRouteInfo.namespace;
				  var version2=vm.msbRouteInfo.oldVersion==""?"null":vm.msbRouteInfo.oldVersion;
                   url=url.replace("{serviceName}",vm.msbRouteInfo.oldServiceName).replace("{version}",version2);

				 $.ajax({
	                "type": 'PUT',
	                "url":  url,
	                "data" : data,
	                "dataType": "json",
	                "contentType":"application/json",
	                success: function (resp) {  
	                	
 					window.location.href="index.html?namespace="+vm.updateServiceInfo.namespace;
	 	
	                },
	                 error: function(XMLHttpRequest, textStatus, errorThrown) {
						
						   vm.server_rtn.warning_block=true;
					       vm.server_rtn.info_block=false; 
					       vm.server_rtn.rtn_info= "msb save fails："+XMLHttpRequest.responseText;                
	                      
	                 }
	            });		

							
				}
               
               
               
			}

		

	});


