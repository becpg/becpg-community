/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilderFactory;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.template.BeCPGTemplateRenderService;
import fr.becpg.repo.template.TemplateRenderException;
import freemarker.cache.ClassTemplateLoader;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

/**
 * Unit tests of {@link ScoreMarkingRenderer}, rendering the shipped templates with FreeMarker
 * configured as the repository configures it.
 *
 * @author matthieu
 */
public class ScoreMarkingRendererTest {

	private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

	private static final String MTL_DETAILS = """
			{"code":"MTL","scale":"Traffic","class":"High","parts":[
			{"code":"ENER-KJO","value":1046,"unit":"kJ","share":12.45},
			{"code":"FAT","label":"Medium","value":10.24,"share":14.63},
			{"code":"FASAT","label":"High","value":6.5},
			{"code":"SUGAR","label":"Low","value":2}]}""";

	private static final String AMBER = "#f0a30a";

	private static final String RED = "#d0021b";

	private static final String GREEN = "#008a3e";

	private ScoreMarkingRenderer renderer;

	@Before
	public void setUp() {
		renderer = new ScoreMarkingRenderer(new ClasspathTemplateRenderService());
	}

	private String renderMtl() {
		return renderer.render(ScoreContext.parse(MTL_DETAILS), Locale.ENGLISH).orElseThrow();
	}

	private static Document parse(String svg) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void testTrafficMarkingIsWellFormedSvg() throws Exception {
		Element svg = parse(renderMtl()).getDocumentElement();

		assertEquals("svg", svg.getLocalName());
		assertEquals(SVG_NAMESPACE, svg.getNamespaceURI());
	}

	@Test
	public void testMarkingIsSizedInPointsOneLightPerPart() throws Exception {
		Element svg = parse(renderMtl()).getDocumentElement();

		assertEquals("268pt", svg.getAttribute("width"));
		assertEquals("78pt", svg.getAttribute("height"));
		assertEquals("0 0 268 78", svg.getAttribute("viewBox"));
		assertEquals(4, partGroups(svg).size());
	}

	/** The groups placing each part of the marking side by side. */
	private static List<Element> partGroups(Element svg) {
		List<Element> groups = new ArrayList<>();
		NodeList all = svg.getElementsByTagNameNS(SVG_NAMESPACE, "g");
		for (int i = 0; i < all.getLength(); i++) {
			Element group = (Element) all.item(i);
			if (group.hasAttribute("transform")) {
				groups.add(group);
			}
		}
		return groups;
	}

	@Test
	public void testEachLightTakesTheColourOfItsVerdict() {
		String svg = renderMtl();

		assertTrue(svg.contains(AMBER));
		assertTrue(svg.contains(RED));
		assertTrue(svg.contains(GREEN));
	}

	@Test
	public void testLightStatesNutrientAmountVerdictAndShare() {
		String svg = renderMtl();

		assertTrue(svg.contains(">Fat</text>"));
		assertTrue(svg.contains(">10.24g</text>"));
		assertTrue(svg.contains(">MED</text>"));
		assertTrue(svg.contains(">15% RI</text>"));
	}

	@Test
	public void testEnergyIsStatedOnAPlainPanelWithoutVerdict() throws Exception {
		Element energy = partGroups(parse(renderMtl()).getDocumentElement()).get(0);

		assertEquals("Energy1046kJ12%RI", energy.getTextContent().replaceAll("\\s+", ""));
		assertFalse(energy.getTextContent().contains("HIGH"));
	}

	@Test
	public void testClipPathsAreUniqueWithinTheMarking() {
		String svg = renderMtl();

		assertTrue(svg.contains("id=\"light1\""));
		assertTrue(svg.contains("id=\"light3\""));
		assertFalse("The energy panel is not clipped", svg.contains("id=\"light0\""));
	}

	@Test
	public void testTranslatedNameTooLongForOneLineIsWrapped() {
		String svg = renderer.render(ScoreContext.parse(MTL_DETAILS), Locale.FRENCH).orElseThrow();

		assertTrue(svg.contains(">Mati\u00E8res</text>"));
		assertTrue(svg.contains(">grasses</text>"));
		assertTrue(svg.contains(">Sucres</text>"));
	}

