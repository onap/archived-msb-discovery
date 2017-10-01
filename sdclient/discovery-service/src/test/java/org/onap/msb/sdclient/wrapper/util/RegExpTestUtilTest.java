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
import org.junit.Test;



public class RegExpTestUtilTest {

    @Test
    public void testhostRegExpTest() {
        Assert.assertTrue(RegExpTestUtil.hostRegExpTest("127.0.0.1:8080"));

        Assert.assertFalse(RegExpTestUtil.hostRegExpTest("0.0.0.1:89"));
    }

    @Test
    public void testhttpUrlRegExpTest() {
        Assert.assertTrue(RegExpTestUtil.httpUrlRegExpTest("http://10.74.151.26:8989"));

        Assert.assertFalse(RegExpTestUtil.httpUrlRegExpTest("httpr://0.74.0.26:8989"));
    }

    @Test
    public void testipRegExpTest() {

        Assert.assertTrue(RegExpTestUtil.ipRegExpTest("10.74.151.26"));

        Assert.assertFalse(RegExpTestUtil.ipRegExpTest("0.74.0.26"));
    }

    @Test
    public void testportRegExpTest() {
        Assert.assertTrue(RegExpTestUtil.portRegExpTest("8989"));

        Assert.assertFalse(RegExpTestUtil.portRegExpTest("99999"));
    }

    @Test
    public void testversionRegExpTest() {

        Assert.assertTrue(RegExpTestUtil.versionRegExpTest("v1"));

        Assert.assertFalse(RegExpTestUtil.versionRegExpTest("vv2"));
    }

    @Test
    public void testurlRegExpTest() {

        Assert.assertTrue(RegExpTestUtil.urlRegExpTest("/test/v1"));

        Assert.assertTrue(RegExpTestUtil.urlRegExpTest("/"));

        Assert.assertFalse(RegExpTestUtil.urlRegExpTest("test/#?qwe"));
    }

    @Test
    public void testserviceNameRegExpTest() {

        Assert.assertTrue(RegExpTestUtil.serviceNameRegExpTest("servive_1"));


        Assert.assertFalse(RegExpTestUtil.serviceNameRegExpTest("servive%_1"));
    }


    @Test
    public void testapiRouteUrlRegExpTest() {

        Assert.assertTrue(RegExpTestUtil.apiRouteUrlRegExpTest("/api/service/v1"));


        Assert.assertFalse(RegExpTestUtil.apiRouteUrlRegExpTest("/servive"));
    }

    @Test
    public void testlabelRegExpTest() {

        Assert.assertTrue(RegExpTestUtil.labelRegExpTest("key:value,key2:value2"));


        Assert.assertFalse(RegExpTestUtil.labelRegExpTest("keyvalue,key2*value2"));
    }



}
