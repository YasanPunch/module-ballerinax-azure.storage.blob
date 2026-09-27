/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.azure.storage.blob.server;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

/**
 * Consumes the storage queue an Event Grid subscription delivers blob events to, and
 * dispatches each event to the attached service.
 *
 * <p>Every method is declared so that the Ballerina side of the API compiles against the
 * surface it expects; none of them carries an implementation yet.
 */
public final class Listener {

    private Listener() {
    }

    public static Object initListener(BObject listenerObj, BString queueName, BMap<BString, Object> config) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object attachService(BObject listenerObj, BObject service, Object name) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object detachService(BObject listenerObj, BObject service) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object startListener(BObject listenerObj) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object gracefulStopListener(BObject listenerObj) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object immediateStopListener(BObject listenerObj) {
        throw new UnsupportedOperationException("not implemented");
    }
}