	@Test
	public void testWordingIsEscaped() throws Exception {
		String details = "{\"code\":\"MTL\",\"scale\":\"Traffic\",\"parts\":[{\"code\":\"A<&>B\",\"label\":\"Low\"}]}";

		String svg = renderer.render(ScoreContext.parse(details), Locale.ENGLISH).orElseThrow();

		assertTrue(svg.contains("A&lt;&amp;&gt;B"));
		parse(svg);
	}

	@Test
	public void testMarkingAvoidsWhatBatikCannotRender() {
		String svg = renderMtl();

		assertFalse(svg.contains("<!DOCTYPE"));
		assertFalse(svg.contains("foreignObject"));
		assertFalse(svg.contains("xlink:href"));
		assertFalse("dominant-baseline is not reliable in Batik", svg.contains("dominant-baseline"));
	}

	private String renderClass(String code, String scoreClass, Locale locale) throws Exception {
		String details = "{\"code\":\"" + code + "\",\"version\":\"2024\",\"scale\":\"Letter\",\"class\":\"" + scoreClass + "\"}";
		String svg = renderer.render(ScoreContext.parse(details), locale).orElseThrow();
		parse(svg);
		return svg;
	}

	@Test
	public void testEcoVadisMedalStatesItsLevelYearAndPercentile() throws Exception {
		String svg = renderClass("ECOVADIS", "Gold", Locale.ENGLISH);

		assertTrue(svg.contains(">ecovadis</text>"));
		assertTrue(svg.contains(">GOLD</text>"));
		assertTrue(svg.contains(">2024</text>"));
		assertTrue(svg.contains(">TOP 5%</text>"));
		assertTrue("the gold metal", svg.contains("#b4862a"));
	}

	@Test
	public void testEcoVadisLevelOutsideTheScaleIsDrawnNeutral() throws Exception {
		String svg = renderClass("ECOVADIS", "Committed", Locale.ENGLISH);

		assertTrue(svg.contains(">COMMITTED</text>"));
		assertFalse("no percentile for an unknown level", svg.contains(">TOP "));
	}

	@Test
	public void testEcoBeautyScoreGrowsTheReachedGrade() throws Exception {
		String svg = renderClass("ECOBEAUTYSCORE", "C", Locale.FRENCH);

		assertTrue(svg.contains("font-size=\"19\" font-weight=\"bold\" fill=\"#ffffff\">C</text>"));
		assertTrue(svg.contains("font-size=\"12\" font-weight=\"bold\" fill=\"#ffffff\">A</text>"));
		assertTrue(svg.contains(">Impact environnemental</text>"));
	}

	@Test
	public void testGreenImpactIndexStampsTheReachedGradeOnALeaf() throws Exception {
		String svg = renderClass("GREENIMPACTINDEX", "B", Locale.ENGLISH);

		assertTrue(svg.contains(">GREEN IMPACT INDEX</text>"));
		assertTrue("one leaf", svg.split("<path ").length == 2);
		assertTrue("four pips", svg.split("<circle ").length == 5);
		assertTrue(svg.contains(">Environmental impact of the product</text>"));
	}

	@Test
	public void testScoreEnteredByHandIsDrawnFromItsLineAndDefinition() {
		NodeRef definitionNodeRef = new NodeRef("workspace://SpacesStore/ecovadis");
		ScoreDefinitionItem definition = new ScoreDefinitionItem();
		definition.setNodeRef(definitionNodeRef);
		definition.setCode("ECOVADIS");
		definition.setScale("Grade");
		definition.setVersion("2024");
		ScoreDefinitionService definitions = mock(ScoreDefinitionService.class);
		when(definitions.getScoreDefinitions()).thenReturn(List.of(definition));

		RegulatoryScoreListDataItem line = new RegulatoryScoreListDataItem();
		line.setScoreDef(definitionNodeRef);
		line.setScoreClass("Silver");
		line.setIsManual(true);

		RenderedScoreMarking marking = new ScoreMarkingRenderer(new ClasspathTemplateRenderService(), definitions).render(line, Locale.ENGLISH).orElseThrow();

		assertEquals("ECOVADIS", marking.code());
		assertTrue(marking.svg().contains(">SILVER</text>"));
		assertTrue(marking.svg().contains(">2024</text>"));
	}

