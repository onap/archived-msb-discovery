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

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.onap.msb.sdclient.core.CatalogService;
import org.onap.msb.sdclient.core.HealthService;
import org.onap.msb.sdclient.core.NodeAddress;


public class JacksonJsonUtilTest {
    @Test
    public void testBeanToJson() {
        try {
            NodeAddress address = new NodeAddress("127.0.0.1", "80");
            String json = JacksonJsonUtil.beanToJson(address);
            Assert.assertEquals("{\"ip\":\"127.0.0.1\",\"port\":\"80\"}", json);
        } catch (Exception e) {
            Assert.fail("Exception" + e.getMessage());
        }
    }

    @Test
    public void testJsonToBean() {
        try {
            String json = "{\"ip\":\"127.0.0.1\",\"port\":\"80\"}";
            NodeAddress address = (NodeAddress) JacksonJsonUtil.jsonToBean(json, NodeAddress.class);
            Assert.assertEquals("127.0.0.1", address.getIp());
            Assert.assertEquals("80", address.getPort());
        } catch (Exception e) {
            Assert.fail("Exception" + e.getMessage());
        }
    }



    @Test
    public void testJsonToListBean() {
        try {
            String json =
                            "[{\"Node\":{\"Node\":\"A23179111\",\"Address\":\"10.74.44.27\",\"CreateIndex\":3,\"ModifyIndex\":318},\"Service\":{\"ID\":\"oo_10.74.56.36_5656\",\"Service\":\"oo\",\"Tags\":[\"url:/root\",\"protocol:REST\",\"version:\",\"visualRange:0|1\",\"ttl:-1\",\"status:1\",\"lb_policy:client_custom\",\"lb_server_params:weight=1 max_fails=1 fail_timeout=16s\",\"checkType:TCP\",\"checkInterval:10\",\"checkUrl:10.56.23.63:8989\"],\"Address\":\"10.74.56.36\",\"Port\":5656,\"EnableTagOverride\":false,\"CreateIndex\":314,\"ModifyIndex\":318},\"Checks\":[{\"Node\":\"A23179111\",\"CheckID\":\"serfHealth\",\"Name\":\"Serf Health Status\",\"Status\":\"passing\",\"Notes\":\"\",\"Output\":\"Agent alive and reachable\",\"ServiceID\":\"\",\"ServiceName\":\"\",\"CreateIndex\":3,\"ModifyIndex\":3},{\"Node\":\"A23179111\",\"CheckID\":\"service:oo_10.74.56.36_5656\",\"Name\":\"Service 'oo' check\",\"Status\":\"critical\",\"Notes\":\"\",\"Output\":\"\",\"ServiceID\":\"oo_10.74.56.36_5656\",\"ServiceName\":\"oo\",\"CreateIndex\":314,\"ModifyIndex\":318}]},{\"Node\":{\"Node\":\"A23179111\",\"Address\":\"10.74.44.27\",\"CreateIndex\":3,\"ModifyIndex\":318},\"Service\":{\"ID\":\"oo_10.78.36.36_111\",\"Service\":\"oo\",\"Tags\":[\"url:/root\",\"protocol:REST\",\"version:\",\"visualRange:0|1\",\"ttl:-1\",\"status:1\",\"lb_policy:client_custom\"],\"Address\":\"10.78.36.36\",\"Port\":111,\"EnableTagOverride\":false,\"CreateIndex\":315,\"ModifyIndex\":315},\"Checks\":[{\"Node\":\"A23179111\",\"CheckID\":\"serfHealth\",\"Name\":\"Serf Health Status\",\"Status\":\"passing\",\"Notes\":\"\",\"Output\":\"Agent alive and reachable\",\"ServiceID\":\"\",\"ServiceName\":\"\",\"CreateIndex\":3,\"ModifyIndex\":3}]}]";

            List<HealthService> list =
                            JacksonJsonUtil.jsonToListBean(json, new TypeReference<List<HealthService>>() {});

            Assert.assertEquals(2, list.size());
            Assert.assertEquals("10.74.44.27", list.get(0).getNode().getAddress());
        } catch (Exception e) {
            Assert.fail("Exception" + e.getMessage());
        }
    }

    @Test
    public void testJsonToList4CatalogService() {
        try {
            String json =
                            "[{\"Node\":\"server\",\"Address\":\"127.0.0.1\",\"TaggedAddresses\":{\"lan\":\"127.0.0.1\",\"wan\":\"127.0.0.1\"},\"ServiceID\":\"_CJ-SNMP_10.74.216.65_12005\",\"ServiceName\":\"CJ-SNMP\",\"ServiceAddress\":\"10.74.216.65\",\"ServicePort\":12005,\"ServiceEnableTagOverride\":false,\"CreateIndex\":1813280,\"ModifyIndex\":1815062}]";

            List<CatalogService> list = JacksonJsonUtil.jsonToListBean(json);

            Assert.assertEquals(1, list.size());
            Assert.assertEquals("CJ-SNMP", list.get(0).getServiceName());
        } catch (Exception e) {
            Assert.fail("Exception" + e.getMessage());
        }
    }

    @Test
    public void testjsonToMapBean() {
        try {
            String json =
                            "{\"1YM-PM-TASK\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"REST\\\",\\\"is_manual\\\":\\\"true\\\",\\\"version\\\":\\\"v1\\\",\\\"url\\\":\\\"/api/pm-task/v1\\\",\\\"status\\\":\\\"1\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"1|0\\\"}\"],\"CJ-FM-history\":[\"\\\"base\\\":{\\\"protocol\\\":\\\"REST\\\",\\\"is_manual\\\":\\\"true\\\",\\\"version\\\":\\\"v1\\\",\\\"url\\\":\\\"/api/fm-history/v1\\\",\\\"status\\\":\\\"1\\\"}\",\"\\\"labels\\\":{\\\"visualRange\\\":\\\"1|0\\\"}\"]}";

            Map<String, String[]> map = JacksonJsonUtil.jsonToMapBean(json);

            Assert.assertEquals(2, map.size());

        } catch (Exception e) {
            Assert.fail("Exception" + e.getMessage());
        }
    }


}
