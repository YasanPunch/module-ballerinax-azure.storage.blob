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

# The context object passed to a listener service's handlers, exposing a container-scoped
# subset of `Client` to act on the event's blob. It cannot be instantiated by user code.
public isolated client class Caller {

    isolated function init(string containerName, *ClientConfiguration config) returns Error? {
        return initCaller(self, containerName, config);
    }

    # Retrieves a blob's content in the form the target type selects.
    #
    # + path - The container-relative path of the blob
    # + options - Optional retrieval options (a byte range, a snapshot id, the binding format)
    # + targetType - The form to retrieve the content in, inferred from the call site
    # + return - The content as `targetType`, or an `Error`
    isolated remote function getBlob(string path, GetBlobOptions? options = (),
            typedesc<RetrievableType> targetType = <>) returns targetType|Error = @java:Method {
        'class: "io.ballerina.lib.azure.storage.blob.client.CallerOps"
    } external;

    # Reads a blob's properties and metadata.
    #
    # + path - The container-relative path of the blob
    # + return - The `BlobProperties`, or an `Error`
    isolated remote function getBlobProperties(string path) returns BlobProperties|Error = @java:Method {
        'class: "io.ballerina.lib.azure.storage.blob.client.CallerOps"
    } external;

    # Downloads a blob to a local file. Fails if a local file already exists at the destination.
    #
    # + sourcePath - The container-relative path of the blob
    # + destinationPath - The local path to write, including the file name
    # + options - Optional download options (a byte range, a snapshot id)
    # + return - An `Error` if the download failed, otherwise `()`
    isolated remote function download(string sourcePath, string destinationPath,
            DownloadOptions? options = ()) returns Error? = @java:Method {
        'class: "io.ballerina.lib.azure.storage.blob.client.CallerOps"
    } external;

    # Uploads in-memory content to the bound container.
    #
    # + content - The content to upload. A record, a record array, or another `json` value is
    #             serialized per the resolved format; `byte[]` and `string` are written as-is
    # + destinationPath - The container-relative path, including the blob name
    # + options - Optional upload options (headers, metadata, format override)
    # + return - An `Error` if the upload failed, otherwise `()`
    isolated remote function upload(UploadContent content, string destinationPath,
            UploadContentOptions? options = ()) returns Error? = @java:Method {
        'class: "io.ballerina.lib.azure.storage.blob.client.CallerOps"
    } external;

    # Deletes a blob.
    #
    # + path - The container-relative path of the blob
    # + options - Optional deletion options (snapshot handling, a snapshot id, the lease id)
    # + return - An `Error` if the blob could not be deleted, otherwise `()`
    isolated remote function deleteBlob(string path, DeleteBlobOptions? options = ()) returns Error? = @java:Method {
        'class: "io.ballerina.lib.azure.storage.blob.client.CallerOps"
    } external;

    # Copies a blob from a URL into the bound container. The copy is asynchronous; watch it
    # with `getBlobProperties`.
    #
    # + sourceUrl - The URL of the source, such as the event's `url`
    # + destinationPath - The container-relative path of the destination blob
    # + options - Optional copy options (destination metadata, tags, tier, lease id)
    # + return - The `CopyInfo` for the accepted copy, or an `Error`
    isolated remote function copyBlobFromUrl(string sourceUrl, string destinationPath,
            CopyOptions? options = ()) returns CopyInfo|Error = @java:Method {
        'class: "io.ballerina.lib.azure.storage.blob.client.CallerOps"
    } external;

    # Replaces a blob's complete index-tag set.
    #
    # + path - The container-relative path of the blob
    # + tags - The new complete tag set; pass `{}` to clear it
    # + options - Optional options (the lease id, when the blob is leased)
    # + return - An `Error` if the tags could not be set, otherwise `()`
    isolated remote function setTags(string path, map<string> tags, TagOptions? options = ())
            returns Error? = @java:Method {
        'class: "io.ballerina.lib.azure.storage.blob.client.CallerOps"
    } external;
}
