package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	/** Constant <code>MASKING="MASKING"</code> */
	private static final String MASKING = "MASKING";
	/** Constant <code>DENATURANT="DENATURANT"</code> */
	private static final String DENATURANT = "DENATURANT";

	private static final Map<String, String> ISO_COUNTRY_NAMES = new HashMap<>();
	static {
		ISO_COUNTRY_NAMES.put("FR", "France");
		ISO_COUNTRY_NAMES.put("DE", "Germany");
		ISO_COUNTRY_NAMES.put("ES", "Spain");
		ISO_COUNTRY_NAMES.put("IT", "Italy");
		ISO_COUNTRY_NAMES.put("AL", "Albania");
		ISO_COUNTRY_NAMES.put("EG", "Egypt");
		ISO_COUNTRY_NAMES.put("EU", "European Union");
		ISO_COUNTRY_NAMES.put("US", "United States");
		ISO_COUNTRY_NAMES.put("CA", "Canada");
		ISO_COUNTRY_NAMES.put("GB", "United Kingdom");
		ISO_COUNTRY_NAMES.put("CN", "China");
		ISO_COUNTRY_NAMES.put("JP", "Japan");
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
		List<String> countries;
		if (regulatoryCountries != null && !regulatoryCountries.isEmpty()) {
			countries = regulatoryCountries;
		} else {
			countries = List.of("FR", "DE", "ES", "IT", "AL", "EG", "EU");
		}

		List<NodeRef> countryRefs = new ArrayList<>();
		for (String country : countries) {
			String name = ISO_COUNTRY_NAMES.getOrDefault(country, country);
			countryRefs.add(getOrCreateCountryRef(country, name));
		}

		RegulatoryListDataItem item = new RegulatoryListDataItem();
		item.setRegulatoryCountriesRef(countryRefs);
		item.setRegulatoryUsagesRef(List.of(
				getOrCreateUsageRef("Body Lotion", "COSMETIC_BODY_LOTION,DECERNIS_Body Lotion"),
				getOrCreateUsageRef("Body Cream", "COSMETIC_BODY_CREAM,DECERNIS_Body Cream")));

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
		ingredients.add(createIngListItemWithType("AQUA", "SOLVENT", 77.18435, "7732-18-5", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("GLYCERIN", DENATURANT, 4.9985, "56-81-5", null, 12600.0, 10000.0, null, false));
		ingredients.add(createIngListItemWithType("HELIANTHUS ANNUUS SEED OIL", "EMOLLIENT", 4.05, "8001-21-6", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("CAPRYLIC/CAPRIC TRIGLYCERIDE", MASKING, 3.5, "73398-61-5", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("CETYL PALMITATE", "EMOLLIENT", 2.5, "540-10-3", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("BUTYROSPERMUM PARKII BUTTER", "SKIN CONDITIONING", 2.0, "194043-92-0", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("CETEARYL GLUCOSIDE", "EMULSIFYING", 1.875, "246159-33-8", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("PARFUM", "DEODORANT", 1.2, "8024-06-4", "Skin Sens. 1:H317, Flam. Liq. 3:H226", null, null, 1.0, true));
		ingredients.add(createIngListItemWithType("SORBITAN OLIVATE", "EMULSIFYING", 0.975, "223706-40-9", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("XANTHAN GUM", "BINDING", 0.7, "11138-66-2", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("CAPRYLYL GLYCOL", "EMOLLIENT", 0.5, "1117-86-8", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("ETHYLHEXYLGLYCERIN", "SKIN CONDITIONING", 0.1998, "70445-33-9", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("CETEARYL ALCOHOL", "EMOLLIENT", 0.15, "67762-27-0", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("SODIUM STEAROYL GLUTAMATE", "CLEANSING", 0.1, "38517-23-6", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("TOCOPHEROL", "ANTIOXIDANT", 0.0502, "1406-66-2", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("CITRIC ACID", "ACIDIC/BASIC/BUFFER", 0.013, "77-92-9", "Eye Irrit. 2:H319", 5400.0, null, null, false));
		ingredients.add(createIngListItemWithType("RHEUM PALMATUM ROOT EXTRACT", "ASTRINGENT", 0.0025, "90106-27-1", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("CITRUS PARADISI FRUIT EXTRACT", "SKIN CONDITIONING", 0.00075, "90045-43-5", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("SODIUM BENZOATE", "ANTICORROSIVE", 0.0006, "532-32-1", "Eye Irrit. 2:H319", 4070.0, null, null, false));
		ingredients.add(createIngListItemWithType("POTASSIUM SORBATE", "PRESERVATIVE", 0.0003, "24634-61-5", "Skin Irrit. 2:H315, Eye Irrit. 2:H319", 3200.0, null, null, false));
		ingredients.add(createIngListItemWithType("3-MCPD", null, 0d, "96-24-2", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("ACETALDEHYDE", MASKING, 0d, "75-07-0", "Flam. Liq. 1:H224, Eye Irrit. 2:H319", 660.0, null, null, false));
		ingredients.add(createIngListItemWithType("ACETYLCEDRENE", "FRAGRANCE/FLAVORING", 0d, "32388-55-9", "Skin Sens. 1:H317, Aquatic Chronic 1:H410", 5200.0, null, 1.0, true));
		ingredients.add(createIngListItemWithType("AFLATOXIN B2", null, 0d, "7220-81-7", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("AFLATOXINS B1", null, 0d, "1162-65-8", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("AFLATOXINS G1", null, 0d, "1165-39-5", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("AFLATOXINS G2", null, 0d, "7241-98-7", null, null, null, null, null));
		ingredients.add(createIngListItemWithType("ALPHA-TERPINENE", "FRAGRANCE/FLAVORING", 0d, "99-86-5", "Flam. Liq. 3:H226, Acute Tox. 4:H302", 1680.0, null, null, false));
		ingredients.add(createIngListItemWithType("AMYLVINYLCARBINYL ACETATE", "FRAGRANCE/FLAVORING", 0d, "2442-10-6", "Skin Irrit. 2:H315, Eye Irrit. 2:H319", null, null, null, false));
		ingredients.add(createIngListItemWithType("ANTIMONY", null, 0d, "7440-36-0", "Carc. 2:H351", 7000.0, null, null, false));
		ingredients.add(createIngListItemWithType("ARSENIC", null, 0d, "7440-38-2", "Acute Tox. 3:H301, Acute Tox. 3:H331", 15.0, null, null, false));
		ingredients.add(createIngListItemWithType("BENZYL ALCOHOL", "FRAGRANCE/FLAVORING", 0d, "100-51-6", "Acute Tox. 4:H302, Eye Irrit. 2:H319", 1230.0, 2000.0, null, false));
		ingredients.add(createIngListItemWithType("BETA-CARYOPHYLLENE", MASKING, 0d, "87-44-5", "Skin Sens. 1B:H317", null, null, null, true));
		ingredients.add(createIngListItemWithType("BUTYRIC ACID", MASKING, 0d, "107-92-6", "Skin Corr. 1B:H314", 2000.0, null, null, false));
		ingredients.add(createIngListItemWithType("Benzo[def]chrysene (Benzo[a]pyrene)", null, 0d, "50-32-8", "Skin Sens. 1:H317, Aquatic Acute 1:H400", null, null, 1.0, true));
		ingredients.add(createIngListItemWithType("CAMPHOR", DENATURANT, 0d, "76-22-2", "Flam. Sol. 2:H228, Acute Tox. 4:H302, Skin Sens. 1:H317", 1310.0, null, null, true));
		ingredients.add(createIngListItemWithType("CARVONE", "FLAVOURING", 0d, "99-49-0", "Skin Sens. 1:H317", 1640.0, null, null, true));
		ingredients.add(createIngListItemWithType("CHROMIUM", null, 0d, "7440-47-3", null, 80.0, null, null, false));
		ingredients.add(createIngListItemWithType("CITRAL", "FLAVOURING", 0d, "5392-40-5", "Skin Irrit. 2:H315, Skin Sens. 1:H317", 4960.0, null, null, true));
		ingredients.add(createIngListItemWithType("CITRONELLOL", "FRAGRANCE/FLAVORING", 0d, "106-22-9", "Skin Irrit. 2:H315, Skin Sens. 1:H317", 3450.0, 2650.0, null, true));
		ingredients.add(createIngListItemWithType("CITRUS AURANTIUM PEEL OIL", null, 0d, "8008-57-9", "Flam. Liq. 3:H226, Skin Sens. 1:H317", 4400.0, null, 1.0, true));
		ingredients.add(createIngListItemWithType("CITRUS LIMON PEEL OIL", MASKING, 0d, "8008-56-8", "Flam. Liq. 3:H226, Skin Sens. 1:H317", 4400.0, null, 1.0, true));
		ingredients.add(createIngListItemWithType("COBALT", null, 0d, "7440-48-4", "Resp. Sens. 1:H334, Skin Sens. 1:H317", 6171.0, null, null, true));
		ingredients.add(createIngListItemWithType("COPPER", null, 0d, "7440-50-8", "Aquatic Acute 1:H400", null, null, 1.0, false));
		ingredients.add(createIngListItemWithType("COUMARIN", "FRAGRANCE/FLAVORING", 0d, "91-64-5", "Acute Tox. 3:H301, Skin Sens. 1:H317", 293.0, null, null, true));
		ingredients.add(createIngListItemWithType("Cadmium", "NONE", 0d, "7440-43-9", "Acute Tox. 2:H330, Carc. 1B:H350", 2330.0, null, null, false));
		ingredients.add(createIngListItemWithType("Chrysene", null, 0d, "218-01-9", "Aquatic Acute 1:H400", null, null, 1.0, false));
		ingredients.add(createIngListItemWithType("D-LIMONENE", MASKING, 0d, "5989-27-5", "Flam. Liq. 3:H226, Skin Irrit. 2:H315, Skin Sens. 1:H317", 4400.0, null, 1.0, true));
		ingredients.add(createIngListItemWithType("DIETHYLENE GLYCOL", null, 0d, "111-46-6", "Acute Tox. 4:H302", 12565.0, 11890.0, null, false));
		ingredients.add(createIngListItemWithType("DIMETHYL PHENETHYL ACETATE", null, 0d, "151-05-3", "Acute Tox. 4:H302", 1000.0, null, null, false));
		ingredients.add(createIngListItemWithType("DIPROPYLENE GLYCOL", MASKING, 0d, "25265-71-8", null, 14850.0, 20000.0, null, false));
		ingredients.add(createIngListItemWithType("EUCALYPTOL", DENATURANT, 0d, "470-82-6", "Flam. Liq. 3:H226, Skin Sens. 1:H317", 2480.0, null, null, true));
		ingredients.add(createIngListItemWithType("EUGENIA CARYOPHYLLUS OIL", null, 0d, "8000-34-8", "Eye Irrit. 2:H319, Skin Sens. 1:H317", 1370.0, null, null, true));
		ingredients.add(createIngListItemWithType("EUGENOL", DENATURANT, 0d, "97-53-0", "Eye Irrit. 2:H319, Skin Sens. 1:H317", 1930.0, null, null, true));
		ingredients.add(createIngListItemWithType("EUGENYL ACETATE", MASKING, 0d, "93-28-7", "Acute Tox. 4:H302, Skin Sens. 1:H317", 1670.0, null, null, true));
		ingredients.add(createIngListItemWithType("Ethylene oxide", null, 0d, "75-21-8", "Flam. Gas 1:H220, Acute Tox. 3:H331", 72.0, null, null, true));
		ingredients.add(createIngListItemWithType("FORMALDEHYDE", null, 0d, "50-00-0", "Skin Corr. 1B:H314, Skin Sens. 1:H317", 100.0, 270.0, null, true));
		ingredients.add(createIngListItemWithType("FUROCOUMARINES", null, 0d, "66-97-7", "Skin Sens. 1:H317", null, null, null, true));
		ingredients.add(createIngListItemWithType("GAMMA-TERPINENE", "FRAGRANCE/FLAVORING", 0d, "99-85-4", "Flam. Liq. 3:H226", 3650.0, null, null, false));
		ingredients.add(createIngListItemWithType("GERANIOL", "FRAGRANCE/FLAVORING", 0d, "106-24-1", "Skin Irrit. 2:H315, Eye Dam. 1:H318, Skin Sens. 1:H317", 3600.0, null, null, true));
		ingredients.add(createIngListItemWithType("GERANYL ACETATE", "FRAGRANCE/FLAVORING", 0d, "105-87-3", "Skin Irrit. 2:H315, Skin Sens. 1:H317", 6330.0, null, null, true));
		ingredients.add(createIngListItemWithType("GLYCOL", "HUMECTANT", 0d, "107-21-1", "Acute Tox. 4:H302", 4700.0, 10600.0, null, false));
		ingredients.add(createIngListItemWithType("HEXANE", null, 0d, "110-54-3", "Flam. Liq. 2:H225, Repr. 2:H361f", 16000.0, null, null, false));
		ingredients.add(createIngListItemWithType("ISOEUGENOL", "FLAVOURING", 0d, "97-54-1", "Acute Tox. 4:H302, Skin Sens. 1:H317", 1500.0, 1912.0, null, true));
		ingredients.add(createIngListItemWithType("ISOPROPYL ALCOHOL", "FOAM CONTROL", 0d, "67-63-0", "Flam. Liq. 2:H225, Eye Irrit. 2:H319", 5045.0, 12800.0, null, false));
		ingredients.add(createIngListItemWithType("JUNIPERUS VIRGINIANA OIL", MASKING, 0d, "8000-27-9", "Aquatic Chronic 1:H410", null, null, 1.0, false));
		ingredients.add(createIngListItemWithType("LAVANDULA OIL/EXTRACT", null, 0d, "8000-28-0", "Skin Sens. 1:H317, Aquatic Chronic 3:H412", null, null, 1.0, true));
		ingredients.add(createIngListItemWithType("LINALOOL", "DEODORANT", 0d, "78-70-6", "Skin Irrit. 2:H315, Eye Irrit. 2:H319, Skin Sens. 1B:H317", 2790.0, 5610.0, null, true));
		ingredients.add(createIngListItemWithType("LINALYL ACETATE", MASKING, 0d, "115-95-7", "Skin Irrit. 2:H315, Eye Irrit. 2:H319", 14550.0, null, null, false));
		ingredients.add(createIngListItemWithType("Lead", "NONE", 0d, "7439-92-1", "Repr. 1A:H360", 1000.0, null, null, false));
		ingredients.add(createIngListItemWithType("MERCURY", null, 0d, "7439-97-6", "Acute Tox. 2:H330, Repr. 1B:H360", null, null, null, false));
		ingredients.add(createIngListItemWithType("METHYL ALCOHOL", DENATURANT, 0d, "67-56-1", "Flam. Liq. 2:H225, Acute Tox. 3:H301, Acute Tox. 3:H311, Acute Tox. 3:H331", 100.0, 300.0, null, false));
		ingredients.add(createIngListItemWithType("METHYL HYDROGENATED ROSINATE", "FILM FORMING", 0d, "8050-31-5", "Aquatic Chronic 3:H412", null, null, null, false));
		ingredients.add(createIngListItemWithType("METHYLENEDIOXYPHENYL METHYLPROPANAL", "FRAGRANCE/FLAVORING", 0d, "1205-17-0", "Skin Sens. 1B:H317", 3600.0, null, null, true));
		ingredients.add(createIngListItemWithType("MYRCENE", "FRAGRANCE/FLAVORING", 0d, "123-35-3", "Flam. Liq. 3:H226, Skin Irrit. 2:H315", null, null, null, false));
		ingredients.add(createIngListItemWithType("NICKEL", null, 0d, "7440-02-0", "Skin Sens. 1:H317, Carc. 2:H351", 9000.0, null, null, true));
		ingredients.add(createIngListItemWithType("OXACYCLOHEXADECENONE", MASKING, 0d, "3100-36-5", "Skin Sens. 1:H317", null, null, null, true));
		ingredients.add(createIngListItemWithType("P-CYMENE", MASKING, 0d, "99-87-6", "Flam. Liq. 3:H226", 4750.0, null, null, false));
		ingredients.add(createIngListItemWithType("PERILLALDEHYDE", MASKING, 0d, "2111-75-3", "Skin Sens. 1:H317", 2500.0, null, null, true));
		ingredients.add(createIngListItemWithType("PHENETHYL ALCOHOL", MASKING, 0d, "60-12-8", "Acute Tox. 4:H302, Eye Irrit. 2:H319", 1790.0, 790.0, null, false));
		ingredients.add(createIngListItemWithType("PINENE", "FOAM CONTROL", 0d, "80-56-8", "Flam. Liq. 3:H226, Skin Sens. 1:H317", 3700.0, null, null, true));
		ingredients.add(createIngListItemWithType("PYRUVIC ACID", MASKING, 0d, "127-17-3", "Skin Corr. 1B:H314", null, null, null, false));
		ingredients.add(createIngListItemWithType("SAFROLE", null, 0d, "94-59-7", "Acute Tox. 4:H302, Carc. 1B:H350", 1950.0, null, null, false));
		ingredients.add(createIngListItemWithType("SODIUM CHLORIDE", "BULKING", 0d, "7647-14-5", "Eye Irrit. 2:H319", 3000.0, null, null, false));
		ingredients.add(createIngListItemWithType("TERPINEOL", MASKING, 0d, "98-55-5", "Skin Irrit. 2:H315, Eye Irrit. 2:H319", 4300.0, null, null, false));
		ingredients.add(createIngListItemWithType("TERPINOLENE", "FRAGRANCE/FLAVORING", 0d, "586-62-9", "Skin Sens. 1:H317", 4390.0, null, null, true));
		ingredients.add(createIngListItemWithType("TETRAMETHYL ACETYLOCTAHYDRONAPHTHALENES", MASKING, 0d, "54464-57-2", "Skin Sens. 1:H317, Aquatic Chronic 1:H410", null, null, 1.0, true));
		ingredients.add(createIngListItemWithType("TRIETHYL CITRATE", MASKING, 0d, "77-89-4", null, 5900.0, null, null, false));
		ingredients.add(createIngListItemWithType("Trichloroethylene", "ABRASIVE", 0d, "79-01-6", "Skin Irrit. 2:H315, Eye Irrit. 2:H319, Carc. 1B:H350", 4920.0, 29000.0, null, false));
		ingredients.add(createIngListItemWithType("VANILLIN", MASKING, 0d, "121-33-5", "Eye Irrit. 2:H319", 1580.0, null, null, false));

		return ingredients;
	}

	/**
	 * <p>createIngListItemWithType.</p>
	 *
	 * @param ingName a {@link java.lang.String} object
	 * @param ingTypeValue a {@link java.lang.String} object
	 * @param ingQtyPerc a double
	 * @param casNumber a {@link java.lang.String} object
	 * @param hazardClass a {@link java.lang.String} object
	 * @param toxicityOral a {@link java.lang.Double} object
	 * @param toxicityDermal a {@link java.lang.Double} object
	 * @param mFactor a {@link java.lang.Double} object
	 * @param superSensitizing a {@link java.lang.Boolean} object
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 */
	private IngListDataItem createIngListItemWithType(String ingName, String ingTypeValue, double ingQtyPerc, String casNumber, String hazardClass,
	                                                  Double toxicityOral, Double toxicityDermal, Double mFactor, Boolean superSensitizing) {
		NodeRef ingType = null;

		if (ingTypeValue != null && !ingTypeValue.isEmpty()) {
			ingType = CharactTestHelper.getOrCreateIngType(nodeService, ingTypeValue);
		}

		// Ensure the ingredient node is registered with its type if any
		CharactTestHelper.getOrCreateIng(nodeService, ingName, ingType);

		// Use the parent's createIngListItem method passing all parameters
		return createIngListItem(ingName, ingQtyPerc, casNumber, hazardClass, toxicityOral, toxicityDermal, mFactor, superSensitizing);
	}
}
