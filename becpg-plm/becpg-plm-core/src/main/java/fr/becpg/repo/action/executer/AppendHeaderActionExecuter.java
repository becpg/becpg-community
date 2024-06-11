/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */

package fr.becpg.repo.action.executer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 *
 * Action used to import text files.
 *
 * @author Quere
 * @version $Id: $Id
 */
public class AppendHeaderActionExecuter extends ActionExecuterAbstractBase {

	/** Constant <code>NAME="append-header"</code> */

	public static final String NAME = "append-header";
	private static final String PARAM_MAPPING_FILE = "mapping-file";
	/** Constant <code>XLSX_EXTENSION=".xlsx"</code> */
	public static final String XLSX_EXTENSION = ".xlsx";
	
	private static final Log logger = LogFactory.getLog(AppendHeaderActionExecuter.class);
	private static final String FULL_PATH_IMPORT_TO_TREAT = "/app:company_home/cm:Exchange/cm:Import/cm:ImportToTreat";
	
	/** Constant <code>SEPARATOR=';'</code> */
	public static final char SEPARATOR = ';';

	private Repository repositoryHelper;
	private ContentService contentService;
	private FileFolderService fileFolderService;
	
	/**
	 * <p>Setter for the field <code>repositoryHelper</code>.</p>
	 *
	 * @param repository a {@link org.alfresco.repo.model.Repository} object.
	 */
	public void setRepositoryHelper(Repository repository) {
		this.repositoryHelper = repository;
	}
	
	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	/**
	 * <p>Setter for the field <code>fileFolderService</code>.</p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object.
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Map headers from data using the provided mapping
	 */
	@Override
	protected void executeImpl(Action ruleAction, NodeRef nodeRef) {
		NodeRef mappingNodeRef = (NodeRef) ruleAction.getParameterValue(PARAM_MAPPING_FILE);
		
		if (mappingNodeRef != null) {
			ContentReader reader = contentService.getReader(mappingNodeRef, ContentModel.PROP_CONTENT);
			
			try (InputStream mappingStream = reader.getContentInputStream()) {
				reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
				File output = TempFileProvider.createTempFile("output_", ".xlsx");
				try (InputStream dataStream = reader.getContentInputStream();
					 InputStreamReader dataISR = new InputStreamReader(new BOMInputStream(dataStream, false), reader.getEncoding());
					 CSVReader dataCSVReader = new CSVReader(dataISR, SEPARATOR);) {
					// Output file generation
				
					appendHeader(mappingStream, dataCSVReader, output, DataFormat.CSV);
					
					NodeRef outputFolderNodeRef = BeCPGQueryBuilder.createQuery()
						.selectNodeByPath(repositoryHelper.getCompanyHome(), FULL_PATH_IMPORT_TO_TREAT);
					FileInfo fileInfo = fileFolderService.create(outputFolderNodeRef, output.getName(),
						ContentModel.PROP_CONTENT);
					NodeRef outputNodeRef = fileInfo.getNodeRef();
					
					ContentWriter writer = contentService.getWriter(outputNodeRef, ContentModel.PROP_CONTENT, true);
					writer.setMimetype(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET);
					writer.putContent(output);
					
					// Remove input file when finished
					fileFolderService.delete(nodeRef);
				} finally {
					if (!output.delete()) {
						logger.error("Cannot delete dir: " + output.getName());
					}
				}
			} catch (IOException e) {
				logger.error("Cannot append headers", e);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_MAPPING_FILE, DataTypeDefinition.NODE_REF,
			false, getParamDisplayLabel(PARAM_MAPPING_FILE)));
	}

	public enum DataFormat {
		XLS, XLSX, CSV
	}
	
