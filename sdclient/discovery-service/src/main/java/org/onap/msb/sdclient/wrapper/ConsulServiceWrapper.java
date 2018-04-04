/**
 * Copyright 2016-2018 ZTE, Inc. and others.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.onap.msb.sdclient.core.AgentService;
import org.onap.msb.sdclient.core.CatalogService;
import org.onap.msb.sdclient.core.Check;
import org.onap.msb.sdclient.core.ConsulResponse;
import org.onap.msb.sdclient.core.HealthService;
import org.onap.msb.sdclient.core.HealthService.Service;
import org.onap.msb.sdclient.core.KeyVaulePair;
import org.onap.msb.sdclient.core.MicroServiceFullInfo;
import org.onap.msb.sdclient.core.MicroServiceInfo;
import org.onap.msb.sdclient.core.Node;
import org.onap.msb.sdclient.core.NodeAddress;
import org.onap.msb.sdclient.core.NodeInfo;
import org.onap.msb.sdclient.core.exception.ExtendedInternalServerErrorException;
import org.onap.msb.sdclient.core.exception.ExtendedNotFoundException;
import org.onap.msb.sdclient.core.exception.UnprocessableEntityException;
import org.onap.msb.sdclient.wrapper.util.ConfigUtil;
import org.onap.msb.sdclient.wrapper.util.DiscoverUtil;
import org.onap.msb.sdclient.wrapper.util.HttpClientUtil;
import org.onap.msb.sdclient.wrapper.util.JacksonJsonUtil;
import org.onap.msb.sdclient.wrapper.util.RegExpTestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

public class ConsulServiceWrapper {


    private static ConsulServiceWrapper instance = new ConsulServiceWrapper();


    private ConsulServiceWrapper() {}

    public static ConsulServiceWrapper getInstance() {
        return instance;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulServiceWrapper.class);

    /**
     * Title: getAllMicroServiceInstances Description: get all services
     * 
     * @return
     * @see com.zte.ums.nfv.eco.hsif.msb.core.IMSBService#getAllMicroServiceInstances()
     */
    public List<MicroServiceFullInfo> getAllMicroServiceInstances() {

        String consulServiceUrl =
                        (new StringBuilder().append("http://").append(ConfigUtil.getInstance().getConsulAddress())
                                        .append(DiscoverUtil.CONSUL_CATALOG_URL).append("/services")).toString();

        String resultJson = HttpClientUtil.httpGet(consulServiceUrl);
        Map<String, String[]> catalogServiceMap = (Map<String, String[]>) JacksonJsonUtil.jsonToMapBean(resultJson);

        List<MicroServiceFullInfo> microServiceFullInfoArray = new ArrayList<MicroServiceFullInfo>();

        if (catalogServiceMap.isEmpty()) {
            return microServiceFullInfoArray;
        }

        for (Map.Entry<String, String[]> entry : catalogServiceMap.entrySet()) {
            Set<String> versionSet = new HashSet<String>();

            Set<String> nsSet = new HashSet<String>();
            nsSet.add("");

            String consul_serviceName = entry.getKey().toString();
            String[] tagList = entry.getValue();

            for (String tag : tagList) {

                if (tag.startsWith("\"base\"")) {
                    String ms_base_json = tag.split("\"base\":")[1];

                    try {
                        Map<String, String> baseMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_base_json, Map.class);
                        if (baseMap.get("version") != null) {
                            versionSet.add(baseMap.get("version"));
                        } else {
                            versionSet.add("");
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }

                    continue;

                }

                if (tag.startsWith("\"ns\"")) {
                    String ms_ns_json = tag.split("\"ns\":")[1];

                    try {
                        Map<String, String> namespaceMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_ns_json, Map.class);
                        if (namespaceMap.get("namespace") != null) {
                            nsSet.add(namespaceMap.get("namespace"));
                        } else {
                            nsSet.add("");
                        }

                        continue;

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LOGGER.error(e.getMessage());
                    }

                    continue;

                }


            }

            for (String ms_version : versionSet) {
                for (String ns : nsSet) {
                    MicroServiceFullInfo microServiceFullInfo =
                                    getMicroServiceInstanceForAll(consul_serviceName, ms_version, ns);
                    if (microServiceFullInfo != null && !"consul".equals(microServiceFullInfo.getServiceName())) {

                        microServiceFullInfoArray.add(microServiceFullInfo);
                    }
                }
            }


        }


        return microServiceFullInfoArray;
    }



    /**
     * @Title getMicroServiceInstanceForAll
     * @Description TODO(get sigle service informations by traversal the entire service list)
     * @param consul_serviceName
     * @param version
     * @param namespace
     * @return
     * @return MicroServiceFullInfo
     */
    public MicroServiceFullInfo getMicroServiceInstanceForAll(String consul_serviceName, String version,
                    String namespace) {

        try {
            ConsulResponse consulResponse = getHealthServices(consul_serviceName, false, "", "");
            if (consulResponse == null) {
                LOGGER.error("microservice not found: serviceName-" + consul_serviceName + ", namespace-" + namespace);
                return null;
            }

            String serviceName = consul_serviceName;
            // Remove version and namespace from consul service name
            // Consul_serviceName Format: serviceName-version-namespace
            if (StringUtils.isNotBlank(version) && StringUtils.isNotBlank(namespace)) {
                if (consul_serviceName.endsWith("-" + version + "-" + namespace)) {
                    serviceName = consul_serviceName.substring(0,
                                    consul_serviceName.length() - version.length() - namespace.length() - 2);
                }
            } else if (StringUtils.isNotBlank(version)) {
                if (consul_serviceName.endsWith("-" + version)) {
                    serviceName = consul_serviceName.substring(0, consul_serviceName.length() - version.length() - 1);
                }
            } else if (StringUtils.isNotBlank(namespace)) {
                if (consul_serviceName.endsWith("-" + namespace)) {
                    serviceName = consul_serviceName.substring(0, consul_serviceName.length() - namespace.length() - 1);
                }
            }
            ConsulResponse serviceResponse =
                            getMicroServiceInfo(consulResponse, serviceName, version, false, "", namespace);
            return (MicroServiceFullInfo) serviceResponse.getResponse();
        } catch (Exception e) {
            if (StringUtils.isNotBlank(namespace)) {
                LOGGER.error("get service List have error:serviceName[" + consul_serviceName + "],version[" + version
                                + "],namespace[" + namespace + "]:" + e.getMessage());
            }
        }
        return null;
    }


    /**
     * @Title getMicroServiceInstance
     * @Description TODO(get single service information by REST API)
     * @param serviceName
     * @param version
     * @param ifPassStatus
     * @param wait
     * @param index
     * @param labels
     * @param namespace
     * @return
     * @return ConsulResponse
     */
    public ConsulResponse getMicroServiceInstance(String serviceName, String version, boolean ifPassStatus, String wait,
                    String index, String labels, String namespace) {
        if ("null".equals(version)) {
            version = "";
        }
        checkServiceNameAndVersion(serviceName, version);

        if (!RegExpTestUtil.labelRegExpTest(labels)) {
            throw new UnprocessableEntityException(
                            "get MicroServiceInfo FAIL: The label query parameter format is wrong (key:value)");
        }

        String consul_serviceName = getServiceName4Consul(serviceName, version, namespace);

        ConsulResponse consulResponse = getHealthServices(consul_serviceName, ifPassStatus, wait, index);
        if (consulResponse == null) {
            String errInfo = "microservice not found: serviceName-" + serviceName + ", namespace-" + namespace;
            throw new ExtendedNotFoundException(errInfo);
        }
        return getMicroServiceInfo(consulResponse, serviceName, version, ifPassStatus, labels, namespace);
    }

    /**
     * Title: getMicroServiceInstance Description: get the target service information
     * 
     * @param serviceName
     * @param version
     * @return
     * @see com.zte.ums.nfv.eco.hsif.msb.core.IMSBService#getMicroServiceInstance(java.lang.String,
     *      java.lang.String)
     */

    public ConsulResponse getMicroServiceInfo(ConsulResponse consulResponse, String serviceName, String version,
                    boolean ifPassStatus, String labels, String namespace) {
        // TODO Auto-generated method stub
        String resultJson = (String) consulResponse.getResponse();
        List<HealthService> healthServiceList =
                        JacksonJsonUtil.jsonToListBean(resultJson, new TypeReference<List<HealthService>>() {});
        if (healthServiceList == null || healthServiceList.size() == 0) {
            String errInfo = "microservice not found: serviceName-" + serviceName + ", namespace-" + namespace;
            throw new ExtendedNotFoundException(errInfo);
        }
        try {
            // label query,format key:value|value2,key2:value2
            boolean islabelQuery = false;
            Map<String, String> query_labelMap = new HashMap<String, String>();
            if (StringUtils.isNotBlank(labels)) {
                islabelQuery = true;
                String[] routeLabels = StringUtils.split(labels, ",");

                for (int i = 0; i < routeLabels.length; i++) {
                    String[] labelArray = StringUtils.split(routeLabels[i], ":");
                    query_labelMap.put(labelArray[0], labelArray[1]);
                }
            }
            MicroServiceFullInfo microServiceInfo = new MicroServiceFullInfo();
            Set<NodeInfo> nodes = new HashSet<NodeInfo>();
            Set<String> serviceLabels = new HashSet<String>();
            Set<KeyVaulePair> serviceMetadatas = new HashSet<KeyVaulePair>();
            Set<String> serviceNetworkPlane = new HashSet<String>();
            String nodeNamespace = "";

            for (HealthService healthService : healthServiceList) {
                Service service = healthService.getService();
                List<String> tagList = service.getTags();

                String ms_url = "", ms_version = "", ms_protocol = "", ms_status = "", ms_publish_port = "",
                                ms_is_manual = "", ms_visualRange = "1", ms_network_plane_type = "", ms_lb_policy = "",
                                ms_host = "", ms_path = "", ms_enable_ssl = "";
                List<KeyVaulePair> ms_metadata = new ArrayList<KeyVaulePair>();

                List<String> nodeLabels = new ArrayList<String>();
                Map<String, String> labelMap = new HashMap<String, String>();

                NodeInfo node = new NodeInfo();

                node.setIp(service.getAddress());
                node.setPort(String.valueOf(service.getPort()));
                node.setNodeId(service.getId());
                try {
                    for (String tag : tagList) {
                        if (tag.startsWith("\"base\"")) {
                            String ms_base_json = tag.split("\"base\":")[1];

                            Map<String, String> baseMap =
                                            (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_base_json, Map.class);
                            ms_url = (baseMap.get("url") == null ? "" : baseMap.get("url"));
                            ms_version = (baseMap.get("version") == null ? "" : baseMap.get("version"));
                            ms_protocol = (baseMap.get("protocol") == null ? "" : baseMap.get("protocol"));
                            ms_status = (baseMap.get("status") == null ? "1" : baseMap.get("status"));

                            if (baseMap.get("enable_ssl") != null) {
                                ms_enable_ssl = (baseMap.get("enable_ssl"));
                            }
                            if (baseMap.get("publish_port") != null) {
                                ms_publish_port = (baseMap.get("publish_port"));
                            }

                            if (baseMap.get("is_manual") != null) {
                                ms_is_manual = baseMap.get("is_manual");
                            }

                            if (baseMap.get("ha_role") != null) {
                                node.setHa_role(baseMap.get("ha_role"));
                            }

                            if (baseMap.get("host") != null) {
                                ms_host = baseMap.get("host");
                            }

                            if (baseMap.get("path") != null) {
                                ms_path = baseMap.get("path");
                            }

                            continue;
                        }

                        if (tag.startsWith("\"labels\"")) {
                            String ms_labels_json = "{" + tag.split("\"labels\":\\{")[1];
                            labelMap = (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_labels_json, Map.class);
                            for (Map.Entry<String, String> labelEntry : labelMap.entrySet()) {
                                if ("visualRange".equals(labelEntry.getKey())) {
                                    ms_visualRange = labelEntry.getValue();
                                } else if ("network_plane_type".equals(labelEntry.getKey())) {
                                    ms_network_plane_type = labelEntry.getValue();
                                } else {
                                    nodeLabels.add(labelEntry.getKey() + ":" + labelEntry.getValue());
                                }
                            }
                            continue;
                        }

                        if (tag.startsWith("\"ns\"")) {
                            String ms_namespace_json = tag.split("\"ns\":")[1];
                            Map<String, String> namespaceMap = (Map<String, String>) JacksonJsonUtil
                                            .jsonToBean(ms_namespace_json, Map.class);

                            if (namespaceMap.get("namespace") != null) {
                                nodeNamespace = namespaceMap.get("namespace");
                            } else {
                                nodeNamespace = "";
                            }

                            continue;
                        }

                        if (tag.startsWith("\"lb\"")) {
                            String ms_lb_json = tag.split("\"lb\":")[1];
                            Map<String, String> lbMap =
                                            (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_lb_json, Map.class);

                            if (lbMap.get("lb_policy") != null) {
                                ms_lb_policy = lbMap.get("lb_policy");
                                if (ms_lb_policy.startsWith("hash") || ms_lb_policy.equals("ip_hash")) {
                                    ms_lb_policy = "ip_hash";
                                }

                            }

                            if (lbMap.get("lb_server_params") != null) {
                                node.setLb_server_params(lbMap.get("lb_server_params").replace(" ", ","));
                            }

                            continue;
                        }

                        if (tag.startsWith("\"checks\"")) {
                            String ms_check_json = tag.split("\"checks\":")[1];
                            Map<String, String> checkMap =
                                            (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_check_json, Map.class);


                            // automatic registry health check
                            if (StringUtils.isNotBlank(checkMap.get("ttl"))) {
                                node.setCheckType("TTL");
                                node.setTtl(checkMap.get("ttl"));
                            } else if (StringUtils.isNotBlank(checkMap.get("http"))) {
                                node.setCheckType("HTTP");
                                node.setCheckUrl(checkMap.get("http"));
                                if (checkMap.get("interval") != null)
                                    node.setCheckInterval(checkMap.get("interval"));
                                if (checkMap.get("timeout") != null)
                                    node.setCheckTimeOut(checkMap.get("timeout"));
                            } else if (StringUtils.isNotBlank(checkMap.get("tcp"))) {
                                node.setCheckType("TCP");
                                node.setCheckUrl(checkMap.get("tcp"));
                                if (checkMap.get("interval") != null)
                                    node.setCheckInterval(checkMap.get("interval"));
                                if (checkMap.get("timeout") != null)
                                    node.setCheckTimeOut(checkMap.get("timeout"));
                            }

                            continue;
                        }

                        if (tag.startsWith("\"metadata\"")) {
                            String ms_metadata_json = "{" + tag.split("\"metadata\":\\{")[1];
                            Map<String, String> metadataMap = (Map<String, String>) JacksonJsonUtil
                                            .jsonToBean(ms_metadata_json, Map.class);
                            for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
                                KeyVaulePair keyVaulePair = new KeyVaulePair();
                                keyVaulePair.setKey(entry.getKey());
                                keyVaulePair.setValue(entry.getValue());
                                ms_metadata.add(keyVaulePair);
                            }
                            continue;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(serviceName + " read tag  throw exception", e);
                }

                // Health check information
                List<Check> checks = healthService.getChecks();
                node.setStatus("passing");
                for (Check check : checks) {
                    if (!"passing".equals(check.getStatus())) {
                        node.setStatus(check.getStatus());
                        break;
                    }
                }

                if (!ms_version.equals(version)) {
                    continue;
                }

                // namespace filter
                if (!namespace.equals(nodeNamespace)) {
                    continue;
                }

                // tag filter
                if (islabelQuery) {
                    boolean ifMatchLabel = false;
                    for (Map.Entry<String, String> query_entry : query_labelMap.entrySet()) {
                        String key = query_entry.getKey();
                        String value = query_entry.getValue();
                        if (StringUtils.isBlank(labelMap.get(key))) {
                            continue;
                        }

                        String[] queryTagArray = StringUtils.split(value, "|");
                        String[] serviceTagArray = StringUtils.split(labelMap.get(key), "|");
                        if (DiscoverUtil.contain(queryTagArray, serviceTagArray)) {
                            ifMatchLabel = true;
                            break;
                        }

                    }

                    if (!ifMatchLabel) {
                        continue;
                    }
                }
                nodes.add(node);
                serviceLabels.addAll(nodeLabels);
                serviceMetadatas.addAll(ms_metadata);

                String[] network_plane_array = StringUtils.split(ms_network_plane_type, "|");
                for (int i = 0; i < network_plane_array.length; i++) {
                    serviceNetworkPlane.add(network_plane_array[i]);
                }
                microServiceInfo.setServiceName(serviceName);
                microServiceInfo.setUrl(ms_url);
                microServiceInfo.setVersion(ms_version);
                microServiceInfo.setProtocol(ms_protocol);
                microServiceInfo.setStatus(ms_status);
                microServiceInfo.setPublish_port(ms_publish_port);
                microServiceInfo.setIs_manual(Boolean.parseBoolean(ms_is_manual));
                microServiceInfo.setVisualRange(ms_visualRange);

                microServiceInfo.setLb_policy(ms_lb_policy);
                microServiceInfo.setNamespace(namespace);
                microServiceInfo.setHost(ms_host);
                microServiceInfo.setPath(ms_path);
                microServiceInfo.setEnable_ssl(Boolean.parseBoolean(ms_enable_ssl));
            }
            if (nodes.isEmpty()) {
                String errInfo = "microservice not found: serviceName-" + serviceName + ",version-" + version
                                + ",namespace-" + namespace + ",labels-" + labels;
                throw new ExtendedNotFoundException(errInfo);

            }
            microServiceInfo.setLabels(new ArrayList<String>(serviceLabels));
            microServiceInfo.setMetadata(new ArrayList<KeyVaulePair>(serviceMetadatas));
            microServiceInfo.setNodes(nodes);
            microServiceInfo.setNetwork_plane_type(StringUtils.join(serviceNetworkPlane.toArray(), "|"));
            return new ConsulResponse(microServiceInfo, consulResponse.getIndex());
        } catch (ExtendedNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtendedInternalServerErrorException(e.getMessage());
        }
    }

    public MicroServiceFullInfo updateMicroServiceInstance(String serviceName, String version, String namespace,
                    MicroServiceInfo microServiceInfo, String requestIP, boolean is_manual) {
        // data format validation
        checkMicroServiceInfo(microServiceInfo);
        deleteMicroService(serviceName, version, namespace);
        return saveMicroServiceInstance(microServiceInfo, true, requestIP, is_manual);
    }

    /**
     * Title: saveMicroServiceInstance Description: save service information
     * 
     * @param microServiceInfo
     * @param createOrUpdate true：add or superaddition renew information.  false：cover
     * @param requestIP request IP address
     * @return
     * @see com.zte.ums.nfv.eco.hsif.msb.core.IMSBService#saveMicroServiceInstance(org.onap.msb.sdclient.core.MicroServiceInfo,
     *      boolean, java.lang.String)
     */
    public MicroServiceFullInfo saveMicroServiceInstance(MicroServiceInfo microServiceInfo, boolean createOrUpdate,
                    String requestIP, boolean is_manual) {

        // data format validation
        checkMicroServiceInfo(microServiceInfo);

        String serviceName = microServiceInfo.getServiceName().trim();

        if (createOrUpdate == false) {
            // cover the original record, add record after delete
            try {
                deleteMicroService(microServiceInfo.getServiceName(), microServiceInfo.getVersion(),
                                microServiceInfo.getNamespace());
            } catch (ExtendedNotFoundException e) {
                String errInfo = "microservice not found: serviceName-" + microServiceInfo.getServiceName()
                                + ",version-" + microServiceInfo.getVersion() + " ,namespace-"
                                + microServiceInfo.getNamespace();
                LOGGER.warn(errInfo);
            }
        }

        Set<Node> nodes = microServiceInfo.getNodes();
        String[] visualRangeArray = StringUtils.split(microServiceInfo.getVisualRange(), "|");

        try {

            for (Node node : nodes) {
                AgentService agentService = new AgentService();

                if (StringUtils.isBlank(node.getIp())) {
                    node.setIp(requestIP);
                }
                String serverId = microServiceInfo.getNamespace() + "_" + microServiceInfo.getVersion() + "_"
                                + serviceName + "_" + node.getIp() + "_" + node.getPort();
                List<String> tags = new ArrayList<String>();

                Map<String, String> baseMap = new HashMap<String, String>();
                Map<String, String> lbMap = new HashMap<String, String>();
                Map<String, String> labelMap = new HashMap<String, String>();
                Map<String, String> metadataMap = new HashMap<String, String>();
                Map<String, String> checkMap = new HashMap<String, String>();
                Map<String, String> nsMap = new HashMap<String, String>();
                // Map<String, String> nodeMap = new HashMap<String, String>();

                baseMap.put("url", microServiceInfo.getUrl());
                baseMap.put("protocol", microServiceInfo.getProtocol());
                baseMap.put("version", microServiceInfo.getVersion());

                baseMap.put("status", "1");
                baseMap.put("is_manual", Boolean.toString(is_manual));
                baseMap.put("enable_ssl", Boolean.toString(microServiceInfo.isEnable_ssl()));

                // save TCP and UDP protocal, nginx port and load balance policy
                if (StringUtils.isNotBlank(microServiceInfo.getPublish_port())) {
                    baseMap.put("publish_port", microServiceInfo.getPublish_port());
                }
                String lb_policy = microServiceInfo.getLb_policy();

                // save the load balance policy of service
                if (StringUtils.isNotBlank(lb_policy)) {
                    switch (lb_policy) {
                        case "round-robin":
                            break;
                        case "ip_hash":
                            if ("TCP".equals(microServiceInfo.getProtocol())
                                            || "UDP".equals(microServiceInfo.getProtocol())) {
                                lbMap.put("lb_policy", "hash $remote_addr");
                            } else {
                                lbMap.put("lb_policy", "ip_hash");
                            }
                            break;
                        default:
                            lbMap.put("lb_policy", lb_policy);
                            break;
                    }

                }

                if (StringUtils.isNotBlank(node.getLb_server_params())) {
                    lbMap.put("lb_server_params", node.getLb_server_params().trim().replace(",", " "));

                }

                if (StringUtils.isNotBlank(node.getHa_role())) {
                    baseMap.put("ha_role", node.getHa_role());
                }

                if (StringUtils.isNotBlank(microServiceInfo.getHost())) {
                    baseMap.put("host", microServiceInfo.getHost().toLowerCase());
                }

                if (StringUtils.isNotBlank(microServiceInfo.getPath())) {
                    baseMap.put("path", microServiceInfo.getPath());
                }

                // save health check parameter
                if (StringUtils.isNotBlank(node.getCheckType())) {

                    AgentService.Check check = agentService.createCheck();

                    if ("TTL".equals(node.getCheckType())) {
                        check.setTtl(node.getTtl());
                        checkMap.put("ttl", node.getTtl());

                    } else if ("HTTP".equals(node.getCheckType())) {
                        check.setInterval(node.getCheckInterval());
                        check.setHttp(node.getCheckUrl());
                        check.setTimeout(node.getCheckTimeOut());

                        checkMap.put("http", node.getCheckUrl());
                        checkMap.put("interval", node.getCheckInterval());
                        checkMap.put("timeout", node.getCheckTimeOut());
                    } else if ("TCP".equals(node.getCheckType())) {
                        check.setInterval(node.getCheckInterval());
                        check.setTcp(node.getCheckUrl());
                        check.setTimeout(node.getCheckTimeOut());

                        checkMap.put("tcp", node.getCheckUrl());
                        checkMap.put("interval", node.getCheckInterval());
                        checkMap.put("timeout", node.getCheckTimeOut());
                    }

                    agentService.setCheck(check);
                }


                List<KeyVaulePair> keyVaulePairs = microServiceInfo.getMetadata();

                if (keyVaulePairs != null && keyVaulePairs.size() > 0) {
                    for (KeyVaulePair keyVaulePair : keyVaulePairs) {
                        metadataMap.put(keyVaulePair.getKey(), keyVaulePair.getValue());
                    }
                }

                // synchronize filter parameter, joint in to json and save it
                labelMap.put("visualRange", StringUtils.join(visualRangeArray, "|"));

                if (StringUtils.isNotBlank(microServiceInfo.getNetwork_plane_type())) {
                    labelMap.put("network_plane_type", microServiceInfo.getNetwork_plane_type());
                }
                if (microServiceInfo.getLabels() != null) {
                    for (String label : microServiceInfo.getLabels()) {
                        String[] labelArray = StringUtils.split(label, ":");
                        if (labelArray.length == 2) {
                            labelMap.put(labelArray[0], labelArray[1]);
                        }
                    }
                }

                if (StringUtils.isNotBlank(microServiceInfo.getNamespace())) {
                    nsMap.put("namespace", microServiceInfo.getNamespace());
                }

                tags.add("\"base\":" + JacksonJsonUtil.beanToJson(baseMap));
                if (!lbMap.isEmpty())
                    tags.add("\"lb\":" + JacksonJsonUtil.beanToJson(lbMap));
                if (!checkMap.isEmpty())
                    tags.add("\"checks\":" + JacksonJsonUtil.beanToJson(checkMap));
                if (!labelMap.isEmpty())
                    tags.add("\"labels\":" + JacksonJsonUtil.beanToJson(labelMap));
                if (!metadataMap.isEmpty())
                    tags.add("\"metadata\":" + JacksonJsonUtil.beanToJson(metadataMap));
                if (!nsMap.isEmpty())
                    tags.add("\"ns\":" + JacksonJsonUtil.beanToJson(nsMap));

                agentService.setTags(tags);

                agentService.setAddress(node.getIp());
                agentService.setId(serverId);
                agentService.setPort(Integer.parseInt(node.getPort()));

                String consul_serviceName = getServiceName4Consul(serviceName, microServiceInfo.getVersion(),
                                microServiceInfo.getNamespace());

                agentService.setName(consul_serviceName);

                int registerResult;
                if (DiscoverUtil.CONSUL_REGISTER_MODE.equals(ConfigUtil.getInstance().getConsulRegisterMode())) {
                    registerResult = ConsulCatalogServiceWrapper.getInstance().saveService(agentService);
                } else {
                    registerResult = ConsulAgentServiceWrapper.getInstance().saveService(agentService);
                }

                if (registerResult != 200) {
                    throw new Exception("register consul service fail:" + registerResult);
                }
            }

            LOGGER.info("save microservice success: serviceName-" + microServiceInfo.getServiceName() + ",version-"
                            + microServiceInfo.getVersion() + " ,namespace-" + microServiceInfo.getNamespace());
            return getMicroServiceInstance(serviceName, microServiceInfo.getVersion(), microServiceInfo.getNamespace());
        } catch (ExtendedNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("register consul service throw exception", e);
            throw new ExtendedInternalServerErrorException(e.getMessage());
        }
    }

    public MicroServiceFullInfo getMicroServiceInstance(String serviceName, String version, String namespace) {
        ConsulResponse serviceResponse = getMicroServiceInstance(serviceName, version, false, "", "", "", namespace);
        return (MicroServiceFullInfo) serviceResponse.getResponse();
    }


    /**
     * Title: deleteMicroService Description: delete service information
     * 
     * @param serviceName
     * @param version
     * @see com.zte.ums.nfv.eco.hsif.msb.core.IMSBService#deleteMicroService(java.lang.String,
     *      java.lang.String)
     */
    public void deleteMicroService(String serviceName, String version, String namespace) {
        if ("null".equals(version)) {
            version = "";
        }
        checkServiceNameAndVersion(serviceName, version);
        String consul_serviceName = getServiceName4Consul(serviceName, version, namespace);

        List<CatalogService> catalogServiceList = getConsulServices(consul_serviceName, version);
        if (catalogServiceList == null || catalogServiceList.size() == 0) {
            String errInfo = "microservice not found: serviceName-" + serviceName + ",version-" + version
                            + " ,namespace-" + namespace;
            throw new ExtendedNotFoundException(errInfo);
        }
        boolean ifFindServiceForNS = false;

        for (CatalogService catalogService : catalogServiceList) {

            List<String> tagList = catalogService.getServiceTags();
            String serviceNamespace = "", serviceVersion = "";
            try {

                for (String tag : tagList) {

                    if (tag.startsWith("\"ns\"")) {
                        String ms_ns_json = tag.split("\"ns\":")[1];
                        Map<String, String> nsMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_ns_json, Map.class);
                        if (nsMap.get("namespace") != null) {
                            serviceNamespace = nsMap.get("namespace");
                        }
                        continue;
                    }
                    if (tag.startsWith("\"base\"")) {
                        String ms_base_json = tag.split("\"base\":")[1];
                        Map<String, String> baseMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_base_json, Map.class);
                        if (baseMap.get("version") != null) {
                            serviceVersion = baseMap.get("version");
                        }
                        continue;
                    }
                }
            } catch (Exception e) {
                LOGGER.error(serviceName + " read tag  throw exception", e);
            }

            if (!serviceNamespace.equals(namespace)) {
                continue;
            }

            if (!serviceVersion.equals(version)) {
                continue;
            }
            ifFindServiceForNS = true;
            String serviceID = catalogService.getServiceId();
            try {

                int delResult;
                if (DiscoverUtil.CONSUL_REGISTER_MODE.equals(ConfigUtil.getInstance().getConsulRegisterMode())) {
                    delResult = ConsulCatalogServiceWrapper.getInstance().deleteService(serviceID);
                } else {
                    delResult = ConsulAgentServiceWrapper.getInstance().deleteService(serviceID);
                }

                if (delResult != 200) {
                    throw new Exception("delete consul service fail:" + delResult);
                }
            } catch (Exception e) {
                LOGGER.error("delete consul service throw exception", e);
                throw new ExtendedInternalServerErrorException(e.getMessage());
            }
        }
        if (!ifFindServiceForNS) {
            String errInfo = "microservice not found: serviceName-" + serviceName + ",version-" + version
                            + ",namespace-" + namespace;
            throw new ExtendedNotFoundException(errInfo);
        }

        LOGGER.info("microservice delete success: serviceName-" + serviceName + ",version-" + version + ",namespace-"
                        + namespace);
    }

    /**
     * Title: deleteMicroServiceInstance Description: delete service node information
     * 
     * @param serviceName
     * @param version
     * @param ip
     * @param port
     * @see com.zte.ums.nfv.eco.hsif.msb.core.IMSBService#deleteMicroServiceInstance(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void deleteMicroServiceInstance(String serviceName, String version, String namespace, String ip,
                    String port) {
        if ("null".equals(version)) {
            version = "";
        }

        checkServiceNameAndVersion(serviceName, version);


        if (!RegExpTestUtil.ipRegExpTest(ip)) {
            throw new UnprocessableEntityException(
                            "delete MicroServiceInfo FAIL:IP(" + ip + ")is not a valid IP address");
        }

        if (!RegExpTestUtil.portRegExpTest(port)) {
            throw new UnprocessableEntityException(
                            "delete MicroServiceInfo FAIL:Port(" + port + ")is not a valid Port address");
        }

        String consul_serviceName = getServiceName4Consul(serviceName, version, namespace);

        List<CatalogService> catalogServiceList = getConsulServices(consul_serviceName, version);

        if (catalogServiceList == null || catalogServiceList.size() == 0) {
            String errInfo = "microservice not found: serviceName-" + serviceName + ",version-" + version;
            LOGGER.warn(errInfo);
            throw new ExtendedNotFoundException(errInfo);
        }

        String node = "", serviceID = "";
        boolean ifFindBNode = false;
        for (CatalogService catalogService : catalogServiceList) {

            String serviceAddress = catalogService.getServiceAddress();
            String servicePort = String.valueOf(catalogService.getServicePort());
            List<String> tagList = catalogService.getServiceTags();
            String ms_version = "", ms_namespace = "";
            try {

                for (String tag : tagList) {

                    if (tag.startsWith("\"base\"")) {
                        String ms_base_json = tag.split("\"base\":")[1];
                        Map<String, String> baseMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_base_json, Map.class);
                        if (baseMap.get("version") != null) {
                            ms_version = baseMap.get("version");
                        }
                    }
                    if (tag.startsWith("\"ns\"")) {
                        String ms_ns_json = tag.split("\"ns\":")[1];
                        Map<String, String> nsMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_ns_json, Map.class);
                        if (nsMap.get("namespace") != null) {
                            ms_namespace = nsMap.get("namespace");
                        }
                    }
                }

            } catch (Exception e) {
                LOGGER.error(serviceName + " read tag  throw exception", e);
            }
            if (serviceAddress.equals(ip) && servicePort.equals(port) && ms_version.equals(version)
                            && ms_namespace.equals(namespace)) {
                node = catalogService.getNode();
                serviceID = catalogService.getServiceId();
                ifFindBNode = true;
                break;
            }
        }
        if (!ifFindBNode) {
            throw new ExtendedNotFoundException("delete MicroServiceInfo FAIL: node-" + ip + ":" + port + " namespace-"
                            + namespace + " not found ");
        }
        try {
            int delResult;
            if (DiscoverUtil.CONSUL_REGISTER_MODE.equals(ConfigUtil.getInstance().getConsulRegisterMode())) {
                delResult = ConsulCatalogServiceWrapper.getInstance().deleteService(serviceID);
            } else {
                delResult = ConsulAgentServiceWrapper.getInstance().deleteService(serviceID);
            }

            if (delResult != 200) {
                throw new Exception("delete consul service fail:" + delResult);
            }
        } catch (Exception e) {
            LOGGER.error("delete consul service throw exception", e);
            throw new ExtendedInternalServerErrorException(e.getMessage());
        }
    }

    /**
     * @Title getConsulServices
     * @Description TODO(pass way: get consul service information according to service name and version)
     * @param serviceName
     * @return
     * @return List<CatalogService>
     */
    private List<CatalogService> getConsulServices(String serviceName, String version) {
        // serviceName = serviceName.replace("/", "*");
        String consulServiceUrl = (new StringBuilder().append("http://")
                        .append(ConfigUtil.getInstance().getConsulAddress()).append(DiscoverUtil.CONSUL_CATALOG_URL)
                        .append("/service/").append(serviceName)).toString();

        String resultJson = HttpClientUtil.httpGet(consulServiceUrl);
        List<CatalogService> catalogServiceList = (List<CatalogService>) JacksonJsonUtil.jsonToListBean(resultJson);

        for (CatalogService catalogService : catalogServiceList) {
            List<String> tagList = catalogService.getServiceTags();
            String ms_version = "";
            try {
                for (String tag : tagList) {

                    if (tag.startsWith("\"base\"")) {
                        String ms_base_json = tag.split("\"base\":")[1];
                        Map<String, String> baseMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_base_json, Map.class);
                        if (baseMap.get("version") != null) {
                            ms_version = baseMap.get("version");
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error(serviceName + " read tag  throw exception", e);
            }
            if (!ms_version.equals(version)) {
                catalogServiceList.remove(catalogService);
                break;
            }
        }
        return catalogServiceList;
    }

    /**
     * @Title getHealthServices
     * @Description TODO(pass way: get health check information of consul by service name)
     * @param serviceName
     * @return List<HealthService>
     */
    private ConsulResponse getHealthServices(String serviceName, boolean ifPassStatus, String wait, String index) {
        // serviceName = serviceName.replace("/", "*");
        StringBuilder healthServiceUrlBuilder =
                        new StringBuilder().append("http://").append(ConfigUtil.getInstance().getConsulAddress())
                                        .append(DiscoverUtil.CONSUL_HEALTH_URL).append(serviceName);

        if (ifPassStatus) {
            healthServiceUrlBuilder.append("?passing");
        }

        if (StringUtils.isNotBlank(wait) && StringUtils.isNotBlank(index)) {
            if (ifPassStatus) {
                healthServiceUrlBuilder.append("&wait=").append(wait).append("&index=").append(index);
            } else {
                healthServiceUrlBuilder.append("?wait=").append(wait).append("&index=").append(index);
            }
        }
        return HttpClientUtil.httpWaitGet(healthServiceUrlBuilder.toString());
    }
    
    public void healthCheckbyTTL(String serviceName, String version, String namespace, NodeAddress checkNode) {
        // TODO Auto-generated method stub
        if ("null".equals(version)) {
            version = "";
        }

        checkServiceNameAndVersion(serviceName, version);
        if (!RegExpTestUtil.ipRegExpTest(checkNode.getIp())) {
            throw new UnprocessableEntityException(
                            "healthCheck by TTL FAIL:IP(" + checkNode.getIp() + ")is not a valid IP address");
        }
        if (!RegExpTestUtil.portRegExpTest(checkNode.getPort())) {
            throw new UnprocessableEntityException(
                            "healthCheck by TTL FAIL:Port(" + checkNode.getPort() + ")is not a valid Port address");
        }
        String consul_serviceName = getServiceName4Consul(serviceName, version, namespace);
        List<CatalogService> catalogServiceList = getConsulServices(consul_serviceName, version);
        if (catalogServiceList == null || catalogServiceList.size() == 0) {
            String errInfo = "microservice not found: serviceName-" + serviceName + ",version-" + version;
            LOGGER.warn(errInfo);
            throw new ExtendedNotFoundException(errInfo);
        }
        boolean ifFindBNode = false;
        for (CatalogService catalogService : catalogServiceList) {
            String serviceAddress = catalogService.getServiceAddress();
            String servicePort = String.valueOf(catalogService.getServicePort());
            boolean ifttlCheck = false;
            List<String> tagList = catalogService.getServiceTags();
            String ms_version = "", ms_namespace = "";
            try {

                for (String tag : tagList) {

                    if (tag.startsWith("\"base\"")) {
                        String ms_base_json = tag.split("\"base\":")[1];

                        Map<String, String> baseMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_base_json, Map.class);
                        if (baseMap.get("version") != null) {
                            ms_version = baseMap.get("version");
                        }
                    }

                    if (tag.startsWith("\"ns\"")) {
                        String ms_ns_json = tag.split("\"ns\":")[1];

                        Map<String, String> nsMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_ns_json, Map.class);
                        if (nsMap.get("namespace") != null) {
                            ms_namespace = nsMap.get("namespace");
                        }
                    }
                    if (tag.startsWith("\"checks\"")) {
                        String ms_check_json = tag.split("\"checks\":")[1];
                        Map<String, String> checkMap =
                                        (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_check_json, Map.class);

                        // automatic registry health check
                        if (StringUtils.isNotBlank(checkMap.get("ttl"))) {
                            ifttlCheck = true;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(serviceName + " read tag  throw exception", e);
            }
            if (serviceAddress.equals(checkNode.getIp()) && servicePort.equals(checkNode.getPort())
                            && ms_version.equals(version) && ms_namespace.equals(namespace)) {
                if (!ifttlCheck) {
                    throw new ExtendedNotFoundException(
                                    "healthCheck by TTL FAIL: Service is not enabled TTL health check ");
                }
                ifFindBNode = true;
                break;
            }
        }
        if (!ifFindBNode) {
            throw new ExtendedNotFoundException("healthCheck by TTL FAIL: node-" + checkNode.getIp() + ":"
                            + checkNode.getPort() + " namespace-" + namespace + " not found ");
        }
        try {
            String checkID = (new StringBuilder().append("service:").append(namespace).append("_").append(serviceName)
                            .append("_").append(checkNode.getIp()).append("_").append(checkNode.getPort())).toString();

            String consulServiceUrl =
                            (new StringBuilder().append("http://").append(ConfigUtil.getInstance().getConsulAddress())
                                            .append(DiscoverUtil.CONSUL_AGENT_TTL_URL).append(checkID)).toString();

            String result = HttpClientUtil.httpGet(consulServiceUrl);
            if ("CheckID does not have associated TTL".equals(result)) {
                throw new ExtendedNotFoundException(
                                "healthCheck by TTL FAIL: Service is not enabled TTL health check ");
            }

        } catch (ExtendedInternalServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtendedInternalServerErrorException("healthCheck by TTL FAIL:" + e.getMessage());
        }
    }

    // public MicroServiceFullInfo getApigatewayServiceInfo4Host(String namespace){
    // return getMicroServiceInstance(DiscoverUtil.APIGATEWAY_SERVINCE, "v1", namespace);
    // }
    //

    public List<MicroServiceFullInfo> getMicroServiceForNodes(String serviceName, String version, boolean ifPassStatus,
                    String labels, String namespace) {
        // TODO Auto-generated method stub
        if ("null".equals(version)) {
            version = "";
        }

        checkServiceNameAndVersion(serviceName, version);

        if (!RegExpTestUtil.labelRegExpTest(labels)) {
            throw new UnprocessableEntityException(
                            "get MicroServiceInfo FAIL: The label query parameter format is wrong (key:value)");
        }
        String consul_serviceName = getServiceName4Consul(serviceName, version, namespace);

        ConsulResponse consulResponse = getHealthServices(consul_serviceName, ifPassStatus, "", "");
        if (consulResponse == null) {
            String errInfo = "microservice not found: serviceName-" + serviceName;
            throw new ExtendedNotFoundException(errInfo);
        }
        String resultJson = (String) consulResponse.getResponse();
        List<HealthService> healthServiceList =
                        JacksonJsonUtil.jsonToListBean(resultJson, new TypeReference<List<HealthService>>() {});
        if (healthServiceList == null || healthServiceList.size() == 0) {
            String errInfo = "microservice not found: serviceName-" + serviceName;
            throw new ExtendedNotFoundException(errInfo);
        }

        try {

            // label query,format key:value|value2,key2:value2
            boolean islabelQuery = false;
            Map<String, String> query_labelMap = new HashMap<String, String>();
            if (StringUtils.isNotBlank(labels)) {
                islabelQuery = true;
                String[] routeLabels = StringUtils.split(labels, ",");
                for (int i = 0; i < routeLabels.length; i++) {
                    String[] labelArray = StringUtils.split(routeLabels[i], ":");
                    query_labelMap.put(labelArray[0], labelArray[1]);
                }
            }
            List<MicroServiceFullInfo> microServiceInfoList = new ArrayList<MicroServiceFullInfo>();
            for (HealthService healthService : healthServiceList) {

                Set<NodeInfo> nodes = new HashSet<NodeInfo>();
                Set<String> serviceLabels = new HashSet<String>();
                String nodeNamespace = "";
                MicroServiceFullInfo microServiceInfo = new MicroServiceFullInfo();

                Service service = healthService.getService();
                List<String> tagList = service.getTags();

                String ms_url = "", ms_version = "", ms_protocol = "", ms_status = "", ms_publish_port = "",
                                ms_is_manual = "", ms_visualRange = "1", ms_network_plane_type = "", ms_lb_policy = "",
                                ms_host = "", ms_path = "", ms_enable_ssl = "";
                List<KeyVaulePair> ms_metadata = new ArrayList<KeyVaulePair>();

                List<String> nodeLabels = new ArrayList<String>();
                Map<String, String> labelMap = new HashMap<String, String>();

                NodeInfo node = new NodeInfo();

                node.setIp(service.getAddress());
                node.setPort(String.valueOf(service.getPort()));
                node.setNodeId(service.getId());
                try {
                    for (String tag : tagList) {
                        if (tag.startsWith("\"base\"")) {
                            String ms_base_json = tag.split("\"base\":")[1];

                            Map<String, String> baseMap =
                                            (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_base_json, Map.class);
                            ms_url = (baseMap.get("url") == null ? "" : baseMap.get("url"));
                            ms_version = (baseMap.get("version") == null ? "" : baseMap.get("version"));
                            ms_protocol = (baseMap.get("protocol") == null ? "" : baseMap.get("protocol"));
                            ms_status = (baseMap.get("status") == null ? "1" : baseMap.get("status"));

                            if (baseMap.get("publish_port") != null) {
                                ms_publish_port = (baseMap.get("publish_port"));
                            }

                            if (baseMap.get("is_manual") != null) {
                                ms_is_manual = baseMap.get("is_manual");

                            }

                            if (baseMap.get("ha_role") != null) {
                                node.setHa_role(baseMap.get("ha_role"));
                            }

                            if (baseMap.get("host") != null) {
                                ms_host = baseMap.get("host");
                            }

                            if (baseMap.get("path") != null) {
                                ms_path = baseMap.get("path");
                            }
                            if (baseMap.get("enable_ssl") != null) {
                                ms_publish_port = (baseMap.get("enable_ssl"));
                            }
                            continue;
                        }
                        if (tag.startsWith("\"labels\"")) {
                            String ms_labels_json = "{" + tag.split("\"labels\":\\{")[1];
                            labelMap = (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_labels_json, Map.class);
                            for (Map.Entry<String, String> labelEntry : labelMap.entrySet()) {
                                if ("visualRange".equals(labelEntry.getKey())) {
                                    ms_visualRange = labelEntry.getValue();
                                } else if ("network_plane_type".equals(labelEntry.getKey())) {
                                    ms_network_plane_type = labelEntry.getValue();
                                } else {
                                    nodeLabels.add(labelEntry.getKey() + ":" + labelEntry.getValue());
                                }
                            }
                            continue;
                        }
                        if (tag.startsWith("\"ns\"")) {
                            String ms_namespace_json = tag.split("\"ns\":")[1];
                            Map<String, String> namespaceMap = (Map<String, String>) JacksonJsonUtil
                                            .jsonToBean(ms_namespace_json, Map.class);
                            if (namespaceMap.get("namespace") != null) {
                                nodeNamespace = namespaceMap.get("namespace");
                            } else {
                                nodeNamespace = "";
                            }
                            continue;
                        }
                        if (tag.startsWith("\"lb\"")) {
                            String ms_lb_json = tag.split("\"lb\":")[1];
                            Map<String, String> lbMap =
                                            (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_lb_json, Map.class);
                            if (lbMap.get("lb_policy") != null) {
                                ms_lb_policy = lbMap.get("lb_policy");
                                if (ms_lb_policy.startsWith("hash") || ms_lb_policy.equals("ip_hash")) {
                                    ms_lb_policy = "ip_hash";
                                }
                            }
                            if (lbMap.get("lb_server_params") != null) {
                                node.setLb_server_params(lbMap.get("lb_server_params").replace(" ", ","));
                            }
                            continue;
                        }
                        if (tag.startsWith("\"checks\"")) {
                            String ms_check_json = tag.split("\"checks\":")[1];
                            Map<String, String> checkMap =
                                            (Map<String, String>) JacksonJsonUtil.jsonToBean(ms_check_json, Map.class);
                            // automatic register health check
                            if (StringUtils.isNotBlank(checkMap.get("ttl"))) {
                                node.setCheckType("TTL");
                                node.setTtl(checkMap.get("ttl"));
                            } else if (StringUtils.isNotBlank(checkMap.get("http"))) {
                                node.setCheckType("HTTP");
                                node.setCheckUrl(checkMap.get("http"));
                                if (checkMap.get("interval") != null)
                                    node.setCheckInterval(checkMap.get("interval"));
                                if (checkMap.get("timeout") != null)
                                    node.setCheckTimeOut(checkMap.get("timeout"));
                            } else if (StringUtils.isNotBlank(checkMap.get("tcp"))) {
                                node.setCheckType("TCP");
                                node.setCheckUrl(checkMap.get("tcp"));
                                if (checkMap.get("interval") != null)
                                    node.setCheckInterval(checkMap.get("interval"));
                                if (checkMap.get("timeout") != null)
                                    node.setCheckTimeOut(checkMap.get("timeout"));
                            }
                            continue;
                        }
                        if (tag.startsWith("\"metadata\"")) {
                            String ms_metadata_json = "{" + tag.split("\"metadata\":\\{")[1];
                            Map<String, String> metadataMap = (Map<String, String>) JacksonJsonUtil
                                            .jsonToBean(ms_metadata_json, Map.class);
                            for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
                                KeyVaulePair keyVaulePair = new KeyVaulePair();
                                keyVaulePair.setKey(entry.getKey());
                                keyVaulePair.setValue(entry.getValue());
                                ms_metadata.add(keyVaulePair);
                            }
                            continue;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(serviceName + " read tag  throw exception", e);
                }

                // health check information
                List<Check> checks = healthService.getChecks();
                node.setStatus("passing");
                for (Check check : checks) {
                    if (!"passing".equals(check.getStatus())) {
                        node.setStatus(check.getStatus());
                        break;
                    }
                }

                if (!ms_version.equals(version)) {
                    continue;
                }

                // namespace filter
                if (!namespace.equals(nodeNamespace)) {
                    continue;
                }

                // label filter
                if (islabelQuery) {
                    boolean ifMatchLabel = false;
                    for (Map.Entry<String, String> query_entry : query_labelMap.entrySet()) {
                        String key = query_entry.getKey();
                        String value = query_entry.getValue();
                        if (StringUtils.isBlank(labelMap.get(key))) {
                            continue;
                        }

                        String[] queryTagArray = StringUtils.split(value, "|");
                        String[] serviceTagArray = StringUtils.split(labelMap.get(key), "|");
                        if (DiscoverUtil.contain(queryTagArray, serviceTagArray)) {
                            ifMatchLabel = true;
                            break;
                        }
                    }
                    if (!ifMatchLabel) {
                        continue;
                    }
                }
                nodes.add(node);
                serviceLabels.addAll(nodeLabels);

                microServiceInfo.setServiceName(serviceName);
                microServiceInfo.setUrl(ms_url);
                microServiceInfo.setVersion(ms_version);
                microServiceInfo.setProtocol(ms_protocol);
                microServiceInfo.setStatus(null);
                microServiceInfo.setPublish_port(ms_publish_port);
                microServiceInfo.setIs_manual(Boolean.parseBoolean(ms_is_manual));
                microServiceInfo.setVisualRange(ms_visualRange);
                microServiceInfo.setNetwork_plane_type(ms_network_plane_type);
                microServiceInfo.setLb_policy(ms_lb_policy);
                microServiceInfo.setHost(ms_host);
                microServiceInfo.setPath(ms_path);
                microServiceInfo.setEnable_ssl(Boolean.parseBoolean(ms_enable_ssl));
                microServiceInfo.setMetadata(ms_metadata);
                microServiceInfo.setNamespace(namespace);
                microServiceInfo.setLabels(new ArrayList<String>(serviceLabels));
                microServiceInfo.setNodes(nodes);
                microServiceInfoList.add(microServiceInfo);
            }
            if (microServiceInfoList.size() == 0) {
                String errInfo = "microservice not found: serviceName-" + serviceName + ",version-" + version
                                + ",namespace-" + namespace + ",labels-" + labels;
                throw new ExtendedNotFoundException(errInfo);
            }
            return microServiceInfoList;
        } catch (ExtendedNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtendedInternalServerErrorException(e.getMessage());
        }
    }


    private String getServiceName4Consul(String serviceName, String version, String namespace) {
        String consul_serviceName = serviceName;

        if (StringUtils.isNotBlank(version)) {
            consul_serviceName = consul_serviceName + "-" + version;
        }
        if (StringUtils.isNotBlank(namespace)) {
            consul_serviceName = consul_serviceName + "-" + namespace;
        }
        return consul_serviceName;
    }

    private void checkMicroServiceInfo(MicroServiceInfo microServiceInfo) {

        if (StringUtils.isBlank(microServiceInfo.getServiceName())
                        || StringUtils.isBlank(microServiceInfo.getProtocol())) {
            throw new UnprocessableEntityException("register MicroServiceInfo FAIL: Some required fields are empty");
        }

        if (microServiceInfo.getNodes() == null || microServiceInfo.getNodes().size() == 0) {
            throw new UnprocessableEntityException("register MicroServiceInfo FAIL: Nodes fields are empty");
        }

        if (!RegExpTestUtil.serviceNameRegExpTest(microServiceInfo.getServiceName().trim())) {
            throw new UnprocessableEntityException("register MicroServiceInfo FAIL:ServiceName("
                            + microServiceInfo.getServiceName() + ")  format error");
        }
        if (StringUtils.isNotBlank(microServiceInfo.getHost())) {
            if (!RegExpTestUtil.serviceNameRegExpTest(microServiceInfo.getHost().trim())) {
                throw new UnprocessableEntityException(
                                "register MicroServiceInfo host (" + microServiceInfo.getHost() + ")  format error");
            }
        }
        if (StringUtils.isNotBlank(microServiceInfo.getLb_policy())) {
            if (!DiscoverUtil.checkExist(DiscoverUtil.LB_POLICY_LIST, microServiceInfo.getLb_policy().trim(), ",")) {
                throw new UnprocessableEntityException("register MicroServiceInfo FAIL:lb_policy is wrong,value range:("
                                + DiscoverUtil.LB_POLICY_LIST + ")");
            }
        }
        if (StringUtils.isNotBlank(microServiceInfo.getVersion())) {
            if (!RegExpTestUtil.versionRegExpTest(microServiceInfo.getVersion())) {
                throw new UnprocessableEntityException("register MicroServiceInfo FAIL:version is not a valid  format");
            }
        }

        if (StringUtils.isNotBlank(microServiceInfo.getUrl())) {

            String url = microServiceInfo.getUrl();
            if (!"/".equals(url)) {
                if (!url.startsWith("/")) {
                    url = "/" + url;
                    microServiceInfo.setUrl(url);
                }
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                    microServiceInfo.setUrl(url);
                }
            }
            if (!RegExpTestUtil.urlRegExpTest(url)) {
                throw new UnprocessableEntityException(
                                "register MicroServiceInfo FAIL:url (" + url + ") is not a valid format");
            }
        } else {
            microServiceInfo.setUrl("/");
        }
        if (StringUtils.isNotBlank(microServiceInfo.getPath())) {

            String path = microServiceInfo.getPath();
            if (!"/".equals(path)) {
                if (!path.startsWith("/")) {
                    path = "/" + path;
                    microServiceInfo.setPath(path);
                }
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                    microServiceInfo.setPath(path);
                }
            }
            if (!RegExpTestUtil.urlRegExpTest(path)) {
                throw new UnprocessableEntityException(
                                "register MicroServiceInfo FAIL:path (" + path + ") is not a valid format");
            }
        }
        for (Node node : microServiceInfo.getNodes()) {

            if (StringUtils.isNotBlank(node.getIp())) {
                if (!RegExpTestUtil.ipRegExpTest(node.getIp())) {
                    throw new UnprocessableEntityException(
                                    "register MicroServiceInfo FAIL:IP(" + node.getIp() + ")is not a valid ip address");
                }
            }

            if (!RegExpTestUtil.portRegExpTest(node.getPort())) {
                throw new UnprocessableEntityException("register MicroServiceInfo FAIL:Port(" + node.getPort()
                                + ")is not a valid Port address");
            }
            if (StringUtils.isNotBlank(node.getLb_server_params())) {
                try {
                    String[] lb_server_params_array = node.getLb_server_params().split(",");
                    for (int i = 0; i < lb_server_params_array.length; i++) {
                        String params = lb_server_params_array[i].split("=")[0];
                        if (!DiscoverUtil.checkExist(DiscoverUtil.LB_PARAMS_LIST, params, ",")) {
                            throw new UnprocessableEntityException(
                                            "register MicroServiceInfo FAIL:lb_server_params is wrong:"
                                                            + lb_server_params_array[i]);
                        }
                    }
                } catch (Exception e) {
                    throw new UnprocessableEntityException(
                                    "register MicroServiceInfo FAIL:lb_server_params'format is wrong:"
                                                    + node.getLb_server_params());
                }
            }

            if (StringUtils.isNotBlank(node.getCheckType())) {
                if (!DiscoverUtil.checkExist(DiscoverUtil.CHECK_TYPE_LIST, node.getCheckType().trim(), ",")) {
                    throw new UnprocessableEntityException(
                                    "register MicroServiceInfo FAIL:checkType is wrong,value range:("
                                                    + DiscoverUtil.CHECK_TYPE_LIST + ")");
                }
                if ("HTTP".equals(node.getCheckType()) || "TCP".equals(node.getCheckType())) {
                    String checkUrl = node.getCheckUrl();
                    if (StringUtils.isBlank(checkUrl)) {
                        throw new UnprocessableEntityException(
                                        "register MicroServiceInfo FAIL:checkUrl field is empty");
                    }
                    if ("HTTP".equals(node.getCheckType())) {
                        if (RegExpTestUtil.httpUrlRegExpTest(checkUrl)) {
                            if ((!checkUrl.startsWith("http://"))&&(!checkUrl.startsWith("https://"))) {
                                checkUrl = "http://" + checkUrl;
                                node.setCheckUrl(checkUrl);
                            }
                        } else {
                            if (!checkUrl.startsWith("/")) {
                                checkUrl = "/" + checkUrl;
                            }
                            checkUrl = "http://" + node.getIp() + ":" + node.getPort() + checkUrl;
                            node.setCheckUrl(checkUrl);
                        }
                    }
                }
            }

            if (StringUtils.isNotBlank(node.getHa_role())) {
                if (!DiscoverUtil.checkExist(DiscoverUtil.CHECK_HA_ROLE_LIST, node.getHa_role().trim(), ",")) {
                    throw new UnprocessableEntityException(
                                    "register MicroServiceInfo FAIL:ha_role is wrong,value range:("
                                                    + DiscoverUtil.CHECK_HA_ROLE_LIST + ")");
                }
            }
        }
        String[] visualRangeArray = StringUtils.split(microServiceInfo.getVisualRange(), "|");
        for (int i = 0; i < visualRangeArray.length; i++) {
            if (!DiscoverUtil.checkExist(DiscoverUtil.VISUAL_RANGE_LIST, visualRangeArray[i], ",")) {
                throw new UnprocessableEntityException("register MicroServiceInfo FAIL:type is wrong,value range:("
                                + DiscoverUtil.VISUAL_RANGE_LIST + ")");
            }
        }

        microServiceInfo.setProtocol(microServiceInfo.getProtocol().toUpperCase());
        if (!DiscoverUtil.checkExist(DiscoverUtil.PROTOCOL_LIST, microServiceInfo.getProtocol().trim(), ",")) {
            throw new UnprocessableEntityException("register MicroServiceInfo FAIL:Protocol is wrong,value range:("
                            + DiscoverUtil.PROTOCOL_LIST + ")");
        }

        if (microServiceInfo.getLabels() != null) {
            for (String label : microServiceInfo.getLabels()) {
                if (!RegExpTestUtil.labelRegExpTest(label)) {
                    throw new UnprocessableEntityException("register MicroServiceInfo FAIL:label[" + label
                                    + "] is not a valid format(key:value)");
                }
            }
        }
        // user-defined distribution port validation
        if (StringUtils.isNotBlank(microServiceInfo.getPublish_port())) {

            if (DiscoverUtil.checkExist(DiscoverUtil.HTTP_PROTOCOL, microServiceInfo.getProtocol())) {

                if (microServiceInfo.getPublish_port().contains("|")) {

                    String[] publishPortArray = StringUtils.split(microServiceInfo.getPublish_port(), "|");

                    int portNum = publishPortArray.length;

                    // port format validation
                    for (int i = 0; i < portNum; i++) {
                        if (!RegExpTestUtil.portRegExpTest(publishPortArray[i])) {
                            throw new UnprocessableEntityException("register MicroServiceInfo FAIL:Public Port("
                                            + publishPortArray[i] + ")is not a valid Port address");
                        }
                    }

                    // port number validation
                    if (portNum == 0 || portNum > 2) {
                        throw new UnprocessableEntityException(
                                        "register MicroServiceInfo FAIL:Public Port num is wrong:" + portNum);
                    } else if (portNum == 2) {
                        // port value equality validation
                        if (publishPortArray[0].equals(publishPortArray[1])) {
                            throw new UnprocessableEntityException(
                                            "register MicroServiceInfo FAIL:Two ports have the same value :"
                                                            + publishPortArray[0]);
                        }
                    } else if (portNum == 1) {
                        throw new UnprocessableEntityException(
                                        "register MicroServiceInfo FAIL:Two ports have one null value");
                    }
                } else {
                    if (!RegExpTestUtil.portRegExpTest(microServiceInfo.getPublish_port())) {
                        throw new UnprocessableEntityException("register MicroServiceInfo FAIL:Public Port("
                                        + microServiceInfo.getPublish_port() + ")is not a valid Port address");
                    }
                }

            } else if ("TCP".equals(microServiceInfo.getProtocol()) || "UDP".equals(microServiceInfo.getProtocol())) {
                if (!RegExpTestUtil.portRegExpTest(microServiceInfo.getPublish_port())) {
                    throw new UnprocessableEntityException("register MicroServiceInfo FAIL:Public Port("
                                    + microServiceInfo.getPublish_port() + ")is not a valid Port address");
                }

                int tcpUdpPortRangeStart = Integer.parseInt(ConfigUtil.getInstance().getTcpudpPortRangeStart());
                int tcpUdpPortRangeEnd = Integer.parseInt(ConfigUtil.getInstance().getTcpudpPortRangeEnd());
                int iPublishPort = Integer.parseInt(microServiceInfo.getPublish_port());

                if (iPublishPort > tcpUdpPortRangeEnd || iPublishPort < tcpUdpPortRangeStart) {
                    throw new UnprocessableEntityException("register MicroServiceInfo FAIL:Public_Port Range ("
                                    + tcpUdpPortRangeStart + "-" + tcpUdpPortRangeEnd + ")");
                }

            } else {
                microServiceInfo.setPublish_port("");
            }
        }
    }
    
    private void checkServiceNameAndVersion(String serviceName, String version) {
        if (StringUtils.isBlank(serviceName)) {
            throw new UnprocessableEntityException("check MicroServiceInfo FAIL:serviceName  can't be empty");
        }

        if (!RegExpTestUtil.serviceNameRegExpTest(serviceName)) {
            throw new UnprocessableEntityException(
                            "check MicroServiceInfo FAIL:ServiceName(" + serviceName + ") format error");
        }

        if (StringUtils.isNotBlank(version)) {
            if (!RegExpTestUtil.versionRegExpTest(version)) {
                throw new UnprocessableEntityException("check MicroServiceInfo FAIL:version is not a valid  format");
            }
        }
    }
}
