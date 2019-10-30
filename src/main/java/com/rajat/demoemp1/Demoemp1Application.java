package com.rajat.demoemp1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

import static springfox.documentation.builders.PathSelectors.regex;

@SpringBootApplication
@Configuration
@EnableSwagger2
public class Demoemp1Application {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        // schema init
        Resource initSchema = new ClassPathResource("schema.sql");
        Resource initData = new ClassPathResource("data.sql");
        DatabasePopulator databasePopulator = new ResourceDatabasePopulator(initSchema, initData);
        //DatabasePopulator databasePopulator = new ResourceDatabasePopulator( initData);
        DatabasePopulatorUtils.execute(databasePopulator, dataSource);
        return dataSource;
    }
    public static void main(String[] args) {
        SpringApplication.run(Demoemp1Application.class, args);
    }

    @Bean
    public Docket swagerConfiguration(){

        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(regex("/rest.*"))
                .apis(RequestHandlerSelectors.basePackage("com.rajat.demoemp1"))
                .build()
                .apiInfo(apiDetails());
    }

    private ApiInfo apiDetails(){
        return new ApiInfo(
                "EMS API",
                "Its a demo version of the oriinal one",
                "1.000007",
                "Free to use for now",
                new springfox.documentation.service.Contact("Rajat Goyal","","rajatgoyal*******.gmail.com"),
                "API License",
                "Coming soon",
                Collections.emptyList()
        );
    }

}