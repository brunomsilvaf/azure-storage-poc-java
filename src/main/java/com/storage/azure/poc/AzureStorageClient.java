package com.storage.azure.poc;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;

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

    public String generateSasToken(BlobServiceClient blobServiceClient, BlobClient blobClient) {

        // get a user delegation key with some expiry date
        UserDelegationKey userDelegationKey =
            blobServiceClient.getUserDelegationKey(OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(10));

        // define permissions and link expiry date
        BlobSasPermission blobSasPermission =  new BlobSasPermission().setReadPermission(true);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(azureConfiguration.expirationMinutes());
        BlobServiceSasSignatureValues serviceSasValues = new BlobServiceSasSignatureValues(expiryTime, blobSasPermission);

        // get the token and invalidate the user delegation key
        String sasToken = blobClient.generateUserDelegationSas(serviceSasValues, userDelegationKey);
        userDelegationKey.setSignedExpiry(OffsetDateTime.now());
        return blobClient.getBlobUrl() + "?" + sasToken;
    }

}
