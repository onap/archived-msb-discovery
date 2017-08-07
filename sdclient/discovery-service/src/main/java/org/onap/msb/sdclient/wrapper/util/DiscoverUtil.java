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
package org.onap.msb.sdclient.wrapper.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/** 
* @ClassName: ApiRouteUtil 
* @Description: TODO(ApiRoute工具类) 
* @author tanghua10186366 
* @date 2015年9月29日 下午3:19:51 
*  
*/
public class DiscoverUtil {

        
    public static final String CONSUL_DEFAULT_PORT="8500";
    
    public static final String APIGATEWAY_SERVINCE = "apigateway";
    
    public static final String ROUTER_SERVINCE = "router";
    
    public static final String APIGATEWAY_SERVINCE_ALL = "all";
    
    public static final String APIGATEWAY_SERVINCE_DEFAULT = "default";
    
    public static final String VISUAL_RANGE_IN = "1";
    
    public static final String VISUAL_RANGE_OUT = "0";
    
    public static final String SERVICENAME_LINE_NAMESPACE="-";
    
    public static final String SPLIT_LINE="|";
    
    public static final String EXTERNAL_NODE_NAME="externalService";


	public static final String REQUEST_SUCCESS = "SUCCESS";
	
	public static final String REQUEST_FAIL = "FAIL";
	
	public static final String VISUAL_RANGE_LIST="0,1"; 
	
    public static final String PROTOCOL_LIST="REST,HTTP,MQ,FTP,SNMP,UI,TCP,UDP,PORTAL"; 
    
    public static final String LB_POLICY_LIST="round-robin,ip_hash,least_conn,client_custom"; 
    
    public static final String LB_PARAMS_LIST="weight,max_fails,fail_timeout";    
    
    public static final String CHECK_TYPE_LIST="HTTP,TCP,TTL";
    
    public static final String CHECK_HA_ROLE_LIST="active,standby";
    
    public static final String CONSUL_CATALOG_URL="/v1/catalog";
    
    public static final String CONSUL_AGENT_URL="/v1/agent/service";
    
    public static final String CONSUL_AGENT_TTL_URL="/v1/agent/check/pass/";
    
    public static final String CONSUL_HEALTH_URL="/v1/health/service/";
    
    public static final String[] PUBLISH_PROTOCOL={"TCP","UDP","HTTP","REST","UI","PORTAL"};
    
    public static final String[] HTTP_PROTOCOL={"HTTP","REST","UI","PORTAL"};
    
   public static  final String TCP_UDP_PORT_RANGE_START="28001";
    
    public static  final String TCP_UDP_PORT_RANGE_END="30000";
    
    public static final String CONSUL_ADDRESSS="127.0.0.1:8500";
    
    public static final String CONSUL_REGISTER_MODE="catalog";
  
//    public static boolean isProtocol_tcp_udp(String protocol){
//      return "TCP".equals(protocol) || "UDP".equals(protocol);
//    }
//    
	
    public static String getRealIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");

        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        

        return request.getRemoteAddr();

    }
    
    public static boolean checkVisualRangeIn(String visualRange){
      return checkExist(visualRange,DiscoverUtil.VISUAL_RANGE_IN, "|");
    }
    
    public static boolean checkVisualRangeOut(String visualRange){
      return checkExist(visualRange,DiscoverUtil.VISUAL_RANGE_OUT, "|");
    }
    
    public static boolean checkExist(String list,String value,String separator){
        String[] listArray=StringUtils.split(list, separator);
        
        for(int i=0;i<listArray.length;i++){
            if(value.equals(listArray[i])) return true;
        }
        return false;
    }
    
    public static boolean checkExist(String[] list,String value){
             for(int i=0;i<list.length;i++){
            if(value.equals(list[i])) return true;
        }
        return false;
    }
    
    public static boolean contain(String[] array,String value[]){
      for(int i=0;i<array.length;i++){
          for(int n=0;n<value.length;n++){
            if(array[i].equals(value[n])){
               return true;  
            }
          }
      }
      return false;

    }
    
 
}
