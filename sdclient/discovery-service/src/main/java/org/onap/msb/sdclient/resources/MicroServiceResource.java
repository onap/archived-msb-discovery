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
package org.onap.msb.sdclient.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpStatus;
import org.onap.msb.sdclient.core.ConsulResponse;
import org.onap.msb.sdclient.core.MicroServiceFullInfo;
import org.onap.msb.sdclient.core.MicroServiceInfo;
import org.onap.msb.sdclient.core.NodeAddress;
import org.onap.msb.sdclient.core.PublishAddress;
import org.onap.msb.sdclient.core.PublishFullAddress;
import org.onap.msb.sdclient.core.exception.ExtendedInternalServerErrorException;
import org.onap.msb.sdclient.health.ConsulLinkHealthCheck;
import org.onap.msb.sdclient.wrapper.ConsulServiceWrapper;
import org.onap.msb.sdclient.wrapper.PublishAddressWrapper;
import org.onap.msb.sdclient.wrapper.util.ConfigUtil;
import org.onap.msb.sdclient.wrapper.util.DiscoverUtil;
import org.onap.msb.sdclient.wrapper.util.JacksonJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheck.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/services")
@Api(tags = {"Service Resource"})
@Produces(MediaType.APPLICATION_JSON)
public class MicroServiceResource {


    @Context
    UriInfo uriInfo; // actual uri info



    private static final Logger LOGGER = LoggerFactory.getLogger(MicroServiceResource.class);

