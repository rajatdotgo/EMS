package com.rajat.demoemp1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

import static springfox.documentation.builders.PathSelectors.regex;

@SpringBootApplication
@EnableSwagger2
public class Demoemp1Application {

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