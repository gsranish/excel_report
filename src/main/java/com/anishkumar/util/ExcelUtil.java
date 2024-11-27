package com.anishkumar.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ExcelUtil {

    private static final String GENERAL = "General";

    public CellStyle getCellStyle(XSSFWorkbook xssfWorkbook, String dataType) {
        logger.debug("inside Excel utility for getCellStyle");
        XSSFFont customFont = xssfWorkbook.createFont();
        customFont.setFontName("Calibri");
        customFont.setFontHeightInPoints((short) 11);
        CellStyle cellStyle = xssfWorkbook.createCellStyle();
        DataFormat dataFormat = xssfWorkbook.createDataFormat();
        cellStyle.setFont(customFont);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        switch (dataType) {
            case GENERAL:
                cellStyle.setDataFormat(dataFormat.getFormat(GENERAL));
                break;
            case "date":
                cellStyle.setDataFormat(dataFormat.getFormat("m/d/yy"));
                break;
            case "number":
                cellStyle.setDataFormat(dataFormat.getFormat("0"));
                break;
            case "percentage":
                cellStyle.setDataFormat(dataFormat.getFormat("0.00%"));
                break;
            case "currency":
                cellStyle.setDataFormat((short) 7);
                break;
            case "negative":
                customFont.setColor(IndexedColors.RED.getIndex());
                cellStyle.setDataFormat((short) 7);
                break;
            case "negativePercentage":
                customFont.setColor(IndexedColors.RED.getIndex());
                cellStyle.setDataFormat(dataFormat.getFormat("0.00%"));
                break;
            default:
                cellStyle.setDataFormat(dataFormat.getFormat(GENERAL));
                break;
        }
        return cellStyle;
    }
}
