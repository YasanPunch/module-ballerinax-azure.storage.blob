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

package io.ballerina.lib.azure.storage.blob.client;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

/**
 * Shared access signature generation. Signing is local, so these carry no
 * {@code Environment}: no request is made to Azure.
 *
 * <p>Every method is declared so that the Ballerina side of the API compiles against the
 * surface it expects; none of them carries an implementation yet.
 */
public final class SasOps {

    private SasOps() {
    }

    public static Object generateAccountSas(BObject self, BMap<BString, Object> values) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object generateContainerSas(BObject self, BMap<BString, Object> values) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object generateSas(BObject self, BString path, BMap<BString, Object> values) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object generateContainerUserDelegationSas(BObject self, BMap<BString, Object> values, BMap<BString,
                                                            Object> key) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object generateUserDelegationSas(BObject self, BString path, BMap<BString, Object> values,
                                                   BMap<BString, Object> key) {
        throw new UnsupportedOperationException("not implemented");
    }
}
