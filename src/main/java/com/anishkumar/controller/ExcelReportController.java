package com.anishkumar.controller;

import com.anishkumar.service.ExcelReportService;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ExcelReportController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExcelReportController.class);
    @Autowired
    private ExcelReportService reportService;

    @GetMapping("/excel")
    public void generateExcelReport(HttpServletResponse response) throws IOException {
        LOGGER.info("ExcelReportController : generateExcelReport called ");
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment;filename=courses.xls";
        response.setHeader(headerKey, headerValue);
        reportService.generate(response);
        response.flushBuffer();
    }

}