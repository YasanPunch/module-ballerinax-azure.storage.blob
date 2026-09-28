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

import ballerina/time;

// ---------------------------------------------------------------------------
// Content
// ---------------------------------------------------------------------------

# The content forms `upload` accepts. `byte[]`, `string` and a byte stream are written as-is;
# the other members are serialized per the resolved format.
public type UploadContent byte[]|string|json|xml|record {}|record {}[]|
    stream<byte[], error?>|stream<record {}, error?>;

# The target forms `getBlob` retrieves. The members mirror `UploadContent`, so anything written
# is readable back in the same type.
public type RetrievableType byte[]|string|json|xml|record {}|record {}[]|
    stream<byte[], error?>|stream<record {}, error?>;

# A blob's standard content headers. Written as a whole set: a field omitted from the record is
# cleared on the blob.
public type ContentHeaders record {|
    # The media type of the blob's content
    string contentType?;
    # The content encoding applied to the blob
    string contentEncoding?;
    # The natural language of the blob's content
    string contentLanguage?;
    # How a user agent should present the content
    string contentDisposition?;
    # The caching directives for the blob
    string cacheControl?;
    # The MD5 hash of the blob's content
    string contentMd5?;
|};

# A byte range, with inclusive start and end offsets.
public type ByteRange record {|
    # The first byte of the range
    int startByte;
    # The last byte of the range
    int endByte;
|};

// ---------------------------------------------------------------------------
// Listing options
// ---------------------------------------------------------------------------

# Options for `Client.listBlobs`.
public type BlobListOptions record {|
    # Return only blobs whose name begins with this prefix
    string prefix?;
    # Group names that extend past this delimiter into a single prefix entry
    string delimiter?;
    # Include each blob's metadata
    boolean includeMetadata = false;
    # Include each blob's index tags
    boolean includeTags = false;
    # Include blob snapshots
    boolean includeSnapshots = false;
    # Include soft-deleted blobs
    boolean includeDeleted = false;
|};

# Options for `Client.listBlobsPage`.
public type BlobPageOptions record {|
    *BlobListOptions;
    # The maximum number of blobs in the page, up to the service maximum of 5,000
    int pageSize?;
    # Resume from a previous `BlobList.nextMarker`
    string marker?;
|};

# Options for `AdminClient.listContainers`.
public type ContainerListOptions record {|
    # Return only containers whose name begins with this prefix
    string prefix?;
    # Include each container's metadata
    boolean includeMetadata = false;
    # Include soft-deleted containers
    boolean includeDeleted = false;
    # The maximum number of containers to return; omit for all
    int 'limit?;
    # Resume from a previous `ContainerList.nextMarker`
    string marker?;
|};

// ---------------------------------------------------------------------------
// Container options
// ---------------------------------------------------------------------------

# Options for `createContainer`.
public type ContainerCreateOptions record {|
    # The container's initial metadata
    map<string> metadata?;
    # The anonymous access level. Omit to create a private container. Anonymous access also
    # requires the storage account to permit it
    PublicAccess publicAccess?;
|};

# Options for `deleteContainer`.
public type DeleteContainerOptions record {|
    # The active lease id, required when the container is leased elsewhere
    string leaseId?;
|};

# Options for `setContainerMetadata`.
public type ContainerMetadataOptions record {|
    # The active lease id, required when the container is leased
    string leaseId?;
|};

# Options for `setContainerAccessPolicy`.
public type AccessPolicyOptions record {|
    # The active lease id, required when the container is leased
    string leaseId?;
|};

// ---------------------------------------------------------------------------
// Blob options
// ---------------------------------------------------------------------------

