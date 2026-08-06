package fr.becpg.test.repo.product.nutrient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.NutDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsData;
import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsDataBuilder;
import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsLabelResolver;
import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsLine;
import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsOptions;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * Checks that the model handed over to a nutrition facts template is complete, ordered and already
 * formatted, so that a template never has to compute or look anything up.
 */
public class NutritionFactsDataBuilderTest {

	private static final String NUTRITION_FACTS_BUNDLE = "alfresco/module/becpg-plm-core/messages/nutrition-facts";

	private static final String SUPPORTED_LOCALES = "en_US, fr_FR";

	private static final String VERTICAL_FORMAT = "vertical";

	private static final String US_REGULATION_KEY = NutritionFactsOptions.US_REGULATION_KEY;

	private static final String CA_REGULATION_KEY = NutritionFactsOptions.CA_REGULATION_KEY;

	private static final NodeRef PRODUCT_NODE_REF = new NodeRef("workspace://SpacesStore/product");

	private NutritionFactsDataBuilder builder;

	private NodeService mlNodeService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		MLTextHelper.setSupportedLocales(SUPPORTED_LOCALES);
		I18NUtil.registerResourceBundle(NUTRITION_FACTS_BUNDLE);

		mlNodeService = Mockito.mock(NodeService.class);
		alfrescoRepository = Mockito.mock(AlfrescoRepository.class);
		builder = new NutritionFactsDataBuilder(mlNodeService, alfrescoRepository);
	}

	@After
	public void tearDown() {
		MLTextHelper.setSupportedLocales(null);
	}

	@Test
	public void testNutrientsAreOrderedAndNamedAsTheRegulationRequires() {

		NutritionFactsData data = build(standardProduct());

		Assert.assertEquals("Calories", data.calories().label());
		Assert.assertEquals(List.of("Total Fat", "Saturated Fat", "Cholesterol", "Includes 10g Added Sugars"),
				data.nutrients().stream().map(NutritionFactsLine::label).toList());
		Assert.assertEquals(List.of("Vitamin D"), data.micronutrients().stream().map(NutritionFactsLine::label).toList());
	}

	@Test
	public void testIndentationAndBoldFollowTheRegulation() {

		NutritionFactsData data = build(standardProduct());

		NutritionFactsLine totalFat = lineOf(data, "Total Fat");
		Assert.assertEquals(1, totalFat.indentLevel());
		Assert.assertTrue("Total Fat is a bold, top level nutrient", totalFat.bold());

		NutritionFactsLine saturatedFat = lineOf(data, "Saturated Fat");
		Assert.assertEquals(2, saturatedFat.indentLevel());
		Assert.assertFalse("An indented nutrient is never bold", saturatedFat.bold());

		Assert.assertEquals("Added sugars sits two levels in, under total sugars", 3, lineOf(data, "Includes 10g Added Sugars").indentLevel());
	}

	@Test
	public void testValuesAndDailyValuesAreAlreadyFormatted() {

		NutritionFactsData data = build(standardProduct());

		NutritionFactsLine totalFat = lineOf(data, "Total Fat");
		Assert.assertEquals("8g", totalFat.value());
		Assert.assertEquals("10%", totalFat.dailyValuePercent());
		Assert.assertTrue(totalFat.hasDailyValue());
	}

	@Test
	public void testMandatoryNutrientStaysOnThePanelAtZero() {

		ProductData product = new ProductData();
		product.setNodeRef(PRODUCT_NODE_REF);
		product.setNutList(List.of(nutListItem("CHOL-", "Cholesterol", 0d, 0d)));

		NutritionFactsData data = build(product);

		Assert.assertEquals("The FDA requires the cholesterol line even at zero", 1, data.nutrients().size());
		Assert.assertEquals("Cholesterol", data.nutrients().get(0).label());
	}

	@Test
	public void testOptionalNutrientAtZeroIsDropped() {

		ProductData product = new ProductData();
		product.setNodeRef(PRODUCT_NODE_REF);
		product.setNutList(List.of(nutListItem("FAMSCIS", "Monounsaturated", 0d, 0d)));

		Assert.assertTrue(build(product).isEmpty());
	}

	@Test
	public void testOptionalNutrientIsKeptWhenExplicitlyAsked() {

		ProductData product = new ProductData();
		product.setNodeRef(PRODUCT_NODE_REF);
		product.setNutList(List.of(nutListItem("FAMSCIS", "Monounsaturated", 0d, 0d)));

		NutritionFactsData data = builder.build(product, Locale.US, VERTICAL_FORMAT,
				NutritionFactsOptions.forRegulation(NutritionFactsOptions.US_REGULATION_KEY).withOptionalNutrients());

		Assert.assertEquals(1, data.nutrients().size());
	}

	@Test
	public void testServingHeaderComesFromTheProduct() {

		mockMlProperty(PLMModel.PROP_PRODUCT_NUMBER_OF_SERVINGS, "8");
		mockMlProperty(PLMModel.PROP_PRODUCT_SERVING_SIZE_TEXT, "2/3 cup (55g)");

		NutritionFactsData data = build(standardProduct());

		Assert.assertEquals("8", data.serving().servingsPerContainer());
		Assert.assertEquals("2/3 cup (55g)", data.serving().servingSize());
		Assert.assertTrue(data.serving().hasServingsPerContainer());
	}

	@Test
	public void testPanelWordingIsSnapshotForTheTemplate() {

		NutritionFactsData data = build(standardProduct());

		Assert.assertEquals("Nutrition Facts", data.label(NutritionFactsLabelResolver.LABEL_TITLE));
		Assert.assertEquals("% Daily Value*", data.label(NutritionFactsLabelResolver.LABEL_DAILY_VALUE));
		Assert.assertTrue(data.footNote().startsWith("* The % Daily Value (DV)"));
		Assert.assertEquals("An unknown label must never leak a message key", "", data.label("noSuchLabel"));
	}

	@Test
	public void testNutrientUnknownToTheRegulationIsDropped() {

		ProductData product = new ProductData();
		product.setNodeRef(PRODUCT_NODE_REF);
		product.setNutList(List.of(nutListItem("NOT_A_NUT_CODE", "Exotic", 12d, 3d)));

		Assert.assertTrue(build(product).isEmpty());
	}

	@Test
	public void testAddedSugarsCarriesItsValueInsideTheRegulatedSentence() {

		NutritionFactsLine addedSugars = lineOf(build(standardProduct()), "Includes 10g Added Sugars");

		Assert.assertFalse("The sentence already carries the amount, a panel must not print it twice", addedSugars.printsValue());
		Assert.assertEquals("The amount stays available for the column formats", "10g", addedSugars.value());
		Assert.assertEquals("20%", addedSugars.dailyValuePercent());
	}

	@Test
	public void testCanadianSaturatedFatCarriesTheTransFatDailyValue() {

		ProductData product = new ProductData();
		product.setNodeRef(PRODUCT_NODE_REF);
		product.setNutList(List.of(nutListItem("FASAT", "Saturated", 1d, 5d, CA_REGULATION_KEY),
				nutListItem("FATRN", "Trans", 2d, 10d, CA_REGULATION_KEY)));

		NutritionFactsData data = builder.build(product, Locale.CANADA, "canada",
				NutritionFactsOptions.forRegulation(NutritionFactsOptions.CA_REGULATION_KEY));

		Assert.assertEquals("Saturated and trans fat share a single percentage under B.01.401", "15%",
				lineOf(data, "Saturated").dailyValuePercent());
		Assert.assertFalse("The trans fat line carries no percentage of its own", lineOf(data, "+ Trans").showDailyValue());
	}

	@Test
	public void testTheUnitedStatesKeepSaturatedAndTransFatApart() {

		ProductData product = new ProductData();
		product.setNodeRef(PRODUCT_NODE_REF);
		product.setNutList(List.of(nutListItem("FASAT", "Saturated", 1d, 5d), nutListItem("FATRN", "Trans", 2d, 10d)));

		Assert.assertEquals("The FDA gives saturated fat its own percentage", "5%",
				lineOf(build(product), "Saturated Fat").dailyValuePercent());
	}

	@Test
	public void testCaloriesArePrintedWithoutTheirUnit() {

		NutritionFactsData data = build(standardProduct());

		Assert.assertEquals("A panel writes Calories 230, never 230kcal", "230", data.calories().value());
	}

	@Test
	public void testNutrientUnnamedByTheRegulationFallsBackOnItsCharactName() {

		ProductData product = new ProductData();
		product.setNodeRef(PRODUCT_NODE_REF);
		product.setNutList(List.of(nutListItemWithCharactName("NACL", "b7322239-9650-4983-b609", "Salt", 1d, 5d)));

		NutritionFactsData data = builder.build(product, Locale.US, VERTICAL_FORMAT,
				NutritionFactsOptions.forRegulation(US_REGULATION_KEY).withOptionalNutrients());

		Assert.assertEquals("A node identifier must never be printed on a label", "Salt", data.nutrients().get(0).label());
	}

	@Test
	public void testServingSizeFallsBackOnTheQuantityWhenNoWordingIsGiven() {

		ProductData product = standardProduct();
		product.setServingSize(55d);
		product.setServingSizeUnit(ProductUnit.g);

		Assert.assertEquals("55g", builder.build(product, Locale.US, VERTICAL_FORMAT).serving().servingSize());
	}

	private NutListDataItem nutListItemWithCharactName(String nutCode, String nodeName, String charactName, Double value, Double gdaPerc) {
		NodeRef nutNodeRef = new NodeRef("workspace://SpacesStore/" + nutCode.toLowerCase());
		NutDataItem nut = new NutDataItem();
		nut.setNutCode(nutCode);
		nut.setName(nodeName);
		nut.setCharactName(new MLText(Locale.ENGLISH, charactName));
		nut.setNodeRef(nutNodeRef);
		Mockito.when(alfrescoRepository.findOne(nutNodeRef)).thenReturn(nut);

		NutListDataItem item = new NutListDataItem();
		item.setNut(nutNodeRef);
		item.setValue(value);
		item.setValuePerServing(value);
		item.setRoundedValue(roundedValue(value, gdaPerc, US_REGULATION_KEY));
		return item;
	}

	private NutritionFactsData build(ProductData product) {
		return builder.build(product, Locale.US, VERTICAL_FORMAT);
	}

	private ProductData standardProduct() {
		ProductData product = new ProductData();
		product.setNodeRef(PRODUCT_NODE_REF);

		List<NutListDataItem> nutList = new ArrayList<>();
		nutList.add(nutListItem("VITD-", "Vitamin D", 2d, 10d));
		nutList.add(nutListItem("SUGAD", "Added sugars", 10d, 20d));
		nutList.add(nutListItem("FAT", "Fat, total", 8d, 10d));
		nutList.add(nutListItem("US_ENER-E14", "Energy", 230d, 12d));
		nutList.add(nutListItem("CHOL-", "Cholesterol", 0d, 0d));
		nutList.add(nutListItem("FASAT", "Fatty acids, saturated", 1d, 5d));
		product.setNutList(nutList);

		return product;
	}

	private NutListDataItem nutListItem(String nutCode, String charactName, Double value, Double gdaPerc) {
		return nutListItem(nutCode, charactName, value, gdaPerc, US_REGULATION_KEY);
	}

	private NutListDataItem nutListItem(String nutCode, String charactName, Double value, Double gdaPerc, String regulationKey) {

		NodeRef nutNodeRef = new NodeRef("workspace://SpacesStore/" + nutCode.replace("_", "").replace("-", ""));

		NutDataItem nut = new NutDataItem();
		nut.setNutCode(nutCode);
		nut.setName(charactName);
		nut.setNodeRef(nutNodeRef);
		Mockito.when(alfrescoRepository.findOne(nutNodeRef)).thenReturn(nut);

		NutListDataItem nutListItem = new NutListDataItem();
		nutListItem.setNut(nutNodeRef);
		nutListItem.setValue(value);
		nutListItem.setValuePerServing(value);
		nutListItem.setRoundedValue(roundedValue(value, gdaPerc, regulationKey));
		return nutListItem;
	}

	private String roundedValue(Double value, Double gdaPerc, String regulationKey) {
		return "{\"v\":{\"" + regulationKey + "\":" + value + "},\"vps\":{\"" + regulationKey + "\":" + value + "},\"gda\":{\""
				+ regulationKey + "\":" + gdaPerc + "}}";
	}

	private void mockMlProperty(org.alfresco.service.namespace.QName property, String value) {
		MLText mlText = new MLText();
		mlText.addValue(Locale.US, value);
		Mockito.when(mlNodeService.getProperty(PRODUCT_NODE_REF, property)).thenReturn(mlText);
	}

	private NutritionFactsLine lineOf(NutritionFactsData data, String label) {
		List<NutritionFactsLine> lines = new ArrayList<>(data.nutrients());
		lines.addAll(data.micronutrients());
		for (NutritionFactsLine line : lines) {
			if (label.equals(line.label())) {
				return line;
			}
		}
		throw new AssertionError("No line labelled " + label);
	}

}
