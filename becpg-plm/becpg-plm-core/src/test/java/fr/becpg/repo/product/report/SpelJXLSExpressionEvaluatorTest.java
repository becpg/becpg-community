/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.jxls.common.Context;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;

/**
 * Unit tests of {@link SpelJXLSExpressionEvaluator}: JXLS 3 commands evaluate the expression the evaluator is bound to.
 */
public class SpelJXLSExpressionEvaluatorTest {

	private static final String ENTITY = "entity";
	private static final String DATA_LIST_ITEM = "dataListItem";
	private static final String PRODUCT_NAME = "Chocolate eclair";
	private static final String NUT_LIST = "nutList";
	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String HAS_NUT_LIST = "nutList.size() > 0";
	private static final String WRONG_EXPRESSION = "unknownProperty";
	private static final String WRONG_EXPRESSION_PREFIX = "Wrong expression: " + WRONG_EXPRESSION;
	private static final Double NUT_VALUE = 12.5d;
	private static final Double OTHER_NUT_VALUE = 3d;
	private static final String AREA_COMMAND = "jx:area(lastCell=\"A2\")";
	private static final String EACH_COMMAND = "jx:each(items=\"nutList\" var=\"dataListItem\" lastCell=\"A2\")";
	private static final String NAME_CELL = "${name}";
	private static final String VALUE_CELL = "${value}";

	private SpelFormulaService formulaService;

	private FinishedProductData product;

	private NutListDataItem nutListItem;

	private Map<String, Object> data;

	@Before
	public void setUp() {
		nutListItem = NutListDataItem.build().withValue(NUT_VALUE);
		product = FinishedProductData.build().withName(PRODUCT_NAME).withNutList(List.of(nutListItem));
		data = new HashMap<>();
		data.put(ENTITY, product);

		SpelExpressionParser parser = new SpelExpressionParser();
		formulaService = mock(SpelFormulaService.class);
		when(formulaService.getSpelParser()).thenReturn(parser);
		when(formulaService.createEntitySpelContext(product)).thenReturn(new StandardEvaluationContext(product));
		when(formulaService.createDataListItemSpelContext(product, nutListItem)).thenReturn(new StandardEvaluationContext(nutListItem));
	}

	@Test
	public void boundExpressionIsReturned() {
		assertEquals(NUT_LIST, evaluatorBoundTo(NUT_LIST).getExpression());
	}

	@Test
	public void eachItemsExpressionEvaluatesToTheEntityList() {
		assertEquals(product.getNutList(), evaluatorBoundTo(NUT_LIST).evaluate(data));
	}

	@Test
	public void boundExpressionIsEvaluatedOnTheCurrentDataListItem() {
		data.put(DATA_LIST_ITEM, nutListItem);

		assertEquals(NUT_VALUE, evaluatorBoundTo(VALUE).evaluate(data));
	}

	@Test
	public void cellExpressionIsEvaluatedOnTheEntity() {
		assertEquals(PRODUCT_NAME, evaluatorBoundTo(null).evaluate(NAME, data));
	}

	@Test
	public void ifConditionIsEvaluated() {
		assertTrue(evaluatorBoundTo(HAS_NUT_LIST).isConditionTrue(jxlsContext()));
	}

	@Test
	public void falseIfConditionIsEvaluated() {
		product.setNutList(List.of());

		assertFalse(evaluatorBoundTo(HAS_NUT_LIST).isConditionTrue(jxlsContext()));
	}

	@Test
	public void wrongExpressionIsReported() {
		Object result = evaluatorBoundTo(WRONG_EXPRESSION).evaluate(data);

		assertTrue(String.valueOf(result).startsWith(WRONG_EXPRESSION_PREFIX));
	}

	@Test
	public void eachCommandFillsOneRowPerListItem() throws IOException {
		NutListDataItem otherNutListItem = NutListDataItem.build().withValue(OTHER_NUT_VALUE);
		product.setNutList(List.of(nutListItem, otherNutListItem));
		when(formulaService.createDataListItemSpelContext(product, otherNutListItem)).thenReturn(new StandardEvaluationContext(otherNutListItem));

		try (Workbook report = fillTemplate()) {
			Sheet sheet = report.getSheetAt(0);
			assertEquals(PRODUCT_NAME, sheet.getRow(0).getCell(0).getStringCellValue());
			assertEquals(NUT_VALUE, sheet.getRow(1).getCell(0).getNumericCellValue(), 0d);
			assertEquals(OTHER_NUT_VALUE, sheet.getRow(2).getCell(0).getNumericCellValue(), 0d);
		}
	}

	private Workbook fillTemplate() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JxlsPoiTemplateFillerBuilder.newInstance()
				.withExpressionEvaluatorFactory(expression -> new SpelJXLSExpressionEvaluator(formulaService, expression))
				.withTemplate(new ByteArrayInputStream(buildTemplate())).buildAndFill(data, () -> out);
		return new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
	}

	private byte[] buildTemplate() throws IOException {
		try (Workbook template = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = template.createSheet();
			sheet.createRow(0).createCell(0).setCellValue(NAME_CELL);
			sheet.createRow(1).createCell(0).setCellValue(VALUE_CELL);
			addComment(sheet, 0, AREA_COMMAND);
			addComment(sheet, 1, EACH_COMMAND);
			template.write(out);
			return out.toByteArray();
		}
	}

	private void addComment(Sheet sheet, int rowIndex, String text) {
		CreationHelper creationHelper = sheet.getWorkbook().getCreationHelper();
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = creationHelper.createClientAnchor();
		anchor.setRow1(rowIndex);
		anchor.setCol1(0);
		Comment comment = drawing.createCellComment(anchor);
		comment.setString(creationHelper.createRichTextString(text));
		sheet.getRow(rowIndex).getCell(0).setCellComment(comment);
	}

	private Context jxlsContext() {
		Context context = mock(Context.class);
		when(context.toMap()).thenReturn(data);
		return context;
	}

	private SpelJXLSExpressionEvaluator evaluatorBoundTo(String expression) {
		return new SpelJXLSExpressionEvaluator(formulaService, expression);
	}
}