	/**
	 * <p>appendHeader.</p>
	 *
	 * @param mappingStream a {@link java.io.InputStream} object
	 * @param dataCSVReader a {@link fr.becpg.common.csv.CSVReader} object
	 * @param xlsOutput a {@link java.io.File} object
	 * @param dataFormat a {@link fr.becpg.repo.action.executer.AppendHeaderActionExecuter.DataFormat} object
	 * @throws java.io.IOException if any.
	 */
	public void appendHeader(InputStream mappingStream, CSVReader dataCSVReader, File xlsOutput, DataFormat dataFormat)
		throws IOException {
		if (!DataFormat.CSV.equals(dataFormat)) {
			throw new IllegalStateException("Not yet implemented");
		}
		
		logger.info("Append headers to: " + xlsOutput.getName() + " - format: " + dataFormat.toString());
		
		try (XSSFWorkbook wb = new XSSFWorkbook(mappingStream)) {
			try (FileOutputStream out = new FileOutputStream(xlsOutput)) {
				Sheet importSheet = wb.getSheet("IMPORT");
				Sheet dataSheet = wb.getSheet("DATA");
				FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

				// Retrieve data from CSV
				List<String[]> lines = dataCSVReader.readAll();
				
				int rowIdx = 0;
				for (String[] line : lines) {
					Row row = dataSheet.createRow(rowIdx);
					
					for (int i = 0; i < line.length; i++) {
						Cell cell = row.createCell(i);
						cell.setCellValue(line[i]);
					}
					rowIdx++;
				}

				rowIdx = 0;
				int columnsRowIdx = 0;
				while ((importSheet.getRow(rowIdx) != null) && (importSheet.getRow(rowIdx).getCell(0) != null)
						&& !"VALUES".equals(importSheet.getRow(rowIdx).getCell(0).getStringCellValue())) {
					if ("COLUMNS".equals(importSheet.getRow(rowIdx).getCell(0).getStringCellValue())) {
						columnsRowIdx = rowIdx;
					}
					rowIdx++;
				}

				List<Integer> emptyRowsIndices = new ArrayList<>(); 
				int lastCellIdx = 0;
				
				if ((columnsRowIdx > 0) && (rowIdx > 0)) {
					// Get last non empty cell index in "COLUMNS" row
					Row columnsRow = importSheet.getRow(columnsRowIdx);
					lastCellIdx = columnsRow.getLastCellNum() - 1;
					
					Cell lastCell = columnsRow.getCell(lastCellIdx, MissingCellPolicy.RETURN_NULL_AND_BLANK);
					while ((lastCell == null) || (lastCell.getCellType() == CellType.BLANK)) {
						lastCellIdx--;
						lastCell = columnsRow.getCell(lastCellIdx, MissingCellPolicy.RETURN_NULL_AND_BLANK);
					}
					
					boolean ignoreHeader = true;
					// Iterate through each rows one by one
					Iterator<Row> dataRowIt = dataSheet.iterator();
					Row previousImportRow = null;
					
					while (dataRowIt.hasNext()) {
						dataRowIt.next();
						
						if (!ignoreHeader) {
							Row importRow = importSheet.getRow(rowIdx);
							importRow = (importRow != null) ? importRow : importSheet.createRow(rowIdx);
							rowIdx++;
							
							Cell values = importRow.createCell(0);
							values.setCellValue("VALUES");
							
							// For each row, iterate through all columns (ignoring first column)
							Cell previousImportCell = null;
							
							boolean isEmptyRow = true;
							for (int i = 1; i < lastCellIdx + 1; i++) {
								if (previousImportRow != null) {
									previousImportCell = previousImportRow.getCell(i, MissingCellPolicy.RETURN_NULL_AND_BLANK);
									
									if ((previousImportCell != null) && (previousImportCell.getCellType() == CellType.FORMULA)) {
										Cell importCell = importRow.createCell(i);
										String formula = changeFormulaRow(wb, previousImportCell, rowIdx - 1);
										importCell.setCellFormula(formula);
										
										CellValue cellVal = evaluator.evaluate(importCell);
										if (!cellVal.getStringValue().isEmpty()) {
											isEmptyRow = false;
										}
									}
								}
							}
							
							if (isEmptyRow) {
								emptyRowsIndices.add(rowIdx - 1);
							}
							
							previousImportRow = importRow;
						} else {
							ignoreHeader = false;
						}
					}
				}
				
				// Clean empty rows
				for (int idx : emptyRowsIndices) {
					Row row = importSheet.getRow(idx); 
					importSheet.removeRow(row);
				}
				
				wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
				wb.setForceFormulaRecalculation(true);
				wb.write(out);
			}
		}
	}
	
	private String changeFormulaRow(XSSFWorkbook wb, Cell cell, int rowIdx) {
		XSSFEvaluationWorkbook evalWb = XSSFEvaluationWorkbook.create(wb);

		Ptg[] ptgs = FormulaParser.parse(cell.getCellFormula(), evalWb, FormulaType.CELL, 0);
		for (int i = 0; i < ptgs.length; i++) {
			if (ptgs[i] instanceof RefPtgBase) {
				RefPtgBase ref = (RefPtgBase) ptgs[i];
				ref.setRow(ref.getRow() + rowIdx - cell.getRowIndex());
			}
		}

		return FormulaRenderer.toFormulaString(evalWb, ptgs);
	}
}
