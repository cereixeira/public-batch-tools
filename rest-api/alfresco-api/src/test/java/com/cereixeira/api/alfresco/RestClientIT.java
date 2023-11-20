package com.cereixeira.api.alfresco;

import com.cereixeira.api.alfresco.config.AlfrescoApiConfig;
import com.cereixeira.connectors.config.S3ConnectorConfig;
import com.cereixeira.connectors.s3.S3RemoteConnector;
import com.cereixeira.connectors.s3.S3Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = {"classpath:alfresco-api-application-test.properties", "classpath:s3-connector-application-test.properties"})
@ContextConfiguration(classes = {AlfrescoApiConfig.class, S3ConnectorConfig.class})
public class RestClientIT {
    @Autowired
    private RestClient restClient;

    @Autowired
    private S3RemoteConnector s3RemoteConnector;

    @Test
    public void postForEntity() throws Exception {
        restClient.createDocumentPostForEntity(getBodyWithFile());
    }
    @Test
    public void postForObject() throws Exception {
        restClient.createDocumentPortForObject(getBodyWithFile());
    }
    @Test
    public void postGenericApi() throws Exception {
        restClient.createDocumentGenericApi(getBodyWithFile());
    }

    @Test
    public void postForObjectS3() throws Exception {
        restClient.createDocumentPortForObject(getBodyWithS3File());
    }

    private MultiValueMap<String, Object> getBodyWithS3File() throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body = addMetadata(body);

        body.add("filedata", getS3Resource());
        return body;
    }

    private Object getS3Resource() throws Exception {
        InputStream is = s3RemoteConnector.getInputStreamFromRemote("migration/DEV/TEST_VARIOS/53494358S-GDC-117471-28109-20221231.pdf");
        S3Resource res = new S3Resource(is, "test.pdf", 1497);
        return res;
    }

    private MultiValueMap<String, Object> getBodyWithFile() throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body = addMetadata(body);

        body.add("filedata", getFile());
        return body;
    }
    private MultiValueMap<String, Object> addMetadata(MultiValueMap<String, Object> body){
        body.add("type", "xyz:type");
        body.add("xyz:id_client", "88807850H");
        body.add("xyz:type_client", "T8");
        body.add("cm:description", "test");
        body.add("cm:name", "test_00.pdf");
        return body;
    }

    private Resource getFile() throws IOException {
        Path testFile = Files.createTempFile("test-file", ".txt");
        System.out.println("Creating and Uploading Test File: " + testFile);
        Files.write(testFile, "Hello World !!, This is a test file.".getBytes());
        return new FileSystemResource(testFile.toFile());
    }
}
