/**
 * Copyright 2016 ZTE, Inc. and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.msb.sdclient.wrapper.consul.async;

import org.onap.msb.sdclient.wrapper.consul.model.ConsulResponse;

/**
 * For API calls that support long-polling, this callback is used to handle
 * the result on success or failure for an async HTTP call.
 *
 * @param <T> The Response type.
 */
public interface ConsulResponseCallback<T> {

    /**
     * Callback for a successful {@link org.onap.msb.sdclient.wrapper.consul.model.ConsulResponse}.
     *
     * @param consulResponse The Consul response.
     */
    void onComplete(ConsulResponse<T> consulResponse);

    /**
     * Callback for an unsuccessful request.
     *
     * @param throwable The exception thrown.
     */
    void onFailure(Throwable throwable);
}
