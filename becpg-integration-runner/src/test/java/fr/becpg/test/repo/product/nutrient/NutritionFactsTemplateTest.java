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
import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsTranslation;
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

	private static final String CANADA_LINEAR_TEMPLATE = "nutritionFacts-canadaLinear.ftlx";

	private static final String CANADA_HORIZONTAL_TEMPLATE = "nutritionFacts-canadaHorizontal.ftlx";

	private static final String CANADA_SUPPLEMENTED_TEMPLATE = "nutritionFacts-canadaSupplemented.ftlx";

	private static final String CANADIAN_FOOTNOTE = "* 5 % ou moins c'est peu, 15 % ou plus c'est beaucoup";

	private static final String ENGLISH_CANADIAN_FOOTNOTE = "* 5% or less is a little, 15% or more is a lot";

	private static final String MODEL_KEY = "nf_data";

	private static final String FOOTNOTE = "* The % Daily Value (DV) tells you how much a nutrient in a serving of food contributes to a daily "
			+ "diet. 2,000 calories a day is used for general nutrition advice.";

	private static final String BOLD = "bold";

	private static final String ARIAL = "Arial";

	private static final double HAIRLINE = 0.25d;

	private static final double PANEL_WIDTH = 144d;

	/** Width of a Canadian panel stating "% valeur quotidienne*", which widens it past its minimum. */
	private static final String CANADA_PANEL_WIDTH = "170pt";

	/** Minimum width of a bilingual Canadian panel, which already holds its daily value header. */
	private static final String CANADA_BILINGUAL_PANEL_WIDTH = "180pt";

	private static final double RULE_MEDIUM = 3d;

	/** Weight of the rule closing the opening block of a Canadian panel, lighter than its parts. */
	private static final double RULE_LIGHT = 1d;

	/** Reference a supplemented food facts table prints on the amounts its closing note covers. */
	private static final String SUPPLEMENT_MARK = "\u2020";

	private static final double INDENT = 5.5d;

	private static final double PAD = 4d;

	private static final String NBSP = "\u00A0";

	private static final String TABULAR_TEMPLATE = "nutritionFacts-tabular.ftlx";

	private static final String DUAL_COLUMN_TEMPLATE = "nutritionFacts-dualColumn.ftlx";

	private static final String LINEAR_TEMPLATE = "nutritionFacts-linear.ftlx";

	private static final String LINEAR_SMALL_TEMPLATE = "nutritionFacts-linearSmall.ftlx";

	private static final double TABULAR_COLUMN_WIDTH = (528d - 148d - 72d) / 2;

	private static final double DUAL_COLUMN_LABEL_WIDTH = 84d;

	private static final String ITALIC_TRANS = "<tspan font-style=\"italic\">Trans</tspan>";

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
	public void testPanelIsSetInArialOnly() throws Exception {
		String svg = renderToString(CANADA_TEMPLATE, canadianPanel());

		Assert.assertTrue("The panel is set in Arial", parse(svg).getDocumentElement().getAttribute("font-family").startsWith(ARIAL));
		Assert.assertFalse("No text switches to another face", svg.contains("<text font-family") || svg.contains(" font-family=\"'"));
		Assert.assertFalse("Emphasis is the bold weight, never a black face", svg.contains("font-weight=\"900\""));
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
		double widest = 0;
		for (Element text : texts) {
			if (isFootnoteLine(text)) {
				footnoteLines++;
				widest = Math.max(widest, estimatedWidth(text));
			}
		}
		Assert.assertEquals("The disclaimer of the FDA takes three lines at the width of a vertical panel", 3, footnoteLines);
		Assert.assertTrue("No footnote line may run past the margin", widest <= PANEL_WIDTH - 2 * PAD);
	}

	@Test
	public void testEveryShippedFormatRendersAWellFormedPanel() throws Exception {
		for (String format : List.of("vertical", "sideBySide", "tabular", "linear", "linearSmall", "simplified", "dualColumn")) {
			String svg = renderToString("nutritionFacts-" + format + ".ftlx", standardPanel());

			Assert.assertEquals(format + " must be a svg", "svg", parse(svg).getDocumentElement().getTagName());
			Assert.assertFalse(format + " must not carry a DOCTYPE", svg.contains("<!DOCTYPE"));
			Assert.assertFalse(format + " must not use foreignObject", svg.contains("foreignObject"));
		}
	}

	@Test
	public void testEveryCanadianFormatRendersAWellFormedPanel() throws Exception {
		for (String format : List.of("canada", "canadaLinear", "canadaHorizontal", "canadaSupplemented")) {
			for (NutritionFactsData data : List.of(canadianPanel(), bilingualCanadianPanel())) {
				String svg = renderToString("nutritionFacts-" + format + ".ftlx", data);

				Assert.assertEquals(format + " must be a svg", "svg", parse(svg).getDocumentElement().getTagName());
				Assert.assertFalse(format + " must not carry a DOCTYPE", svg.contains("<!DOCTYPE"));
			}
		}
	}

	@Test
	public void testLinearFormatUsesTheRegulatedAbbreviations() throws Exception {
		NutritionFactsData data = panelData(new NutritionFactsLine("FASAT", "Saturated Fat", "Sat. Fat", "Saturated Fat", "Sat. Fat", "1g", null,
				"5%", null, 2, false, true, false, false, false));

		Assert.assertTrue("The linear format names nutrients by their abbreviation",
				sentence(renderToString(LINEAR_TEMPLATE, data)).contains("Sat. Fat 1g (5% DV)"));
	}

	@Test
	public void testLinearFormatKeepsTheSpaceBetweenTwoEmphasisedWords() throws Exception {
		Document panel = parse(renderToString("nutritionFacts-linearSmall.ftlx", standardPanel()));

		Assert.assertTrue("The whole declaration is one paragraph, heading included",
				findText(panel, "Nutrition Facts").getTextContent().startsWith("Nutrition Facts 8 servings per container,"));
	}

	@Test
	public void testLinearFormatsAreDrawnAcrossThePackage() throws Exception {
		for (String format : List.of("linear", "linearSmall")) {
			Element svg = parse(renderToString("nutritionFacts-" + format + ".ftlx", standardPanel())).getDocumentElement();

			Assert.assertEquals(format + " states its declaration in a sentence, which needs width", "504pt", svg.getAttribute("width"));
		}
	}

	@Test
	public void testSideBySideFormatIsWidenedForItsPairedMicronutrients() throws Exception {
		Element svg = parse(renderToString("nutritionFacts-sideBySide.ftlx", standardPanel())).getDocumentElement();

		Assert.assertEquals("Two declarations on one line do not fit the width of a vertical panel", "176pt", svg.getAttribute("width"));
	}

	@Test
	public void testDailyValueOfAMicronutrientIsNotEmphasised() throws Exception {
		Document panel = render();

		Assert.assertEquals("The percentage of a mandatory nutrient is set in bold", BOLD, findText(panel, "10%").getAttribute("font-weight"));
		Assert.assertEquals("The percentage of a vitamin stays in the regular weight", "", findText(panel, "45%").getAttribute("font-weight"));
	}

	@Test
	public void testTabularFormatIsDrawnWider() throws Exception {
		Element svg = parse(renderToString("nutritionFacts-tabular.ftlx", standardPanel())).getDocumentElement();

		Assert.assertEquals("A tabular panel runs across the width", "528pt", svg.getAttribute("width"));

		String markup = renderToString("nutritionFacts-tabular.ftlx", standardPanel());
		Assert.assertTrue("Its title is stacked on two lines in the left band", markup.contains(">Nutrition<") && markup.contains(">Facts<"));
		Assert.assertTrue("Each column carries the Amount/serving header", markup.contains("Amount/serving"));
	}

	@Test
	public void testTabularFormatDrawsNoVerticalRule() throws Exception {
		List<Element> rects = elements(parse(renderToString("nutritionFacts-tabular.ftlx", standardPanel())), "rect");

		for (Element rect : rects) {
			Assert.assertNotEquals("The bands and the columns are told apart by the air between them, never by a vertical rule", HAIRLINE,
					Double.parseDouble(rect.getAttribute("width")), 0.001d);
		}
	}

	@Test
	public void testTabularRulesAreBrokenBetweenTheTwoColumns() throws Exception {
		List<Element> rects = elements(parse(renderToString("nutritionFacts-tabular.ftlx", standardPanel())), "rect");

		double hairlines = 0;
		for (Element rect : rects) {
			if ((Math.abs(Double.parseDouble(rect.getAttribute("height")) - HAIRLINE) < 0.001d)
					&& (Double.parseDouble(rect.getAttribute("width")) > TABULAR_COLUMN_WIDTH)) {
				hairlines++;
			}
		}
		Assert.assertEquals("No hairline runs from one column into the other", 0, hairlines, 0d);
	}

	@Test
	public void testTabularServingSizeStandsUnderItsWording() throws Exception {
		Document panel = parse(renderToString(TABULAR_TEMPLATE, standardPanel()));

		Assert.assertEquals("The wording stands alone on its line", "Serving size", findText(panel, "Serving size").getTextContent());
		Assert.assertTrue("and the serving size goes on the next one", y(findText(panel, "2/3 cup (55g)")) > y(findText(panel, "Serving size")));
	}

	@Test
	public void testTabularCaloriesValueIsCentredAgainstItsTwoLines() throws Exception {
		Document panel = parse(renderToString(TABULAR_TEMPLATE, standardPanel()));

		double calories = y(findText(panel, "Calories"));
		double perServing = y(findText(panel, "Per serving"));
		double value = y(findText(panel, "230"));
		Assert.assertTrue("The value sits between the baselines of Calories and Per serving", (value > calories) && (value < perServing));
	}

	@Test
	public void testTabularRuleAboveANestedLineStartsAtItsIndentation() throws Exception {
		List<Element> rects = elements(parse(renderToString(TABULAR_TEMPLATE, standardPanel())), "rect");

		double nestedStart = 148d + TABULAR_COLUMN_WIDTH + 4d + 2 * INDENT;
		boolean found = false;
		for (Element rect : rects) {
			found |= Math.abs(Double.parseDouble(rect.getAttribute("x")) - nestedStart) < 0.001d;
		}
		Assert.assertTrue("The rule above Added Sugars starts where that line starts", found);
	}

	@Test
	public void testTabularMicronutrientDeclarationIsNeverSplit() throws Exception {
		String svg = renderToString(TABULAR_TEMPLATE, standardPanel());

		Assert.assertTrue("A declaration of the vitamin line wraps as a whole", svg.contains("Potassium" + NBSP + "235mg" + NBSP + "6%"));
	}

	@Test
	public void testDualColumnLeavesOutAPercentageTheServingColumnLeavesOut() throws Exception {
		String svg = renderToString(DUAL_COLUMN_TEMPLATE, panelData(line("PRO-", "Protein", "3g", null, 1, true, false, false, "55g", "110%")));

		Assert.assertFalse("Protein declares no daily value per serving, so none per container either", svg.contains("110%"));
	}

	@Test
	public void testDualColumnAmountStartsJustAfterItsRule() throws Exception {
		Element amount = findText(parse(renderToString(DUAL_COLUMN_TEMPLATE, standardPanel())), "8g");

		Assert.assertEquals("An amount is set against the rule opening its column, air kept after the rule",
				DUAL_COLUMN_LABEL_WIDTH + 4d, Double.parseDouble(amount.getAttribute("x")), 0.001d);
		Assert.assertEquals("and reads from there", "", amount.getAttribute("text-anchor"));
	}

	@Test
	public void testDualColumnFootnoteHoldsOnThreeLines() throws Exception {
		Document panel = parse(renderToString(DUAL_COLUMN_TEMPLATE, standardPanel()));

		int footnoteLines = 0;
		for (Element text : elements(panel, "text")) {
			footnoteLines += Double.parseDouble(text.getAttribute("font-size")) == 6d && y(text) > y(findText(panel, "Potassium")) ? 1 : 0;
		}
		Assert.assertEquals("The disclaimer takes three lines", 3, footnoteLines);
	}

	@Test
	public void testLinearFormatOpensOnTheServingsAsTheFdaWordsThem() throws Exception {
		String sentence = sentence(renderToString(LINEAR_SMALL_TEMPLATE, usLinearPanel()));

		Assert.assertTrue("Servings then serving size, both followed by a colon",
				sentence.startsWith("Nutrition Facts Servings: 8, Serv. size: 2/3 cup (55g), Amount per serving: Calories 230,"));
	}

	@Test
	public void testLinearFormatStatesTheCaloriesAndTheFatInOneSentence() throws Exception {
		Element calories = findText(parse(renderToString(LINEAR_TEMPLATE, usLinearPanel())), "Amount per serving");

		Assert.assertTrue("Total Fat follows the calories on the same line", calories.getTextContent().contains("Calories 230, Total" + NBSP + "Fat 8g"));
	}

	@Test
	public void testLinearFormatEmphasisesOnlyTheNutrientsTheColumnsEmphasise() throws Exception {
		String svg = renderToString(LINEAR_TEMPLATE, usLinearPanel());

		Assert.assertTrue("Total Fat is set in bold", svg.contains(">Total" + NBSP + "Fat</tspan>"));
		Assert.assertFalse("Saturated Fat stays in the regular weight", svg.contains("Saturated" + NBSP + "Fat</tspan>"));
		Assert.assertFalse("and so does a vitamin", svg.contains("Vitamin" + NBSP + "D</tspan>"));
	}

	@Test
	public void testLinearFormatStatesAddedSugarsInsideTotalSugars() throws Exception {
		String sentence = sentence(renderToString(LINEAR_TEMPLATE, usLinearPanel()));

		Assert.assertTrue("Added sugars are stated in the parenthesis of total sugars",
				sentence.contains("Total Sugars 12g (Incl. 10g Added Sugars, 20% DV),"));
	}

	@Test
	public void testLinearFormatStatesAVitaminByItsPercentageAlone() throws Exception {
		String sentence = sentence(renderToString(LINEAR_TEMPLATE, usLinearPanel()));

		Assert.assertTrue("A vitamin is stated without its amount", sentence.contains("Vitamin D (10% DV), Calcium (20% DV)"));
		Assert.assertFalse("whatever its amount", sentence.contains("2mcg"));
	}

	@Test
	public void testLinearFormatLeavesOutTheFootnote() throws Exception {
		for (String template : List.of(LINEAR_TEMPLATE, LINEAR_SMALL_TEMPLATE)) {
			Assert.assertFalse(template + " states no daily value footnote", renderToString(template, usLinearPanel()).contains("tells you how much"));
		}
	}

	@Test
	public void testLinearFormatNeverBreaksANutrientName() throws Exception {
		for (Element text : elements(parse(renderToString(LINEAR_SMALL_TEMPLATE, usLinearPanel())), "text")) {
			String content = text.getTextContent().trim();
			Assert.assertFalse("No line starts with the second word of a name: " + content, content.startsWith("Carb.") || content.startsWith("Fat "));
		}
		Assert.assertTrue("The words of a name are joined by a no-break space", renderToString(LINEAR_TEMPLATE, usLinearPanel()).contains("Total" + NBSP + "Carb."));
	}

	@Test
	public void testTransIsSetInItalicsInEveryUsFormat() throws Exception {
		for (String format : List.of("vertical", "sideBySide", "simplified", "tabular", "dualColumn")) {
			Assert.assertTrue(format + " sets Trans in italics", renderToString("nutritionFacts-" + format + ".ftlx", standardPanel()).contains(ITALIC_TRANS + " Fat"));
		}
		for (String template : List.of(LINEAR_TEMPLATE, LINEAR_SMALL_TEMPLATE)) {
			Assert.assertTrue(template + " sets Trans in italics", renderToString(template, standardPanel()).contains(ITALIC_TRANS + NBSP + "Fat"));
		}
	}

	@Test
	public void testItalicsApplyToTheWholeWordOnly() throws Exception {
		String svg = renderToString(VERTICAL_TEMPLATE, panelData(line("TRANSG", "Transglutaminase", "1g", null, 1, false)));

		Assert.assertFalse("A word merely starting with Trans stays upright", svg.contains(ITALIC_TRANS));
	}

	@Test
	public void testCanadianPanelsSetNoWordInItalics() throws Exception {
		for (String template : List.of(CANADA_TEMPLATE, CANADA_LINEAR_TEMPLATE, CANADA_HORIZONTAL_TEMPLATE)) {
			Assert.assertFalse(template + " declares no italic word", renderToString(template, bilingualCanadianPanel()).contains("font-style"));
		}
	}

	@Test
	public void testDualColumnFormatCarriesBothColumnsOfFigures() throws Exception {
		String svg = renderToString("nutritionFacts-dualColumn.ftlx", standardPanel());

		Assert.assertEquals("Four columns of figures need a wider panel, 3 inches", "216pt", parse(svg).getDocumentElement().getAttribute("width"));
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
	public void testCanadianPanelRulesTheGroupsAndNotEveryLine() throws Exception {
		List<Element> rects = elements(parse(renderToString(CANADA_TEMPLATE, canadianPanel())), "rect");

		Assert.assertEquals("A medium rule closes the calories, the core nutrients and the minerals", 3, countByHeight(rects, RULE_MEDIUM));
		Assert.assertEquals("The title and the serving are closed by a lighter rule", 1, countByHeight(rects, RULE_LIGHT));
		Assert.assertEquals("A hairline separates the declarations inside a group, and nothing else", 2, countByHeight(rects, HAIRLINE));
	}

	@Test
	public void testCanadianCaloriesAreClosedByARuleStoppingAtTheMiddle() throws Exception {
		List<Element> rects = elements(parse(renderToString(CANADA_TEMPLATE, canadianPanel())), "rect");

		double shortest = canadaPanelWidth();
		for (Element rect : rects) {
			if (Math.abs(Double.parseDouble(rect.getAttribute("height")) - RULE_MEDIUM) < 0.001d) {
				shortest = Math.min(shortest, Double.parseDouble(rect.getAttribute("width")));
			}
		}
		Assert.assertEquals("The rule under the calories leaves the daily value header standing free",
				(canadaPanelWidth() - 2 * PAD) / 2, shortest, 0.01d);
	}

	@Test
	public void testCanadianSharedPercentageIsPrintedBetweenTheTwoLinesItCovers() throws Exception {
		Document panel = parse(renderToString(CANADA_TEMPLATE, canadianPanel()));

		double saturated = Double.parseDouble(findText(panel, "saturés").getAttribute("y"));
		double trans = Double.parseDouble(findText(panel, "+ trans").getAttribute("y"));
		double percent = Double.parseDouble(findText(panel, "5%").getAttribute("y"));

		Assert.assertTrue("A percentage covering saturated and trans fat sits between their two lines",
				(percent > saturated) && (percent < trans));
	}

	@Test
	public void testCanadianPercentagesStayInTheBodyFace() throws Exception {
		Document panel = parse(renderToString(CANADA_TEMPLATE, canadianPanel()));

		Assert.assertEquals("The Canadian panel leaves every percentage in the regular weight", "",
				findText(panel, "10%").getAttribute("font-weight"));
	}

	@Test
	public void testCanadianFootnoteSetsItsVerdictsInTheHeavyFace() throws Exception {
		String svg = renderToString(CANADA_TEMPLATE, canadianPanel());

		Assert.assertTrue("The little/lot rule states its two verdicts in bold",
				svg.contains("font-weight=\"bold\">peu</tspan>") && svg.contains("font-weight=\"bold\">beaucoup</tspan>"));
	}

	@Test
	public void testBilingualCanadianPanelStatesEveryFixedWordingTwice() throws Exception {
		Document panel = parse(renderToString(CANADA_TEMPLATE, bilingualCanadianPanel()));

		Assert.assertNotNull("A bilingual panel is titled in both official languages", findText(panel, "Nutrition Facts"));
		Assert.assertNotNull(findText(panel, "Valeur nutritive"));
		Assert.assertNotNull("Its serving is stated once per language", findText(panel, "Per 1 tasse"));
		Assert.assertNotNull(findText(panel, "Pour 1 tasse"));
		Assert.assertNotNull("So is its daily value header", findText(panel, "% Daily Value*"));
		Assert.assertNotNull(findText(panel, "% valeur quotidienne*"));
		Assert.assertNotNull("And so is its footnote", findText(panel, ENGLISH_CANADIAN_FOOTNOTE.substring(0, 12)));
	}

	@Test
	public void testCanadianPanelIsWidenedToFitItsDailyValueHeader() throws Exception {
		Element french = parse(renderToString(CANADA_TEMPLATE, canadianPanel())).getDocumentElement();
		Element bilingual = parse(renderToString(CANADA_TEMPLATE, bilingualCanadianPanel())).getDocumentElement();

		Assert.assertEquals("The header shares the line of the calories and must never be printed over them", CANADA_PANEL_WIDTH,
				french.getAttribute("width"));
		Assert.assertEquals("A bilingual panel states that same header within its own minimum width", CANADA_BILINGUAL_PANEL_WIDTH,
				bilingual.getAttribute("width"));
	}

	@Test
	public void testCanadianLinearPanelRunsItsHeadingIntoTheSentence() throws Exception {
		Document panel = parse(renderToString(CANADA_LINEAR_TEMPLATE, canadianPanel()));

		Assert.assertTrue("The title, the serving and the calories open the same sentence",
				findText(panel, "Valeur nutritive").getTextContent().startsWith("Valeur nutritive Pour 1 tasse (250 mL): Calories 230,"));
	}

	@Test
	public void testCanadianLinearPanelExplainsItsPercentages() throws Exception {
		String svg = renderToString(CANADA_LINEAR_TEMPLATE, canadianPanel());

		Assert.assertTrue("Canada states the bare percentage", svg.contains("(10%)"));
		Assert.assertNotNull("and explains it at the foot of the panel", findText(parse(svg), "% = % valeur quotidienne"));
	}

	@Test
	public void testCanadianLinearPanelStatesItsNutrientsInTheFourLinesOfTheRegulation() throws Exception {
		Document panel = parse(renderToString(CANADA_LINEAR_TEMPLATE, canadianPanel()));

		Assert.assertTrue("The fat line closes on the cholesterol, whatever the order of the table",
				findText(panel, "Lipides").getTextContent().startsWith("Lipides 8 g (10%), saturés 1 g, + trans 0 g (5%)"));
		Assert.assertNotNull("The sodium closes the line of the carbohydrates and the protein", findText(panel, "Sodium"));
		Assert.assertNotNull("The minerals take a line of their own", findText(panel, "Potassium"));
	}

	@Test
	public void testSupplementedPanelDeclaresWhatTheFoodWasSupplementedWith() throws Exception {
		Document panel = parse(renderToString(CANADA_SUPPLEMENTED_TEMPLATE, supplementedCanadianPanel()));

		Assert.assertNotNull("A supplemented food carries the title the regulation words", findText(panel, "Info-aliment"));
		Assert.assertNotNull("Its added ingredients are declared under their own caption", findText(panel, "Supplémenté en"));
		Assert.assertNotNull(findText(panel, "Caféine"));
		Assert.assertNotNull(findText(panel, "Vitamine B6"));
		Assert.assertNotNull("And the block closes on the note stating what the amounts cover",
				findText(panel, SUPPLEMENT_MARK + "Comprend les quantités"));
	}

	@Test
	public void testSupplementedBlockClosesThePanelUnderTheFootnote() throws Exception {
		Document panel = parse(renderToString(CANADA_SUPPLEMENTED_TEMPLATE, supplementedCanadianPanel()));

		double footNote = Double.parseDouble(findText(panel, "* 5 % ou moins").getAttribute("y"));
		double caption = Double.parseDouble(findText(panel, "Supplémenté en").getAttribute("y"));

		Assert.assertTrue("The little/lot rule reads on the table and not on the added ingredients", caption > footNote);
	}

	@Test
	public void testSupplementedTitleIsSetOnASingleLinePerLanguage() throws Exception {
		Document panel = parse(renderToString(CANADA_SUPPLEMENTED_TEMPLATE, supplementedCanadianPanel()));

		Assert.assertEquals("A title broken in two reads as two titles", "Info-aliment supplémenté",
				findText(panel, "Info-aliment").getTextContent());
	}

	@Test
	public void testSupplementedCaptionIsSetLikeADeclarationOfTheTable() throws Exception {
		Element caption = findText(parse(renderToString(CANADA_SUPPLEMENTED_TEMPLATE, supplementedCanadianPanel())), "Supplémenté en");

		Assert.assertEquals("The caption carries the weight the regulation gives the Sodium line", "8", caption.getAttribute("font-size"));
		Assert.assertEquals("and is set in bold", BOLD, caption.getAttribute("font-weight"));
		Assert.assertTrue("It opens the block the closing note explains", caption.getTextContent().endsWith(SUPPLEMENT_MARK));
	}

	@Test
	public void testSupplementedNutrientKeptInTheTableCarriesTheMark() throws Exception {
		Document panel = parse(renderToString(CANADA_SUPPLEMENTED_TEMPLATE, supplementedCanadianPanel()));

		Assert.assertEquals("A supplemented nutrient the regulation keeps on its line states what its amount covers",
				"Potassium 235 mg" + SUPPLEMENT_MARK, findText(panel, "Potassium").getTextContent());
		Assert.assertEquals("A nutrient the food was not supplemented with carries nothing", "Calcium 260 mg",
				findText(panel, "Calcium").getTextContent());
		Assert.assertNotNull("And the note is tied to the marks by the same reference",
				findText(panel, SUPPLEMENT_MARK + "Comprend les quantités"));
	}

	@Test
	public void testStandardCanadianPanelIgnoresTheSupplementalBlock() throws Exception {
		String svg = renderToString(CANADA_TEMPLATE, supplementedCanadianPanel());

		Assert.assertFalse("The standard table declares no supplemental ingredient, whatever the product carries",
				svg.contains("Supplémenté en") || svg.contains("Caféine"));
		Assert.assertFalse("and marks no amount, having no note to tie the mark to", svg.contains(SUPPLEMENT_MARK));
	}

	@Test
	public void testCanadianHorizontalPanelIsDrawnAcrossThePackage() throws Exception {
		Element svg = parse(renderToString(CANADA_HORIZONTAL_TEMPLATE, bilingualCanadianPanel())).getDocumentElement();

		Assert.assertEquals("A horizontal panel runs across the width", "504pt", svg.getAttribute("width"));
	}

	private NutritionFactsData canadianPanel() {
		return canadianPanel(canadianLabels(false), NutritionFactsTranslation.none(), CANADIAN_FOOTNOTE);
	}

	/** The same panel stating both official languages, which is what B.01.454 asks for. */
	private NutritionFactsData bilingualCanadianPanel() {
		NutritionFactsTranslation secondary = new NutritionFactsTranslation(canadianLabels(false), null, CANADIAN_FOOTNOTE);
		return canadianPanel(canadianLabels(true), secondary, ENGLISH_CANADIAN_FOOTNOTE);
	}

	private NutritionFactsData canadianPanel(Map<String, String> labels, NutritionFactsTranslation secondary, String footNote) {
		return new NutritionFactsData("canada", "CA", new NutritionFactsServing(null, "1 tasse (250 mL)"),
				line("US_ENER-E14", "Calories", "230", null, 1, true),
				List.of(line("FAT", "Lipides", "8 g", "10%", 1, true), line("FASAT", "saturés", "1 g", "5%", 2, false, true),
						line("FATRN", "+ trans", "0 g", null, 2, false), line("NA", "Sodium", "160 mg", "7%", 1, true)),
				List.of(line("K", "Potassium", "235 mg", "5%", 1, false), line("CA", "Calcium", "260 mg", "20%", 1, false)), List.of(), footNote, "",
				labels, secondary);
	}

	/**
	 * The same panel, of a food supplemented with caffeine, vitamin B6 and potassium. The potassium
	 * is what the regulation keeps on its own line although the food was supplemented with it.
	 */
	private NutritionFactsData supplementedCanadianPanel() {
		Map<String, String> labels = canadianLabels(false);
		NutritionFactsData panel = canadianPanel();
		return new NutritionFactsData("canadaSupplemented", "CA", panel.serving(), panel.calories(), panel.nutrients(),
				List.of(line("K", "Potassium", "235 mg", "5%", 1, false, false, true), line("CA", "Calcium", "260 mg", "20%", 1, false)),
				List.of(line("CAFFN", "Caféine", "100 mg", null, 1, false), line("VITB6-", "Vitamine B6", "1,3 mg", "76%", 1, false)),
				panel.footNote(), "", labels, NutritionFactsTranslation.none());
	}

	/** Width the Canadian panels are drawn at, as a figure the geometry of a rule can be checked against. */
	private double canadaPanelWidth() {
		return Double.parseDouble(CANADA_PANEL_WIDTH.replace("pt", ""));
	}

	private Map<String, String> canadianLabels(boolean english) {
		Map<String, String> labels = new LinkedHashMap<>();
		labels.put("title", english ? "Nutrition Facts" : "Valeur nutritive");
		labels.put("servingSize", english ? "Per" : "Pour");
		labels.put("dailyValue", english ? "% Daily Value*" : "% valeur quotidienne*");
		labels.put("dailyValueSuffix", "");
		labels.put("linearLegend", english ? "% = % Daily Value" : "% = % valeur quotidienne");
		labels.put("footNoteEmphasis", english ? "a little,a lot" : "peu,beaucoup");
		labels.put("dailyValueShort", english ? "%DV*" : "%VQ*");
		labels.put("supplementedTitle", english ? "Supplemented Food Facts" : "Info-aliment supplémenté");
		labels.put("supplementedWith", english ? "Supplemented with" : "Supplémenté en");
		labels.put("supplementedNote", english ? "Includes naturally occurring and supplemental amounts"
				: "Comprend les quantités naturelles et supplémentées");
		return labels;
	}

	/** Tells a drawn line of the disclaimer from any other text of the panel. */
	private boolean isFootnoteLine(Element text) {
		return (text.getTextContent().length() > 20) && FOOTNOTE.contains(text.getTextContent());
	}

	/** Width a line of text takes, counted with the average glyph of Arial at that size. */
	private double estimatedWidth(Element text) {
		return text.getTextContent().length() * Double.parseDouble(text.getAttribute("font-size")) * 0.43d;
	}

	private double textStart(Document panel, String label) {
		return Double.parseDouble(findText(panel, label).getAttribute("x"));
	}

	/** Every text of a panel read as one sentence, no-break spaces read as spaces. */
	private String sentence(String svg) throws Exception {
		StringBuilder sentence = new StringBuilder();
		for (Element text : elements(parse(svg), "text")) {
			sentence.append(text.getTextContent().trim()).append(' ');
		}
		return sentence.toString().replace(NBSP, " ").replaceAll("\\s+", " ").trim();
	}

	private double y(Element text) {
		return Double.parseDouble(text.getAttribute("y"));
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
				List.of(), FOOTNOTE,
				"Not a significant source of other nutrients.", panelLabels(), NutritionFactsTranslation.none());
	}

	/** A US panel with the wordings the FDA gives the linear format, Includes Added Sugars embedding its amount. */
	private NutritionFactsData usLinearPanel() {
		Map<String, String> labels = panelLabels();
		labels.put("servingsShort", "Servings:");
		labels.put("servingSizeShort", "Serv. size:");
		List<NutritionFactsLine> nutrients = List.of(line("FAT", "Total Fat", "8g", "10%", 1, true), line("FASAT", "Saturated Fat", "1g", "5%", 2, false),
				line("CHO-", "Total Carbohydrate", "Total Carb.", "37g", "13%", 1, true), line("SUGAR", "Total Sugars", "12g", null, 2, false),
				new NutritionFactsLine("SUGAD", "Includes 10g Added Sugars", "Incl. 10g Added Sugars", "Includes Added Sugars", "Incl. Added Sugars",
						"10g", null, "20%", null, 3, false, true, true, false, false));
		NutritionFactsData standard = panelData();
		return new NutritionFactsData("linear", "US", standard.serving(), standard.calories(), nutrients, standard.micronutrients(), List.of(),
				FOOTNOTE, "", labels, NutritionFactsTranslation.none());
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
		labels.put("dailyValueSuffix", "DV");
		labels.put("italicWord", "Trans");
		return labels;
	}

	private NutritionFactsLine line(String nutCode, String label, String value, String dailyValue, int indentLevel, boolean bold) {
		return line(nutCode, label, value, dailyValue, indentLevel, bold, false);
	}

	private NutritionFactsLine line(String nutCode, String label, String value, String dailyValue, int indentLevel, boolean bold,
			boolean sharedDailyValue) {
		return line(nutCode, label, value, dailyValue, indentLevel, bold, sharedDailyValue, false);
	}

	private NutritionFactsLine line(String nutCode, String label, String value, String dailyValue, int indentLevel, boolean bold,
			boolean sharedDailyValue, boolean supplemental) {
		return line(nutCode, label, value, dailyValue, indentLevel, bold, sharedDailyValue, supplemental, value, dailyValue);
	}

	private NutritionFactsLine line(String nutCode, String label, String value, String dailyValue, int indentLevel, boolean bold,
			boolean sharedDailyValue, boolean supplemental, String valuePerContainer, String dailyValuePerContainer) {
		return new NutritionFactsLine(nutCode, label, label, label, label, value, valuePerContainer, dailyValue, dailyValuePerContainer,
				indentLevel, bold, dailyValue != null, false, sharedDailyValue, supplemental);
	}

	/** A line whose abbreviated wording differs from its full one, "Total Carb.". */
	private NutritionFactsLine line(String nutCode, String label, String abbreviation, String value, String dailyValue, int indentLevel,
			boolean bold) {
		return new NutritionFactsLine(nutCode, label, abbreviation, label, abbreviation, value, value, dailyValue, dailyValue, indentLevel, bold,
				dailyValue != null, false, false, false);
	}

}
