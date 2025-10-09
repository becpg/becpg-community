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
	}

	// Static inner Builder class
	public static class Builder extends StandardSoapTestProduct.Builder {

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public StandardBodyMilkTestProduct build() {
			return new StandardBodyMilkTestProduct(this);
		}
	}

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

	private List<RegulatoryListDataItem> createRegulatoryList() {
		List<RegulatoryListDataItem> regulatoryList = new ArrayList<>();
		regulatoryList.add(
				createRegulatoryListItem(
				List.of("France", "Germany", "Spain", "Italy", "Albania", "Egypt", "European Union"),
				List.of("Body Cream", "Body Soap", "Hand Soap"))
				);
		return regulatoryList;
	}

	private RegulatoryListDataItem createRegulatoryListItem(List<String> countries, List<String> usages) {
		RegulatoryListDataItem item = new RegulatoryListDataItem();
		item.setRegulatoryCountriesRef(countries.stream().map(this::getOrCreateCountryRef).toList());
		item.setRegulatoryUsagesRef(usages.stream().map(this::getOrCreateUsageRef).toList());
		return item;
	}

	private NodeRef getOrCreateUsageRef(String usage) {
		HashMap<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, usage);
		properties.put(PLMModel.PROP_REGULATORY_CODE, usage);
		properties.put(PLMModel.PROP_REGULATORY_MODULE, "COSMETICS");
		return CharactTestHelper.getOrCreateNode(nodeService,
				"/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:RegulatoryUsages", usage, PLMModel.TYPE_REGULATORY_USAGE, properties);
	}

	private NodeRef getOrCreateCountryRef(String country) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PLMModel.PROP_REGULATORY_CODE, country);
		return CharactTestHelper.getOrCreateGeo(nodeService, country, country, properties);
	}

	private void saveEntityAssociations(RawMaterialData bodyMilkProduct) {
		if (bodyMilkProduct.getProductSpecifications() != null) {
			for (fr.becpg.repo.product.data.ProductSpecificationData productSpecificationData : bodyMilkProduct.getProductSpecifications()) {
				nodeService.createAssociation(bodyMilkProduct.getNodeRef(), productSpecificationData.getNodeRef(), 
						fr.becpg.model.PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			}
		}
	}

	private List<IngListDataItem> createBodyMilkIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Create ingredients with their types
		ingredients.add(createIngListItemWithType("AQUA", "SOLVENT", 77.18435));
		ingredients.add(createIngListItemWithType("GLYCERIN", "DENATURANT", 4.9985));
		ingredients.add(createIngListItemWithType("HELIANTHUS ANNUUS SEED OIL", "EMOLLIENT", 4.05));
		ingredients.add(createIngListItemWithType("CAPRYLIC/CAPRIC TRIGLYCERIDE", "MASKING", 3.5));
		ingredients.add(createIngListItemWithType("CETYL PALMITATE", "EMOLLIENT", 2.5));
		ingredients.add(createIngListItemWithType("BUTYROSPERMUM PARKII BUTTER", "SKIN CONDITIONING", 2.0));
		ingredients.add(createIngListItemWithType("CETEARYL GLUCOSIDE", "EMULSIFYING", 1.875));
		ingredients.add(createIngListItemWithType("PARFUM", "DEODORANT", 1.2));
		ingredients.add(createIngListItemWithType("SORBITAN OLIVATE", "EMULSIFYING", 0.975));
		ingredients.add(createIngListItemWithType("XANTHAN GUM", "BINDING", 0.7));
		ingredients.add(createIngListItemWithType("CAPRYLYL GLYCOL", "EMOLLIENT", 0.5));
		ingredients.add(createIngListItemWithType("ETHYLHEXYLGLYCERIN", "SKIN CONDITIONING", 0.1998));
		ingredients.add(createIngListItemWithType("CETEARYL ALCOHOL", "EMOLLIENT", 0.15));
		ingredients.add(createIngListItemWithType("SODIUM STEAROYL GLUTAMATE", "CLEANSING", 0.1));
		ingredients.add(createIngListItemWithType("TOCOPHEROL", "ANTIOXIDANT", 0.0502));
		ingredients.add(createIngListItemWithType("CITRIC ACID", "ACIDIC/BASIC/BUFFER", 0.013));
		ingredients.add(createIngListItemWithType("RHEUM PALMATUM ROOT EXTRACT", "ASTRINGENT", 0.0025));
		ingredients.add(createIngListItemWithType("CITRUS PARADISI FRUIT EXTRACT", "SKIN CONDITIONING", 0.00075));
		ingredients.add(createIngListItemWithType("SODIUM BENZOATE", "ANTICORROSIVE", 0.0006));
		ingredients.add(createIngListItemWithType("POTASSIUM SORBATE", "PRESERVATIVE", 0.0003));
		ingredients.add(createIngListItemWithType("3-MCPD", null, 0));
		ingredients.add(createIngListItemWithType("ACETALDEHYDE", "MASKING", 0));
		ingredients.add(createIngListItemWithType("ACETYLCEDRENE", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("AFLATOXIN B2", null, 0));
		ingredients.add(createIngListItemWithType("AFLATOXINS B1", null, 0));
		ingredients.add(createIngListItemWithType("AFLATOXINS G1", null, 0));
		ingredients.add(createIngListItemWithType("AFLATOXINS G2", null, 0));
		ingredients.add(createIngListItemWithType("ALPHA-TERPINENE", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("AMYLVINYLCARBINYL ACETATE", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("ANTIMONY", null, 0));
		ingredients.add(createIngListItemWithType("ARSENIC", null, 0));
		ingredients.add(createIngListItemWithType("BENZYL ALCOHOL", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("BETA-CARYOPHYLLENE", "MASKING", 0));
		ingredients.add(createIngListItemWithType("BUTYRIC ACID", "MASKING", 0));
		ingredients.add(createIngListItemWithType("Benzo[def]chrysene (Benzo[a]pyrene)", null, 0));
		ingredients.add(createIngListItemWithType("CAMPHOR", "DENATURANT", 0));
		ingredients.add(createIngListItemWithType("CARVONE", "FLAVOURING", 0));
		ingredients.add(createIngListItemWithType("CHROMIUM", null, 0));
		ingredients.add(createIngListItemWithType("CITRAL", "FLAVOURING", 0));
		ingredients.add(createIngListItemWithType("CITRONELLOL", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("CITRUS AURANTIUM PEEL OIL", null, 0));
		ingredients.add(createIngListItemWithType("CITRUS LIMON PEEL OIL", "MASKING", 0));
		ingredients.add(createIngListItemWithType("COBALT", null, 0));
		ingredients.add(createIngListItemWithType("COPPER", null, 0));
		ingredients.add(createIngListItemWithType("COUMARIN", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("Cadmium", "NONE", 0));
		ingredients.add(createIngListItemWithType("Chrysene", null, 0));
		ingredients.add(createIngListItemWithType("D-LIMONENE", "MASKING", 0));
		ingredients.add(createIngListItemWithType("DIETHYLENE GLYCOL", null, 0));
		ingredients.add(createIngListItemWithType("DIMETHYL PHENETHYL ACETATE", null, 0));
		ingredients.add(createIngListItemWithType("DIPROPYLENE GLYCOL", "MASKING", 0));
		ingredients.add(createIngListItemWithType("EUCALYPTOL", "DENATURANT", 0));
		ingredients.add(createIngListItemWithType("EUGENIA CARYOPHYLLUS OIL", null, 0));
		ingredients.add(createIngListItemWithType("EUGENOL", "DENATURANT", 0));
		ingredients.add(createIngListItemWithType("EUGENYL ACETATE", "MASKING", 0));
		ingredients.add(createIngListItemWithType("Ethylene oxide", null, 0));
		ingredients.add(createIngListItemWithType("FORMALDEHYDE", null, 0));
		ingredients.add(createIngListItemWithType("FUROCOUMARINES", null, 0));
		ingredients.add(createIngListItemWithType("GAMMA-TERPINENE", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("GERANIOL", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("GERANYL ACETATE", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("GLYCOL", "HUMECTANT", 0));
		ingredients.add(createIngListItemWithType("HEXANE", null, 0));
		ingredients.add(createIngListItemWithType("ISOEUGENOL", "FLAVOURING", 0));
		ingredients.add(createIngListItemWithType("ISOPROPYL ALCOHOL", "FOAM CONTROL", 0));
		ingredients.add(createIngListItemWithType("JUNIPERUS VIRGINIANA OIL", "MASKING", 0));
		ingredients.add(createIngListItemWithType("LAVANDULA OIL/EXTRACT", null, 0));
		ingredients.add(createIngListItemWithType("LINALOOL", "DEODORANT", 0));
		ingredients.add(createIngListItemWithType("LINALYL ACETATE", "MASKING", 0));
		ingredients.add(createIngListItemWithType("Lead", "NONE", 0));
		ingredients.add(createIngListItemWithType("MERCURY", null, 0));
		ingredients.add(createIngListItemWithType("METHYL ALCOHOL", "DENATURANT", 0));
		ingredients.add(createIngListItemWithType("METHYL HYDROGENATED ROSINATE", "FILM FORMING", 0));
		ingredients.add(createIngListItemWithType("METHYLENEDIOXYPHENYL METHYLPROPANAL", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("MYRCENE", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("NICKEL", null, 0));
		ingredients.add(createIngListItemWithType("OXACYCLOHEXADECENONE", "MASKING", 0));
		ingredients.add(createIngListItemWithType("P-CYMENE", "MASKING", 0));
		ingredients.add(createIngListItemWithType("PERILLALDEHYDE", "MASKING", 0));
		ingredients.add(createIngListItemWithType("PHENETHYL ALCOHOL", "MASKING", 0));
		ingredients.add(createIngListItemWithType("PINENE", "FOAM CONTROL", 0));
		ingredients.add(createIngListItemWithType("PYRUVIC ACID", "MASKING", 0));
		ingredients.add(createIngListItemWithType("SAFROLE", null, 0));
		ingredients.add(createIngListItemWithType("SODIUM CHLORIDE", "BULKING", 0));
		ingredients.add(createIngListItemWithType("TERPINEOL", "MASKING", 0));
		ingredients.add(createIngListItemWithType("TERPINOLENE", "FRAGRANCE/FLAVORING", 0));
		ingredients.add(createIngListItemWithType("TETRAMETHYL ACETYLOCTAHYDRONAPHTHALENES", "MASKING", 0));
		ingredients.add(createIngListItemWithType("TRIETHYL CITRATE", "MASKING", 0));
		ingredients.add(createIngListItemWithType("Trichloroethylene", "ABRASIVE", 0));
		ingredients.add(createIngListItemWithType("VANILLIN", "MASKING", 0));

		return ingredients;
	}

	private IngListDataItem createIngListItemWithType(String ingName, String ingTypeValue, double ingQtyPerc) {
		NodeRef ingType = null;
		
		if (ingTypeValue != null && !ingTypeValue.isEmpty()) {
			ingType = CharactTestHelper.getOrCreateIngType(nodeService, ingTypeValue);
		}
		
		NodeRef ing = CharactTestHelper.getOrCreateIng(nodeService, ingName, ingType);

		return IngListDataItem.build().withIngredient(ing).withQtyPerc(ingQtyPerc);
	}
}