	@Test
	public void testScoreEnteredByHandWithoutVerdictHasNoMarking() {
		RegulatoryScoreListDataItem line = new RegulatoryScoreListDataItem();
		line.setScoreDef(new NodeRef("workspace://SpacesStore/ecovadis"));

		assertEquals(Optional.empty(), new ScoreMarkingRenderer(new ClasspathTemplateRenderService(), mock(ScoreDefinitionService.class)).render(line, Locale.ENGLISH));
	}

	@Test
	public void testScoreHoldingOnlyAClassIsMarked() {
		ScoreContext score = ScoreContext.parse("{\"code\":\"ECOBEAUTYSCORE\",\"scale\":\"Letter\",\"class\":\"A\",\"parts\":[]}");

		assertTrue(renderer.render(score, Locale.ENGLISH).isPresent());
	}

	@Test
	public void testScoreWithoutPartsHasNoMarking() {
		ScoreContext score = ScoreContext.parse("{\"code\":\"MTL\",\"scale\":\"Traffic\",\"parts\":[]}");

		assertEquals(Optional.empty(), renderer.render(score, Locale.ENGLISH));
	}

	@Test
	public void testScaleWithoutTemplateIsLeftToTheBrowser() {
		ScoreContext score = ScoreContext.parse("{\"code\":\"CUSTOM\",\"scale\":\"Custom\",\"class\":\"B\",\"parts\":[{\"code\":\"FAT\"}]}");

		assertEquals(Optional.empty(), renderer.render(score, Locale.ENGLISH));
	}

	/** One score of every scale the reference data ships, as the formulation details them. */
	private static final List<String> SHIPPED_SCALES = List.of(
			"{\"code\":\"NUTRISCORE\",\"scale\":\"Letter\",\"class\":\"E\",\"value\":29,\"version\":\"2017\",\"parts\":[{\"code\":\"FASAT\",\"value\":9.5,\"share\":27.3,\"contribution\":9}]}",
			"{\"code\":\"NUTRIGRADE\",\"scale\":\"Letter\",\"class\":\"D\",\"value\":3,\"parts\":[{\"code\":\"SUGAR\",\"label\":\"D\",\"value\":140}]}",
			"{\"code\":\"PPWR\",\"scale\":\"Letter\",\"class\":\"NR\",\"value\":40,\"unit\":\"%\",\"parts\":[]}",
			"{\"code\":\"PLANETSCORE_PESTICIDES\",\"scale\":\"Gauge\",\"class\":\"E\",\"value\":0.495,\"parts\":[]}",
			"{\"code\":\"PLANETSCORE\",\"scale\":\"Mark\",\"class\":\"D\",\"parts\":[{\"code\":\"PLANETSCORE_PESTICIDES\",\"label\":\"E\"},{\"code\":\"PLANETSCORE_CLIMAT\",\"label\":\"B\"}]}",
			"{\"code\":\"HSR\",\"scale\":\"Stars\",\"class\":\"1.5\",\"value\":1.5,\"unit\":\"stars\",\"parts\":[]}",
			"{\"code\":\"WARNINGS_MX\",\"scale\":\"Warnings\",\"class\":\"2\",\"value\":2,\"parts\":[{\"code\":\"SUGAR\",\"label\":\"EXCESO AZ\u00daCARES\"},{\"code\":\"FASAT\",\"label\":\"EXCESO GRASAS SATURADAS\"}]}",
			"{\"code\":\"WARNINGS_BR\",\"scale\":\"Warnings\",\"class\":\"1\",\"value\":1,\"parts\":[{\"code\":\"SUGAR\",\"label\":\"ALTO EM A\u00c7\u00daCAR ADICIONADO\"}]}",
			"{\"code\":\"WARNINGS_IL\",\"scale\":\"Warnings\",\"class\":\"1\",\"value\":1,\"parts\":[{\"code\":\"NA\",\"label\":\"HIGH SODIUM\"}]}",
			"{\"code\":\"NUTRINFORM\",\"scale\":\"Numeric\",\"parts\":[{\"code\":\"ENER-E14\",\"label\":\"Energy\",\"value\":57.3,\"unit\":\"kJ\",\"share\":2.87},{\"code\":\"FASAT\",\"label\":\"Saturates\",\"value\":2.85,\"share\":14.25}]}",
			"{\"code\":\"FSAOFCOM\",\"scale\":\"Numeric\",\"class\":\"HFSS\",\"value\":19,\"unit\":\"points\",\"parts\":[{\"code\":\"SUGAR\",\"value\":32,\"share\":30.4,\"contribution\":7}]}",
			"{\"code\":\"EF31\",\"scale\":\"Numeric\",\"value\":0.052167317738889205,\"unit\":\"Pt\",\"parts\":[{\"code\":\"WATER_USE\",\"value\":0.0558,\"unit\":\"m3 world eq/kg\",\"share\":0.79}]}",
			"{\"code\":\"NOVA\",\"scale\":\"Grade\",\"class\":\"4\",\"value\":4,\"parts\":[]}",
			"{\"code\":\"YUKA\",\"scale\":\"Grade\",\"value\":30,\"unit\":\"/100\",\"parts\":[{\"code\":\"ADDITIVES\",\"value\":100,\"share\":100}]}");

