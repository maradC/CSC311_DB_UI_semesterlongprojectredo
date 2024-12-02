package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

public class StorageUploader {

    private BlobContainerClient containerClient;

    public StorageUploader( ) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString("DefaultEndpointsProtocol=https;AccountName=maradiagacsc311storage;AccountKey=tZeSFsbGqCNdkOsxASJPk1nPdSYMvKfualeXWppcN7uScxE6SdVcAqU4Y0F1yp2aXhLIDzV7gTrB+ASt8HLGxw==;EndpointSuffix=core.windows.net")
                .containerName("media-files")
                .buildClient();
    }
    public void uploadFile(String filePath, String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(filePath);
    }
    public BlobContainerClient getContainerClient(){
        return containerClient;
    }



}

