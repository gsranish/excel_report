package com.anishkumar;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ExcelReportApplication {

	public static void main(String[] args) {
		log.debug("Excel Report Application started at DEBUG MODE ");
		SpringApplication.run(ExcelReportApplication.class, args);
	}

}