# Options for `deleteBlob`.
public type DeleteBlobOptions record {|
    # What happens to the blob's snapshots. Required when the blob has snapshots
    DeleteSnapshotsOption deleteSnapshots?;
    # Deletes this snapshot instead of the blob
    string snapshotId?;
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

# Options for `setBlobMetadata`.
public type BlobMetadataOptions record {|
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

# Options for `setContentHeaders`.
public type ContentHeaderOptions record {|
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

// ---------------------------------------------------------------------------
// Transfer options
// ---------------------------------------------------------------------------

# Options for the uploads.
public type UploadOptions record {|
    # The content headers stored with the new blob
    ContentHeaders contentHeaders?;
    # The metadata stored with the new blob
    map<string> metadata?;
    # The index tags stored with the new blob
    map<string> tags?;
    # The access tier the new blob starts in
    AccessTier accessTier?;
    # The active lease id, required when the destination blob is leased
    string leaseId?;
|};

# Options for `upload`, extending the upload options with the serialization format.
public type UploadContentOptions record {|
    *UploadOptions;
    # The serialization format for structured content; when absent, the format is inferred
    # from the destination path's extension (`.json`, `.xml`, `.csv`)
    FileFormat fileFormat?;
|};

# Options for `download`.
public type DownloadOptions record {|
    # Reads only this byte range of the blob
    ByteRange range?;
    # Reads this snapshot instead of the live blob
    string snapshotId?;
|};

# Options for `getBlob`, extending the download options with the binding format.
public type GetBlobOptions record {|
    *DownloadOptions;
    # The binding format for record targets; when absent, the format is inferred from the
    # path's extension (`.json`, `.xml`, `.csv`)
    FileFormat fileFormat?;
|};

// ---------------------------------------------------------------------------
// Copy, tier, tag and snapshot options
// ---------------------------------------------------------------------------

# Options for the copies.
public type CopyOptions record {|
    # The metadata stored with the destination blob; when omitted, the destination inherits
    # the source's metadata
    map<string> metadata?;
    # The index tags stored with the destination blob
    map<string> tags?;
    # The access tier the destination blob starts in
    AccessTier accessTier?;
    # The destination's active lease id. An asynchronous copy over a leased destination
    # requires that lease to be infinite
    string leaseId?;
|};

# Options for `abortCopy`.
public type AbortCopyOptions record {|
    # The destination blob's active lease id, required when the destination is leased
    string leaseId?;
|};

# Options for `setAccessTier`.
public type SetAccessTierOptions record {|
    # How quickly an archived blob is rehydrated; meaningful when leaving the archive tier
    RehydratePriority rehydratePriority?;
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

# Options for the index-tag writes.
public type TagOptions record {|
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

# Options for `createSnapshot`.
public type CreateSnapshotOptions record {|
    # The snapshot's own metadata; when omitted, the snapshot inherits the blob's metadata
    map<string> metadata?;
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

// ---------------------------------------------------------------------------
// Append blob, page blob and block options
// ---------------------------------------------------------------------------

# Options for creating an append blob or a page blob.
public type CreateBlobOptions record {|
    # The content headers stored with the new blob
    ContentHeaders contentHeaders?;
    # The metadata stored with the new blob
    map<string> metadata?;
    # The index tags stored with the new blob
    map<string> tags?;
    # The active lease id, required when an existing blob at the path is leased
    string leaseId?;
|};

# Options for the append-block operations.
public type AppendBlockOptions record {|
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

# Options for the page write operations.
public type PageOptions record {|
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

# Options for `listPageRanges`.
public type PageRangeOptions record {|
    # Restricts the listing to this byte range
    ByteRange range?;
    # Lists the ranges of this snapshot instead of the live blob
    string snapshotId?;
|};

# Options for `stageBlock`.
public type StageBlockOptions record {|
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

# Options for `stageBlockFromUrl`.
public type StageBlockFromUrlOptions record {|
    # Stages only this byte range of the source instead of its whole content
    ByteRange sourceRange?;
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

# Options for `commitBlockList`.
public type CommitBlockListOptions record {|
    # The content headers stored with the committed blob
    ContentHeaders contentHeaders?;
    # The metadata stored with the committed blob
    map<string> metadata?;
    # The index tags stored with the committed blob
    map<string> tags?;
    # The access tier the committed blob starts in
    AccessTier accessTier?;
    # The active lease id, required when the blob is leased
    string leaseId?;
|};

// ---------------------------------------------------------------------------
// SAS signature values
// ---------------------------------------------------------------------------

# The permissions granted by a container SAS. Every permission is off unless enabled.
public type ContainerSasPermissions record {|
    # Read blob content, properties, and metadata
    boolean read = false;
    # Append a block to an append blob
    boolean add = false;
    # Create a new blob
    boolean create = false;
    # Write blob content, properties, and metadata
    boolean write = false;
    # Delete a blob
    boolean delete = false;
    # List the container's blobs
    boolean list = false;
    # Read and write index tags
    boolean tag = false;
    # Run an index-tag query
    boolean filter = false;
|};

# The permissions granted by a blob SAS. Every permission is off unless enabled.
public type BlobSasPermissions record {|
    # Read the blob's content, properties, and metadata
    boolean read = false;
    # Append a block to the append blob
    boolean add = false;
    # Create the blob
    boolean create = false;
    # Write the blob's content, properties, and metadata
    boolean write = false;
    # Delete the blob
    boolean delete = false;
    # Read and write the blob's index tags
    boolean tag = false;
|};

# The values signed into a container SAS token. A parameter may be carried by the stored
# access policy or by the token, but not both.
public type ContainerSasSignatureValues record {|
    # When the token expires. May be omitted only when `identifier` supplies it
    time:Utc expiryTime?;
    # The permissions granted. May be omitted only when `identifier` supplies them
    ContainerSasPermissions permissions?;
    # A stored access policy on the container whose window and permissions the token inherits
    string identifier?;
    # When the token becomes valid; omit for immediately valid
    time:Utc startTime?;
    # The protocols a request presenting the token may use
    SasProtocol protocol?;
    # An IP address or range the requests must come from (e.g. `168.1.5.60-168.1.5.70`)
    string ipRange?;
|};

# The values signed into a blob SAS token. A parameter may be carried by the stored access
# policy or by the token, but not both.
public type BlobSasSignatureValues record {|
    # When the token expires. May be omitted only when `identifier` supplies it
    time:Utc expiryTime?;
    # The permissions granted. May be omitted only when `identifier` supplies them
    BlobSasPermissions permissions?;
    # A stored access policy on the container whose window and permissions the token inherits
    string identifier?;
    # When the token becomes valid; omit for immediately valid
    time:Utc startTime?;
    # The protocols a request presenting the token may use
    SasProtocol protocol?;
    # An IP address or range the requests must come from (e.g. `168.1.5.60-168.1.5.70`)
    string ipRange?;
|};

# The permissions granted by an account-level SAS. Every permission is off unless enabled.
public type AccountSasPermissions record {|
    # Read content, properties, and metadata
    boolean read = false;
    # Write content, properties, and metadata
    boolean write = false;
    # Delete resources
    boolean delete = false;
    # List containers and their blobs
    boolean list = false;
    # Add content (append-style operations)
    boolean add = false;
    # Create new resources
    boolean create = false;
    # Update a queued message; required by the `Listener`
    boolean update = false;
    # Get and delete queued messages; required by the `Listener`
    boolean process = false;
|};

# The storage services an account SAS covers.
public type AccountSasServices record {|
    # The blob service
    boolean blob = false;
    # The queue service; enable it for a `Listener` credential
    boolean queue = false;
|};

# The resource types an account SAS covers.
public type AccountSasResourceTypes record {|
    # Service-level operations, such as reading the service properties
    boolean 'service = false;
    # Container-level operations, such as creating or listing containers
    boolean container = false;
    # Object-level operations on a blob
    boolean 'object = false;
|};

# The values signed into an account-level SAS token.
public type AccountSasSignatureValues record {|
    # When the token expires
    time:Utc expiryTime;
    # The permissions granted
    AccountSasPermissions permissions;
    # The services the token covers
    AccountSasServices services;
    # The resource types the token covers
    AccountSasResourceTypes resourceTypes;
    # When the token becomes valid; omit for immediately valid
    time:Utc startTime?;
    # The protocols a request presenting the token may use
    SasProtocol protocol?;
    # An IP address or range the requests must come from (e.g. `168.1.5.60-168.1.5.70`)
    string ipRange?;
|};
