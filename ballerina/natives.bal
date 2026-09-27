// Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

// The externs that are not themselves client methods: client construction, which validates the
// credential locally and makes no call to Azure.

isolated function initAdminClient(AdminClient adminClient, ClientConfiguration config)
        returns Error? = @java:Method {
    'class: "io.ballerina.lib.azure.storage.blob.client.ClientInit"
} external;

isolated function initClient(Client blobClient, string containerName, ClientConfiguration config)
        returns Error? = @java:Method {
    'class: "io.ballerina.lib.azure.storage.blob.client.ClientInit"
} external;

isolated function initCaller(Caller caller, string containerName, ClientConfiguration config)
        returns Error? = @java:Method {
    'class: "io.ballerina.lib.azure.storage.blob.client.ClientInit"
} external;