	@Test
	public void testEveryShippedScaleIsMarkedWithWellFormedSvg() throws Exception {
		for (String details : SHIPPED_SCALES) {
			for (Locale locale : List.of(Locale.ENGLISH, Locale.FRENCH)) {
				String svg = renderer.render(ScoreContext.parse(details), locale).orElseThrow(() -> new AssertionError("No marking for " + details));
				Element root = parse(svg).getDocumentElement();
				assertEquals(details, SVG_NAMESPACE, root.getNamespaceURI());
				assertTrue(details, root.getAttribute("width").endsWith("pt"));
				assertFalse(details, svg.contains("dominant-baseline"));
				assertFalse(details, svg.contains("foreignObject"));
				assertFalse("a figure is never written with a decimal comma", svg.matches("(?s).*=\"[0-9]+,[0-9]+\".*"));
			}
		}
	}

	private String renderShipped(String code, Locale locale) {
		for (String details : SHIPPED_SCALES) {
			if (details.contains("\"code\":\"" + code + "\",\"scale\"")) {
				return renderer.render(ScoreContext.parse(details), locale).orElseThrow();
			}
		}
		throw new IllegalArgumentException(code);
	}

	@Test
	public void testLetterStripGrowsTheReachedClassUnderTheNameOfTheScheme() {
		String svg = renderShipped("NUTRISCORE", Locale.ENGLISH);

		assertTrue(svg.contains(">NUTRI-SCORE</text>"));
		assertTrue("the reached class is outlined", svg.contains("fill=\"#ff0100\" stroke=\"#333333\""));
		assertTrue("the other classes are faded", svg.contains("fill=\"#00853f\" opacity=\"0.45\""));
	}

	@Test
	public void testLetterThemeNamingItsOwnClassesGradesOnThem() {
		String svg = renderShipped("PPWR", Locale.ENGLISH);

		assertTrue(svg.contains(">NR</text>"));
		assertFalse("the PPWR has no E grade", svg.contains(">E</text>"));
	}

	@Test
	public void testNutriGradeIsStampedAsATag() {
		String svg = renderShipped("NUTRIGRADE", Locale.ENGLISH);

		assertTrue(svg.contains(">NUTRI-GRADE</text>"));
		assertTrue("four pips, one per grade", svg.split("<circle ").length == 5);
	}

