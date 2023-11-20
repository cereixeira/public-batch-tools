package com.cereixeira.api.alfresco;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;

@Service
public class RestClient {
    private Logger logger = LogManager.getLogger(RestClient.class);
    private static final String UUID = "nodeId";

    @Value("${alfesco.api.service.createdocument.url}")
    private String serviceCreateDocumentUrl;
    @Value("${alfesco.api.service.createdocument.username}")
    private String serviceCreateDocumentUsername;
    @Value("${alfesco.api.service.createdocument.password}")
    private String serviceCreateDocumentPassword;

    public CreateDocumentResponse createDocumentPostForEntity(MultiValueMap<String, Object> body) throws IOException {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<CreateDocumentResponse> response = restTemplate.postForEntity(serviceCreateDocumentUrl, requestEntity, CreateDocumentResponse.class);
        logger.debug(response.getBody().getNodeId());
        return response.getBody();
    }

    public CreateDocumentResponse createDocumentPortForObject(MultiValueMap<String, Object> body) throws IOException {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        CreateDocumentResponse createDoc = restTemplate.postForObject(serviceCreateDocumentUrl, requestEntity, CreateDocumentResponse.class);

        logger.debug(createDoc.getNodeId());
        return createDoc;
    }

    public CreateDocumentResponse createDocumentGenericApi(MultiValueMap<String, Object> body) throws IOException {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<CreateDocumentResponse> response = restTemplate.exchange
                (serviceCreateDocumentUrl,
                        HttpMethod.POST,
                        httpEntity,
                        CreateDocumentResponse.class);

        logger.debug(response.getBody());
        return response.getBody();
    }

    private HttpHeaders createHeaders(){
        return new HttpHeaders() {{
            String auth = serviceCreateDocumentUsername + ":" + serviceCreateDocumentPassword;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.defaultCharset()) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }

}
