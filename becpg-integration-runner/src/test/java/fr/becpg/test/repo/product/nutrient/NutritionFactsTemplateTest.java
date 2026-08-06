package fr.becpg.test.repo.product.nutrient;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsData;
import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsLine;
import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsServing;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

/**
 * Checks the SVG a nutrition facts template produces: it has to be well formed XML, free of the
 * constructs Batik refuses, and drawn with the rules and the indentation the regulation states.
 * Batik is what BIRT hands the panel to, so anything it cannot parse never reaches a PDF.
 */
public class NutritionFactsTemplateTest {

	private static final String VERTICAL_TEMPLATE = "nutritionFacts-vertical.ftlx";

	private static final String CANADA_TEMPLATE = "nutritionFacts-canada.ftlx";

	private static final String MODEL_KEY = "nf_data";

	private static final double HAIRLINE = 0.25d;

	private static final double PANEL_WIDTH = 144d;

	private static final double INDENT = 5.5d;

	private static final double PAD = 4d;

	private Configuration configuration;

	@Before
	public void setUp() {
		configuration = new Configuration(Configuration.VERSION_2_3_30);
		configuration.setTemplateLoader(new ClassTemplateLoader(NutritionFactsTemplateTest.class, "/beCPG/templates"));
		configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
		configuration.setNumberFormat("computer");
		configuration.setRecognizeStandardFileExtensions(true);
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
		configuration.setLocalizedLookup(true);
	}

	@Test
	public void testPanelIsWellFormedSvg() throws Exception {
		Element svg = render().getDocumentElement();

		Assert.assertEquals("svg", svg.getTagName());
		Assert.assertEquals("http://www.w3.org/2000/svg", svg.getAttribute("xmlns"));
		Assert.assertEquals("144pt", svg.getAttribute("width"));
		Assert.assertTrue("The panel must declare its height", svg.getAttribute("height").endsWith("pt"));
	}

	@Test
	public void testPanelAvoidsWhatBatikCannotRender() throws Exception {
		String svg = renderToString();

		Assert.assertFalse("A DOCTYPE makes Batik fetch a DTD over the network", svg.contains("<!DOCTYPE"));
		Assert.assertFalse("foreignObject is not rendered by the PDF path of Batik", svg.contains("foreignObject"));
		Assert.assertFalse("An external reference cannot be resolved by the report server", svg.contains("xlink:href"));
	}

	@Test
	public void testRulesAreDrawnAsRectanglesOfTheRequiredThickness() throws Exception {
		List<Element> rects = elements(render(), "rect");

		Assert.assertEquals("One hairline above each of the 10 nutrients, 3 between the 4 vitamins, one under the title", 14,
				countByHeight(rects, HAIRLINE));
		Assert.assertEquals("A thick rule under the serving block and another above the vitamins", 2, countByHeight(rects, 7d));
		Assert.assertEquals("A medium rule under the calories and another above the footnote", 2, countByHeight(rects, 3d));
	}

	@Test
	public void testNutrientLinesAreIndentedByTheirDepth() throws Exception {
		Document panel = render();

		Assert.assertEquals(PAD, textStart(panel, "Total Fat"), 0.01d);
		Assert.assertEquals(PAD + INDENT, textStart(panel, "Saturated Fat"), 0.01d);
		Assert.assertEquals(PAD + 2 * INDENT, textStart(panel, "Added Sugars"), 0.01d);
	}

	@Test
	public void testDailyValuesArePinnedToTheRightMargin() throws Exception {
		Element percent = findText(render(), "10%");

		Assert.assertEquals("end", percent.getAttribute("text-anchor"));
		Assert.assertEquals(PANEL_WIDTH - PAD, Double.parseDouble(percent.getAttribute("x")), 0.01d);
	}

	@Test
	public void testTitleIsCondensedToTheExactPanelWidth() throws Exception {
		Element title = findText(render(), "Nutrition Facts");

		Assert.assertEquals("spacingAndGlyphs", title.getAttribute("lengthAdjust"));
		Assert.assertEquals(PANEL_WIDTH - 2 * PAD, Double.parseDouble(title.getAttribute("textLength")), 0.01d);
	}

	@Test
	public void testLabelsAreEscapedSoThatTheSvgStaysParsable() throws Exception {
		NutritionFactsData data = panelData(line("FAT", "Fat & <oil>", "8g", "10%", 1, true));

		Assert.assertTrue("An ampersand must be escaped, SVG being strict XML", renderToString(data).contains("Fat &amp; &lt;oil&gt;"));
		Assert.assertNotNull("The panel must still parse", parse(renderToString(data)));
	}

