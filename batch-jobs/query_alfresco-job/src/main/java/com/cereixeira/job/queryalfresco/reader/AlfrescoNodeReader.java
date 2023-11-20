package com.cereixeira.job.queryalfresco.reader;

import com.cereixeira.job.queryalfresco.constants.ParamName;
import com.cereixeira.job.queryalfresco.dto.InputRecordDTO;
import org.alfresco.search.handler.SearchApi;
import org.alfresco.search.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

public class AlfrescoNodeReader implements ItemReader<List<InputRecordDTO>> {
    private static final Logger logger = LoggerFactory.getLogger(AlfrescoNodeReader.class);

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.jobExecution = stepExecution.getJobExecution();
    }
    private JobExecution jobExecution;

    private final static int items = 100;
    private int skip = 0-items;
    private int count = 0;

    private String query;

    @Autowired
    SearchApi searchApi;

    private SearchRequest getSearchRequest() {
        SearchRequest req = new SearchRequest();
        RequestLimits rl = new RequestLimits();
        rl.setPermissionEvaluationCount(items);
        //rl.setPermissionEvaluationTime(1000);
        req.setLimits(rl);
        RequestPagination rp = new RequestPagination();
        rp.setSkipCount(skip += items);
        rp.setMaxItems(items);
        req.setPaging(rp);
        return req;
    }

    @Override
    synchronized public List<InputRecordDTO> read() {
        List<InputRecordDTO> inputList;
        SearchRequest req = getSearchRequest();
        String query = getQuery();

        logger.debug("#read - skip: {}, items: {}, count:{}", skip, items, ++count);

        ResponseEntity<ResultSetPaging> result = searchApi.search(req.query(new RequestQuery()
        .language(RequestQuery.LanguageEnum.AFTS)
        .query(query)));

        if (result.getBody().getList().getEntries().isEmpty()){
            return null;
        } else {
            List<ResultSetRowEntry> nodeList = result.getBody().getList().getEntries();

            inputList = nodeList.stream().map(i -> getDTO(i.getEntry())).collect(Collectors.toList());

            return inputList;
        }
    }

    private InputRecordDTO getDTO(ResultNode i) {
        InputRecordDTO dto = new InputRecordDTO();
        dto.setUuid(i.getId());
        dto.setValue(i.getName());
        return dto;
    }

    private String getQuery() {
        if(this.query == null){
            this.query = jobExecution.getJobParameters().getString(ParamName.QUERY);
        }
        return this.query;
    }
}