	@Test
	public void testWarningsDrawOneMarkPerPartInTheShapeOfTheirCountry() {
		String mexico = renderShipped("WARNINGS_MX", Locale.ENGLISH);
		assertEquals("one octagon per warning", 3, mexico.split("<polygon ").length);
		assertTrue(mexico.contains(">SATURADAS</text>"));

		assertTrue("Israel stamps a red circle", renderShipped("WARNINGS_IL", Locale.ENGLISH).contains("<circle "));
		assertTrue("Brazil prints a magnifying glass", renderShipped("WARNINGS_BR", Locale.ENGLISH).contains("stroke-width=\"3\""));
	}

	@Test
	public void testNutrInformDrawsOneBatteryPerPartFilledToItsShare() {
		String svg = renderShipped("NUTRINFORM", Locale.ENGLISH);

		assertTrue(svg.contains(">14% RI</text>"));
		assertTrue("the battery of saturates is filled to 14.25%", svg.contains("width=\"5.1\""));
		assertFalse("no plate when the score states no value", svg.contains(">NUTRINFORM</text>"));
	}

	@Test
	public void testNumericScoreStatesItsValueUnitAndClass() {
		String fsa = renderShipped("FSAOFCOM", Locale.ENGLISH);
		assertTrue(fsa.contains(">19 points</text>"));
		assertTrue(fsa.contains(">HFSS</text>"));

		assertTrue("three significant digits under a hundred", renderShipped("EF31", Locale.ENGLISH).contains(">0.0522 Pt</text>"));
		assertTrue("in the locale of the marking", renderShipped("EF31", Locale.FRENCH).contains(">0,0522 Pt</text>"));
	}

	@Test
	public void testStarsAreFilledByHalfStarSteps() {
		String svg = renderShipped("HSR", Locale.ENGLISH);

		assertTrue(svg.contains(">HEALTH STAR RATING</text>"));
		assertEquals("one half star", 1, svg.split("clip-path=\"url\\(#halfStar").length - 1);
		assertTrue(svg.contains(">1.5</text>"));
	}

	@Test
	public void testGradeWithoutClassStatesItsValue() {
		assertTrue(renderShipped("YUKA", Locale.ENGLISH).contains(">30/100</text>"));
		assertTrue(renderShipped("NOVA", Locale.ENGLISH).contains("fill=\"#ff0100\""));
	}

	@Test
	public void testMarkListsTheAxesItGrades() {
		String svg = renderShipped("PLANETSCORE", Locale.FRENCH);

		assertTrue(svg.contains(">Pesticides</text>"));
		assertTrue(svg.contains(">Climat</text>"));
		assertFalse("an axis without level is not listed", svg.contains(">Biodiversit"));
	}

	@Test
	public void testTemplateNamedAfterTheCodeComesBeforeTheScale() {
		assertEquals(List.of("score-mtl_portion.ftlx", "score-traffic.ftlx"), ScoreMarkingRenderer.candidateTemplates("MTL_PORTION", "Traffic"));
	}

	@Test
	public void testTemplateNamedAfterTheCodeIsUsedWhenPresent() {
		RecordingTemplateRenderService templates = new RecordingTemplateRenderService("score-mtl.ftlx");

		new ScoreMarkingRenderer(templates).render(ScoreContext.parse(MTL_DETAILS), Locale.ENGLISH);

		assertEquals("score-mtl.ftlx", templates.rendered);
	}

	@Test
	public void testBrokenTemplateCostsTheMarkingOnly() {
		ScoreMarkingRenderer brokenRenderer = new ScoreMarkingRenderer(new RecordingTemplateRenderService("score-traffic.ftlx") {
			@Override
			public String render(String templateName, Locale locale, Map<String, Object> model) {
				throw new TemplateRenderException("Cannot render template: " + templateName);
			}
		});

		assertEquals(Optional.empty(), brokenRenderer.render(ScoreContext.parse(MTL_DETAILS), Locale.ENGLISH));
	}

