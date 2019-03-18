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
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.msb.sdclient.DiscoverAppConfig;
import org.onap.msb.sdclient.core.ConsulResponse;
import org.onap.msb.sdclient.core.KeyVaulePair;
import org.onap.msb.sdclient.core.MicroServiceFullInfo;
import org.onap.msb.sdclient.core.MicroServiceInfo;
import org.onap.msb.sdclient.core.Node;
import org.onap.msb.sdclient.core.NodeAddress;
import org.onap.msb.sdclient.core.NodeInfo;
import org.onap.msb.sdclient.core.exception.ExtendedNotFoundException;
import org.onap.msb.sdclient.core.exception.UnprocessableEntityException;
import org.onap.msb.sdclient.wrapper.util.ConfigUtil;
import org.onap.msb.sdclient.wrapper.util.HttpClientUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClientUtil.class, ConfigUtil.class, DiscoverAppConfig.class})
public class ConsulServiceWrapperTest {

    private static final String restJson =
                    "[{\"Node\":{\"Node\":\"server\",\"Address\":\"127.0.0.1\"},\"Service\":{\"ID\":\"_test_10.74.56.36_5656\",\"Service\":\"test\",\"Tags\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"REST\\\",\\\"ha_role\\\":\\\"active\\\",\\\"is_manual\\\":\\\"true\\\",\\\"version\\\":\\\"v1\\\",\\\"url\\\":\\\"/test\\\",\\\"status\\\":\\\"1\\\"}\",\"\\\"lb\\\":{\\\"lb_server_params\\\":\\\"weight=10 max_fails=10 fail_timeout=10s\\\",\\\"lb_policy\\\":\\\"ip_hash\\\"}\",\"\\\"checks\\\":{\\\"http\\\":\\\"http://10.74.56.36:5656\\\",\\\"interval\\\":\\\"10s\\\",\\\"timeout\\\":\\\"10s\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"0\\\",\\\"key\\\":\\\"value\\\"}\",\"\\\"metadata\\\":{\\\"key\\\":\\\"value\\\"}\"],\"Address\":\"10.74.56.36\",\"Port\":5656},\"Checks\":[{\"Node\":\"server\",\"CheckID\":\"service:_test_10.74.56.36_5656\",\"Name\":\"Service 'test' check\",\"Status\":\"critical\"}]}]";
    private static final String catalogJson =
                    "[{\"Node\":\"server\",\"Address\":\"127.0.0.1\",\"TaggedAddresses\":{\"lan\":\"127.0.0.1\",\"wan\":\"127.0.0.1\"},\"ServiceID\":\"_test_10.74.56.36_5656\",\"ServiceName\":\"test\",\"ServiceTags\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"REST\\\",\\\"version\\\":\\\"v1\\\",\\\"url\\\":\\\"/test\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"0\\\"}\"],\"ServiceAddress\":\"10.74.56.36\",\"ServicePort\":5656,\"ServiceEnableTagOverride\":false,\"CreateIndex\":1819452,\"ModifyIndex\":1819454}]";
    private static final String catalog4ttlJson =
                    "[{\"Node\":\"server\",\"Address\":\"127.0.0.1\",\"TaggedAddresses\":{\"lan\":\"127.0.0.1\",\"wan\":\"127.0.0.1\"},\"ServiceID\":\"_test_10.74.56.36_5656\",\"ServiceName\":\"test\",\"ServiceTags\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"REST\\\",\\\"version\\\":\\\"v1\\\",\\\"url\\\":\\\"/test\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"0\\\"}\",\"\\\"checks\\\":{\\\"ttl\\\":\\\"10s\\\"}\"],\"ServiceAddress\":\"10.74.56.36\",\"ServicePort\":5656,\"ServiceEnableTagOverride\":false,\"CreateIndex\":1819452,\"ModifyIndex\":1819454}]";
    private static final String mockRestUrl = "http://127.0.0.1:8500/v1/health/service/test-v1";
    private static final String mockPostUrl = "http://127.0.0.1:8500/v1/catalog/register";
    private static final String mockdel_gettUrl = "http://127.0.0.1:8500/v1/catalog/service/test-v1";
    private static final String mockdeltUrl = "http://127.0.0.1:8500/v1/catalog/deregister";
    private static final String mockdeltUrl4agent = "http://127.0.0.1:8500/v1/agent/deregister";