    @GET
    @Path("/")
    @ApiOperation(value = "get all microservices ", code = HttpStatus.SC_OK, response = MicroServiceFullInfo.class,
                    responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    message = "get microservice List  fail", response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public List<MicroServiceFullInfo> getMicroService() {
        return ConsulServiceWrapper.getInstance().getAllMicroServiceInstances();
    }

    @POST
    @Path("/")
    @ApiOperation(value = "add one microservice ", code = HttpStatus.SC_CREATED, response = MicroServiceFullInfo.class)
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "add microservice fail",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST,
                                    message = "Unprocessable MicroServiceInfo JSON REQUEST", response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response addMicroService(
                    @ApiParam(value = "MicroServiceInfo Instance Info",
                                    required = true) MicroServiceInfo microServiceInfo,
                    @Context HttpServletRequest request,
                    @ApiParam(value = "createOrUpdate",
                                    required = false) @QueryParam("createOrUpdate") @DefaultValue("true") boolean createOrUpdate,
                    @ApiParam(value = "is_manual",
                                    required = false) @QueryParam("is_manual") @DefaultValue("false") boolean is_manual) {
        LOGGER.error("**** json string:" + microServiceInfo);
        String ip = DiscoverUtil.getRealIp(request);

        try {
            LOGGER.info("[POST REQUEST] Request IP:" + ip + ",Request Param:[createOrUpdate]" + createOrUpdate
                            + ",Request Body:" + JacksonJsonUtil.beanToJson(microServiceInfo));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOGGER.error("[POST REQUEST] beanToJson is wrong:" + e.getMessage());
        }

        MicroServiceFullInfo microServiceFullInfo = ConsulServiceWrapper.getInstance()
                        .saveMicroServiceInstance(microServiceInfo, createOrUpdate, ip, is_manual);
        URI returnURI = uriInfo.getAbsolutePathBuilder()
                        .path("/" + microServiceInfo.getServiceName() + "/version/" + microServiceInfo.getVersion())
                        .build();
        return Response.created(returnURI).entity(microServiceFullInfo).build();
    }



    @GET
    @Path("/{serviceName}/version/{version}/nodes")
    @ApiOperation(value = "get one microservice ", code = HttpStatus.SC_OK, response = MicroServiceFullInfo.class,
                    responseContainer = "List")
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "microservice not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "get microservice fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public List<MicroServiceFullInfo> getMicroServiceNodes(
                    @ApiParam(value = "microservice serviceName") @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"") @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "Format key:value,Multiple use ',' split",
                                    required = false) @QueryParam("labels") @DefaultValue("") String labels,
                    @ApiParam(value = "namespace",
                                    required = false) @QueryParam("namespace") @DefaultValue("") String namespace,
                    @ApiParam(value = "if true then only query passing services",
                                    required = false) @QueryParam("ifPassStatus") @DefaultValue("true") boolean ifPassStatus) {

        return ConsulServiceWrapper.getInstance().getMicroServiceForNodes(serviceName, version, ifPassStatus, labels,
                        namespace);


    }

    @GET
    @Path("/{serviceName}/version/{version}")
    @ApiOperation(value = "get one microservice nodes", code = HttpStatus.SC_OK, response = MicroServiceFullInfo.class)
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "microservice not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "get microservice fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getMicroService(
                    @ApiParam(value = "microservice serviceName") @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"") @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "Format key:value,Multiple use ',' split",
                                    required = false) @QueryParam("labels") @DefaultValue("") String labels,
                    @ApiParam(value = "namespace",
                                    required = false) @QueryParam("namespace") @DefaultValue("") String namespace,
                    @ApiParam(value = "if true then only query passing services",
                                    required = false) @QueryParam("ifPassStatus") @DefaultValue("true") boolean ifPassStatus,
                    @ApiParam(value = "wait", required = false) @QueryParam("wait") @DefaultValue("") String wait,
                    @ApiParam(value = "index", required = false) @QueryParam("index") @DefaultValue("") String index) {


        ConsulResponse<MicroServiceFullInfo> serviceResponse = ConsulServiceWrapper.getInstance()
                        .getMicroServiceInstance(serviceName, version, ifPassStatus, wait, index, labels, namespace);
        return Response.ok(serviceResponse.getResponse()).header("X-Consul-Index", serviceResponse.getIndex()).build();


    }

    @PUT
    @Path("/{serviceName}/version/{version}")
    @ApiOperation(value = "update one microservice by serviceName and version", code = HttpStatus.SC_CREATED,
                    response = MicroServiceFullInfo.class)
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "update microservice fail",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST,
                                    message = "Unprocessable MicroServiceInfo JSON REQUEST", response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response updateMicroService(
                    @ApiParam(value = "microservice serviceName") @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"") @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "namespace",
                                    required = false) @QueryParam("namespace") @DefaultValue("") String namespace,
                    @ApiParam(value = "microservice Instance Info", required = true) MicroServiceInfo microServiceInfo,
                    @Context HttpServletRequest request,
                    @ApiParam(value = "protocol",
                                    required = false) @QueryParam("protocol") @DefaultValue("") String protocol,
                    @ApiParam(value = "is_manual", required = false,
                                    hidden = true) @QueryParam("is_manual") @DefaultValue("false") boolean is_manual) {

        String ip = DiscoverUtil.getRealIp(request);
        MicroServiceFullInfo microServiceFullInfo = ConsulServiceWrapper.getInstance()
                        .updateMicroServiceInstance(serviceName, version, namespace, microServiceInfo, ip, is_manual);
        return Response.created(uriInfo.getAbsolutePathBuilder().build()).entity(microServiceFullInfo).build();

    }

    @PUT
    @Path("/{serviceName}/version/{version}/ttl")
    @ApiOperation(value = "passing one microservice health check by ttl", code = HttpStatus.SC_CREATED,
                    response = NodeAddress.class)
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "health check by ttl fail",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Unprocessable CheckNode JSON REQUEST",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response healthCheckbyTTL(
                    @ApiParam(value = "microservice serviceName") @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"") @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "namespace",
                                    required = false) @QueryParam("namespace") @DefaultValue("") String namespace,
                    @ApiParam(value = "CheckNode Instance Info", required = true) NodeAddress checkNode) {

        ConsulServiceWrapper.getInstance().healthCheckbyTTL(serviceName, version, namespace, checkNode);
        return Response.created(uriInfo.getAbsolutePathBuilder().build()).entity(checkNode).build();

    }



    @DELETE
    @Path("/{serviceName}/version/{version}/nodes/{ip}/{port}")
    @ApiOperation(value = "delete single node by serviceName and version and node", code = HttpStatus.SC_NO_CONTENT)
    @ApiResponses(value = {@ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = "delete node succeed "),
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "node not found", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "delete node fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public void deleteNode(
                    @ApiParam(value = "microservice serviceName",
                                    required = true) @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"",
                                    required = false) @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "namespace",
                                    required = false) @QueryParam("namespace") @DefaultValue("") String namespace,
                    @ApiParam(value = "ip") @PathParam("ip") String ip,
                    @ApiParam(value = "port") @PathParam("port") String port, @ApiParam(value = "protocol",
                                    required = false) @QueryParam("protocol") @DefaultValue("") String protocol) {
        LOGGER.info("[DELETE NODE REQUEST] serviceName:" + serviceName + ",version:" + version + ",namespace:"
                        + namespace + ",protocol:" + protocol + ",ip:" + ip + ",port:" + port);
        ConsulServiceWrapper.getInstance().deleteMicroServiceInstance(serviceName, version, namespace, ip, port);

    }


    @DELETE
    @Path("/{serviceName}/version/{version}")
    @ApiOperation(value = "delete one full microservice by serviceName and version", code = HttpStatus.SC_NO_CONTENT)
    @ApiResponses(value = {@ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = "delete microservice succeed "),
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "microservice not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "delete microservice fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public void deleteMicroService(
                    @ApiParam(value = "microservice serviceName",
                                    required = true) @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"",
                                    required = false) @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "namespace",
                                    required = false) @QueryParam("namespace") @DefaultValue("") String namespace,
                    @ApiParam(value = "protocol",
                                    required = false) @QueryParam("protocol") @DefaultValue("") String protocol) {

        LOGGER.info("[DELETE REQUEST] serviceName:" + serviceName + ",version:" + version + ",namespace:" + namespace
                        + ",protocol:" + protocol);

        ConsulServiceWrapper.getInstance().deleteMicroService(serviceName, version, namespace);

    }



    // @PUT
    // @Path("/{serviceName}/version/{version}/status/{status}")
    // @ApiOperation(value = "update one microservice's status by serviceName and version",
    // response = RouteResult.class)
    // @ApiResponses(value = {@ApiResponse(code = 500, message =
    // "update microservice status error ")})
    // @Produces(MediaType.APPLICATION_JSON)
    // @Timed
    // public RouteResult updateMicroServiceStatus(
    // @ApiParam(value = "microservice serviceName", required = true) @PathParam("serviceName")
    // String serviceName,
    // @ApiParam(value = "microservice version", required = false) @PathParam("version")
    // @DefaultValue("") String version,
    // @ApiParam(value = "microservice status", required = true) @PathParam("status") String status)
    // {
    //
    // return MicroServiceWrapper.getInstance().updateMicroServiceStatus(serviceName, version,
    // status);
    //
    // }


    @GET
    @Path("/{serviceName}/version/{version}/publishaddress")
    @ApiOperation(value = "get one microservice's inner publishaddress", code = HttpStatus.SC_OK,
                    response = PublishAddress.class)
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "publishaddress not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "get publishaddress fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public PublishAddress getPublishaddress(
                    @ApiParam(value = "microservice serviceName") @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"") @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "namespace",
                                    required = false) @QueryParam("namespace") @DefaultValue("") String namespace,
                    @ApiParam(value = "Waiting time,Scope: 5-300, unit: second",
                                    required = false) @QueryParam("wait") @DefaultValue("0") int wait) {


        return PublishAddressWrapper.getInstance().getPublishaddress(serviceName, version, namespace, wait);


    }

    @GET
    @Path("/apigatewayserviceinfo")
    @ApiOperation(value = "get apigateway AddressInfo", code = HttpStatus.SC_OK, response = MicroServiceFullInfo.class,
                    responseContainer = "List")
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "apigateway ServiceInfo not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                    message = "get apigateway ServiceInfo fail", response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Set<MicroServiceFullInfo> getApigatewayServiceInfo(
                    @ApiParam(value = "namespace",
                                    required = false) @QueryParam("namespace") @DefaultValue("") String namespace,
                    @ApiParam(value = "visualRange",
                                    required = false) @QueryParam("visualRange") @DefaultValue("1") String visualRange) {


        return PublishAddressWrapper.getInstance().getApigatewayServiceInfo(namespace, visualRange);


    }

    @GET
    @Path("/{serviceName}/version/{version}/allpublishaddress")
    @ApiOperation(value = "get one microservice's all publishaddress", code = HttpStatus.SC_OK,
                    response = PublishFullAddress.class, responseContainer = "List")
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "publishaddress not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "get publishaddress fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Set<PublishFullAddress> getAllPublishaddress(
                    @ApiParam(value = "microservice serviceName") @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"") @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "namespace",
                                    required = false) @QueryParam("namespace") @DefaultValue("") String namespace,
                    @ApiParam(value = "outSystem:0,inSystem:1,all:0|1(default)",
                                    required = false) @QueryParam("visualRange") @DefaultValue("0|1") String visualRange) {


        return PublishAddressWrapper.getInstance().getAllPublishaddress(serviceName, version, namespace, visualRange);


    }


    @GET
    @Path("/tcpudpportrange")
    @ApiOperation(value = "get tcp and udp port range", code = HttpStatus.SC_OK, response = String.class,
                    responseContainer = "List")
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "port range not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "get port range fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public String[] getTCP_UDP_portRange() {

        return new String[] {ConfigUtil.getInstance().getTcpudpPortRangeStart(),
                        ConfigUtil.getInstance().getTcpudpPortRangeEnd()};

    }

    @GET
    @Path("/health")
    @ApiOperation(value = "sdclient healthy check ", code = HttpStatus.SC_OK, response = String.class)
    @ApiResponses(value = {@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "check fail",
                    response = String.class)})
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    public Response health() {

        ArrayList<HealthCheck> healthcheckArray = new ArrayList<HealthCheck>();

        // consul link check
        healthcheckArray.add(new ConsulLinkHealthCheck());

        // begin check
        for (int i = 0; i < healthcheckArray.size(); i++) {
            Result rst = healthcheckArray.get(i).execute();

            if (!rst.isHealthy()) {
                LOGGER.warn("health check failed:" + rst.getMessage());
                throw new ExtendedInternalServerErrorException(rst.getMessage());
            }
        }

        return Response.ok("sdclient healthy check:ok").build();
    }

}
