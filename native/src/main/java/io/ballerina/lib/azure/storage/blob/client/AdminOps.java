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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

/**
 * Account-level operations: the container lifecycle, the account's blob service
 * configuration, account information, and user delegation keys.
 *
 * <p>Every method is declared so that the Ballerina side of the API compiles against the
 * surface it expects; none of them carries an implementation yet.
 */
public final class AdminOps {

    private AdminOps() {
    }

    public static Object hasContainer(Environment env, BObject self, BString containerName) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object createContainer(Environment env, BObject self, BString containerName, Object options) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object deleteContainer(Environment env, BObject self, BString containerName, Object options) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object undeleteContainer(Environment env, BObject self, BString containerName,
                                           BString deletedContainerVersion) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object getServiceProperties(Environment env, BObject self) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object setServiceProperties(Environment env, BObject self, BMap<BString, Object> properties) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object getAccountInfo(Environment env, BObject self) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object getUserDelegationKey(Environment env, BObject self, BArray startTime, BArray expiryTime) {
        throw new UnsupportedOperationException("not implemented");
    }
}