    private static final String mockgetListUrl = "http://127.0.0.1:8500/v1/catalog/services";
    private static final String restListJson =
                    "{\"consul\":[],\"test-tt\":[\"\\\"labels\\\":{\\\"visualRange\\\":\\\"0\\\"}\",\"\\\"base\\\":{\\\"version\\\":\\\"v1\\\",\\\"protocol\\\":\\\"REST\\\",\\\"url\\\":\\\"/api/microservices/v1\\\",\\\"visualRange\\\":\\\"0\\\"}\",\"\\\"ns\\\":{\\\"namespace\\\":\\\"tt\\\"}\"]}";


    private static ConsulServiceWrapper consulServiceWrapper = ConsulServiceWrapper.getInstance();


    @Test
    public void test_getAllMicroServiceInstances() {
        mockGetList();
        List<MicroServiceFullInfo> serviceList = consulServiceWrapper.getAllMicroServiceInstances();
        Assert.assertEquals(1, serviceList.size());
        MicroServiceFullInfo service = serviceList.get(0);
        Assert.assertEquals("test-tt", service.getServiceName());
        Assert.assertEquals("", service.getNamespace());
    }


    @Test
    public void test_getMicroServiceInstance() {
        mockGetRest();
        MicroServiceFullInfo service = consulServiceWrapper.getMicroServiceInstance("test", "v1", "");
        Assert.assertEquals(service.getServiceName(), "test");
        Assert.assertEquals(service.getProtocol(), "REST");
    }

    @Test
    public void test_getMicroServiceForNodes() {
        mockGetRest();
        List<MicroServiceFullInfo> services = consulServiceWrapper.getMicroServiceForNodes("test", "v1", false, "", "");
        Assert.assertEquals(1, services.size());
        Assert.assertEquals(services.get(0).getServiceName(), "test");
        Assert.assertEquals(services.get(0).getProtocol(), "REST");
    }

    @Test
    public void test_saveMicroServiceInstance() {
        MicroServiceInfo serviceInfo = new MicroServiceInfo();
        serviceInfo.setServiceName("test");
        serviceInfo.setVersion("v1");
        serviceInfo.setNamespace("ns");
        serviceInfo.setPublish_port("8086");
        serviceInfo.setUrl("/api/test/v1");
        serviceInfo.setProtocol("REST");
        serviceInfo.setVisualRange("1");
        serviceInfo.setLb_policy("ip_hash");
        serviceInfo.setHost("host");
        serviceInfo.setPath("/test");
        serviceInfo.setNetwork_plane_type("net");

        List<KeyVaulePair> metadata = new ArrayList<KeyVaulePair>();
        metadata.add(new KeyVaulePair("key1", "val1"));
        metadata.add(new KeyVaulePair("key2", "val2"));
        serviceInfo.setMetadata(metadata);


        List<String> labels = new ArrayList<String>();
        labels.add("111:111");
        labels.add("222:222");
        serviceInfo.setLabels(labels);

        Set<Node> nodes = new HashSet<Node>();
        Node node = new Node();
        node.setIp("10.74.44.1");
        node.setPort("10080");
        node.setLb_server_params("weight=1,max_fails=5,fail_timeout=8");
        node.setHa_role("active");
        node.setCheckType("HTTP");
        node.setCheckInterval("10");
        node.setCheckTimeOut("10");
        node.setCheckUrl("http://check");
        nodes.add(node);
        serviceInfo.setNodes(nodes);

        mockGetRest4null();
        // mockGetPost();
        try {
            consulServiceWrapper.saveMicroServiceInstance(serviceInfo, true, "127.0.0.1", true);
        } catch (Exception e) {
            Assert.assertEquals("HTTP 500 Internal Server Error", e.getMessage());
        }
    }


