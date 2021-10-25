package com.storage.azure.poc;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "azure")
public interface AzureStorageConfiguration {

    AuthenticationTypes authenticationType();
    String storageAccountName();
    Long expirationMinutes();

    // SAS token: Storage account -> shared access signature
    // Shared key credential & connection string: Storage account -> access keys
    Optional<String> sasToken();
    Optional<String> key();
    Optional<String> connectionString();
    Optional<String> clientId();
    Optional<String> username();
    Optional<String> password();
    Optional<String> clientSecret();
    Optional<String> tenantId();

    // for more specific access
    Optional<String> containerName();
    Optional<String> blobName();

    default String getStorageAccountUrl() {
        return String.format("https://%s.blob.core.windows.net", storageAccountName());
    }
}
