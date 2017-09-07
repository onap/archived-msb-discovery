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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.onap.msb.sdclient.core.KeyVaulePair;
import org.onap.msb.sdclient.core.MicroServiceFullInfo;
import org.onap.msb.sdclient.core.NodeInfo;
import org.onap.msb.sdclient.core.PublishAddress;
import org.onap.msb.sdclient.core.PublishFullAddress;
import org.onap.msb.sdclient.core.exception.ExtendedNotFoundException;
import org.onap.msb.sdclient.core.exception.UnprocessableEntityException;
import org.onap.msb.sdclient.wrapper.util.DiscoverUtil;
import org.onap.msb.sdclient.wrapper.util.RegExpTestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishAddressWrapper {

    private static PublishAddressWrapper instance = new PublishAddressWrapper();


    private PublishAddressWrapper() {}

    public static PublishAddressWrapper getInstance() {
        return instance;
    }

    private final String ROUTE_DEFAULT_WAY = "ip";

    private final String ROUTE_IP = "ip";

    private final String ROUTE_DOMAIN = "domain";

    private final String ROUTE_DEFAULT_SUBDOMAIN = "openpalette.zte.com.cn";

    private final String METADATA_ROUTE_WAY = "routeWay";

    private final String METADATA_ROUTE_SUBDOMAIN = "routeSubdomain";



    private static final Logger LOGGER = LoggerFactory.getLogger(PublishAddressWrapper.class);

    public static volatile Map<String, List<MicroServiceFullInfo>> publishApigateWayList =
                    new HashMap<String, List<MicroServiceFullInfo>>();

    private ConsulClientApp consulClientApp;


    ExecutorService exec = Executors.newCachedThreadPool();


    public void setConsulClientApp(ConsulClientApp consulClientApp) {
        this.consulClientApp = consulClientApp;
    }



    /**
     * @Title getAllPublishaddress
     * @Description TODO(get all publishaddresss list by service,rest-interface master methods)
     * @param serviceName
     * @param version
     * @param namespace
     * @param visualRange
     * @return
     * @return List<PublishFullAddress>
     */
    public Set<PublishFullAddress> getAllPublishaddress(String serviceName, String version, String namespace,
                    String visualRange) {

        if ("null".equals(version)) {
            version = "";
        }


        // 1.Check input parameter format efficacy
        checkServiceInputFormat(serviceName, version, visualRange);



        // 2.get service Info
        MicroServiceFullInfo serviceInfo =
                        ConsulServiceWrapper.getInstance().getMicroServiceInstance(serviceName, version, namespace);

        if (!DiscoverUtil.checkExist(DiscoverUtil.PUBLISH_PROTOCOL, serviceInfo.getProtocol())) {
            throw new ExtendedNotFoundException("This service's Protocol (" + serviceInfo.getProtocol()
                            + ") is not published to apigateway");
        }

        if ("TCP".equals(serviceInfo.getProtocol()) || "UDP".equals(serviceInfo.getProtocol())) {
            if (StringUtils.isBlank(serviceInfo.getPublish_port())) {
                throw new ExtendedNotFoundException("This service's  Protocol (" + serviceInfo.getProtocol()
                                + ") is not published to apigateway");
            }
        }

        Set<PublishFullAddress> publishFullAddressList = new HashSet<PublishFullAddress>();

        // 3.get in-system apigateway publish address (visualRange=1)
        if (DiscoverUtil.checkVisualRangeIn(visualRange)) {
            Set<PublishFullAddress> publishFullAddressInList =
                            getPublishFullAddress(namespace, DiscoverUtil.VISUAL_RANGE_IN, serviceInfo);
            if (publishFullAddressInList != null && publishFullAddressInList.size() > 0) {
                publishFullAddressList.addAll(publishFullAddressInList);
            }

        }

        // 4.get out-system apigateway publish address (visualRange=0)
        if (DiscoverUtil.checkVisualRangeOut(visualRange)) {
            Set<PublishFullAddress> publishFullAddressOutList =
                            getPublishFullAddress(namespace, DiscoverUtil.VISUAL_RANGE_OUT, serviceInfo);
            if (publishFullAddressOutList != null && publishFullAddressOutList.size() > 0) {
                publishFullAddressList.addAll(publishFullAddressOutList);
            }
        }

        if (publishFullAddressList.size() > 0) {
            return publishFullAddressList;
        }

        throw new ExtendedNotFoundException("This service's publish Address is not found");
    }

    /**
     * @Title getApigatewayServiceInfo
     * @Description TODO(get one apigatewayServiceInfo by namespace,rest-interface master methods)
     * @param namespace
     * @param visualRange
     * @return
     * @return List<MicroServiceFullInfo>
     */
    public Set<MicroServiceFullInfo> getApigatewayServiceInfo(String namespace, String visualRange) {

        if (!DiscoverUtil.checkExist(DiscoverUtil.VISUAL_RANGE_LIST, visualRange, ",")) {
            throw new UnprocessableEntityException("get ApigatewayServiceInfo FAIL:visualRange is wrong,value range:("
                            + DiscoverUtil.VISUAL_RANGE_LIST + ")");
        }

        List<MicroServiceFullInfo> apigatewayList;

        if (DiscoverUtil.checkVisualRangeIn(visualRange)) {
            apigatewayList = getApiGateWayFromCache(DiscoverUtil.APIGATEWAY_SERVINCE, namespace);

        } else {
            apigatewayList = getApiGateWayFromCache(DiscoverUtil.ROUTER_SERVINCE, namespace);

            if (apigatewayList != null) {
                if (StringUtils.isNotBlank(System.getenv("ROUTER_IP"))) {
                    for (MicroServiceFullInfo routerInfo : apigatewayList) {
                        for (NodeInfo node : routerInfo.getNodes()) {
                            node.setIp(System.getenv("ROUTER_IP"));
                        }

                    }
                }
            }
        }


        if (apigatewayList == null || apigatewayList.isEmpty()) {
            throw new ExtendedNotFoundException("This service's  publish Address is not found");
        } else {
            Set<MicroServiceFullInfo> apigatewaySet = new HashSet<MicroServiceFullInfo>(apigatewayList);
            return apigatewaySet;
        }



    }



    /**
     * @Title convert2PublishFullAddress
     * @Description TODO(convert to PublishFullAddress from MicroServiceFullInfo )
     * @param apigatewayInfo
     * @param serviceInfo
     * @return List<PublishFullAddress>
     */
    private List<PublishFullAddress> convert2PublishFullAddress(MicroServiceFullInfo apigatewayInfo,
                    MicroServiceFullInfo serviceInfo) {

        List<PublishFullAddress> publishFullAddressList = new ArrayList<PublishFullAddress>();



        String routeWay = this.ROUTE_DEFAULT_WAY, routeSubdomain = this.ROUTE_DEFAULT_SUBDOMAIN;

        List<KeyVaulePair> metadata = apigatewayInfo.getMetadata();
        if (metadata != null) {

            for (KeyVaulePair keyVaulePair : metadata) {
                if (this.METADATA_ROUTE_WAY.equals(keyVaulePair.getKey())) {
                    routeWay = keyVaulePair.getValue();
                }
                if (this.METADATA_ROUTE_SUBDOMAIN.equals(keyVaulePair.getKey())) {
                    routeSubdomain = keyVaulePair.getValue();
                }
            }
        }

        NodeInfo apigatewayNode = (NodeInfo) apigatewayInfo.getNodes().toArray()[0];

        String[] routeWays = StringUtils.split(routeWay, DiscoverUtil.SPLIT_LINE);
        for (int i = 0; i < routeWays.length; i++) {
            PublishFullAddress publishFullAddress = new PublishFullAddress();
            // set service publish visualRange
            publishFullAddress.setVisualRange(apigatewayInfo.getVisualRange());
            if (this.ROUTE_IP.equals(routeWays[i])) {
                // ----routeWay:ip-----

                // set service publish ip
                publishFullAddress.setIp(apigatewayNode.getIp());
                if (DiscoverUtil.VISUAL_RANGE_OUT.equals(apigatewayInfo.getVisualRange())) {
                    if (StringUtils.isNotBlank(System.getenv("ROUTER_IP"))) {
                        publishFullAddress.setIp(System.getenv("ROUTER_IP"));
                    }
                }



                // set service publish url
                publishFullAddress.setPublish_url(getPublishUrl4IP(serviceInfo));

                // set service port
                if (DiscoverUtil.VISUAL_RANGE_IN.equals(apigatewayInfo.getVisualRange())) {
                    publishFullAddress.setPort(apigatewayNode.getPort());
                    publishFullAddress.setPublish_protocol("http");
                    publishFullAddressList.add(publishFullAddress);
                } else {

                    String[] publishPorts = StringUtils.split(serviceInfo.getPublish_port(), DiscoverUtil.SPLIT_LINE);
                    if (publishPorts.length == 2) {
                        // multiPublishPort: https|http
                        publishFullAddress.setPort(publishPorts[0]);
                        publishFullAddress.setPublish_protocol("https");
                        publishFullAddressList.add(publishFullAddress);


                        PublishFullAddress publishFullAddress2 = new PublishFullAddress(publishFullAddress.getIp(),
                                        publishPorts[1], publishFullAddress.getPublish_url(),
                                        publishFullAddress.getVisualRange(), "http");
                        publishFullAddressList.add(publishFullAddress2);

                    } else {
                        // single Port

                        if (StringUtils.isNotBlank(serviceInfo.getPublish_port())) {
                            publishFullAddress.setPort(serviceInfo.getPublish_port());
                            publishFullAddress.setPublish_protocol("https");
                        } else {
                            publishFullAddress.setPort(apigatewayNode.getPort());
                            publishFullAddress.setPublish_protocol("http");
                        }

                        if ("TCP".equals(serviceInfo.getProtocol()) || "UDP".equals(serviceInfo.getProtocol())) {
                            publishFullAddress.setPublish_protocol(serviceInfo.getProtocol());
                        }

                        publishFullAddressList.add(publishFullAddress);

                    }
                }


            } else if (this.ROUTE_DOMAIN.equals(routeWays[i])) {
                // ----routeWay:domain-----

                // set service domain
                String host = getHost4Domain(serviceInfo);
                publishFullAddress.setDomain(host + "." + routeSubdomain);


                if ("TCP".equals(serviceInfo.getProtocol()) || "UDP".equals(serviceInfo.getProtocol())) {
                    publishFullAddress.setPort(serviceInfo.getPublish_port());
                    publishFullAddress.setPublish_protocol(serviceInfo.getProtocol());
                } else {
                    publishFullAddress.setPublish_protocol("http");
                    publishFullAddress.setPort(apigatewayNode.getPort());
                }

                // set service publish url
                publishFullAddress.setPublish_url(getPublishUrl4Domain(serviceInfo));


                publishFullAddressList.add(publishFullAddress);
            }



        }

        return publishFullAddressList;
    }


    /**
     * @Title getPublishFullAddress
     * @Description TODO(get PublishFullAddress List for namespace and visualRange)
     * @param namespace
     * @param visualRange
     * @param serviceInfo
     * @return List<PublishFullAddress>
     */
    private Set<PublishFullAddress> getPublishFullAddress(String namespace, String visualRange,
                    MicroServiceFullInfo serviceInfo) {

        if (DiscoverUtil.checkVisualRangeIn(visualRange)) {
            if (!DiscoverUtil.checkVisualRangeIn(serviceInfo.getVisualRange())) {
                return null;
            }
        } else {
            if (!DiscoverUtil.checkVisualRangeOut(serviceInfo.getVisualRange())) {
                return null;
            }
        }


        Set<PublishFullAddress> publishFullAddressList = new HashSet<PublishFullAddress>();
        List<MicroServiceFullInfo> apigatewayList = getApigatewayInfo4Service(namespace, visualRange);
        if (apigatewayList != null && !apigatewayList.isEmpty()) {
            for (MicroServiceFullInfo apigatewayInfo : apigatewayList) {
                if (isPublish2apigateway(apigatewayInfo, serviceInfo)) {
                    publishFullAddressList.addAll(convert2PublishFullAddress(apigatewayInfo, serviceInfo));
                }
            }
        }
        return publishFullAddressList;
    }



    private String getHost4Domain(MicroServiceFullInfo serviceInfo) {
        String host = "";
        if (StringUtils.isNotBlank(serviceInfo.getHost())) {
            host = serviceInfo.getHost();
        } else {
            if (StringUtils.isNotBlank(serviceInfo.getNamespace())) {
                host = serviceInfo.getServiceName() + DiscoverUtil.SERVICENAME_LINE_NAMESPACE
                                + serviceInfo.getNamespace();
            } else {
                host = serviceInfo.getServiceName();
            }
        }

        return host;
    }

    private String getPublishPort(MicroServiceFullInfo apigatewayInfo, MicroServiceFullInfo serviceInfo) {

        NodeInfo node = (NodeInfo) apigatewayInfo.getNodes().toArray()[0];
        String port = "";

        if ("TCP".equals(serviceInfo.getProtocol()) || "UDP".equals(serviceInfo.getProtocol())) {
            return serviceInfo.getPublish_port();
        }

        if (DiscoverUtil.VISUAL_RANGE_IN.equals(apigatewayInfo.getVisualRange())) {
            port = node.getPort();
        } else {
            if (StringUtils.isNotBlank(serviceInfo.getPublish_port())) {
                port = serviceInfo.getPublish_port();
            } else {
                port = node.getPort();
            }
        }

        return port;

    }



    private String getPublishUrl4Domain(MicroServiceFullInfo serviceInfo) {
        String publish_url = "/";
        if (StringUtils.isNotBlank(serviceInfo.getPath()) && !"/".equals(serviceInfo.getPath())) {
            publish_url = serviceInfo.getPath();
        } else {
            publish_url = serviceInfo.getUrl();
        }
        return publish_url;
    }

    private String getPublishUrl4IP(MicroServiceFullInfo serviceInfo) {

        String publish_url = "/";
        if (StringUtils.isNotBlank(serviceInfo.getPath()) && !"/".equals(serviceInfo.getPath())) {
            publish_url = serviceInfo.getPath();
        } else {
            String versionUrl = "";
            String serviceNameUrl = serviceInfo.getServiceName();

            if (StringUtils.isNotBlank(serviceInfo.getVersion())) {
                versionUrl = "/" + serviceInfo.getVersion();
            }
            switch (serviceInfo.getProtocol()) {
                case "REST":
                    publish_url = "/api/" + serviceNameUrl + versionUrl;
                    break;
                case "UI":
                    publish_url = "/iui/" + serviceNameUrl;
                    break;
                case "HTTP":
                    publish_url = "/" + serviceNameUrl + versionUrl;
                    break;
                case "PORTAL":
                    publish_url = "/" + serviceNameUrl + versionUrl;
                    break;
                case "TCP":
                    publish_url = serviceInfo.getUrl();
                    break;
                case "UDP":
                    publish_url = serviceInfo.getUrl();
                    break;
            }
        }

        return publish_url;
    }


    private void checkServiceInputFormat(String serviceName, String version, String visualRange) {
        if (StringUtils.isBlank(serviceName)) {
            throw new UnprocessableEntityException("serviceName  can't be empty");
        }

        if (!RegExpTestUtil.serviceNameRegExpTest(serviceName)) {
            throw new UnprocessableEntityException(
                            "get MicroServiceInfo FAIL:ServiceName(" + serviceName + ") format error");
        }

        if (StringUtils.isNotBlank(version)) {
            if (!RegExpTestUtil.versionRegExpTest(version)) {
                throw new UnprocessableEntityException("version (" + version + ") is not a valid  format");
            }
        }

        if (!DiscoverUtil.checkVisualRangeIn(visualRange) && !DiscoverUtil.checkVisualRangeOut(visualRange)) {
            throw new UnprocessableEntityException("get ApigatewayServiceInfo FAIL:visualRange is wrong,value range:("
                            + DiscoverUtil.VISUAL_RANGE_LIST + ")");
        }
    }


    /**
     * @Title getApigatewayInfo4Service
     * @Description TODO(get apigatewayServiceInfo List by namespaces[all & service-namespace])
     * @param namespace
     * @param visualRange
     * @return
     * @return List<MicroServiceFullInfo>
     */
    private List<MicroServiceFullInfo> getApigatewayInfo4Service(String namespace, String visualRange) {

        String apigatewayName;
        if (DiscoverUtil.checkVisualRangeIn(visualRange)) {
            apigatewayName = DiscoverUtil.APIGATEWAY_SERVINCE;
        } else {
            apigatewayName = DiscoverUtil.ROUTER_SERVINCE;
        }


        String apigateway_ns;
        if (StringUtils.isBlank(namespace)) {
            apigateway_ns = DiscoverUtil.APIGATEWAY_SERVINCE_DEFAULT;
        } else {
            apigateway_ns = namespace;
        }

        String[] apigateway_ns_array = {DiscoverUtil.APIGATEWAY_SERVINCE_ALL, apigateway_ns};
        List<MicroServiceFullInfo> apigatewayList4Service = new ArrayList<MicroServiceFullInfo>();
        for (int i = 0; i < apigateway_ns_array.length; i++) {
            List<MicroServiceFullInfo> apigatewayList = getApiGateWayFromCache(apigatewayName, apigateway_ns_array[i]);
            if (apigatewayList != null) {
                apigatewayList4Service.addAll(apigatewayList);
            }
        }

        return apigatewayList4Service;

    }


    private boolean isPublish2apigateway(MicroServiceFullInfo apigatewayInfo, MicroServiceFullInfo serviceInfo) {
        return isPublishByNetwork_plane_typeMatches(apigatewayInfo.getNetwork_plane_type(),
                        serviceInfo.getNetwork_plane_type())
                        && isPublishByRouteLabels(apigatewayInfo.getLabels(), serviceInfo.getLabels());
    }

    /**
     * Determine whether the service needs to publish to apigateway TODO: according to the
     * service_network_plane filter conditions
     * 
     * @param String
     * @return
     */

    private boolean isPublishByNetwork_plane_typeMatches(String apigateway_network_plane,
                    String service_network_plane) {

        if (StringUtils.isBlank(apigateway_network_plane))
            return true;
        String[] routeNetwork_plane_typeArray = StringUtils.split(apigateway_network_plane, "|");
        String[] serviceVisualRangeArray = StringUtils.split(service_network_plane, "|");
        if (DiscoverUtil.contain(serviceVisualRangeArray, routeNetwork_plane_typeArray)) {
            return true;
        }

        return false;
    }


    /**
     * Determine whether the service needs to publish to apigateway TODO: according to the labels
     * filter conditions
     * 
     * @param labelMap
     * @return
     */
    private boolean isPublishByRouteLabels(List<String> apigatewayLabels, List<String> serviceLabels) {
        if (apigatewayLabels == null || apigatewayLabels.isEmpty()) {
            return true;
        }

        Map<String, String> apigateway_labelMap = new HashMap<String, String>();
        Map<String, String> service_labelMap = new HashMap<String, String>();
        for (String label : apigatewayLabels) {
            String[] labelArray = label.split(":");
            apigateway_labelMap.put(labelArray[0], labelArray[1]);
        }

        for (String label : serviceLabels) {
            String[] labelArray = label.split(":");
            service_labelMap.put(labelArray[0], labelArray[1]);
        }

        for (Map.Entry<String, String> entry : apigateway_labelMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Multiple values match
            String[] routeLalelsArray = StringUtils.split(value, "|");
            if (StringUtils.isBlank(service_labelMap.get(key))) {
                continue;
            }

            String[] serviceLabelsArray = StringUtils.split(service_labelMap.get(key), "|");

            if (DiscoverUtil.contain(routeLalelsArray, serviceLabelsArray)) {
                return true;
            }

        }

        return false;
    }



    private List<MicroServiceFullInfo> getApiGateWayFromCache(String apigatewayName, String apigatewayNamespace) {
        String apigatewayConsulName = apigatewayName + DiscoverUtil.SERVICENAME_LINE_NAMESPACE + apigatewayNamespace;
        if (publishApigateWayList.get(apigatewayConsulName) == null) {

            try {
                List<MicroServiceFullInfo> apigatewayList = ConsulServiceWrapper.getInstance()
                                .getMicroServiceForNodes(apigatewayName, "v1", true, "", apigatewayNamespace);
                if (!apigatewayList.isEmpty()) {
                    consulClientApp.startHealthNodeListen(apigatewayConsulName);
                    return apigatewayList;
                }
            } catch (ExtendedNotFoundException e) {
                LOGGER.warn("ApiGateWay Info not found:[serviceName]" + apigatewayName + ",[namespace]"
                                + apigatewayNamespace);
            }

        } else {
            return publishApigateWayList.get(apigatewayConsulName);
        }

        return null;
    }


    public PublishAddress getPublishaddress(String serviceName, String version, String namespace, int wait) {
        if ("null".equals(version)) {
            version = "";
        }

        // 1.Check input parameter format efficacy
        checkServiceInputFormat(serviceName, version, DiscoverUtil.VISUAL_RANGE_IN);


        MicroServiceFullInfo microServiceFullInfo =
                        ConsulServiceWrapper.getInstance().getMicroServiceInstance(serviceName, version, namespace);

        if (!DiscoverUtil.checkVisualRangeIn(microServiceFullInfo.getVisualRange())) {
            throw new ExtendedNotFoundException("This service is not published internally");
        }

        if (!DiscoverUtil.checkExist(DiscoverUtil.PUBLISH_PROTOCOL, microServiceFullInfo.getProtocol())) {
            throw new ExtendedNotFoundException("This service's Protocol (" + microServiceFullInfo.getProtocol()
                            + ") is not published to apigateway");
        }

        List<PublishAddress> publishaddress_all = new ArrayList<PublishAddress>();
        List<PublishAddress> publishaddress_ns = new ArrayList<PublishAddress>();

        List<MicroServiceFullInfo> apigatewayList_in_all =
                        getApiGateWayFromCache(DiscoverUtil.APIGATEWAY_SERVINCE, "all");
        if (apigatewayList_in_all != null && !apigatewayList_in_all.isEmpty()) {
            for (MicroServiceFullInfo apigateway : apigatewayList_in_all) {
                if (isPublish2apigateway(apigateway, microServiceFullInfo)) {
                    publishaddress_all.add(convert2PublishAddress(apigateway, microServiceFullInfo));
                }
            }
        }



        String apigateway_ns;
        if (StringUtils.isBlank(namespace)) {
            apigateway_ns = DiscoverUtil.APIGATEWAY_SERVINCE_DEFAULT;
        } else {
            apigateway_ns = namespace;
        }

        List<MicroServiceFullInfo> apigatewayList_in_ns =
                        getApiGateWayFromCache(DiscoverUtil.APIGATEWAY_SERVINCE, apigateway_ns);
        if (apigatewayList_in_ns != null && !apigatewayList_in_ns.isEmpty()) {
            for (MicroServiceFullInfo apigateway : apigatewayList_in_ns) {
                if (isPublish2apigateway(apigateway, microServiceFullInfo)) {
                    publishaddress_ns.add(convert2PublishAddress(apigateway, microServiceFullInfo));
                }
            }
        }



        // 即时返回
        if (wait < 5) {
            if (publishaddress_ns.size() > 0) {
                return publishaddress_ns.get(0);
            } else if (publishaddress_all.size() > 0) {
                return publishaddress_all.get(0);
            }


            throw new ExtendedNotFoundException("This service's publish address is not found");
        }

        if (wait > 300) {
            wait = 300;
        }


        // get service publish url
        String publish_url = "/";
        if (StringUtils.isNotBlank(microServiceFullInfo.getPath())) {
            publish_url = microServiceFullInfo.getPath();
        } else {
            String versionUrl = "";
            String serviceNameUrl = microServiceFullInfo.getServiceName();


            if (StringUtils.isNotBlank(microServiceFullInfo.getVersion())) {
                versionUrl = "/" + microServiceFullInfo.getVersion();
            }
            switch (microServiceFullInfo.getProtocol()) {
                case "REST":
                    publish_url = "/api/" + serviceNameUrl + versionUrl;
                    break;
                case "UI":
                    publish_url = "/iui/" + serviceNameUrl;
                    break;
                case "HTTP":
                    publish_url = "/" + serviceNameUrl + versionUrl;
                    break;
                case "TCP":
                    publish_url = microServiceFullInfo.getUrl();
                    break;
                case "UDP":
                    publish_url = microServiceFullInfo.getUrl();
                    break;
            }
        }

        // 延迟监听返回
        Future<PublishAddress> f = exec.submit(new TimeTask(namespace, publish_url));
        try {
            return f.get(wait, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            LOGGER.error(e.getMessage());
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            LOGGER.error(e.getMessage());
        } catch (TimeoutException e) {
            // 定义超时后的状态修改
            // LOGGER.error(e.getMessage());
            LOGGER.error(e.getMessage());
        } finally {

            f.cancel(true);
        }

        if (publishaddress_ns.size() > 0) {
            return publishaddress_ns.get(0);
        } else if (publishaddress_all.size() > 0) {
            return publishaddress_all.get(0);
        }

        throw new ExtendedNotFoundException("This service's apigatewayInfo is not found");

    }


    private PublishAddress convert2PublishAddress(MicroServiceFullInfo apigatewayInfo,
                    MicroServiceFullInfo serviceInfo) {
        PublishAddress publishAddress = new PublishAddress();

        NodeInfo node = (NodeInfo) apigatewayInfo.getNodes().toArray()[0];
        publishAddress.setIp(node.getIp());

        if (DiscoverUtil.VISUAL_RANGE_IN.equals(apigatewayInfo.getVisualRange())) {
            publishAddress.setPort(node.getPort());
        } else {
            if (StringUtils.isNotBlank(serviceInfo.getPublish_port())) {
                publishAddress.setPort(serviceInfo.getPublish_port());
            } else {
                publishAddress.setPort(node.getPort());
            }
        }


        // get service publish url
        String publish_url = "/";
        if (StringUtils.isNotBlank(serviceInfo.getPath())) {
            publish_url = serviceInfo.getPath();
        } else {
            String versionUrl = "";
            String serviceNameUrl = serviceInfo.getServiceName();


            if (StringUtils.isNotBlank(serviceInfo.getVersion())) {
                versionUrl = "/" + serviceInfo.getVersion();
            }
            switch (serviceInfo.getProtocol()) {
                case "REST":
                    publish_url = "/api/" + serviceNameUrl + versionUrl;
                    break;
                case "UI":
                    publish_url = "/iui/" + serviceNameUrl;
                    break;
                case "HTTP":
                    publish_url = "/" + serviceNameUrl + versionUrl;
                    break;
                case "TCP":
                    publish_url = serviceInfo.getUrl();
                    break;
                case "UDP":
                    publish_url = serviceInfo.getUrl();
                    break;
            }
        }

        publishAddress.setPublish_url(publish_url);


        return publishAddress;
    }



    public class TimeTask implements Callable<PublishAddress> {

        private String namespace;
        private String publish_url;

        @Override
        public PublishAddress call() throws Exception {


            while (true) {
                List<PublishAddress> oldAddress = getApigatewayListFromCache(namespace, publish_url);


                Thread.sleep(2000);
                // LOGGER.info("oldAddress:"+oldAddress);
                List<PublishAddress> newAddress = getApigatewayListFromCache(namespace, publish_url);
                if (!oldAddress.equals(newAddress)) {
                    // LOGGER.info("CHANGED:"+oldAddress+"-"+apigatewayAddress);

                    return newAddress.get(0);
                }
            }

        }

        TimeTask(String namespace, String publish_url) {
            this.namespace = namespace;
            this.publish_url = publish_url;
        }

    }

    private List<PublishAddress> getApigatewayListFromCache(String namespace, String publish_url) {
        List<PublishAddress> fullAddress = new ArrayList<PublishAddress>();
        String apigatewayName4ns =
                        DiscoverUtil.APIGATEWAY_SERVINCE + DiscoverUtil.SERVICENAME_LINE_NAMESPACE + namespace;
        if (publishApigateWayList.get(apigatewayName4ns) != null) {
            List<MicroServiceFullInfo> publishaddress4ns = publishApigateWayList.get(apigatewayName4ns);
            for (MicroServiceFullInfo address : publishaddress4ns) {
                NodeInfo node = (NodeInfo) address.getNodes().toArray()[0];
                fullAddress.add(new PublishAddress(node.getIp(), node.getPort(), publish_url));
            }

        } else {
            if (publishApigateWayList.get(DiscoverUtil.APIGATEWAY_SERVINCE_ALL) != null) {
                List<MicroServiceFullInfo> publishaddress4all =
                                publishApigateWayList.get(DiscoverUtil.APIGATEWAY_SERVINCE_ALL);
                for (MicroServiceFullInfo address : publishaddress4all) {
                    NodeInfo node = (NodeInfo) address.getNodes().toArray()[0];
                    fullAddress.add(new PublishAddress(node.getIp(), node.getPort(), publish_url));
                }
            }
        }

        return fullAddress;
    }

}
