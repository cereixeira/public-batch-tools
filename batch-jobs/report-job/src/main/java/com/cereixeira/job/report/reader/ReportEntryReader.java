package com.cereixeira.job.report.reader;

import com.cereixeira.databases.constants.DatabasesConstants;
import com.cereixeira.databases.repository.EntryEntity;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReportEntryReader {

    @Value("${job.report.entry.db.page.size}")
    private int pagingSize;

    @Qualifier(DatabasesConstants.BEAN_CUSTOM_DS)
    @Autowired
    private DataSource customSource;

    public ItemReader<EntryEntity> databaseReader(String executionName) throws Exception {
        return new JdbcPagingItemReaderBuilder<EntryEntity>()
                .name("pagingItemReader")
                .dataSource(customSource)
                .pageSize(pagingSize)
                .queryProvider(queryProvider(executionName).getObject())
                .rowMapper(new BeanPropertyRowMapper<>(EntryEntity.class))
                .build();
    }
    public SqlPagingQueryProviderFactoryBean queryProvider(String executionName){
        SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
        provider.setDataSource(customSource);
        provider.setSelectClause("SELECT E.ID, E.UNIQUE_REF AS uniqueRef, E.NODE_REF AS nodeRef, E.COMPLETED AS completed");
        provider.setFromClause("FROM ENTRY E, BLOCK B, EXECUTION EX");
        provider.setWhereClause("WHERE EX.NAME='"+executionName+"' AND EX.ID=B.ID_EXECUTION AND B.ID=E.ID_BLOCK");
        provider.setSortKeys(sortById());

        return provider;
    }

    private Map<String, Order> sortById() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("E.ID", Order.ASCENDING);
        return sortConfiguration;
    }
}