    @Test
    public void test_saveMicroServiceInstance2() {
        MicroServiceInfo serviceInfo = new MicroServiceInfo();
        serviceInfo.setServiceName("test");
        serviceInfo.setVersion("v1");
        serviceInfo.setNamespace("ns");
        serviceInfo.setPublish_port("28005");
        serviceInfo.setUrl("/api/test/v1");
        serviceInfo.setProtocol("TCP");
        serviceInfo.setVisualRange("1");
        serviceInfo.setLb_policy("ip_hash");
        serviceInfo.setHost("host");
        serviceInfo.setPath("/test");
        serviceInfo.setNetwork_plane_type("net");

        List<KeyVaulePair> metadata = new ArrayList<KeyVaulePair>();
        metadata.add(new KeyVaulePair("key1", "val1"));
        metadata.add(new KeyVaulePair("key2", "val2"));
        serviceInfo.setMetadata(metadata);


        List<String> labels = new ArrayList<String>();
        labels.add("111:111");
        labels.add("222:222");
        serviceInfo.setLabels(labels);

        Set<Node> nodes = new HashSet<Node>();
        Node node = new Node();
        node.setIp("10.74.44.1");
        node.setPort("28005");
        node.setLb_server_params("weight=1,max_fails=5,fail_timeout=8");
        node.setHa_role("active");
        node.setCheckType("TCP");
        node.setCheckInterval("10");
        node.setCheckTimeOut("10");
        node.setCheckUrl("tcp://check");
        nodes.add(node);
        serviceInfo.setNodes(nodes);

        mockGetRest4null();
        // mockGetPost();
        try {
            consulServiceWrapper.saveMicroServiceInstance(serviceInfo, true, "127.0.0.1", true);
        } catch (Exception e) {
            Assert.assertEquals("HTTP 500 Internal Server Error", e.getMessage());
        }
    }

    @Test
    public void test_saveMicroServiceInstance4agent() {
        MicroServiceInfo serviceInfo = new MicroServiceInfo();
        serviceInfo.setServiceName("test");
        serviceInfo.setVersion("v1");
        serviceInfo.setUrl("/api/test/v1");
        serviceInfo.setProtocol("REST");
        serviceInfo.setVisualRange("1");
        Set<Node> nodes = new HashSet<Node>();
        Node node = new Node();
        node.setIp("10.74.44.1");
        node.setPort("10080");
        nodes.add(node);
        serviceInfo.setNodes(nodes);

        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv("CONSUL_REGISTER_MODE")).thenReturn("agent");
        DiscoverAppConfig discoverConfig = PowerMockito.mock(DiscoverAppConfig.class);
        ConfigUtil.getInstance().initConsulRegisterMode(discoverConfig);


        mockGetRest4null();
        // mockGetPost();
        try {
            consulServiceWrapper.saveMicroServiceInstance(serviceInfo, true, "127.0.0.1", true);
        } catch (Exception e) {
            Assert.assertEquals("HTTP 500 Internal Server Error", e.getMessage());
        }

