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

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.onap.msb.sdclient.core.ConsulResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final String CONTENT_TYPE = "Content-type";
    private static final String APPLICATION_JSON_CHARSET = "application/json; charset=utf-8";
    private static final String APPLICATION_JSON = "application/json";
    private static final String ACCEPT = "Accept";

    public static int httpPutWithJSON(String url, String params) {
        int result = 0;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET);
        httpPut.setHeader(ACCEPT, APPLICATION_JSON);
        httpPut.setEntity(new StringEntity(params, Charset.forName("UTF-8")));
        try {
            CloseableHttpResponse res = httpClient.execute(httpPut);
            result = res.getStatusLine().getStatusCode();
            if (res.getStatusLine().getStatusCode() != 200) {
                logger.error(String.valueOf(result));
            }
            res.close();
        } catch (IOException e) {
            String errorMsg = url + ":httpPutWithJSON connect faild";
            logger.error(errorMsg, e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                String errorMsg = url + ":close  httpClient faild";
                logger.error(errorMsg, e);
            }
        }

        return result;

    }

    public static int httpPostWithJSON(String url, String params) {
        int result = 0;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET);
        httpPost.setHeader(ACCEPT, APPLICATION_JSON);
        httpPost.setEntity(new StringEntity(params, Charset.forName("UTF-8")));
        try {
            CloseableHttpResponse res = httpClient.execute(httpPost);
            result = res.getStatusLine().getStatusCode();
            if (res.getStatusLine().getStatusCode() != 200) {
                logger.error(String.valueOf(result));
            }
            res.close();
        } catch (IOException e) {
            String errorMsg = url + ":httpPostWithJSON connect faild";
            logger.error(errorMsg, e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                String errorMsg = url + ":close  httpClient faild";
                logger.error(errorMsg, e);
            }
        }

        return result;

    }

    public static void delete(String url, String parameter) throws Exception {
        String result = null;
        String baseUrl;
        if (parameter != null) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("serviceName", parameter));
            baseUrl = url + "?" + URLEncodedUtils.format(params, "UTF-8");
        } else {
            baseUrl = url;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();;
        try {

            HttpDelete httpDelete = new HttpDelete(baseUrl);
            CloseableHttpResponse res = httpClient.execute(httpDelete);

            if (res.getStatusLine().getStatusCode() != 200) {
                throw new Exception("delete fail");
            }

            res.close();
        } catch (IOException e) {
            String errorMsg = baseUrl + ":delete connect faild";
            logger.error(errorMsg, e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                String errorMsg = baseUrl + ":close  httpClient faild";
                logger.error(errorMsg, e);
            }
        }

    }

    public static String httpGet(String url) {
        String result = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET);
        httpGet.setHeader(ACCEPT, APPLICATION_JSON);
        try {
            CloseableHttpResponse res = httpClient.execute(httpGet);

            res.getLastHeader("X-Consul-Index");
            result = EntityUtils.toString(res.getEntity());
            if (res.getStatusLine().getStatusCode() != 200) {
                logger.error(result);
            }
            res.close();
        } catch (ClientProtocolException e) {
            String errorMsg = url + ":httpGetWithJSON connect faild";
            logger.error(errorMsg);
        } catch (IOException e) {
            String errorMsg = url + ":httpGetWithJSON connect faild";
            logger.error(errorMsg);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                String errorMsg = url + ":close  httpClient faild";
                logger.error(errorMsg);
            }
        }

        return result;

    }

    @SuppressWarnings("unchecked")
    public static <T> ConsulResponse<T> httpWaitGet(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET);
        httpGet.setHeader(ACCEPT, APPLICATION_JSON);
        try {
            CloseableHttpResponse res = httpClient.execute(httpGet);
            String result = EntityUtils.toString(res.getEntity());

            if (res.getStatusLine().getStatusCode() != 200) {
                logger.error(result);
            } else {
                String indexHeaderValue = res.getLastHeader("X-Consul-Index").getValue();
                BigInteger index = new BigInteger(indexHeaderValue);

                return new ConsulResponse<T>((T) result, index);

            }

            res.close();
        } catch (ClientProtocolException e) {
            String errorMsg = url + ":httpGetWithJSON connect faild " + e.getMessage();
            logger.error(errorMsg);
        } catch (IOException e) {
            String errorMsg = url + ":httpGetWithJSON connect faild " + e.getMessage();
            logger.error(errorMsg);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                String errorMsg = url + ":close  httpClient faild " + e.getMessage();
                logger.error(errorMsg);
            }
        }

        return null;

    }

    public static int httpGetStatus(String url) throws Exception {
        int iStatus = 500;
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();// 设置请求和传输超时时间
        httpGet.setConfig(requestConfig);
        httpGet.addHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET);
        httpGet.setHeader(ACCEPT, APPLICATION_JSON);
        try {
            CloseableHttpResponse res = httpClient.execute(httpGet);

            iStatus = res.getStatusLine().getStatusCode();
            res.close();
        } catch (ClientProtocolException e) {
            logger.error(url, " httpGet connect faild:" , e);
        } catch (IOException e) {
            logger.error(url, " httpGet connect faild:" , e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error(url ," httpGet close faild:" , e);
            }
        }

        return iStatus;

    }

}
