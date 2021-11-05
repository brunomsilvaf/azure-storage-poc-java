package com.storage.azure.poc;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Collectors;

// Regarding container name:
// may only contain lowercase letters, numbers, and hyphens, and must begin with a letter or a number.
// Each hyphen must be preceded and followed by a non-hyphen character.
// The name must also be between 3 and 63 characters long.
@Path("/azure/container")
public class AzureContainerController {

    @Inject
    AzureStorageClient azureStorageClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String listContainers() {

        BlobServiceClient blobServiceClient = azureStorageClient.getBlobServiceClient();
        return blobServiceClient.listBlobContainers()
                                .stream()
                                .map(l -> blobServiceClient.getBlobContainerClient(l.getName()).getBlobContainerUrl())
                                .collect(Collectors.joining("\n"));
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{containerName}")
    public String createContainer(@PathParam("containerName") String containerName) {

        BlobServiceClient blobServiceClient = azureStorageClient.getBlobServiceClient();
        return blobServiceClient.createBlobContainer(containerName).getBlobContainerUrl();
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{containerName}")
    public void deleteContainer(@PathParam("containerName") String containerName) {

        BlobServiceClient blobServiceClient = azureStorageClient.getBlobServiceClient();
        blobServiceClient.deleteBlobContainer(containerName);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{containerName}/blob")
    public String listBlobs(@PathParam("containerName") String containerName) {

        BlobServiceClient blobServiceClient = azureStorageClient.getBlobServiceClient();
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        //BlobContainerClient blobContainerClient = azureStorageClient.getBlobContainerClient();

        return blobContainerClient.listBlobs()
                                  .stream()
                                  .map(l -> blobContainerClient.getBlobClient(l.getName()).getBlobUrl())
                                  .collect(Collectors.joining("\n"));
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{containerName}/blob/{blobName}")
    public byte[] getBlobContent(
        @PathParam("containerName") String containerName,
        @PathParam("blobName") String blobName) {

        BlobServiceClient blobServiceClient = azureStorageClient.getBlobServiceClient();
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.download(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{containerName}/blob/{blobName}/link")
    public String getBlobLink(
        @PathParam("containerName") String containerName,
        @PathParam("blobName") String blobName) {

        BlobServiceClient blobServiceClient = azureStorageClient.getBlobServiceClient();
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        return azureStorageClient.generateSasToken(blobServiceClient, blobClient);
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{containerName}/blob/{blobName}")
    public String uploadDataToBlob(
        @PathParam("containerName") String containerName,
        @PathParam("blobName") String blobName,
        String body) {

        BlobServiceClient blobServiceClient = azureStorageClient.getBlobServiceClient();
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(body.getBytes())) {
            blobClient.upload(dataStream, body.length(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return blobClient.getBlobUrl();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{containerName}/uploadSample")
    public String uploadDataToBlob(@PathParam("containerName") String containerName) {

        BlobServiceClient blobServiceClient = azureStorageClient.getBlobServiceClient();
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        int n = (int) (Math.random() * 4) + 1;
        String blobName = "sample" + n + ".mp3";

        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        URL url = this.getClass().getClassLoader().getResource("samples/" + blobName);
        if (url != null) {
            blobClient.uploadFromFile(url.getPath());
        }

        return blobClient.getBlobUrl();
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{containerName}/blob/{blobName}")
    public void deleteBlob(
        @PathParam("containerName") String containerName,
        @PathParam("blobName") String blobName) {

        BlobServiceClient blobServiceClient = azureStorageClient.getBlobServiceClient();
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        blobClient.delete();
    }
}