	@Test
	public void testFootnoteIsWrappedInsideThePanel() throws Exception {
		List<Element> texts = elements(render(), "text");

		int footnoteLines = 0;
		for (Element text : texts) {
			if (text.getTextContent().contains("Daily Value (DV)") || text.getTextContent().contains("nutrition advice")) {
				footnoteLines++;
			}
		}
		Assert.assertTrue("The footnote has to be split over several lines to fit the panel", footnoteLines > 1);
	}

	@Test
	public void testEveryShippedFormatRendersAWellFormedPanel() throws Exception {
		for (String format : List.of("vertical", "sideBySide", "tabular", "linear", "simplified", "dualColumn")) {
			String svg = renderToString("nutritionFacts-" + format + ".ftlx", standardPanel());

			Assert.assertEquals(format + " must be a svg", "svg", parse(svg).getDocumentElement().getTagName());
			Assert.assertFalse(format + " must not carry a DOCTYPE", svg.contains("<!DOCTYPE"));
			Assert.assertFalse(format + " must not use foreignObject", svg.contains("foreignObject"));
		}
	}

	@Test
	public void testLinearFormatUsesTheRegulatedAbbreviations() throws Exception {
		NutritionFactsData data = panelData(
				new NutritionFactsLine("FASAT", "Saturated Fat", "Sat. Fat", "Saturated Fat", "1g", null, "5%", null, 2, false, true, false));

		Assert.assertTrue("The linear format names nutrients by their abbreviation",
				renderToString("nutritionFacts-linear.ftlx", data).contains("Sat. Fat"));
	}

	@Test
	public void testTabularFormatIsDrawnWider() throws Exception {
		Element svg = parse(renderToString("nutritionFacts-tabular.ftlx", standardPanel())).getDocumentElement();

		Assert.assertEquals("A tabular panel runs across the width", "552pt", svg.getAttribute("width"));

		String markup = renderToString("nutritionFacts-tabular.ftlx", standardPanel());
		Assert.assertTrue("Its title is stacked on two lines in the left band", markup.contains(">Nutrition<") && markup.contains(">Facts<"));
		Assert.assertTrue("Each column carries the Amount/serving header", markup.contains("Amount/serving"));
	}

	@Test
	public void testDualColumnFormatCarriesBothColumnsOfFigures() throws Exception {
		String svg = renderToString("nutritionFacts-dualColumn.ftlx", standardPanel());

		Assert.assertEquals("Four columns of figures need a wider panel", "252pt", parse(svg).getDocumentElement().getAttribute("width"));
		Assert.assertTrue("The per container header must be drawn", svg.contains("Per container"));
	}

	@Test
	public void testSimplifiedFormatClosesOnItsStatement() throws Exception {
		Assert.assertTrue("A simplified panel states what it does not list",
				renderToString("nutritionFacts-simplified.ftlx", standardPanel()).contains("Not a significant source"));
	}

	@Test
	public void testCanadianPanelOpensOnASingleServingLine() throws Exception {
		Document panel = parse(renderToString(CANADA_TEMPLATE, canadianPanel()));

		Assert.assertNotNull("The Canadian panel is titled Valeur nutritive in French", findText(panel, "Valeur nutritive"));
		Assert.assertNotNull("It opens on a Per <serving> line", findText(panel, "Pour 1 tasse"));
		Assert.assertNotNull("Its footnote states the little/lot rule", findText(panel, "* 5 % ou moins"));
	}

	@Test
	public void testCanadianPanelKeepsTheSameRuleVocabulary() throws Exception {
		List<Element> rects = elements(parse(renderToString(CANADA_TEMPLATE, canadianPanel())), "rect");

		Assert.assertEquals("A thick rule under the serving line and another above the minerals", 2, countByHeight(rects, 7d));
		Assert.assertEquals("A medium rule under the calories and another above the footnote", 2, countByHeight(rects, 3d));
	}

