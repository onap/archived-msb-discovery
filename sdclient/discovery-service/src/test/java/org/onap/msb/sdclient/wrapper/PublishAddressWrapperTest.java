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

package org.onap.msb.sdclient.wrapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.onap.msb.sdclient.core.ConsulResponse;
import org.onap.msb.sdclient.core.KeyVaulePair;
import org.onap.msb.sdclient.core.MicroServiceFullInfo;
import org.onap.msb.sdclient.core.NodeInfo;
import org.onap.msb.sdclient.core.PublishAddress;
import org.onap.msb.sdclient.core.PublishFullAddress;
import org.onap.msb.sdclient.core.exception.UnprocessableEntityException;

import org.onap.msb.sdclient.wrapper.util.HttpClientUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;



@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClientUtil.class})
public class PublishAddressWrapperTest {

    private static final String restJson =
                    "[{\"Node\":{\"Node\":\"A23179111\",\"Address\":\"10.74.44.16\"},\"Service\":{\"ID\":\"_test_10.74.44.16_2356\",\"Service\":\"test\",\"Tags\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"REST\\\",\\\"url\\\":\\\"/api/test/v1\\\",\\\"version\\\":\\\"v1\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"0|1\\\",\\\"apigateway\\\":\\\"test\\\",\\\"network_plane_type\\\":\\\"net\\\"}\"],\"Address\":\"10.74.44.16\",\"Port\":8086},\"Checks\":[{\"CheckID\":\"serfHealth\",\"Name\":\"Serf Health Status\",\"Status\":\"passing\"}]}]";
    private static final String mockRestUrl = "http://127.0.0.1:8500/v1/health/service/test-v1";

    private static final String uiJson =
                    "[{\"Node\":{\"Node\":\"A23179111\",\"Address\":\"10.74.44.16\"},\"Service\":{\"ID\":\"_test_10.74.44.16_2356\",\"Service\":\"IUI_test\",\"Tags\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"UI\\\",\\\"url\\\":\\\"/iui/test\\\",\\\"path\\\":\\\"/iui/test_path\\\",\\\"host\\\":\\\"testhost\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"0|1\\\",\\\"apigateway\\\":\\\"test\\\",\\\"network_plane_type\\\":\\\"net\\\"}\"],\"Address\":\"10.74.44.16\",\"Port\":8086},\"Checks\":[{\"CheckID\":\"serfHealth\",\"Name\":\"Serf Health Status\",\"Status\":\"passing\"}]}]";
    private static final String mockUIUrl = "http://127.0.0.1:8500/v1/health/service/IUI_test";

    private static final String tcpJson =
                    "[{\"Node\":{\"Node\":\"A23179111\",\"Address\":\"10.74.44.16\"},\"Service\":{\"ID\":\"_test_10.74.44.16_2356\",\"Service\":\"tcp_test\",\"Tags\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"TCP\\\",\\\"url\\\":\\\"/\\\",\\\"version\\\":\\\"v1\\\",\\\"publish_port\\\":\\\"40001\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"0|1\\\",\\\"apigateway\\\":\\\"test\\\",\\\"network_plane_type\\\":\\\"net\\\"}\"],\"Address\":\"10.74.44.16\",\"Port\":8086},\"Checks\":[{\"CheckID\":\"serfHealth\",\"Name\":\"Serf Health Status\",\"Status\":\"passing\"}]}]";
    private static final String mockTCPUrl = "http://127.0.0.1:8500/v1/health/service/tcp_test-v1";

    private static final String httpJson =
                    "[{\"Node\":{\"Node\":\"A23179111\",\"Address\":\"10.74.44.16\"},\"Service\":{\"ID\":\"_test_10.74.44.16_2356\",\"Service\":\"test\",\"Tags\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"HTTP\\\",\\\"url\\\":\\\"/test\\\",\\\"version\\\":\\\"v1\\\",\\\"host\\\":\\\"testhost\\\"}\",\"\\\"ns\\\":{\\\"namespace\\\":\\\"ns\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"0|1\\\",\\\"apigateway\\\":\\\"test\\\",\\\"network_plane_type\\\":\\\"net\\\"}\"],\"Address\":\"10.74.44.16\",\"Port\":8086},\"Checks\":[{\"CheckID\":\"serfHealth\",\"Name\":\"Serf Health Status\",\"Status\":\"passing\"}]}]";
    private static final String mockHTTPUrl = "http://127.0.0.1:8500/v1/health/service/test-v1-ns";



