package com.anishkumar.service;

import java.io.IOException;
import java.util.List;

import com.anishkumar.entity.Course;
import com.anishkumar.repository.CourseRepository;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExcelReportService {

    @Autowired
    private CourseRepository courseRepo;

    public static CellStyle getCellStyle(XSSFWorkbook xssfWorkbook, String dataType) {
    // setting the font style
    XSSFFont customFont = xssfWorkbook.createFont();
    customFont.setFontName("Calibre");
    customFont.setFontHeightInPoints((short)11);
    XSSFDataFormat format = xssfWorkbook.getCreationHelper().createDataFormat();
    CellStyle cellStyle = xssfWorkbook.createCellStyle();
    cellStyle.setFont(customFont);
    switch(dataType) {
        case "general":
            // creating a style for general data type with left alignment
            cellStyle.setDataFormat(format.getFormat("General"));
            cellStyle.setAlignment(HorizontalAlignment.LEFT); // for left alignment
            break;
        case "date":
            // creating a style for date data type with left alignment
            cellStyle.setDataFormat(format.getFormat("m/d/yy"));
            cellStyle.setAlignment(HorizontalAlignment.LEFT); // for left alignment
            break;
        case "number":
            // creating a style for number data type with right alignment
            cellStyle.setDataFormat(format.getFormat("0"));
            cellStyle.setAlignment(HorizontalAlignment.RIGHT); // for right alignment
            break;
        case "percentage":
            // creating a style for percentage data type with right alignment
            cellStyle.setDataFormat(format.getFormat("0%"));
            cellStyle.setAlignment(HorizontalAlignment.RIGHT); // for right alignment
            break;
        case "currency":
            // creating a style for currency data type with left alignment
            cellStyle.setDataFormat((short) 7);
            cellStyle.setAlignment(HorizontalAlignment.LEFT); // for left alignment
            break;
        default:
            cellStyle.setDataFormat(format.getFormat("General"));
            break;
        }
        return cellStyle;
    }

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
            idCell. setCellStyle (getCellStyle (workbook, "number") ) ;
            idCell. setCellValue (course.getCid ()) ;
            // create second cell as Name
            Cell nameCell = dataRow.createCell(1);
            nameCell. setCellStyle (getCellStyle (workbook, "general")) ;
            nameCell. setCellValue (course.getName () ) ;
            // create third cell as Price
            Cell priceCell = dataRow.createCell(2);
            priceCell.setCellStyle (getCellStyle (workbook, "currency")) ;
            priceCell. setCellValue (course.getPrice ()) ;
            // create fourth cell as Discount
            Cell discountCell = dataRow.createCell(3);
            discountCell.setCellStyle (getCellStyle (workbook, "percentage")) ;
            discountCell. setCellValue (course.getPrice ()) ;
            // create fifth cell as Date
            Cell dateCell = dataRow.createCell (4);
            dateCell. setCellStyle (getCellStyle (workbook, "date")) ;
            dateCell. setCellValue (course.getPrice ()) ;
            dataRowIndex++;
        }
        ServletOutputStream ops = response.getOutputStream();
        workbook.write(ops);
        workbook.close();
        ops.close();
    }

}