	private NutritionFactsData canadianPanel() {
		Map<String, String> labels = new LinkedHashMap<>();
		labels.put("title", "Valeur nutritive");
		labels.put("servingSize", "Pour");
		labels.put("dailyValue", "% valeur quotidienne*");

		return new NutritionFactsData("canada", "CA", new NutritionFactsServing(null, "1 tasse (250 mL)"),
				line("US_ENER-E14", "Calories", "230", null, 1, true),
				List.of(line("FAT", "Lipides", "8 g", "10%", 1, true), line("FASAT", "saturés", "1 g", "5%", 2, false),
						line("NA", "Sodium", "160 mg", "7%", 1, true)),
				List.of(line("K", "Potassium", "235 mg", "5%", 1, false), line("CA", "Calcium", "260 mg", "20%", 1, false)),
				"* 5 % ou moins c'est peu, 15 % ou plus c'est beaucoup", "", labels);
	}

	private double textStart(Document panel, String label) {
		return Double.parseDouble(findText(panel, label).getAttribute("x"));
	}

	private Element findText(Document panel, String startsWith) {
		for (Element text : elements(panel, "text")) {
			if (text.getTextContent().startsWith(startsWith)) {
				return text;
			}
		}
		throw new AssertionError("No text starting with " + startsWith);
	}

	private int countByHeight(List<Element> rects, double height) {
		int count = 0;
		for (Element rect : rects) {
			if (Math.abs(Double.parseDouble(rect.getAttribute("height")) - height) < 0.001d) {
				count++;
			}
		}
		return count;
	}

	private List<Element> elements(Document panel, String tagName) {
		NodeList nodes = panel.getElementsByTagName(tagName);
		List<Element> elements = new java.util.ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			elements.add((Element) nodes.item(i));
		}
		return elements;
	}

	private Document render() throws Exception {
		return parse(renderToString());
	}

	private Document parse(String svg) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		return factory.newDocumentBuilder().parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
	}

	private String renderToString() throws Exception {
		return renderToString(standardPanel());
	}

	private String renderToString(NutritionFactsData data) throws Exception {
		return renderToString(VERTICAL_TEMPLATE, data);
	}

	private String renderToString(String templateName, NutritionFactsData data) throws Exception {
		java.io.StringWriter writer = new java.io.StringWriter();
		configuration.getTemplate(templateName, Locale.US).process(Map.of(MODEL_KEY, data), writer);
		return writer.toString();
	}

	private NutritionFactsData standardPanel() {
		return panelData(line("FAT", "Total Fat", "8g", "10%", 1, true), line("FASAT", "Saturated Fat", "1g", "5%", 2, false),
				line("FATRN", "Trans Fat", "0g", null, 2, false), line("CHOL-", "Cholesterol", "0mg", "0%", 1, true),
				line("NA", "Sodium", "160mg", "7%", 1, true), line("CHO-", "Total Carbohydrate", "37g", "13%", 1, true),
				line("FIBTG", "Dietary Fiber", "4g", "14%", 2, false), line("SUGAR", "Total Sugars", "12g", null, 2, false),
				line("SUGAD", "Added Sugars", "10g", "20%", 3, false), line("PRO-", "Protein", "3g", null, 1, true));
	}

	private NutritionFactsData panelData(NutritionFactsLine... nutrients) {
		return new NutritionFactsData("vertical", "US", new NutritionFactsServing("8", "2/3 cup (55g)"),
				line("US_ENER-E14", "Calories", "230", null, 1, true), List.of(nutrients),
				List.of(line("VITD-", "Vitamin D", "2mcg", "10%", 1, false), line("CA", "Calcium", "260mg", "20%", 1, false),
						line("FE", "Iron", "8mg", "45%", 1, false), line("K", "Potassium", "235mg", "6%", 1, false)),
				"* The % Daily Value (DV) tells you how much a nutrient in a serving of food contributes to a daily diet. "
						+ "2,000 calories a day is used for general nutrition advice.",
				"Not a significant source of other nutrients.", panelLabels());
	}

	private Map<String, String> panelLabels() {
		Map<String, String> labels = new LinkedHashMap<>();
		labels.put("title", "Nutrition Facts");
		labels.put("servingsPerContainer", "servings per container");
		labels.put("servingSize", "Serving size");
		labels.put("amountPerServing", "Amount per serving");
		labels.put("amountPerServingShort", "Amount/serving");
		labels.put("dailyValue", "% Daily Value*");
		labels.put("perServing", "Per serving");
		labels.put("perContainer", "Per container");
		return labels;
	}

	private NutritionFactsLine line(String nutCode, String label, String value, String dailyValue, int indentLevel, boolean bold) {
		return new NutritionFactsLine(nutCode, label, label, label, value, value, dailyValue, dailyValue, indentLevel, bold, dailyValue != null, false);
	}

}
