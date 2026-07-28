package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.*;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;

/**
 * <p>StandardBodyMilkTestProduct class.</p>
 *
 * @author matthieu
 */
public class StandardBodyMilkTestProduct extends StandardSoapTestProduct {

	private static final Map<String, String> ISO_COUNTRY_NAMES = new HashMap<>();
	public static final String DENATURANT = "DENATURANT";
	public static final String EMOLLIENT = "EMOLLIENT";
	public static final String MASKING = "MASKING";
	public static final String SKIN_CONDITIONING = "SKIN CONDITIONING";
	public static final String FRAGRANCE_FLAVORING = "FLAVOURING";
	public static final String FLAVOURING = "FLAVOURING";

	public static final String EYE_IRRIT_2_H_319 = "Eye Irrit. 2:H319";
	public static final String SKIN_IRRIT_2_H_315_EYE_IRRIT_2_H_319 = "Skin Irrit. 2:H315, Eye Irrit. 2:H319";
	public static final String SKIN_SENS_1_H_317 = "Skin Sens. 1:H317";
	public static final String SKIN_IRRIT_2_H_315_SKIN_SENS_1_H_317 = "Skin Irrit. 2:H315, Skin Sens. 1:H317";
	public static final String FLAM_LIQ_3_H_226_SKIN_SENS_1_H_317 = "Flam. Liq. 3:H226, Skin Sens. 1:H317";
	public static final String ACUTE_TOX_4_H_302 = "Acute Tox. 4:H302";

	static {
		ISO_COUNTRY_NAMES.put("ES", "Spain");
		ISO_COUNTRY_NAMES.put("FR", "France");
		ISO_COUNTRY_NAMES.put("AL", "Albania");
		ISO_COUNTRY_NAMES.put("EU", "European Union");
		ISO_COUNTRY_NAMES.put("US", "United States");
		ISO_COUNTRY_NAMES.put("EG", "Egypt");
		ISO_COUNTRY_NAMES.put("BR", "Brazil");
		ISO_COUNTRY_NAMES.put("IT", "Italy");
		ISO_COUNTRY_NAMES.put("FI", "Finland");
		ISO_COUNTRY_NAMES.put("TR", "Turkey");
		ISO_COUNTRY_NAMES.put("PE", "Peru");
		ISO_COUNTRY_NAMES.put("DE", "Germany");
		ISO_COUNTRY_NAMES.put("AR", "Argentina");
		ISO_COUNTRY_NAMES.put("JP", "Japan");
		ISO_COUNTRY_NAMES.put("PT", "Portugal");
		ISO_COUNTRY_NAMES.put("MX", "Mexico");
	}

	protected List<String> regulatoryCountries = new ArrayList<>();


	/**
	 * <p>Constructor for StandardBodyMilkTestProduct.</p>
	 *
	 * @param builder a {@link fr.becpg.repo.sample.StandardBodyMilkTestProduct.Builder} object
	 */
	protected StandardBodyMilkTestProduct(Builder builder) {
		super(builder);
		isWithCompo = false;
		isWithScore = false;
		isWithPhysico = true;
		isWithSpecification = false;
		isWithToxicology = false;
		this.regulatoryCountries = builder.regulatoryCountries;
	}

	// Static inner Builder class
	public static class Builder extends StandardSoapTestProduct.Builder {
		private List<String> regulatoryCountries = new ArrayList<>();

