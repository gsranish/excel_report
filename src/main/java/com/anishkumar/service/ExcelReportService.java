package com.anishkumar.service;

import java.io.IOException;
import java.util.List;

import com.anishkumar.entity.Course;
import com.anishkumar.repository.CourseRepository;

import com.anishkumar.util.ExcelUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExcelReportService {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private ExcelUtil excelUtil;


    public void generate (HttpServletResponse response) throws IOException {
        List<Course> courses = courseRepo. findAll() ;
        XSSFWorkbook workbook = new XSSFWorkbook () ;
        XSSFSheet sheet = workbook. createSheet ("Courses Info");
        XSSFRow row = sheet. createRow (0) ;
        row. createCell (0). setCellValue ("ID") ;
        row. createCell (1). setCellValue ("Name") ;
        row. createCell (2) . setCellValue ("Price") ;
        row. createCell (4) . setCellValue ("Hire Date") ;
        int dataRowIndex = 1;
        for (Course course : courses) {
            XSSFRow dataRow = sheet.createRow (dataRowIndex) ;
            // create first cell as Id
            Cell idCell = dataRow.createCell(0);
            idCell. setCellStyle (excelUtil.getCellStyle (workbook, "number") ) ;
            idCell. setCellValue (course.getCid ()) ;
            // create second cell as Name
            Cell nameCell = dataRow.createCell(1);
            nameCell. setCellStyle (excelUtil.getCellStyle (workbook, "general")) ;
            nameCell. setCellValue (course.getName () ) ;
            // create third cell as Price
            Cell priceCell = dataRow.createCell(2);
            priceCell.setCellStyle (excelUtil.getCellStyle (workbook, "currency")) ;
            priceCell. setCellValue (course.getPrice ()) ;
            // create fourth cell as Discount
            Cell discountCell = dataRow.createCell(3);
            discountCell.setCellStyle (excelUtil.getCellStyle (workbook, "percentage")) ;
            discountCell. setCellValue (course.getPrice ()) ;
            // create fifth cell as Date
            Cell dateCell = dataRow.createCell (4);
            dateCell. setCellStyle (excelUtil.getCellStyle (workbook, "date")) ;
            dateCell. setCellValue (course.getPrice ()) ;
            dataRowIndex++;
        }
        ServletOutputStream ops = response.getOutputStream();
        workbook.write(ops);
        workbook.close();
        ops.close();
    }

}