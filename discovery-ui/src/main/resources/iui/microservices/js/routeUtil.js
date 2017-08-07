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
var routeUtil = {};

routeUtil.growl=function(title,message,type){
      $.growl({
    icon: "fa fa-envelope-o fa-lg",
    title: "&nbsp;&nbsp;"+$.i18n.prop('org_onap_msb_route_property_ttl')+title,
    message: message+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
      },{
        type: type
      });
}


routeUtil.showApiGateWayAddress=function(){



  if($("input[name='chkVisualRange']:checked").length==0) return false;

  if(vm.msbRouteInfo.protocol=='REST' || vm.msbRouteInfo.protocol=='HTTP' || vm.msbRouteInfo.protocol=='UI' || vm.msbRouteInfo.protocol=='PORTAL') return true;

   if(vm.msbRouteInfo.protocol=='UDP' || vm.msbRouteInfo.protocol=='TCP') {
    if(vm.if_publish_port.length==1) return true;
   }


    return false;


}


routeUtil.showHealthStatus=function(nodes){

    for(var i=0;i<nodes.length;i++){

        if(nodes[i].status!="passing"){
           return " <span class='label label-warning'>"+$.i18n.prop('org_onap_msb_discover_property_abnormal')+"</span>"; 
        }
    }

    return " <span class='label label-success'>"+$.i18n.prop('org_onap_msb_discover_property_normal')+"</span>";


}

routeUtil.showVisualRange=function(visualRange){

    var rangArray=visualRange.split("|");

    var visualRangeText="";

if(rangArray.length>1) rangArray.sort(function(a,b){return a>b?1:-1}); 

    for(var i=0;i<rangArray.length;i++){
        if(rangArray[i] === '0'){
              visualRangeText+= "<span class='label-visualRange label-sysOut'>"+$.i18n.prop('org_onap_msb_route_form_intersystem')+"</span>";
           }
         else if(rangArray[i] === '1'){
              visualRangeText+= "<span class='label-visualRange label-sysIn'>"+$.i18n.prop('org_onap_msb_route_form_insystem')+"</span>";
           }

    }

return visualRangeText;

}


routeUtil.showCheckInterval=function(node){
  if(node.checkType=="TTL"){
    return node.ttl==0?"":node.ttl;
  }
  else if(node.checkType=="TCP" || node.checkType=="HTTP"){
    return node.checkInterval==0?"":node.checkInterval;
  }
  else{
    return "";
  }
}




routeUtil.ifAPIUrl=function(url){
  if(url=="" || url ==null) return false;
       
    var reg_api_match=/^(\/api\/.*?)$/;
   return reg_api_match.test(url);
      
}


routeUtil.checkLables=function(apiGateway_labels_array,service_labels){
 if(apiGateway_labels_array==null || apiGateway_labels_array.length==0) return true;
 if(service_labels=="") return false;
 var service_labels_array=service_labels.split(",");
  for(var i=0;i<service_labels_array.length;i++){
   for(var j=0;j<apiGateway_labels_array.length;j++){
     if(service_labels_array[i].trim()==apiGateway_labels_array[j].trim()){
      return true;
     }
  }
 }
 return false;
}

routeUtil.checkNetwork_plane_type=function(apiGateway_networkPlaneType,service_networkPlaneType){
  if(apiGateway_networkPlaneType==null || apiGateway_networkPlaneType=="") return true;
  var apiGateway_networkPlaneType_array=apiGateway_networkPlaneType.split("|");
  var service_networkPlaneType_array=service_networkPlaneType.split("|")

 for(var i=0;i<service_networkPlaneType_array.length;i++){
   for(var j=0;j<apiGateway_networkPlaneType_array.length;j++){
     if(service_networkPlaneType_array[i].trim()==apiGateway_networkPlaneType_array[j].trim()){
      return true;
     }
  }
 }
 return false;

}


routeUtil.checkTargetServiceUrl=function(service){

  if(service==null) return false;

   return routeUtil.checkNetwork_plane_type(service.network_plane_type,$("input[name='networkPlaneType']").val()) &&
          routeUtil.checkLables(service.labels,$("#labels").val());

}


Array.prototype.unique = function(){
 var res = [];
 var json = {};
 for(var i = 0; i < this.length; i++){
  if(!json[this[i]]){
   res.push(this[i]);
   json[this[i]] = 1;
  }
 }
 return res;
}


