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
package org.onap.msb.sdclient;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class DiscoverAppConfig extends Configuration {
    @NotEmpty
    private String defaultWorkspace = "discover-works";

    @NotEmpty
    private String defaultName = "discover-service";


    @Valid
    private String consulAdderss;

    @Valid
    private String consulRegisterMode;


    @JsonProperty
    public String getConsulRegisterMode() {
        return consulRegisterMode;
    }

    @JsonProperty
    public void setConsulRegisterMode(String consulRegisterMode) {
        this.consulRegisterMode = consulRegisterMode;
    }

    @JsonProperty
    public String getDefaultWorkspace() {
        return defaultWorkspace;
    }

    @JsonProperty
    public void setDefaultWorkspace(String defaultWorkspace) {
        this.defaultWorkspace = defaultWorkspace;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

    @JsonProperty
    public String getConsulAdderss() {
        return consulAdderss;
    }

    @JsonProperty
    public void setConsulAdderss(String consulAdderss) {
        this.consulAdderss = consulAdderss;
    }



}
