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

import org.junit.Assert;
import org.junit.Test;


public class HttpClientUtilTest {

    private String testIp = "http://127.0.0.1:8500";
    private String errorIp = "http://127.0.0.3:8500";

    @Test
    public void test_httpGetStatus() {
        try {
            int result = HttpClientUtil.httpGetStatus(testIp);
            if (result == 200) {
                Assert.assertEquals("Consul Agent", HttpClientUtil.httpGet(testIp));
            } else {
                Assert.assertEquals(500, result);
            }


        } catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void test_httpGet_fail() {
        try {
            Assert.assertNull(HttpClientUtil.httpGet(errorIp));


        } catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void test_httpWaitGet_fail() {
        try {
            Assert.assertNull(HttpClientUtil.httpWaitGet(errorIp));


        } catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }
    }


    @Test
    public void test_httpdelete() {
        try {
            HttpClientUtil.delete(testIp, "service");

        } catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void test_httpPostWithJSON() {
        String url = testIp + "/v1/catalog/service";
        String json =
                        "[{\"Node\":\"server\",\"Address\":\"127.0.0.1\",\"TaggedAddresses\":{\"lan\":\"127.0.0.1\",\"wan\":\"127.0.0.1\"},\"ServiceID\":\"_CJ-SNMP_10.74.216.65_12005\",\"ServiceName\":\"CJ-SNMP\",\"ServiceAddress\":\"10.74.216.65\",\"ServicePort\":12005,\"ServiceEnableTagOverride\":false,\"CreateIndex\":1813280,\"ModifyIndex\":1815062}]";

        try {
            int result = HttpClientUtil.httpPostWithJSON(url, json);
            if (result == 201) {
                Assert.assertEquals("Consul Agent", HttpClientUtil.httpGet(testIp));
            } else {
                Assert.assertEquals(0, result);
            }

        } catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }
    }
}