routeUtil.buildTargetServiceUrl=function(service,namespace){

namespace=namespace==""?"default":namespace;
var serviceName=vm.msbRouteInfo.serviceName==""?"serviceName":vm.msbRouteInfo.serviceName;
var targetServiceUrlArray=[]; 
//routeWay
 var routeWay="ip",routeSubdomain="openpalette.zte.com.cn";
 var metadataArray=service.metadata;
 if(metadataArray!=null){
   for(var i=0;i<metadataArray.length;i++){
      if(metadataArray[i].key=="routeWay"){
        routeWay=metadataArray[i].value;
      }
      if(metadataArray[i].key=="routeSubdomain"){
        routeSubdomain=metadataArray[i].value;
      }
   }
}


//get path
var path="";
 var protocol=$("select[name='protocol']").find("option:selected").text();
 if(vm.msbRouteInfo.path.trim()!="" && vm.msbRouteInfo.path.trim()!="/"){
   path=vm.msbRouteInfo.path;
 }
 else{
  var version=vm.msbRouteInfo.version==""?"":"/"+vm.msbRouteInfo.version
   if(protocol=='UI'){

   path="/iui/"+serviceName;
   
  }
  else if(protocol=='REST'){
     
      path="/api/"+serviceName+version;
      

  }
   else if(protocol=='HTTP' || (protocol=="" && vm.msbRouteInfo.protocol=="PORTAL")){
      var reg_customName_match=/^(\/.*?)$/;
        if(!reg_customName_match.test(serviceName)) {
     
           path="/"+serviceName+version;
        }
        else{
          path=serviceName+version;
        }
      

   } 
 

 }

 //get host
 var  host;
  if(vm.msbRouteInfo.host!=""){
   host=vm.msbRouteInfo.host;
 }
 else{
     if(vm.msbRouteInfo.namespace!="" && vm.msbRouteInfo.namespace!="default")   host=serviceName+"-"+vm.msbRouteInfo.namespace;
     else host=serviceName;
 }

//get publish port
var publish_port,publish_protocol;



//get publish URL

  var routeWays=routeWay.split("|");
  for(var i=0;i<routeWays.length;i++){
    if(routeWays[i]=="ip"){

      if(service.visualRange=="1"){
        

          if(protocol=='TCP'||protocol=='UDP'){
              publish_protocol=protocol.toLowerCase();
               publish_port=vm.msbRouteInfo.publish_port;       
            }
            else{
              publish_protocol="http";
               publish_port=10080; // service.nodes[0].port;         
            }  

         targetServiceUrlArray.push("<span> ns:"+namespace+" </span> "+publish_protocol+"://"+service.nodes[0].ip+":"+publish_port+path);
      }
      else{
        var publishPorts=vm.msbRouteInfo.publish_port.split("|");
        if(publishPorts.length==2){
          // multiPublishPort: https|http
          
           targetServiceUrlArray.push("<span> ns:"+namespace+" </span> "+"https://"+service.nodes[0].ip+":"+publishPorts[0]+path);
           targetServiceUrlArray.push("<span> ns:"+namespace+" </span> "+"http://"+service.nodes[0].ip+":"+publishPorts[1]+path);

        }
        else{
           // single Port
            

            if(protocol=='TCP'||protocol=='UDP'){
              publish_protocol=protocol.toLowerCase();
              publish_port=vm.msbRouteInfo.publish_port;
            }
            else{
               if(vm.msbRouteInfo.publish_port!=""){
                publish_port=vm.msbRouteInfo.publish_port;
                publish_protocol="https";
             }
             else{
                publish_port=80;  //service.nodes[0].port;
                publish_protocol="http";

                //https default:443
                var https_default_publish_port=443;
                 targetServiceUrlArray.push("<span> ns:"+namespace+" </span> https://"+service.nodes[0].ip+path);
              }
            }


          var portInfo= publish_port==80?"":":"+ publish_port;
          targetServiceUrlArray.push("<span> ns:"+namespace+" </span> "+publish_protocol+"://"+service.nodes[0].ip+portInfo+path);
        }

      }

 
    }
    else if(routeWays[i]=="domain"){
      var domain=host+"."+routeSubdomain;

      var publish_url;
        if(vm.msbRouteInfo.path.trim()!="" && vm.msbRouteInfo.path.trim()!="/" ){
          publish_url=vm.msbRouteInfo.path;
        }
        else{
           if(vm.msbRouteInfo.url.trim()==path || vm.msbRouteInfo.url.trim()=="/"){
             publish_url="/";
           }
           else{
            publish_url=path;
           }

        }

     if(protocol=='TCP'||protocol=='UDP'){       
     
        publish_port=vm.msbRouteInfo.publish_port;
        publish_protocol=protocol.toLowerCase();
      } 
      else
      {
       
        publish_protocol="http";
        publish_port=80;//service.nodes[0].port;     
        targetServiceUrlArray.push("<span> ns:"+namespace+" </span> https://"+domain+publish_url);
   
      }

       

        var portInfo= publish_port==80?"":":"+ publish_port ;        
        targetServiceUrlArray.push("<span> ns:"+namespace+" </span> "+publish_protocol+"://"+domain+portInfo+publish_url);
    }

  }


   return targetServiceUrlArray;

   
}

