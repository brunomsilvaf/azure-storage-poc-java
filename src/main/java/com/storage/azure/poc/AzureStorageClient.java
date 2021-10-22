package com.storage.azure.poc;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AzureStorageClient {

    @Inject
    AzureStorageConfiguration azureConfiguration;

    public BlobServiceClient getBlobServiceClient(){
        switch (azureConfiguration.authenticationType()) {
            case SAS_TOKEN:
                return new BlobServiceClientBuilder()
                    .endpoint(azureConfiguration.getStorageAccountUrl())
                    .sasToken(azureConfiguration.sasToken().orElseThrow())
                    .buildClient();
            case CONNECTION_STRING:
                return new BlobServiceClientBuilder()
                    .endpoint(azureConfiguration.getStorageAccountUrl())
                    .connectionString(azureConfiguration.connectionString().orElseThrow())
                    .buildClient();
            case KEY:
                return new BlobServiceClientBuilder()
                    .endpoint(azureConfiguration.getStorageAccountUrl())
                    .credential(new StorageSharedKeyCredential(azureConfiguration.storageAccountName(), azureConfiguration.key().orElseThrow()))
                    .buildClient();
            case SERVICE_PRINCIPAL:
                return new BlobServiceClientBuilder()
                    .endpoint(azureConfiguration.getStorageAccountUrl())
                    .credential(new ClientSecretCredentialBuilder()
                                    .clientId(azureConfiguration.clientId().orElseThrow())
                                    .clientSecret(azureConfiguration.clientSecret().orElseThrow())
                                    .tenantId(azureConfiguration.tenantId().orElseThrow())
                                    .build())
                    .buildClient();
            case USER_CREDENTIALS:
                return new BlobServiceClientBuilder()
                    .endpoint(azureConfiguration.getStorageAccountUrl())
                    .credential(new UsernamePasswordCredentialBuilder()
                                    .clientId(azureConfiguration.clientId().orElseThrow())
                                    .username(azureConfiguration.username().orElseThrow())
                                    .password(azureConfiguration.password().orElseThrow())
                                    .build())
                    .buildClient();
            default:
                return null;
        }
    }

    public BlobContainerClient getBlobContainerClient(){
        // can also use BlobContainerClientBuilder
        return getBlobServiceClient().getBlobContainerClient(azureConfiguration.containerName().orElseThrow());
    }

    public BlobClient getBlobClient() {
        // can also use BlobClientBuilder
        return getBlobContainerClient().getBlobClient(azureConfiguration.blobName().orElseThrow());
    }

}