        PowerMockito.when(System.getenv("CONSUL_REGISTER_MODE")).thenReturn("catalog");
        ConfigUtil.getInstance().initConsulRegisterMode(discoverConfig);
    }

    @Test
    public void test_deleteMicroService() {
        mockGet4Delete();
        mockDelete();
        consulServiceWrapper.deleteMicroService("test", "v1", "");
    }

    @Test
    public void test_deleteMicroServiceInstance() {
        mockGet4Delete();
        mockDelete();
        consulServiceWrapper.deleteMicroServiceInstance("test", "v1", "", "10.74.56.36", "5656");
    }

    @Test
    public void test_deleteMicroService4agent() {
        mockGet4Delete();
        mockDelete4agent();

        DiscoverAppConfig discoverConfig = PowerMockito.mock(DiscoverAppConfig.class);
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv("CONSUL_REGISTER_MODE")).thenReturn("agent");
        ConfigUtil.getInstance().initConsulRegisterMode(discoverConfig);

        consulServiceWrapper.deleteMicroService("test", "v1", "");

        PowerMockito.when(System.getenv("CONSUL_REGISTER_MODE")).thenReturn("catalog");
        ConfigUtil.getInstance().initConsulRegisterMode(discoverConfig);
    }



    @Test
    public void test_healthCheckbyTTL() {
        NodeAddress nodeAddress = new NodeAddress("10.74.56.36", "5656");
        mockGet4healthCheck();
        consulServiceWrapper.healthCheckbyTTL("test", "v1", "", nodeAddress);
    }



    private void mockGetRest() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        ConsulResponse<Object> consulResponse = new ConsulResponse(restJson, new BigInteger("1000"));
        PowerMockito.when(HttpClientUtil.httpWaitGet(mockRestUrl)).thenReturn(consulResponse);
    }


    private void mockGet4Delete() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        PowerMockito.when(HttpClientUtil.httpGet(mockdel_gettUrl)).thenReturn(catalogJson);

    }

    private void mockGetList() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        PowerMockito.when(HttpClientUtil.httpGet(mockgetListUrl)).thenReturn(restListJson);

        ConsulResponse<Object> consulResponse = new ConsulResponse(restJson, new BigInteger("1000"));
        PowerMockito.when(HttpClientUtil.httpWaitGet("http://127.0.0.1:8500/v1/health/service/test-tt"))
                        .thenReturn(consulResponse);

    }

    private void mockGet4healthCheck() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        PowerMockito.when(HttpClientUtil.httpGet(mockdel_gettUrl)).thenReturn(catalog4ttlJson);


        String checkUrl = "http://127.0.0.1:8500/v1/agent/check/pass/service:_test_10.74.56.36_5656";
        PowerMockito.when(HttpClientUtil.httpGet(checkUrl)).thenReturn("ok");

    }

    private void mockDelete() {
        String serviceJson = "{\"Node\": \"externalService\",\"ServiceID\": \"_test_10.74.56.36_5656\"}";
        PowerMockito.when(HttpClientUtil.httpPutWithJSON(mockdeltUrl, serviceJson)).thenReturn(200);

    }

    private void mockDelete4agent() {
        PowerMockito.when(HttpClientUtil.httpPutWithJSON(
                        "http://127.0.0.1:8500/v1/agent/service/deregister/_test_10.74.56.36_5656", ""))
                        .thenReturn(200);

    }



    private void mockGetRest4null() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        PowerMockito.when(HttpClientUtil.httpWaitGet(mockRestUrl)).thenReturn(null);
    }

    private void mockGetPost() {
        PowerMockito.mockStatic(HttpClientUtil.class);
        String serviceJson =
                        "{\"Node\":\"externalService\",\"Address\":\"127.0.0.1\",\"Service\":{\"ID\":\"_test_10.74.44.1_10080\",\"Service\":\"test\",\"Tags\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"REST\\\",\\\"status\\\":\\\"1\\\",\\\"enable_ssl\\\":\\\"false\\\",\\\"is_manual\\\":\\\"true\\\",\\\"url\\\":\\\"/api/test/v1\\\",\\\"version\\\":\\\"v1\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"1\\\"}\"],\"Address\":\"10.74.44.1\",\"Port\":10080}}";
        PowerMockito.when(HttpClientUtil.httpPutWithJSON(mockPostUrl, serviceJson)).thenReturn(200);


    }

}