	@Test
	public void testScoreLineIsRenderedFromItsStoredDetail() {
		RegulatoryScoreListDataItem line = new RegulatoryScoreListDataItem();
		line.setDetails(MTL_DETAILS);

		RenderedScoreMarking marking = renderer.render(line, Locale.ENGLISH).orElseThrow();

		assertEquals("MTL", marking.code());
		assertEquals("High", marking.scoreClass());
		assertTrue(marking.svg().startsWith("<svg"));
	}

	@Test
	public void testScoreIsPickedByItsCodeAmongTheLines() {
		RegulatoryScoreListDataItem nutriScore = new RegulatoryScoreListDataItem();
		nutriScore.setDetails("{\"code\":\"NUTRISCORE\",\"scale\":\"Letter\",\"class\":\"B\",\"parts\":[{\"code\":\"FAT\"}]}");
		RegulatoryScoreListDataItem trafficLights = new RegulatoryScoreListDataItem();
		trafficLights.setDetails(MTL_DETAILS);

		RenderedScoreMarking marking = renderer.renderScore(List.of(nutriScore, trafficLights), "MTL", Locale.ENGLISH).orElseThrow();

		assertEquals("MTL", marking.code());
		assertTrue(marking.svg().contains(">Fat</text>"));
	}

	@Test
	public void testMissingScoreHasNoMarking() {
		RegulatoryScoreListDataItem trafficLights = new RegulatoryScoreListDataItem();
		trafficLights.setDetails(MTL_DETAILS);

		assertEquals(Optional.empty(), renderer.renderScore(List.of(trafficLights), "NUTRIGRADE", Locale.ENGLISH));
		assertEquals(Optional.empty(), renderer.renderScore(null, "MTL", Locale.ENGLISH));
	}

	@Test
	public void testUnreadableOrMissingDetailHasNoMarking() {
		assertEquals(Optional.empty(), renderer.renderDetails("{not json", Locale.ENGLISH));
		assertEquals(Optional.empty(), renderer.renderDetails(" ", Locale.ENGLISH));
		assertEquals(Optional.empty(), renderer.renderDetails(null, Locale.ENGLISH));
	}

	/** FreeMarker set up as FreemarkerTemplateRenderServiceImpl sets it, on the classpath only. */
	private static class ClasspathTemplateRenderService implements BeCPGTemplateRenderService {

		private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);

		ClasspathTemplateRenderService() {
			configuration.setTemplateLoader(new ClassTemplateLoader(ScoreMarkingRendererTest.class, "/beCPG/templates"));
			configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
			configuration.setNumberFormat("computer");
			configuration.setRecognizeStandardFileExtensions(true);
			configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			configuration.setLogTemplateExceptions(false);
			configuration.setLocalizedLookup(true);
			configuration.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
		}

		@Override
		public String render(String templateName, Locale locale, Map<String, Object> model) {
			StringWriter writer = new StringWriter();
			render(templateName, locale, model, writer);
			return writer.toString();
		}

		@Override
		public void render(String templateName, Locale locale, Map<String, Object> model, Writer writer) {
			try {
				configuration.getTemplate(templateName, locale).process(model, writer);
			} catch (IOException | TemplateException e) {
				throw new TemplateRenderException("Cannot render template: " + templateName, e);
			}
		}

		@Override
		public boolean exists(String templateName, Locale locale) {
			try {
				configuration.getTemplate(templateName, locale);
				return true;
			} catch (TemplateNotFoundException e) {
				return false;
			} catch (IOException e) {
				throw new TemplateRenderException("Cannot read template: " + templateName, e);
			}
		}

		@Override
		public void clearCache() {
			configuration.clearTemplateCache();
		}
	}

	/** Knows a single template and records which one it was asked to render. */
	private static class RecordingTemplateRenderService extends ClasspathTemplateRenderService {

		private final String existingTemplate;

		private String rendered;

		RecordingTemplateRenderService(String existingTemplate) {
			this.existingTemplate = existingTemplate;
		}

		@Override
		public String render(String templateName, Locale locale, Map<String, Object> model) {
			rendered = templateName;
			return "<svg/>";
		}

		@Override
		public boolean exists(String templateName, Locale locale) {
			return existingTemplate.equals(templateName);
		}
	}

}
