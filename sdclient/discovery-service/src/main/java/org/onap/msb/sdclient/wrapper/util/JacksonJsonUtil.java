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
            logger.error(" JsonTobean faild:" + e.getMessage());
        }
        return vo;
    }

    public static <T> T jsonToListBean(String json, TypeReference<T> valueTypeRef) {
        try {

            ObjectMapper objectMapper = getMapperInstance();

            return objectMapper.readValue(json, valueTypeRef);

        } catch (Exception e) {
            logger.error(" JsonTobean faild:" + e.getMessage());
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
            logger.error("JsonTobean faild");
        }
        return vo;
    }




}
