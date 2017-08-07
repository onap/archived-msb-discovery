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
			$id : "routeController",		
			namespace:"",
			loading:true,
			searchTypeIndex:0,
			selectSearch:{
				name:$.i18n.prop("org_onap_msb_discover_searchtype_all"),
				index:1,
				context:""				
			},
			setSearchType:function(name,index){
				vm.selectSearch.name=name;
				vm.selectSearch.index=index;
			},
			searchTypeName:[],			
			dataTableLanguage: {},			
			$msbRouteUrl:apiBasePath+'/services',	
			$msbListByNamespaceUrl:apiBasePath+'/servicelist',
			$msbRouteInstanceUrl :apiBasePath+'/services/{serviceName}/version/{version}',	
			msbRouteArray :  [],													
			initMSBRoute:function(){
				vm.loading=true; 
				vm.initIUIfori18n();
				var url= window.location.search.substr(1);
		 		var namespace=routeUtil.getQueryString(url,"namespace");
		 		
		 		vm.namespace=namespace==""?"default":namespace;



				$.ajax({
	                "type": 'get',
	                "url":  vm.$msbRouteUrl,//+"?namespace="+vm.namespace,
	                "dataType": "json",
	                success: function (resp) {  
	                      vm.msbRouteArray = (resp==null)?[]:resp;  
	                
	                     vm.msbRouteArray.sort(function(a,b){return a.serviceName>b.serviceName?1:-1}); 
	                                    	
	                },
	                 error: function(XMLHttpRequest, textStatus, errorThrown) {
						   bootbox.alert("get serviceListInfo  in namespace ["+vm.namespace+"] fails："+XMLHttpRequest.responseText);                       
	                       return;
	                 },
	                 complete:function(){

	                 	table=$('#msbTable').DataTable({
						     
							  "oLanguage": vm.dataTableLanguage,
							   "dom": '<"top">rt<"bottom"lip><"clear">',
					  			"sPaginationType": "bootstrap_extended",
					  			 "columnDefs": [ {
								      "targets": [0,7],
								      "searchable": false,
								      "bSortable": false,
								    }],
								   "order": [[0, 'asc']]
							});
	                 		vm.loading=false;
	                 	
	            		}
				});



				
	       
	           
			},
			
			searchService:function(){

				/*if(vm.selectSearch.context=="") return;

				if(vm.selectSearch.index==0){
					 $('#msbTable').DataTable().search(
        			vm.selectSearch.context,true,true
    				).draw();

				}
				else{
					
				}*/

				 $('#msbTable').DataTable().column(vm.selectSearch.index).search(
        			vm.selectSearch.context,true,true
    				).draw();
			},
			viewmsbRoute:function(serviceName,version,namespace){

						//window.location.href=
						window.open("serviceMng.html?type=view&serviceName="+serviceName+"&version="+version+"&namespace="+namespace);

			},
			updatemsbRoute:function(serviceName,version,namespace){

						window.location.href="serviceMng.html?type=update&serviceName="+serviceName+"&version="+version+"&namespace="+namespace;

			},
			delmsbRoute:function(serviceName,version,namespace,obj){

	
				bootbox.confirm($.i18n.prop('org_onap_msb_discover_err_service_del_ask',[serviceName],[version]),function(result){
				if(result){
					var url= vm.$msbRouteInstanceUrl;
				    var version2=version==""?"null":version;
				  
                        url=url.replace("{serviceName}",serviceName).replace("{version}",version2)+"?namespace="+namespace;
	
					 $.ajax({
	                "type": 'DELETE',
	                "url": url,
	                "dataType": "json",
	                success: function (resp) { 
	               
							var msbServiceArray=vm.msbRouteArray;	
	               
	                         $(obj).parent().parent().parent().addClass('selected');
	                        
	                        for(var i=0;i<msbServiceArray.length;i++){
			                       if(msbServiceArray[i].serviceName == serviceName &&
			                       		msbServiceArray[i].version==version ){
			                            msbServiceArray.splice(i, 1);
			                            break;
			                        }
								}

							 table.row('.selected').remove().draw( false );

                   			 routeUtil.growl("",$.i18n.prop('org_onap_msb_discover_service_del_success',"success"));
                   			
            	
	                },
	                 error: function(XMLHttpRequest, textStatus, errorThrown) {
						
					        bootbox.alert($.i18n.prop('org_onap_msb_discover_service_del_fail')+XMLHttpRequest.responseText);                
	                      
	                 }
	                 
	                     
	            });


			       }
	
	  		 	});
				
			},
			initIUIfori18n:function(){
				vm.selectSearch.name=$.i18n.prop("org_onap_msb_discover_searchtype_servicename");	
				vm.searchTypeName=[
			  
			   {
			   	name:$.i18n.prop("org_onap_msb_discover_searchtype_servicename"),
			   	index:1
			   }
			  
			 ];
 				vm.dataTableLanguage={
                "sProcessing": "<img src='../img/loading-spinner-grey.gif'/><span>&nbsp;&nbsp;Loadding...</span>",   
                "sLengthMenu": $.i18n.prop("org_onap_msb_route-table-sLengthMenu"),
                "sZeroRecords": $.i18n.prop("org_onap_msb_route-table-sZeroRecords"),
                "sInfo": "<span class='seperator'>  </span>" + $.i18n.prop("org_onap_msb_route-table-sInfo"),
                "sInfoEmpty": $.i18n.prop("org_onap_msb_route-table-sInfoEmpty"),
                "sGroupActions": $.i18n.prop("org_onap_msb_route-table-sGroupActions"),
                "sAjaxRequestGeneralError": $.i18n.prop("org_onap_msb_route-table-sAjaxRequestGeneralError"),
                "sEmptyTable": $.i18n.prop("org_onap_msb_route-table-sEmptyTable"),
                "oPaginate": {
                    "sPrevious": $.i18n.prop("org_onap_msb_route-table-sPrevious"),
                    "sNext": $.i18n.prop("org_onap_msb_route-table-sNext"),
                    "sPage": $.i18n.prop("org_onap_msb_route-table-sPage"),
                    "sPageOf": $.i18n.prop("org_onap_msb_route-table-sPageOf")
                },
                "sSearch": $.i18n.prop("org_onap_msb_route-table-search"),
                "sInfoFiltered": $.i18n.prop("org_onap_msb_route-table-infofilter") 
            };	

			}
			
		
		
			
		

	});


