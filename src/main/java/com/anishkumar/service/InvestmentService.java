package com.anishkumar.service;

import com.anishkumar.entity.InvestmentHoldingsVO;
import com.anishkumar.util.EcxFormatterUtil;
import com.anishkumar.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class InvestmentService {

    @Autowired
    private ExcelUtil excelUtil;

    public byte[] exportToExcel(InvestmentVO investmentVO, UserDetailVO userDetailVO, String currencyCode,
                                int priceDecimalPrecision, Boolean unrealizedGainLossPerctArrow, Map<String, Object> siteConfigData, long firmId, String groupBy) {
        log.debug("Inside exportToExcel for Investment");
        String fileName = "Investment_temp_" + System.currentTimeMillis() + ".xlsx";
        String filePath = tempPath + fileName;
        boolean enableAccountMasking = Boolean.parseBoolean(siteConfigData.get(ECXConstants.KEY_FTR_ENABLE_ACCOUNT_NUMBER_MASKING).toString());
        boolean notCustodiedAssetFeature = Boolean.parseBoolean(siteConfigData.get(ECXConstants.KEY_FTR_ASSET_NOT_CUSTODIED_ICON).toString());
        boolean ftrShowPMEMOdelName = (Boolean)siteConfigData.get(ECXConstants.KEY_FTR_DISPLAY_STRATEGY_NAME_WITH_PORTFOLIO_PME);
        log.debug("filePath - {}", filePath);
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
            XSSFSheet sheet = workbook.createSheet(fileName);
            XSSFRow headerRow = sheet.createRow(0);
            List<ColumnConfigurationVO> columnConfigurationVOs = investmentVO.getColumnConfigurationVOs();
            Collections.sort(columnConfigurationVOs, new ColumnConfigOrderComparator());
            List<String> columnKeys = new ArrayList<>();
            int cellIdx = 0;
            for (ColumnConfigurationVO columnConfig : columnConfigurationVOs) {
                boolean isMandatoryOrSelectable = columnConfig.getIsMandatory() || columnConfig.getIsSelectable();
                boolean isNotFactsheetColumn = !columnConfig.getColumnKey().equals(ECXConstants.COL_KEY_INVESTMENT_FACTSHEET);
                log.info("columnConfig" + columnConfig);
                log.info("exportName" + columnConfig.getExportName());
                if (isMandatoryOrSelectable && isNotFactsheetColumn) {
                    String exportName = columnConfig.getExportName();
                    String displayText = columnConfig.getDisplayText().replaceAll("\\<.*?>", " ");
                    String columnKey = columnConfig.getColumnKey();
                    if (exportName == null || exportName.trim().isEmpty()) {
                        headerRow.createCell(cellIdx++).setCellValue(displayText);
                        if (columnKey.equals("investmentCategory")) {
                            headerRow.createCell(cellIdx++).setCellValue(ECXConstants.EXPORT_ACCOUNT_NAME);
                            headerRow.createCell(cellIdx++).setCellValue(ECXConstants.EXPORT_ACCOUNT_NUMBER);
                            if (investmentVO.isPortfolioFlag() == 1) {
                                headerRow.createCell(cellIdx++).setCellValue(ECXConstants.EXPORT_PORTFOLIO_NAME);
                            }
                            if (groupBy.equals("portfolios") && ftrShowPMEMOdelName) {
                                headerRow.createCell(cellIdx++).setCellValue(ECXConstants.EXPORT_PME_MODEL_NAME);
                            }
                        }
                    } else {
                        String[] stackedNames = exportName.split("\\|");
                        if (columnKey.equals(ECXConstants.UNITS_COLUMN)) {
                            headerRow.createCell(cellIdx++).setCellValue(displayText);
                            if (notCustodiedAssetFeature && investmentVO.isShowSplitTradedQuantity()) {
                                for (String name : stackedNames) {
                                    headerRow.createCell(cellIdx++).setCellValue(name);
                                }
                            }
                        } else {
                            for (String name : stackedNames) {
                                headerRow.createCell(cellIdx++).setCellValue(name);
                                log.info("name1" + name);
                                log.info("columnKeyame1" + columnKey);
                                log.info("exportName1" + columnConfig.getExportName());
                                log.info("unrealiame1" + unrealizedGainLossPerctArrow);
                                if(name.equalsIgnoreCase(exportName) && columnKey.equalsIgnoreCase(ECXConstants.SORT_BY_UNREALIZED_GAIN_LOSS)){
                                    if(unrealizedGainLossPerctArrow){
                                        headerRow.createCell(cellIdx++).setCellValue("Unrealized gain/loss percentage");
                                    }
                                }
                            }
                        }
                    }
                    columnKeys.add(columnKey);
                }
            }
            int dataRowIndex = 1;
            if(investmentVO.getInvestmentHoldings() != null) {
                List<InvestmentHoldingsVO> investmentVOs = investmentVO.getInvestmentHoldings();
                Map<String, String> pmeMap = new HashMap<String, String>();
                for (InvestmentHoldingsVO investmentHoldingsVO : investmentVOs) {
                    String currencyCodeVal = investmentHoldingsVO.getCurrencyCodeCharacter();
                    String currencyCodeValCsv = EcxCurrencyUtil.getUnicodebyCurrencyCodeForCsv(currencyCodeVal);
                    XSSFRow dataRow = sheet.createRow(dataRowIndex);
                    int cellIndex = 0;
                    for (String key : columnKeys) {
                        XSSFCell dataCell = dataRow.createCell(cellIndex++);
                        switch (key) {
                            case "assetName":
                                String assetName = notCustodiedAssetFeature && investmentHoldingsVO.isNotCustodiedAsset() ?
                                        EcxFormatterUtil.nullCheck(investmentHoldingsVO.getAssetName())
                                                + "*" : EcxFormatterUtil.nullCheck(investmentHoldingsVO.getAssetName());
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "General"));
                                dataCell.setCellValue(assetName);
                                boolean secondaryAssetFeatureFlag = Boolean.TRUE.equals(siteConfigData.get(ECXConstants.KEY_FTR_SECONDARY_ASSET_IDENTIFIER));
                                String primaryAssetIdentifier = investmentHoldingsVO.getAssetIdentifierPrimary();
                                String secondaryIdentifier = investmentHoldingsVO.getAssetIdentifierSecondary();
                                if (investmentHoldingsVO.getAssetIdentifier().contains("-") && secondaryAssetFeatureFlag) {
                                    dataRow.createCell(cellIndex++).setCellValue("-");
                                    dataRow.createCell(cellIndex++).setCellValue("-");
                                } else if (primaryAssetIdentifier != null && secondaryIdentifier == null && secondaryAssetFeatureFlag) {
                                    dataRow.createCell(cellIndex++).setCellValue(primaryAssetIdentifier);
                                    dataRow.createCell(cellIndex++).setCellValue("-");
                                } else if (secondaryAssetFeatureFlag) {
                                    dataRow.createCell(cellIndex++).setCellValue(primaryAssetIdentifier);
                                    dataRow.createCell(cellIndex++).setCellValue(secondaryIdentifier);
                                } else {
                                    dataRow.createCell(cellIndex++).setCellValue(primaryAssetIdentifier);
                                }
                                break;
                            case "investmentCategory":
                                dataCell.setCellValue(investmentHoldingsVO.getInvestmentCategory());
                                dataRow.createCell(cellIndex++).setCellValue(investmentHoldingsVO.getAccountShortName());
                                dataRow.createCell(cellIndex++).setCellValue(enableAccountMasking ? maskingUtility.getAsterikString(investmentHoldingsVO.getAccountNumber())
                                        : investmentHoldingsVO.getAccountNumber());
                                if (investmentVO.isPortfolioFlag() == 1) {
                                    dataRow.createCell(cellIndex++).setCellValue(EcxFormatterUtil.nullCheck(investmentHoldingsVO.getPortfolios()));
                                }
                                if (groupBy.equals("portfolios") && ftrShowPMEMOdelName) {
                                    if (isPmeModelAdded(investmentHoldingsVO, pmeMap)) {
                                        dataRow.createCell(cellIndex++).setCellValue(investmentHoldingsVO.getPmeModelName());
                                    } else {
                                        String pmeModelName = callPMEModelWS(siteConfigData, firmId, investmentHoldingsVO.getAccountNumber(), investmentHoldingsVO.getPortfolios());
                                        dataRow.createCell(cellIndex++).setCellValue(pmeModelName);
                                        pmeMap.put(investmentHoldingsVO.getAccountNumber(), investmentHoldingsVO.getPortfolios());
                                        investmentHoldingsVO.setPmeModelName(pmeModelName);
                                    }
                                }
                                break;
                            case "units":
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "number"));
                                dataCell.setCellValue(EcxFormatterUtil.nullCheck(investmentHoldingsVO.getUnits()));
                                if (investmentVO.isShowSplitTradedQuantity()) {
                                    Double tardedQuantityInsideSEI = (investmentHoldingsVO.getTardedQuantityInsideSEI() == null) ? 0.0d : investmentHoldingsVO.getTardedQuantityInsideSEI();
                                    Cell tardedQuantityInsideSEICell = dataRow.createCell(cellIndex++);
                                    tardedQuantityInsideSEICell.setCellValue(tardedQuantityInsideSEI);
                                    tardedQuantityInsideSEICell.setCellStyle(excelUtil.getCellStyle(workbook, "number"));
                                    Double tardedQuantityOutsideSEI = (investmentHoldingsVO.getTardedQuantityOutsideSEI() == null) ? 0.0d : investmentHoldingsVO.getTardedQuantityOutsideSEI();
                                    Cell tardedQuantityOutsideSEICell = dataRow.createCell(cellIndex++);
                                    tardedQuantityOutsideSEICell.setCellValue(tardedQuantityOutsideSEI);
                                    tardedQuantityOutsideSEICell.setCellStyle(excelUtil.getCellStyle(workbook, "number"));
                                }
                                break;
                            case "price":
                                Double price = (investmentHoldingsVO.getPrice() == null) ? 0.0d : investmentHoldingsVO.getPrice();
                                if (investmentHoldingsVO.getBookingBasisCd() == ECXConstants.PRICE_PERCENTAGE_INDICATOR) {
                                    dataCell.setCellValue(price/100);
                                    dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "percentage"));
                                } else {
                                    dataCell.setCellValue(EcxFormatterUtil.nullCheckForDouble(investmentHoldingsVO.getPrice(),
                                            true, currencyCodeValCsv, false, true, priceDecimalPrecision));
                                    dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "currency"));
                                    if(price<0) {
                                        dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "negative"));
                                    }
                                }
                                break;
                            case "costBasis":
                                String costBasis = EcxFormatterUtil.nullCheckForDouble(investmentHoldingsVO.getCostBasis(),true,currencyCodeValCsv,false,false, null);
                                dataCell.setCellValue(EcxFormatterUtil.appendNegativeSign(costBasis));
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "currency"));
                                if(costBasis.contains("-")) {
                                    dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "negative"));
                                }
                                break;
                            case "unrealizedGainLoss":
                                String unrealizedGainLoss = EcxFormatterUtil.nullCheckForDouble(investmentHoldingsVO.getUnrealizedGainLoss(),
                                        true, currencyCodeValCsv, false, false, null);
                                String cellValue = EcxFormatterUtil.appendNegativeSign(unrealizedGainLoss);
                                dataCell.setCellValue(cellValue);
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "currency"));
                                if(cellValue.contains("-")) {
                                    dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "negative"));
                                }
                                if (unrealizedGainLossPerctArrow) {
                                    Double unrealizedGainLossPercent = (investmentHoldingsVO.getUnrealizedGainLossPercent() == null) ? 0.0d
                                            : investmentHoldingsVO.getUnrealizedGainLossPercent()/100;
                                    Cell unrealizedGainLossPercentCell = dataRow.createCell(cellIndex++);
                                    unrealizedGainLossPercentCell.setCellValue(unrealizedGainLossPercent);
                                    unrealizedGainLossPercentCell.setCellStyle(excelUtil.getCellStyle(workbook, "percentage"));
                                    if(unrealizedGainLossPercent<0) {
                                        unrealizedGainLossPercentCell.setCellStyle(excelUtil.getCellStyle(workbook, "negativePercentage"));
                                    }
                                }
                                break;
                            case "marketValue":
                                String marketValue = EcxFormatterUtil.nullCheckForDouble(investmentHoldingsVO.getMarketValue(),true, currencyCodeValCsv, false, false, null);
                                dataCell.setCellValue(EcxFormatterUtil.appendNegativeSign(marketValue));
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "currency"));
                                if(marketValue.contains("-")) {
                                    dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "negative"));
                                }
                                break;
                            case "estimatedAnnualIncome":
                                String estimatedAnnualIncome = EcxFormatterUtil.nullCheckForDouble(investmentHoldingsVO.getEstimatedAnnualIncome(),
                                        true, currencyCodeValCsv, false, false, null);
                                dataCell.setCellValue(estimatedAnnualIncome);
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "currency"));
                                break;
                            case "accruedIncome":
                                String accruedIncome = EcxFormatterUtil.nullCheckForDouble(investmentHoldingsVO.getAccruedIncome(),
                                        true, currencyCodeValCsv, false, false, null);
                                dataCell.setCellValue(EcxFormatterUtil.appendNegativeSign(accruedIncome));
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "currency"));
                                if(accruedIncome.contains("-")) {
                                    dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "negative"));
                                }
                                break;
                            case "currentYield":
                                Double currentYield = (investmentHoldingsVO.getCurrentYield() == null) ? 0.0d : investmentHoldingsVO.getCurrentYield();
                                dataCell.setCellValue(currentYield/100);
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "percentage"));
                                break;
                            case "dirtyMarketValue":
                                String dirtyMarketValue = EcxFormatterUtil.nullCheckForDouble(investmentHoldingsVO.getDirtyMarketValue(),
                                        true, currencyCodeValCsv, false, false, null);
                                dataCell.setCellValue(EcxFormatterUtil.appendNegativeSign(dirtyMarketValue));
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "currency"));
                                if(dirtyMarketValue.contains("-")) {
                                    dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "negative"));
                                }
                                break;
                            case "cleanMarketValue":
                                String cleanMarketValue = EcxFormatterUtil.nullCheckForDouble(investmentHoldingsVO.getCleanMarketValue(),
                                        true, currencyCodeValCsv, false, false, null);
                                dataCell.setCellValue(EcxFormatterUtil.appendNegativeSign(cleanMarketValue));
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "currency"));
                                if(cleanMarketValue.contains("-")) {
                                    dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "negative"));
                                }
                                break;
                            case "accounts":
                                dataCell.setCellValue(EcxFormatterUtil.nullCheck(investmentHoldingsVO.getAccounts()));
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "General"));
                                break;
                            case "portfolios":
                                dataCell.setCellValue(EcxFormatterUtil.nullCheck(investmentHoldingsVO.getPortfolios()));
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "General"));
                                break;
                            case "investmentStrategy":
                                dataCell.setCellValue(EcxFormatterUtil.nullCheck(investmentHoldingsVO.getInvestmentStrategy()));
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "General"));
                                break;
                            case "investmentManager":
                                dataCell.setCellValue(EcxFormatterUtil.nullCheck(investmentHoldingsVO.getInvestmentManager()));
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "General"));
                                break;
                            case "fxRate":
                                String fxRateval = nullCheckDouble(investmentHoldingsVO.getFxRate());
                                dataCell.setCellValue(fxRateval);
                                dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "number"));
                                if(fxRateval.contains("-")) {
                                    dataCell.setCellStyle(excelUtil.getCellStyle(workbook, "negative"));
                                }
                                break;
                        }
                    }
                    dataRowIndex++;
                }
            }
            workbook.write(outByteStream);
            return outByteStream.toByteArray();
        } catch (IOException e) {
            log.error("Exception while export - Investments : {}", e);
        }
        return null;
    }
}
