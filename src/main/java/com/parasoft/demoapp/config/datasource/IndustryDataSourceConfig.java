package com.parasoft.demoapp.config.datasource;

import com.parasoft.demoapp.exception.CannotDetermineTargetDataSourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(DataSourceConfigurationProperties.Industry.class)
@EnableJpaRepositories(
        entityManagerFactoryRef = "industryEntityManager",
        transactionManagerRef = "industryTransactionManager",
        basePackages = {"com.parasoft.demoapp.repository.industry"}
)
public class IndustryDataSourceConfig {

    @Autowired
    private DataSourceConfigurationProperties.Industry dataSourceConfigurationProperties;

    private Map<Object, Object> industryDataSources;

    @Bean(name = "industryDataSource")
    public DataSource getIndustryDataSource() {
        IndustryRoutingDataSource dataSource = new IndustryRoutingDataSource();
        Map<Object, Object> industryDataSources = dataSourceConfigurationProperties.createTargetDataSources();

        this.industryDataSources = industryDataSources;
        dataSource.setTargetDataSources(industryDataSources);

        return dataSource;
    }

    @Bean(name = "industryEntityManager")
    public LocalContainerEntityManagerFactoryBean industryEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                               @Qualifier("industryDataSource") DataSource industryDataSource) {
        return builder
                .dataSource(industryDataSource)
                .packages("com.parasoft.demoapp.model.industry")
                .persistenceUnit("industry_PU")
                .build();
    }

    @Bean(name = "industryTransactionManager")
    public PlatformTransactionManager industryTransactionManager(
            @Qualifier("industryEntityManager") EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(entityManagerFactory) {
            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
                try {
                    super.doBegin(transaction, definition);
                } catch (CannotCreateTransactionException e) {
                    if(e.getCause() instanceof CannotDetermineTargetDataSourceException) {
                        throw new CannotCreateTransactionException(e.getCause().getMessage(), e);
                    } else {
                        throw e;
                    }
                }
            };
        };
    }

    public Map<Object, Object> getIndustryDataSources(){
        return industryDataSources;
    }
}