routeUtil.changeTargetServiceUrl=function(){

vm.targetServiceUrl4SysOut="";
vm.targetServiceUrl4SysIn="";
vm.publishUrl.ifShowPublishUrl4SysOut=false;
vm.publishUrl.ifShowPublishUrl4SysIn=false;
if(routeUtil.showApiGateWayAddress()==false) return;




$("input[name='chkVisualRange']:checked").each(function(){
     

   if($(this).val()==0) 
   {
   // targetAllServiceUrlArray.push("["+$.i18n.prop('org_onap_msb_route_form_intersystem')+"]");
       var targetAllServiceUrlArray=[];
       vm.publishUrl.ifShowPublishUrl4SysOut=true;
    
       for(var i=0;i<vm.routeService.length;i++){
         if(routeUtil.checkTargetServiceUrl(vm.routeService[i])){
            var targetServiceUrlArray = routeUtil.buildTargetServiceUrl(vm.routeService[i],vm.msbRouteInfo.namespace);

            if(targetServiceUrlArray.length>0){
               routeUtil.concatArray(targetAllServiceUrlArray,targetServiceUrlArray);
            }
         }
       }
     
     
       //vm.targetServiceUrl+="(ns:"+vm.msbRouteInfo.namespace+") 未匹配到对应apigateway<br>"; 
    

   for(var i=0;i<vm.routeService_all.length;i++){
      if(routeUtil.checkTargetServiceUrl(vm.routeService_all[i])){
        var targetServiceUrlArray = routeUtil.buildTargetServiceUrl(vm.routeService_all[i],"all");

         if(targetServiceUrlArray.length>0){
               routeUtil.concatArray(targetAllServiceUrlArray,targetServiceUrlArray);
            }
     }
   }

    vm.publishUrl.publishUrl4SysOut=targetAllServiceUrlArray.unique().join("<br>");
     

   }
   else if($(this).val()==1){
     var targetAllServiceUrlArray=[];
     vm.publishUrl.ifShowPublishUrl4SysIn=true;
    //targetAllServiceUrlArray.push("["+$.i18n.prop('org_onap_msb_route_form_insystem')+"]");
   
       for(var i=0;i<vm.apiGatewayService.length;i++){
         if(routeUtil.checkTargetServiceUrl(vm.apiGatewayService[i])){
            var targetServiceUrlArray = routeUtil.buildTargetServiceUrl(vm.apiGatewayService[i],vm.msbRouteInfo.namespace);
            if(targetServiceUrlArray.length>0){
               routeUtil.concatArray(targetAllServiceUrlArray,targetServiceUrlArray);
            }
         }
       }
     

  for(var i=0;i<vm.apiGatewayService_all.length;i++){
    if(routeUtil.checkTargetServiceUrl(vm.apiGatewayService_all[i])){
        var targetServiceUrlArray =routeUtil.buildTargetServiceUrl(vm.apiGatewayService_all[i],"all");
        if(targetServiceUrlArray.length>0){
               routeUtil.concatArray(targetAllServiceUrlArray,targetServiceUrlArray);
        }
     }
   }

     vm.publishUrl.publishUrl4SysIn=targetAllServiceUrlArray.unique().join("<br>");
     
   }


 


});


  
}

routeUtil.getQueryString=function(url,name){
 var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"); 
    var r = url.match(reg); 
    if (r != null) return unescape(r[2]); 
    return null; 
      
}


routeUtil.concatArray=function(parentArray,childArray){
 
 for (var i=0; i < childArray.length; i++) {
    parentArray.push(childArray[i]);
  }

}