		public Builder withRegulatoryCountries(List<String> regulatoryCountries) {
			this.regulatoryCountries = regulatoryCountries;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public StandardBodyMilkTestProduct build() {
			return new StandardBodyMilkTestProduct(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void createPhysicoChems(ProductData milkProduct) {
		addPhysicoChemProperty(milkProduct, "pH", "pH", 5.5);
	}

	/** {@inheritDoc} */
	@Override
	public RawMaterialData createTestProduct() {

		// Create the body milk raw material product
		RawMaterialData bodyMilkProduct = RawMaterialData.build()
				.withName(uniqueName("🧴 Body Milk 💧"))
				.withUnit(ProductUnit.kg)
				.withQty(100d)
				.withIngList(createBodyMilkIngredients())
				.withRegulatoryList(createRegulatoryList());

		// Add physico-chemical properties if enabled
		if (isWithPhysico) {
			createPhysicoChems(bodyMilkProduct);
		}

		// Add toxicology list if enabled
		if (isWithToxicology) {
			bodyMilkProduct.setToxList(createToxList());
		}

		// Add LCA properties if score is enabled
		if (isWithScore) {
			bodyMilkProduct.setScoreList(new ArrayList<>());
			addLCAProperty(bodyMilkProduct, CLIMATE_CHANGE, "CLIMATE_CHANGE", 50.5d);
		}

		alfrescoRepository.create(destFolder, bodyMilkProduct);

		// Save associations if specifications are enabled
		if (isWithSpecification) {
			bodyMilkProduct.setProductSpecifications(createProductSpecifications());
			saveEntityAssociations(bodyMilkProduct);
		}

		return bodyMilkProduct;
	}

	/**
	 * <p>createRegulatoryList.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	private List<RegulatoryListDataItem> createRegulatoryList() {
		List<RegulatoryListDataItem> regulatoryList = new ArrayList<>();
		Collection<String> countries;
		if (regulatoryCountries != null && !regulatoryCountries.isEmpty()) {
			countries = regulatoryCountries;
		} else {
			countries = ISO_COUNTRY_NAMES.keySet();
		}

		List<NodeRef> countryRefs = new ArrayList<>();
		for (String country : countries) {
			String name = ISO_COUNTRY_NAMES.getOrDefault(country, country);
			countryRefs.add(getOrCreateCountryRef(country, name));
		}

		RegulatoryListDataItem item = new RegulatoryListDataItem();
		item.setRegulatoryCountriesRef(countryRefs);
		item.setRegulatoryUsagesRef(List.of(
				getOrCreateUsageRef("Body soap", "COSMETIC_BODY_SOAP,DECERNIS_Body Soap"),
				getOrCreateUsageRef("Hand soap", "COSMETIC_HAND_SOAP,DECERNIS_Hand Soap"),
				getOrCreateUsageRef("Body Cream/Lotion", "COSMETIC_BODY_CREAM_LOTION,DECERNIS_Body Cream/Lotion")
		));

		regulatoryList.add(item);
		return regulatoryList;
	}

	/**
	 * <p>getOrCreateUsageRef.</p>
	 *
	 * @param name           the usage charact name
	 * @param regulatoryCode the regulatory code ({@code <beCPGcode>,DECERNIS_<phrase>})
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private NodeRef getOrCreateUsageRef(String name, String regulatoryCode) {
		HashMap<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(PLMModel.PROP_REGULATORY_CODE, regulatoryCode);
		properties.put(PLMModel.PROP_REGULATORY_MODULE, "COSMETICS");
		return CharactTestHelper.getOrCreateNode(nodeService,
				"/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:RegulatoryUsages", name, PLMModel.TYPE_REGULATORY_USAGE, properties);
	}

	/**
	 * <p>getOrCreateCountryRef.</p>
	 *
	 * @param code a {@link java.lang.String} object
	 * @param name a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private NodeRef getOrCreateCountryRef(String code, String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PLMModel.PROP_REGULATORY_CODE, name); // so decernis still works
		return CharactTestHelper.getOrCreateGeo(nodeService, code, name, properties);
	}

	/**
	 * <p>saveEntityAssociations.</p>
	 *
	 * @param bodyMilkProduct a {@link fr.becpg.repo.product.data.RawMaterialData} object
	 */
	private void saveEntityAssociations(RawMaterialData bodyMilkProduct) {
		if (bodyMilkProduct.getProductSpecifications() != null) {
			for (fr.becpg.repo.product.data.ProductSpecificationData productSpecificationData : bodyMilkProduct.getProductSpecifications()) {
				nodeService.createAssociation(bodyMilkProduct.getNodeRef(), productSpecificationData.getNodeRef(),
						fr.becpg.model.PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			}
		}
	}

	/**
	 * <p>createBodyMilkIngredients.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	private List<IngListDataItem> createBodyMilkIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Create ingredients with their types and all parameters (CAS numbers, hazard classes, toxicities)
		ingredients.add(createIngListItemWithType("AQUA", "SOLVENT", 77.18435, "7732-18-5", null, null, null, null, null, null, null));
		ingredients.add(createIngListItemWithType("GLYCERIN", DENATURANT, 4.9985, "56-81-5", null, 12600.0, 10000.0, null, false, "200-289-5", "200-289-5,616-940-5"));
		ingredients.add(createIngListItemWithType("HELIANTHUS ANNUUS SEED OIL", EMOLLIENT, 4.05, "8001-21-6", null, null, null, null, null, "232-273-9", "232-273-9,617-619-2"));
		ingredients.add(createIngListItemWithType("CAPRYLIC/CAPRIC TRIGLYCERIDE", MASKING, 3.5, "73398-61-5", null, null, null, null, null, "265-724-3", "265-724-3,277-452-2,924-490-9"));
		ingredients.add(createIngListItemWithType("CETYL PALMITATE", EMOLLIENT, 2.5, "540-10-3", null, null, null, null, null, "208-736-6,309-375-8", "208-736-6,309-375-8"));
		ingredients.add(createIngListItemWithType("BUTYROSPERMUM PARKII BUTTER", SKIN_CONDITIONING, 2.0, "194043-92-0", null, null, null, null, null, null, "293-515-7,606-306-6,925-906-1"));
		ingredients.add(createIngListItemWithType("CETEARYL GLUCOSIDE", "EMULSIFYING", 1.875, "246159-33-8", null, null, null, null, null, null, "928-659-8"));
		ingredients.add(createIngListItemWithType("PARFUM", "DEODORANT", 1.2, "8024-06-4", "Skin Sens. 1:H317, Flam. Liq. 3:H226", null, null, 1.0, true, null, null));
		ingredients.add(createIngListItemWithType("SORBITAN OLIVATE", "EMULSIFYING", 0.975, "223706-40-9", null, null, null, null, null, null, "607-008-9,922-601-5"));
		ingredients.add(createIngListItemWithType("XANTHAN GUM", "BINDING", 0.7, "11138-66-2", null, null, null, null, null, "234-394-2", "234-394-2,619-321-8"));
		ingredients.add(createIngListItemWithType("CAPRYLYL GLYCOL", EMOLLIENT, 0.5, "1117-86-8", null, null, null, null, null, "214-254-7", "214-254-7"));
		ingredients.add(createIngListItemWithType("ETHYLHEXYLGLYCERIN", SKIN_CONDITIONING, 0.1998, "70445-33-9", null, null, null, null, null, null, "408-080-2,615-116-2"));
		ingredients.add(createIngListItemWithType("CETEARYL ALCOHOL", EMOLLIENT, 0.15, "67762-27-0", null, null, null, null, null, "267-008-6", "267-008-6,616-857-4"));
		ingredients.add(createIngListItemWithType("SODIUM STEAROYL GLUTAMATE", "CLEANSING", 0.1, "38517-23-6", null, null, null, null, null, null, "253-980-9"));
		ingredients.add(createIngListItemWithType("TOCOPHEROL", "ANTIOXIDANT", 0.0502, "1406-66-2", null, null, null, null, null, "215-798-8", "200-201-5,215-798-8,604-195-9,606-803-8"));
		ingredients.add(createIngListItemWithType("CITRIC ACID", "BUFFERING", 0.013, "77-92-9", EYE_IRRIT_2_H_319, 5400.0, null, null, false, "201-069-1", "201-069-1,920-306-6,921-454-4"));
		ingredients.add(createIngListItemWithType("RHEUM PALMATUM ROOT EXTRACT", "ASTRINGENT", 0.0025, "90106-27-1", null, null, null, null, null, null, null));
		ingredients.add(createIngListItemWithType("CITRUS PARADISI FRUIT EXTRACT", SKIN_CONDITIONING, 0.00075, "90045-43-5", null, null, null, null, null, null, null));
		ingredients.add(createIngListItemWithType("SODIUM BENZOATE", "ANTICORROSIVE", 0.0006, "532-32-1", EYE_IRRIT_2_H_319, 4070.0, null, null, false, "208-534-8", "208-534-8"));
		ingredients.add(createIngListItemWithType("POTASSIUM SORBATE", "PRESERVATIVE", 0.0003, "24634-61-5", SKIN_IRRIT_2_H_315_EYE_IRRIT_2_H_319, 3200.0, null, null, false, "246-376-1", "246-376-1"));
		ingredients.add(createIngListItemWithType("3-MCPD", null, 0.0, "96-24-2", null, null, null, null, null, "202-492-4", "202-492-4"));
		ingredients.add(createIngListItemWithType("ACETALDEHYDE", MASKING, 0.0, "75-07-0", "Flam. Liq. 1:H224, Eye Irrit. 2:H319", 660.0, null, null, false, "200-836-8,216-641-6,223-921-1", "200-836-8,202-590-7,216-641-6,223-921-1,415-620-0"));
		ingredients.add(createIngListItemWithType("ACETYLCEDRENE", FRAGRANCE_FLAVORING, 0.0, "32388-55-9", "Skin Sens. 1:H317, Aquatic Chronic 1:H410", 5200.0, null, 1.0, true, "251-020-3,268-253-1", "251-020-3,268-253-1"));
		ingredients.add(createIngListItemWithType("AFLATOXIN B2", null, 0.0, "7220-81-7", null, null, null, null, null, "230-618-8", "230-618-8"));
		ingredients.add(createIngListItemWithType("AFLATOXINS B1", null, 0.0, "1162-65-8", null, null, null, null, null, "214-603-3", "214-603-3"));
		ingredients.add(createIngListItemWithType("AFLATOXINS G1", null, 0.0, "1165-39-5", null, null, null, null, null, "214-615-9", "214-615-9"));
		ingredients.add(createIngListItemWithType("AFLATOXINS G2", null, 0.0, "7241-98-7", null, null, null, null, null, "230-643-4", "230-643-4"));
		ingredients.add(createIngListItemWithType("ALPHA-TERPINENE", FRAGRANCE_FLAVORING, 0.0, "99-86-5", "Flam. Liq. 3:H226, Acute Tox. 4:H302", 1680.0, null, null, false, "202-795-1", "202-795-1"));
		ingredients.add(createIngListItemWithType("AMYLVINYLCARBINYL ACETATE", FRAGRANCE_FLAVORING, 0.0, "2442-10-6", SKIN_IRRIT_2_H_315_EYE_IRRIT_2_H_319, null, null, null, false, "219-474-7", "219-474-7"));
		ingredients.add(createIngListItemWithType("ANTIMONY", null, 0.0, "7440-36-0", "Carc. 2:H351", 7000.0, null, null, false, "231-146-5", "231-146-5"));
		ingredients.add(createIngListItemWithType("ARSENIC", null, 0.0, "7440-38-2", "Acute Tox. 3:H301, Acute Tox. 3:H331", 15.0, null, null, false, null, null));
		ingredients.add(createIngListItemWithType("BENZYL ALCOHOL", FRAGRANCE_FLAVORING, 0.0, "100-51-6", "Acute Tox. 4:H302, Eye Irrit. 2:H319", 1230.0, 2000.0, null, false, "202-859-9", "202-859-9,234-343-4"));
		ingredients.add(createIngListItemWithType("BETA-CARYOPHYLLENE", MASKING, 0.0, "87-44-5", "Skin Sens. 1B:H317", null, null, null, true, "201-746-1", "201-746-1"));
		ingredients.add(createIngListItemWithType("BUTYRIC ACID", MASKING, 0.0, "107-92-6", "Skin Corr. 1B:H314", 2000.0, null, null, false, "203-532-3,203-532-3", "203-532-3,294-759-7,203-532-3"));
		ingredients.add(createIngListItemWithType("Benzo[def]chrysene (Benzo[a]pyrene)", null, 0.0, "50-32-8", "Skin Sens. 1:H317, Aquatic Acute 1:H400", null, null, 1.0, true, "200-028-5,205-892-7", "200-028-5,205-892-7"));
		ingredients.add(createIngListItemWithType("CAMPHOR", DENATURANT, 0.0, "76-22-2", "Flam. Sol. 2:H228, Acute Tox. 4:H302, Skin Sens. 1:H317", 1310.0, null, null, true, "200-945-0,244-350-4", "200-945-0,207-355-2,244-350-4,617-014-3"));
		ingredients.add(createIngListItemWithType("CARVONE", FLAVOURING, 0.0, "99-49-0", SKIN_SENS_1_H_317, 1640.0, null, null, true, "202-759-5", "202-759-5,218-827-2,229-352-5"));
		ingredients.add(createIngListItemWithType("CHROMIUM", null, 0.0, "7440-47-3", null, 80.0, null, null, false, "231-157-5", "231-157-5"));
		ingredients.add(createIngListItemWithType("CITRAL", FLAVOURING, 0.0, "5392-40-5", SKIN_IRRIT_2_H_315_SKIN_SENS_1_H_317, 4960.0, null, null, true, "203-379-2,205-476-5,226-394-6", "203-379-2,205-476-5,226-394-6,266-394-6"));
		ingredients.add(createIngListItemWithType("CITRONELLOL", FRAGRANCE_FLAVORING, 0.0, "106-22-9", SKIN_IRRIT_2_H_315_SKIN_SENS_1_H_317, 3450.0, 2650.0, null, true, "203-375-0,247-737-6", "203-375-0,247-737-6"));
		ingredients.add(createIngListItemWithType("CITRUS AURANTIUM PEEL OIL", null, 0.0, "8008-57-9", FLAM_LIQ_3_H_226_SKIN_SENS_1_H_317, 4400.0, null, 1.0, true, null, "614-782-1"));
		ingredients.add(createIngListItemWithType("CITRUS LIMON PEEL OIL", MASKING, 0.0, "8008-56-8", FLAM_LIQ_3_H_226_SKIN_SENS_1_H_317, 4400.0, null, 1.0, true, null, null));
		ingredients.add(createIngListItemWithType("COBALT", null, 0.0, "7440-48-4", "Resp. Sens. 1:H334, Skin Sens. 1:H317", 6171.0, null, null, true, "231-158-0", "231-158-0,919-713-1"));
		ingredients.add(createIngListItemWithType("COPPER", null, 0.0, "7440-50-8", "Aquatic Acute 1:H400", null, null, 1.0, false, "231-159-6", "231-159-6,310-193-6,918-168-7"));
		ingredients.add(createIngListItemWithType("COUMARIN", FRAGRANCE_FLAVORING, 0.0, "91-64-5", "Acute Tox. 3:H301, Skin Sens. 1:H317", 293.0, null, null, true, "202-086-7", "202-086-7"));
		ingredients.add(createIngListItemWithType("Cadmium", "NONE", 0.0, "7440-43-9", "Acute Tox. 2:H330, Carc. 1B:H350", 2330.0, null, null, false, "231-152-8", "231-152-8"));
		ingredients.add(createIngListItemWithType("Chrysene", null, 0.0, "218-01-9", "Aquatic Acute 1:H400", null, null, 1.0, false, "205-923-4", "205-923-4"));
		ingredients.add(createIngListItemWithType("D-LIMONENE", MASKING, 0.0, "5989-27-5", "Flam. Liq. 3:H226, Skin Irrit. 2:H315, Skin Sens. 1:H317", 4400.0, null, 1.0, true, "227-813-5", "205-341-0,227-813-5,5989-27-5"));
		ingredients.add(createIngListItemWithType("DIETHYLENE GLYCOL", null, 0.0, "111-46-6", ACUTE_TOX_4_H_302, 12565.0, 11890.0, null, false, "203-872-2", "203-872-2"));
		ingredients.add(createIngListItemWithType("DIMETHYL PHENETHYL ACETATE", null, 0.0, "151-05-3", ACUTE_TOX_4_H_302, 1000.0, null, null, false, null, null));
		ingredients.add(createIngListItemWithType("DIPROPYLENE GLYCOL", MASKING, 0.0, "25265-71-8", null, 14850.0, 20000.0, null, false, "203-821-4,246-770-3", "203-821-4,246-770-3"));
		ingredients.add(createIngListItemWithType("EUCALYPTOL", DENATURANT, 0.0, "470-82-6", FLAM_LIQ_3_H_226_SKIN_SENS_1_H_317, 2480.0, null, null, true, "207-431-5", "207-428-9,207-431-5,617-029-5"));
		ingredients.add(createIngListItemWithType("EUGENIA CARYOPHYLLUS OIL", null, 0.0, "8000-34-8", "Eye Irrit. 2:H319, Skin Sens. 1:H317", 1370.0, null, null, true, null, "616-772-2,616-969-3"));
		ingredients.add(createIngListItemWithType("EUGENOL", DENATURANT, 0.0, "97-53-0", "Eye Irrit. 2:H319, Skin Sens. 1:H317", 1930.0, null, null, true, "202-589-1", "202-235-6,202-589-1"));
		ingredients.add(createIngListItemWithType("EUGENYL ACETATE", MASKING, 0.0, "93-28-7", "Acute Tox. 4:H302, Skin Sens. 1:H317", 1670.0, null, null, true, "202-235-6", "202-235-6"));
		ingredients.add(createIngListItemWithType("Ethylene oxide", null, 0.0, "75-21-8", "Flam. Gas 1:H220, Acute Tox. 3:H331", 72.0, null, null, true, null, null));
		ingredients.add(createIngListItemWithType("FORMALDEHYDE", null, 0.0, "50-00-0", "Skin Corr. 1B:H314, Skin Sens. 1:H317", 100.0, 270.0, null, true, "200-001-8", "200-001-8"));
		ingredients.add(createIngListItemWithType("FUROCOUMARINES", null, 0.0, "66-97-7", SKIN_SENS_1_H_317, null, null, null, true, null, "206-066-9,207-604-5,223-459-0"));
		ingredients.add(createIngListItemWithType("GAMMA-TERPINENE", FRAGRANCE_FLAVORING, 0.0, "99-85-4", "Flam. Liq. 3:H226", 3650.0, null, null, false, "202-794-6", "202-794-6"));
		ingredients.add(createIngListItemWithType("GERANIOL", FRAGRANCE_FLAVORING, 0.0, "106-24-1", "Skin Irrit. 2:H315, Eye Dam. 1:H318, Skin Sens. 1:H317", 3600.0, null, null, true, "203-377-1", "203-377-1"));
		ingredients.add(createIngListItemWithType("GERANYL ACETATE", FRAGRANCE_FLAVORING, 0.0, "105-87-3", SKIN_IRRIT_2_H_315_SKIN_SENS_1_H_317, 6330.0, null, null, true, "203-341-5", "203-341-5,906-083-8"));
		ingredients.add(createIngListItemWithType("GLYCOL", "HUMECTANT", 0.0, "107-21-1", ACUTE_TOX_4_H_302, 4700.0, 10600.0, null, false, "203-473-3", "203-473-3,920-413-8"));
		ingredients.add(createIngListItemWithType("HEXANE", null, 0.0, "110-54-3", "Flam. Liq. 2:H225, Repr. 2:H361f", 16000.0, null, null, false, "203-777-6", "203-777-6"));
		ingredients.add(createIngListItemWithType("ISOEUGENOL", FLAVOURING, 0.0, "97-54-1", "Acute Tox. 4:H302, Skin Sens. 1:H317", 1500.0, 1912.0, null, true, "202-590-7", "202-590-7,227-633-7,227-678-2"));
		ingredients.add(createIngListItemWithType("ISOPROPYL ALCOHOL", "ANTIFOAMING", 0.0, "67-63-0", "Flam. Liq. 2:H225, Eye Irrit. 2:H319", 5045.0, 12800.0, null, false, "200-661-7", "200-661-7,414-810-0,273-530-5"));
		ingredients.add(createIngListItemWithType("JUNIPERUS VIRGINIANA OIL", MASKING, 0.0, "8000-27-9", "Aquatic Chronic 1:H410", null, null, 1.0, false, null, "917-390-1"));
		ingredients.add(createIngListItemWithType("LAVANDULA OIL/EXTRACT", null, 0.0, "8000-28-0", "Skin Sens. 1:H317, Aquatic Chronic 3:H412", null, null, 1.0, true, null, null));
		ingredients.add(createIngListItemWithType("LINALOOL", "DEODORANT", 0.0, "78-70-6", "Skin Irrit. 2:H315, Eye Irrit. 2:H319, Skin Sens. 1B:H317", 2790.0, 5610.0, null, true, "201-134-4,245-083-6", "201-134-4,204-810-7,204-811-2,245-083-6"));
		ingredients.add(createIngListItemWithType("LINALYL ACETATE", MASKING, 0.0, "115-95-7", SKIN_IRRIT_2_H_315_EYE_IRRIT_2_H_319, 14550.0, null, null, false, "204-116-4,254-806-4,257-347-8", "204-116-4,254-806-4,257-347-8"));
		ingredients.add(createIngListItemWithType("Lead", "NONE", 0.0, "7439-92-1", "Repr. 1A:H360", 1000.0, null, null, false, "231-100-4", "231-100-4,920-238-7"));
		ingredients.add(createIngListItemWithType("MERCURY", null, 0.0, "7439-97-6", "Acute Tox. 2:H330, Repr. 1B:H360", null, null, null, false, "231-106-7", "231-106-7,924-835-3"));
		ingredients.add(createIngListItemWithType("METHYL ALCOHOL", DENATURANT, 0.0, "67-56-1", "Flam. Liq. 2:H225, Acute Tox. 3:H301, Acute Tox. 3:H311, Acute Tox. 3:H331", 100.0, 300.0, null, false, "200-659-6", "200-659-6"));
		ingredients.add(createIngListItemWithType("METHYL HYDROGENATED ROSINATE", "FILM FORMING", 0.0, "8050-31-5", "Aquatic Chronic 3:H412", null, null, null, false, "232-476-2", "232-476-2"));
		ingredients.add(createIngListItemWithType("METHYLENEDIOXYPHENYL METHYLPROPANAL", FRAGRANCE_FLAVORING, 0.0, "1205-17-0", "Skin Sens. 1B:H317", 3600.0, null, null, true, "214-881-6", "214-881-6"));
		ingredients.add(createIngListItemWithType("MYRCENE", FRAGRANCE_FLAVORING, 0.0, "123-35-3", "Flam. Liq. 3:H226, Skin Irrit. 2:H315", null, null, null, false, "204-622-5", "204-622-5"));
		ingredients.add(createIngListItemWithType("NICKEL", null, 0.0, "7440-02-0", "Skin Sens. 1:H317, Carc. 2:H351", 9000.0, null, null, true, "231-111-4", "231-111-4"));
		ingredients.add(createIngListItemWithType("OXACYCLOHEXADECENONE", MASKING, 0.0, "3100-36-5", SKIN_SENS_1_H_317, null, null, null, true, null, "422-320-3,609-040-9"));
		ingredients.add(createIngListItemWithType("P-CYMENE", MASKING, 0.0, "99-87-6", "Flam. Liq. 3:H226", 4750.0, null, null, false, "202-796-7", "202-796-7"));
		ingredients.add(createIngListItemWithType("PERILLALDEHYDE", MASKING, 0.0, "2111-75-3", SKIN_SENS_1_H_317, 2500.0, null, null, true, "218-302-8", "218-302-8"));
		ingredients.add(createIngListItemWithType("PHENETHYL ALCOHOL", MASKING, 0.0, "60-12-8", "Acute Tox. 4:H302, Eye Irrit. 2:H319", 1790.0, 790.0, null, false, "200-456-2", "200-456-2"));
		ingredients.add(createIngListItemWithType("PINENE", "ANTIFOAMING", 0.0, "80-56-8", FLAM_LIQ_3_H_226_SKIN_SENS_1_H_317, 3700.0, null, null, true, null, null));
		ingredients.add(createIngListItemWithType("PYRUVIC ACID", MASKING, 0.0, "127-17-3", "Skin Corr. 1B:H314", null, null, null, false, "204-824-3", "204-824-3"));
		ingredients.add(createIngListItemWithType("SAFROLE", null, 0.0, "94-59-7", "Acute Tox. 4:H302, Carc. 1B:H350", 1950.0, null, null, false, "202-345-4", "202-345-4"));
		ingredients.add(createIngListItemWithType("SODIUM CHLORIDE", "BULKING", 0.0, "7647-14-5", EYE_IRRIT_2_H_319, 3000.0, null, null, false, null, null));
		ingredients.add(createIngListItemWithType("TERPINEOL", MASKING, 0.0, "98-55-5", SKIN_IRRIT_2_H_315_EYE_IRRIT_2_H_319, 4300.0, null, null, false, "232-268-1", "232-268-1,251-835-4"));
		ingredients.add(createIngListItemWithType("TERPINOLENE", FRAGRANCE_FLAVORING, 0.0, "586-62-9", SKIN_SENS_1_H_317, 4390.0, null, null, true, "209-578-0", "209-578-0"));
		ingredients.add(createIngListItemWithType("TETRAMETHYL ACETYLOCTAHYDRONAPHTHALENES", MASKING, 0.0, "54464-57-2", "Skin Sens. 1:H317, Aquatic Chronic 1:H410", null, null, 1.0, true, "259-174-3", "259-174-3,915-730-3"));
		ingredients.add(createIngListItemWithType("TRIETHYL CITRATE", MASKING, 0.0, "77-89-4", null, 5900.0, null, null, false, "201-070-7", "201-070-7"));
		ingredients.add(createIngListItemWithType("Trichloroethylene", "ABRASIVE", 0.0, "79-01-6", "Skin Irrit. 2:H315, Eye Irrit. 2:H319, Carc. 1B:H350", 4920.0, 29000.0, null, false, "201-167-4", "200-663-8,201-167-4,234-190-3,922-902-1"));
		ingredients.add(createIngListItemWithType("VANILLIN", MASKING, 0.0, "121-33-5", EYE_IRRIT_2_H_319, 1580.0, null, null, false, "204-465-2", "204-465-2"));

		return ingredients;
	}

	/**
	 * <p>createIngListItemWithType.</p>
	 *
	 * @param ingName          a {@link String} object
	 * @param ingTypeValue     a {@link String} object
	 * @param ingQtyPerc       a double
	 * @param casNumber        a {@link String} object
	 * @param hazardClass      a {@link String} object
	 * @param toxicityOral     a {@link Double} object
	 * @param toxicityDermal   a {@link Double} object
	 * @param mFactor          a {@link Double} object
	 * @param superSensitizing a {@link Boolean} object
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 */
	private IngListDataItem createIngListItemWithType(String ingName, String ingTypeValue, double ingQtyPerc, String casNumber, String hazardClass,
	                                                  Double toxicityOral, Double toxicityDermal, Double mFactor, Boolean superSensitizing, String ceNumber, String ecNumber) {
		NodeRef ingType = null;

		if (ingTypeValue != null && !ingTypeValue.isEmpty()) {
			ingType = CharactTestHelper.getOrCreateIngType(nodeService, ingTypeValue);
		}

		// Ensure the ingredient node is registered with its type if any
		CharactTestHelper.getOrCreateIng(nodeService, ingName, ingType);

		// Use the parent's createIngListItem method passing all parameters
		return createIngListItem(ingName, ingQtyPerc, casNumber, hazardClass, toxicityOral, toxicityDermal, mFactor, superSensitizing, ceNumber, ecNumber);
	}
}
