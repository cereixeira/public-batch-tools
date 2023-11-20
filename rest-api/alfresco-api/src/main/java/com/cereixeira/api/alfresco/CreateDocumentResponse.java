package com.cereixeira.api.alfresco;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDocumentResponse {

    private String nodeId;
    private String sbid;
    private String status;
    private String internalCode;
    private String description;
}