    private static PublishAddressWrapper publishAddressWrapper = PublishAddressWrapper.getInstance();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        mockinitApigatewayInfo();
    }


    @Test
    public void test_get_apigateway_publishaddress4Rest() {
        mockGetRest();

        Set<PublishFullAddress> publishFullAddressList =
                        publishAddressWrapper.getAllPublishaddress("test", "v1", "", "1");
        int addressNum = publishFullAddressList.size();
        Assert.assertEquals(2, addressNum);

    }


    @Test
    public void test_get_router_publishaddress4Ui() {
        mockgetUi();

        Set<PublishFullAddress> publishFullAddressList =
                        publishAddressWrapper.getAllPublishaddress("IUI_test", "null", "", "0");
        int addressNum = publishFullAddressList.size();
        Assert.assertEquals(2, addressNum);


    }

    @Test
    public void test_get_router_publishaddress4Http() {
        mockgetHttp();

        Set<PublishFullAddress> publishFullAddressList =
                        publishAddressWrapper.getAllPublishaddress("test", "v1", "ns", "0");
        int addressNum = publishFullAddressList.size();
        Assert.assertEquals(1, addressNum);


    }

    @Test
    public void test_get_router_publishaddress4Tcp() {
        mockgetTcp();

        Set<PublishFullAddress> publishFullAddressList =
                        publishAddressWrapper.getAllPublishaddress("tcp_test", "v1", "", "0");
        int addressNum = publishFullAddressList.size();
        Assert.assertEquals(2, addressNum);



    }


    @Test
    public void test_get_apigateway_publishaddress4nameError() {
        try {
            publishAddressWrapper.getAllPublishaddress("*.test", "v1", "", "1");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnprocessableEntityException);
        }
    }

    @Test
    public void test_get_apigateway_publishaddress4nameEmpty() {
        try {
            publishAddressWrapper.getAllPublishaddress("", "v1", "", "1");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnprocessableEntityException);
        }
    }

    @Test
    public void test_get_apigateway_publishaddress4versionError() {
        try {
            publishAddressWrapper.getAllPublishaddress("test", "verison1", "", "1");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnprocessableEntityException);
        }
    }

    @Test
    public void test_get_apigateway_publishaddress4visualRangeError() {
        try {
            publishAddressWrapper.getAllPublishaddress("test", "v1", "", "12");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnprocessableEntityException);
        }
    }

    @Test
    public void test_getPublishaddress4Rest() {
        mockGetRest();
        PublishAddress publishAddress = publishAddressWrapper.getPublishaddress("test", "v1", "", 0);
        String test_address = "10.74.44.2:10080/api/test/v1";
        Assert.assertEquals(test_address, publishAddress.toString());
    }

    @Test
    public void test_getPublishaddress4UI() {
        mockgetUi();
        PublishAddress publishAddress = publishAddressWrapper.getPublishaddress("IUI_test", "", "", 1);
        String test_address = "10.74.44.2:10080/iui/test_path";
        Assert.assertEquals(test_address, publishAddress.toString());
    }

    @Test
    public void test_getPublishaddress4HTTP() {
        mockgetHttp();
        PublishAddress publishAddress = publishAddressWrapper.getPublishaddress("test", "v1", "ns", 1);
        String test_address = "10.74.44.1:10080/test/v1";
        Assert.assertEquals(test_address, publishAddress.toString());
    }

    @Test
    public void test_getPublishaddress4time() {
        mockgetTcp();
        PublishAddress publishAddress = publishAddressWrapper.getPublishaddress("tcp_test", "v1", "", 5);
        String test_address = "10.74.44.2:10080/";
        Assert.assertEquals(test_address, publishAddress.toString());
    }

    @Test
    public void test_getApigatewayServiceInfo_in() {
        Set<MicroServiceFullInfo> apigatewayList = publishAddressWrapper.getApigatewayServiceInfo("all", "1");
        Iterator<MicroServiceFullInfo> apigateway = apigatewayList.iterator();

        Set<NodeInfo> nodes = apigateway.next().getNodes();

        NodeInfo testNode = new NodeInfo();
        testNode.setIp("10.74.44.1");
        testNode.setPort("10080");

        NodeInfo node = new NodeInfo();
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            node = (NodeInfo) it.next();
        }

        Assert.assertEquals(testNode, node);
    }


    /*
     * @Test public void test_getApigatewayServiceInfo_out() { List<MicroServiceFullInfo>
     * apigatewayList = publishAddressWrapper.getApigatewayServiceInfo("all", "0"); Set<NodeInfo>
     * nodes = apigatewayList.get(0).getNodes();
     * 
     * NodeInfo testNode = new NodeInfo(); testNode.setIp("10.74.44.3"); testNode.setPort("80");
     * 
     * NodeInfo node = new NodeInfo(); Iterator it = nodes.iterator(); while (it.hasNext()) { node =
     * (NodeInfo) it.next(); }
     * 
     * Assert.assertEquals(testNode, node); }
     */

    @Test
    public void test_getApigatewayServiceInfo_422ERROR() {
        try {
            Set<MicroServiceFullInfo> apigatewayList = publishAddressWrapper.getApigatewayServiceInfo("test", "2");

            Assert.fail("should not process to here.");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnprocessableEntityException);
        }

    }



    private void mockGetRest() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        ConsulResponse<Object> consulResponse = new ConsulResponse(restJson, new BigInteger("1000"));
        PowerMockito.when(HttpClientUtil.httpWaitGet(mockRestUrl)).thenReturn(consulResponse);
    }

    private void mockgetUi() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        ConsulResponse<Object> consulResponse = new ConsulResponse(uiJson, new BigInteger("1000"));
        PowerMockito.when(HttpClientUtil.httpWaitGet(mockUIUrl)).thenReturn(consulResponse);
    }

    private void mockgetHttp() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        ConsulResponse<Object> consulResponse = new ConsulResponse(httpJson, new BigInteger("1000"));
        PowerMockito.when(HttpClientUtil.httpWaitGet(mockHTTPUrl)).thenReturn(consulResponse);
    }

    private void mockgetTcp() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        ConsulResponse<Object> consulResponse = new ConsulResponse(tcpJson, new BigInteger("1000"));
        PowerMockito.when(HttpClientUtil.httpWaitGet(mockTCPUrl)).thenReturn(consulResponse);
    }


    private static void mockinitApigatewayInfo() {
        mockApigatewayInfo4all();
        mockApigatewayInfo4Default();
        mockRouterInfo4all();
        mockRouterInfo4Default();
    }

    private static void mockApigatewayInfo4all() {
        MicroServiceFullInfo serviceInfo = new MicroServiceFullInfo();
        serviceInfo.setServiceName("apigateway");
        serviceInfo.setVersion("v1");
        serviceInfo.setUrl("/api/microservices/v1");
        serviceInfo.setProtocol("REST");
        serviceInfo.setVisualRange("1");
        serviceInfo.setNamespace("all");
        Set<NodeInfo> nodes = new HashSet<NodeInfo>();
        NodeInfo node = new NodeInfo();
        node.setIp("10.74.44.1");
        node.setPort("10080");
        nodes.add(node);
        serviceInfo.setNodes(nodes);

        List<MicroServiceFullInfo> apigatewayList = new ArrayList<MicroServiceFullInfo>();
        apigatewayList.add(serviceInfo);
        publishAddressWrapper.publishApigateWayList.put("apigateway-all", apigatewayList);

    }

    private static void mockApigatewayInfo4Default() {
        MicroServiceFullInfo serviceInfo = new MicroServiceFullInfo();
        serviceInfo.setServiceName("apigateway");
        serviceInfo.setVersion("v1");
        serviceInfo.setUrl("/api/microservices/v1");
        serviceInfo.setProtocol("REST");
        serviceInfo.setVisualRange("1");
        serviceInfo.setNamespace("default");
        List<String> labels = new ArrayList<String>();
        labels.add("apigateway:test|test2");
        serviceInfo.setLabels(labels);
        Set<NodeInfo> nodes = new HashSet<NodeInfo>();
        NodeInfo node = new NodeInfo();
        node.setIp("10.74.44.2");
        node.setPort("10080");
        nodes.add(node);
        serviceInfo.setNodes(nodes);

        List<MicroServiceFullInfo> apigatewayList = new ArrayList<MicroServiceFullInfo>();
        apigatewayList.add(serviceInfo);
        publishAddressWrapper.publishApigateWayList.put("apigateway-default", apigatewayList);
    }


    private static void mockRouterInfo4all() {
        MicroServiceFullInfo serviceInfo = new MicroServiceFullInfo();
        serviceInfo.setServiceName("router");
        serviceInfo.setVersion("v1");
        serviceInfo.setUrl("/api/microservices/v1");
        serviceInfo.setProtocol("REST");
        serviceInfo.setVisualRange("0");
        serviceInfo.setNamespace("all");

        Set<NodeInfo> nodes = new HashSet<NodeInfo>();
        NodeInfo node = new NodeInfo();
        node.setIp("10.74.44.3");
        node.setPort("80");
        nodes.add(node);
        serviceInfo.setNodes(nodes);

        List<MicroServiceFullInfo> apigatewayList = new ArrayList<MicroServiceFullInfo>();
        apigatewayList.add(serviceInfo);
        publishAddressWrapper.publishApigateWayList.put("router-all", apigatewayList);
    }

    private static void mockRouterInfo4Default() {
        MicroServiceFullInfo serviceInfo = new MicroServiceFullInfo();
        serviceInfo.setServiceName("router");
        serviceInfo.setVersion("v1");
        serviceInfo.setUrl("/api/microservices/v1");
        serviceInfo.setProtocol("REST");
        serviceInfo.setVisualRange("0");
        serviceInfo.setNamespace("default");
        serviceInfo.setNetwork_plane_type("net|net2");
        List<KeyVaulePair> metadatas = new ArrayList<KeyVaulePair>();
        metadatas.add(new KeyVaulePair("routeWay", "domain"));
        metadatas.add(new KeyVaulePair("routeSubdomain", "openpalette.zte.com.cn"));
        serviceInfo.setMetadata(metadatas);

        Set<NodeInfo> nodes = new HashSet<NodeInfo>();
        NodeInfo node = new NodeInfo();
        node.setIp("10.74.44.4");
        node.setPort("80");
        nodes.add(node);
        serviceInfo.setNodes(nodes);

        List<MicroServiceFullInfo> apigatewayList = new ArrayList<MicroServiceFullInfo>();
        apigatewayList.add(serviceInfo);
        publishAddressWrapper.publishApigateWayList.put("router-default", apigatewayList);
    }

}
