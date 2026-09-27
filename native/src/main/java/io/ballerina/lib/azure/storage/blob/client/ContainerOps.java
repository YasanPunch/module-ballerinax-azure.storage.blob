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
 * Operations on the bound container itself: its properties, metadata, and access policy.
 *
 * <p>Every method is declared so that the Ballerina side of the API compiles against the
 * surface it expects; none of them carries an implementation yet.
 */
public final class ContainerOps {

    private ContainerOps() {
    }

    public static Object getContainerProperties(Environment env, BObject self) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object setContainerMetadata(Environment env, BObject self, BMap<BString, BString> metadata,
                                              Object options) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object getContainerAccessPolicy(Environment env, BObject self) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object setPublicAccess(Environment env, BObject self, BString access, Object options) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object setContainerAccessPolicy(Environment env, BObject self, BArray identifiers, Object options) {
        throw new UnsupportedOperationException("not implemented");
    }
}
