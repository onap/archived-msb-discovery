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

import java.util.List;
import java.util.Map;

import org.onap.msb.sdclient.core.CatalogService;
import org.onap.msb.sdclient.core.HealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class JacksonJsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JacksonJsonUtil.class);

    private static ObjectMapper mapper;

    /**
     * 获取ObjectMapper实例
     * 
     * @param createNew 方式：true，新实例；false,存在的mapper实例
     * @return
     */
    public static synchronized ObjectMapper getMapperInstance() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper;
    }

    /**
     * 将java对象转换成json字符串
     * 
     * @param obj 准备转换的对象
     * @return json字符串
     * @throws Exception
     */
    public static String beanToJson(Object obj) throws Exception {
        String json = null;
        try {
            ObjectMapper objectMapper = getMapperInstance();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            json = objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Class beanToJson faild:" + e.getMessage());
            throw new Exception("Class beanToJson faild:" + e.getMessage());
        }
        return json;
    }



    /**
     * 将json字符串转换成java对象
     * 
     * @param json 准备转换的json字符串
     * @param cls 准备转换的类
     * @return
     * @throws Exception
     */
    public static Object jsonToBean(String json, Class<?> cls) throws Exception {
        Object vo = null;
        try {
            ObjectMapper objectMapper = getMapperInstance();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            vo = objectMapper.readValue(json, cls);

        } catch (Exception e) {
            logger.error(cls + " JsonTobean faild:" + e.getMessage());
            throw new Exception(cls + " JsonTobean faild:" + e.getMessage());
        }
        return vo;
    }



    /**
     * 将json字符串转换成java集合对象
     * 
     * @param json 准备转换的json字符串
     * @param cls 准备转换的类
     * @return
     * @throws Exception
     */
    public static List<CatalogService> jsonToListBean(String json) {
        List<CatalogService> vo = null;
        try {

            ObjectMapper objectMapper = getMapperInstance();


            vo = objectMapper.readValue(json, new TypeReference<List<CatalogService>>() {});

        } catch (Exception e) {
            String errorMsg = " JsonTobean faild:" + e.getMessage();
            logger.error(errorMsg);
        }
        return vo;
    }

    public static <T> T jsonToListBean(String json, TypeReference<T> valueTypeRef) {
        try {

            ObjectMapper objectMapper = getMapperInstance();


            return objectMapper.readValue(json, valueTypeRef);

        } catch (Exception e) {
            String errorMsg = " JsonTobean faild:" + e.getMessage();
            logger.error(errorMsg);
        }
        return null;
    }



    /**
     * 将json字符串转换成java集合对象
     * 
     * @param json 准备转换的json字符串
     * @param cls 准备转换的类
     * @return
     * @throws Exception
     */
    public static Map<String, String[]> jsonToMapBean(String json) {
        Map<String, String[]> vo = null;
        try {

            ObjectMapper objectMapper = getMapperInstance();


            vo = objectMapper.readValue(json, new TypeReference<Map<String, String[]>>() {});

        } catch (Exception e) {
            String errorMsg = " JsonTobean faild";
            logger.error(errorMsg);
        }
        return vo;
    }


    public static void main(String[] args) {
        String json = "[{\"Node\":{\"Node\":\"A23179111\",\"Address\":\"10.74.44.27\",\"CreateIndex\":3,\"ModifyIndex\":318},\"Service\":{\"ID\":\"oo_10.74.56.36_5656\",\"Service\":\"oo\",\"Tags\":[\"url:/root\",\"protocol:REST\",\"version:\",\"visualRange:0|1\",\"ttl:-1\",\"status:1\",\"lb_policy:client_custom\",\"lb_server_params:weight=1 max_fails=1 fail_timeout=16s\",\"checkType:TCP\",\"checkInterval:10\",\"checkUrl:10.56.23.63:8989\"],\"Address\":\"10.74.56.36\",\"Port\":5656,\"EnableTagOverride\":false,\"CreateIndex\":314,\"ModifyIndex\":318},\"Checks\":[{\"Node\":\"A23179111\",\"CheckID\":\"serfHealth\",\"Name\":\"Serf Health Status\",\"Status\":\"passing\",\"Notes\":\"\",\"Output\":\"Agent alive and reachable\",\"ServiceID\":\"\",\"ServiceName\":\"\",\"CreateIndex\":3,\"ModifyIndex\":3},{\"Node\":\"A23179111\",\"CheckID\":\"service:oo_10.74.56.36_5656\",\"Name\":\"Service 'oo' check\",\"Status\":\"critical\",\"Notes\":\"\",\"Output\":\"\",\"ServiceID\":\"oo_10.74.56.36_5656\",\"ServiceName\":\"oo\",\"CreateIndex\":314,\"ModifyIndex\":318}]},{\"Node\":{\"Node\":\"A23179111\",\"Address\":\"10.74.44.27\",\"CreateIndex\":3,\"ModifyIndex\":318},\"Service\":{\"ID\":\"oo_10.78.36.36_111\",\"Service\":\"oo\",\"Tags\":[\"url:/root\",\"protocol:REST\",\"version:\",\"visualRange:0|1\",\"ttl:-1\",\"status:1\",\"lb_policy:client_custom\"],\"Address\":\"10.78.36.36\",\"Port\":111,\"EnableTagOverride\":false,\"CreateIndex\":315,\"ModifyIndex\":315},\"Checks\":[{\"Node\":\"A23179111\",\"CheckID\":\"serfHealth\",\"Name\":\"Serf Health Status\",\"Status\":\"passing\",\"Notes\":\"\",\"Output\":\"Agent alive and reachable\",\"ServiceID\":\"\",\"ServiceName\":\"\",\"CreateIndex\":3,\"ModifyIndex\":3}]}]";
        List<HealthService> list = jsonToListBean(json, new TypeReference<List<HealthService>>() {});
        System.out.println(list);

    }



}
