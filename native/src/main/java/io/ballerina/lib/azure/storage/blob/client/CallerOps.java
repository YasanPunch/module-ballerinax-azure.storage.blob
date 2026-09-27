/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.azure.storage.blob.client;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

/**
 * The operations the listener's {@code Caller} forwards, each with the signature of its
 * container-bound {@code Client} counterpart.
 *
 * <p>Every method is declared so that the Ballerina side of the API compiles against the
 * surface it expects; none of them carries an implementation yet.
 */
public final class CallerOps {

    private CallerOps() {
    }

    public static Object getBlob(Environment env, BObject self, BString path, Object options, BTypedesc targetType) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object getBlobProperties(Environment env, BObject self, BString path) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object download(Environment env, BObject self, BString sourcePath, BString destinationPath,
                                  Object options) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object upload(Environment env, BObject self, Object content, BString destinationPath,
                                Object options) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object deleteBlob(Environment env, BObject self, BString path, Object options) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object copyBlobFromUrl(Environment env, BObject self, BString sourceUrl, BString destinationPath,
                                         Object options) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Object setTags(Environment env, BObject self, BString path, BMap<BString, BString> tags,
                                 Object options) {
        throw new UnsupportedOperationException("not implemented");
    }
}
