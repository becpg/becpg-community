package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GHSModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PackModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.ToxType;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem;
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem.SignalWord;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ToxListDataItem;
import fr.becpg.repo.product.formulation.clp.HazardClassificationFormulaContext;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;

/**
 * <p>StandardSoapTestProduct class.</p>
 *
 * @author matthieu
 */
public class StandardSoapTestProduct extends SampleProductBuilder {

	/** Constant <code>H226_FORBIDDEN="Product should not contain H226"</code> */
	public static final String H226_FORBIDDEN = "Product should not contain H226";
	/** Constant <code>H290_DANGER_FORBIDDEN="Product should not contain H290 with Da"{trunked}</code> */
	public static final String H290_DANGER_FORBIDDEN = "Product should not contain H290 with Danger";
	/** Constant <code>DANGER_FORBIDDEN="Product should not contain Danger"</code> */
	public static final String DANGER_FORBIDDEN = "Product should not contain Danger";
	/** Constant <code>GHS07_FORBIDDEN="Product should not contain Pictogram GH"{trunked}</code> */
	public static final String GHS07_FORBIDDEN = "Product should not contain Pictogram GHS07";
	/** Constant <code>CLIMATE_CHANGE="Climate change"</code> */
	public static final String CLIMATE_CHANGE = "Climate change";

	// Transport subcategories
	/** Constant <code>CLIENT_TRANSPORT_IMPACTS="Client transport carbon impact (kg CO2/"{trunked}</code> */
	public static final String CLIENT_TRANSPORT_IMPACTS = "Client transport carbon impact (kg CO2/kg/km)";
	/** Constant <code>SUPPLIER_TRANSPORT_IMPACT="Supplier transport carbon impact (kCO2/"{trunked}</code> */
	public static final String SUPPLIER_TRANSPORT_IMPACT = "Supplier transport carbon impact (kCO2/kg/km)";

	/** Constant <code>EPI_SCORE="EPI Score"</code> */
	public static final String EPI_SCORE = "EPI Score";
	/** Constant <code>SPI_SCORE="SPI Score"</code> */
	public static final String SPI_SCORE = "SPI Score";

	/** Constant <code>SUPPLIER_ECOVADIS_CERTIFICATION="Supplier Ecovadis Certification Score"</code> */
	public static final String SUPPLIER_ECOVADIS_CERTIFICATION = "Supplier Ecovadis Certification Score";

	/** Constant <code>CLAIM_COLD_PROCESS="Cold process soap"</code> */
	public static final String CLAIM_COLD_PROCESS = "Cold process soap";
	/** Constant <code>CLAIM_PALM_OIL_FREE="Palm oil free"</code> */
	public static final String CLAIM_PALM_OIL_FREE = "Palm oil free";

	private static final String CHARACTS_PATH = "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:";
	private static final String LISTS_PATH = "/app:company_home/cm:System/cm:Lists/bcpg:entityLists/cm:";
	private static final String PACK_MATERIALS_LIST = "pmMaterials";
	private static final String MICROBIO_UNITS_LIST = "MicrobioUnits";
	private static final String ALLERGENS_LIST = "Allergens";
	private static final String MICROBIOS_LIST = "Microbios";
	private static final String PROCESS_STEPS_LIST = "ProcessSteps";
	private static final String REGULATORY_USAGES_PATH = CHARACTS_PATH + "RegulatoryUsages";
	private static final String REGULATORY_MODULE_COSMETICS = "COSMETICS";
	private static final String CFU_PER_G = "CFU/g";
	private static final String INCI_LIST_RULE = "INCI list";
	private static final String RENDER_FORMULA = "render()";
	private static final String REPORT_LOCALE_ENGLISH = "en";
	private static final List<String> LABELING_LOCALES = List.of(REPORT_LOCALE_ENGLISH, "fr");
	private static final String CLAIM_COLD_PROCESS_CODE = "COLD_PROCESS";
	private static final String CLAIM_PALM_OIL_FREE_CODE = "PALM_OIL_FREE";
	private static final String FRANCE_CODE = "FR";
	private static final String FRANCE = "France";
	private static final String EU_CODE = "EU";
	private static final String EUROPEAN_UNION = "European Union";
	private static final String USAGE_BODY_SOAP = "Body soap";
	private static final String USAGE_BODY_SOAP_CODE = "COSMETIC_BODY_SOAP,DECERNIS_Body Soap";
	private static final String USAGE_HAND_SOAP = "Hand soap";
	private static final String USAGE_HAND_SOAP_CODE = "COSMETIC_HAND_SOAP,DECERNIS_Hand Soap";

	/** Labeling threshold of the fragrance allergens, in percent: the leave-on one, the strictest of Regulation (EC) No 1223/2009. */
	private static final double ALLERGEN_LABELING_THRESHOLD = 0.001d;

	/** A packaging material with its constitutive material, tare in grams, packaging level and quantity per product. */
	protected record PackagingSpec(String name, String material, double tare, PackagingLevel level, double qty) {
	}

	/** A manufacturing process step and its description. */
	protected record ProcessStepSpec(String name, String description) {
	}

	/** A microbiological criterion: a maximum count in CFU/g, or a text criterion such as an absence. */
	protected record MicrobioSpec(String name, Double maxi, String textCriteria) {
	}

	/** A fragrance allergen and its share in the product holding it, in percent. */
	protected record AllergenShare(String name, double qtyPerc) {
	}

	private static final List<PackagingSpec> SOAP_PACKAGING = List.of(
			new PackagingSpec("Soap wrapper kraft paper", "Kraft paper", 5d, PackagingLevel.Primary, 1d),
			new PackagingSpec("Corrugated case x24", "Corrugated cardboard", 250d, PackagingLevel.Secondary, 1d / 24d));

	private static final List<ProcessStepSpec> SOAP_PROCESS = List.of(
			new ProcessStepSpec("Saponification", "Mix the olive oil with the sodium hydroxide solution at 40 °C until trace."),
			new ProcessStepSpec("Moulding", "Add the essential oils and pour the paste into the moulds."),
			new ProcessStepSpec("Curing", "Cut the bars and let them cure for four weeks."));

	private static final List<MicrobioSpec> SOAP_MICROBIOLOGICAL_LIMITS = List.of(new MicrobioSpec("Total aerobic mesophilic count", 1000d, null),
			new MicrobioSpec("Yeasts and moulds", 100d, null));

	/** Fragrance allergens of the essential oils mix: linalool from lavender, limonene from eucalyptus and tea tree. */
	private static final List<AllergenShare> ESSENTIAL_OILS_ALLERGENS = List.of(new AllergenShare("Linalool", 12d),
			new AllergenShare("Limonene", 3d));

	protected boolean isWithCompo = true;
	protected boolean isWithScore = false;
	protected boolean isWithPhysico = true;
	protected boolean isWithSpecification = false;
	protected boolean isWithToxicology = false;
	protected boolean isWithPackaging = false;
	protected boolean isWithProcess = false;
	protected boolean isWithLabeling = false;
	protected boolean isWithClaim = false;
	protected boolean isWithMicrobio = false;
	protected boolean isWithAllergens = false;
	protected boolean isWithRegulatory = false;

	/**
	 * <p>Constructor for StandardSoapTestProduct.</p>
	 *
	 * @param builder a {@link fr.becpg.repo.sample.StandardSoapTestProduct.Builder} object
	 */
	protected StandardSoapTestProduct(Builder builder) {
		super(builder);
		this.isWithCompo = builder.isWithCompo;
		this.isWithPhysico = builder.isWithPhysico;
		this.isWithSpecification = builder.isWithSpecification;
		this.isWithScore = builder.isWithScore;
		this.isWithToxicology = builder.isWithToxicology;
		this.isWithPackaging = builder.isWithPackaging;
		this.isWithProcess = builder.isWithProcess;
		this.isWithLabeling = builder.isWithLabeling;
		this.isWithClaim = builder.isWithClaim;
		this.isWithMicrobio = builder.isWithMicrobio;
		this.isWithAllergens = builder.isWithAllergens;
		this.isWithRegulatory = builder.isWithRegulatory;
	}

	// Static inner Builder class
	public static class Builder extends SampleProductBuilder.Builder<Builder> {
		private boolean isWithCompo = true;
		private boolean isWithPhysico = true;
		private boolean isWithSpecification = false;
		private boolean isWithScore = false;
		private boolean isWithToxicology = false;
		private boolean isWithPackaging = false;
		private boolean isWithProcess = false;
		private boolean isWithLabeling = false;
		private boolean isWithClaim = false;
		private boolean isWithMicrobio = false;
		private boolean isWithAllergens = false;
		private boolean isWithRegulatory = false;

		public Builder withCompo(boolean isWithCompo) {
			this.isWithCompo = isWithCompo;
			return this;
		}

		public Builder withPhysico(boolean isWithPhysico) {
			this.isWithPhysico = isWithPhysico;
			return this;
		}

		public Builder withSpecification(boolean isWithSpecification) {
			this.isWithSpecification = isWithSpecification;
			return this;
		}

		public Builder withScore(boolean isWithScore) {
			this.isWithScore = isWithScore;
			return this;
		}
		
		public Builder withToxicology(boolean isWithToxicology) {
			this.isWithToxicology = isWithToxicology;
			return this;
		}

		/**
		 * @param isWithPackaging true to create the packaging list and its packaging materials
		 * @return this builder
		 */
		public Builder withPackaging(boolean isWithPackaging) {
			this.isWithPackaging = isWithPackaging;
			return this;
		}

		/**
		 * @param isWithProcess true to create the process list and its process steps
		 * @return this builder
		 */
		public Builder withProcess(boolean isWithProcess) {
			this.isWithProcess = isWithProcess;
			return this;
		}

		/**
		 * @param isWithLabeling true to create the labeling rules
		 * @return this builder
		 */
		public Builder withLabeling(boolean isWithLabeling) {
			this.isWithLabeling = isWithLabeling;
			return this;
		}

		/**
		 * @param isWithClaim true to create the claim list
		 * @return this builder
		 */
		public Builder withClaim(boolean isWithClaim) {
			this.isWithClaim = isWithClaim;
			return this;
		}

		/**
		 * @param isWithMicrobio true to create the microbiological criteria list
		 * @return this builder
		 */
		public Builder withMicrobio(boolean isWithMicrobio) {
			this.isWithMicrobio = isWithMicrobio;
			return this;
		}

		/**
		 * @param isWithAllergens true to create the allergen lists of the raw materials
		 * @return this builder
		 */
		public Builder withAllergens(boolean isWithAllergens) {
			this.isWithAllergens = isWithAllergens;
			return this;
		}

		/**
		 * @param isWithRegulatory true to create the regulatory list (markets and usages)
		 * @return this builder
		 */
		public Builder withRegulatory(boolean isWithRegulatory) {
			this.isWithRegulatory = isWithRegulatory;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public StandardSoapTestProduct build() {
			return new StandardSoapTestProduct(this);
		}

	}

	// Raw Material Names
	/** Constant <code>SODIUM_HYDROXIDE="Sodium Hydroxide"</code> */
	public static final String SODIUM_HYDROXIDE = "Sodium Hydroxide";
	/** Constant <code>SODIUM_CARBONATE="Sodium Carbonate"</code> */
	public static final String SODIUM_CARBONATE = "Sodium Carbonate";
	/** Constant <code>SODIUM_CHLORIDE="Sodium Chloride"</code> */
	public static final String SODIUM_CHLORIDE = "Sodium Chloride";
	/** Constant <code>OLIVE_OIL="Olive Oil"</code> */
	public static final String OLIVE_OIL = "Olive Oil";
	/** Constant <code>ESSENTIAL_OILS="Essential Oils Mix"</code> */
	public static final String ESSENTIAL_OILS = "Essential Oils Mix";

	protected NodeRef sodiumHydroxideNodeRef;
	protected NodeRef oliveOilNodeRef;
	protected NodeRef essentialOilsNodeRef;

	/** {@inheritDoc} */
	@Override
	public ProductData createTestProduct() {

		// Create the soap finished product
		FinishedProductData soapProduct = FinishedProductData.build().withName(uniqueName("🧼 Standard Natural Olive Soap 🫒💧"))
				.withUnit(ProductUnit.kg).withQty(1000d).withDensity(1.2d);

		if (isWithScore) {
			
			nodeService.setProperty(CharactTestHelper.getOrCreateScoreCriterion(nodeService, CLIENT_TRANSPORT_IMPACTS),ProjectModel.PROP_SCORE_CRITERION_FORMULATED, true);
			
			//Client
			ClientData clientData = ClientData.build().withName(uniqueName("Natural Soap Dealer Shop")).withScoreList(List.of(ScoreListDataItem
					.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, CLIENT_TRANSPORT_IMPACTS)).withScore(55d)));

			alfrescoRepository.create(destFolder, clientData);
			soapProduct.setClients(List.of(clientData));
			soapProduct.setScoreList(new ArrayList<>());
		}

		if (isWithCompo || isWithToxicology) {
			// Initialize raw materials if not already done
			if (sodiumHydroxideNodeRef == null) {
				initRawMaterialsWithIngredients();
			}

			soapProduct = soapProduct.withCompoList(List.of(
					CompoListDataItem.build().withQtyUsed(130d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(sodiumHydroxideNodeRef),
					CompoListDataItem.build().withQtyUsed(800d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(oliveOilNodeRef),
					CompoListDataItem.build().withQtyUsed(70d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(essentialOilsNodeRef)));
		}

		if (isWithPhysico) {

			createPhysicoChems(soapProduct);

		}

		if (isWithSpecification) {
			soapProduct.setProductSpecifications(createProductSpecifications());
		}
		
		if (isWithToxicology) {
			soapProduct.setToxList(createToxList());
		}

		addOptionalLists(soapProduct);

		alfrescoRepository.create(destFolder, soapProduct);

		saveEntityAssociations(soapProduct);

		return soapProduct;
	}

	/**
	 * <p>createToxList.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	protected List<ToxListDataItem> createToxList() {
		List<ToxListDataItem> toxList = new ArrayList<>();
		ToxListDataItem toxListDataItem = new ToxListDataItem();
		toxListDataItem.setTox(CharactTestHelper.getOrCreateTox(nodeService, "Adult RO Face", 140.0, true, true,
				List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.OcularIrritation, ToxType.SystemicIngredient)));
		toxList.add(toxListDataItem);
		
		toxListDataItem = new ToxListDataItem();
		toxListDataItem.setTox(CharactTestHelper.getOrCreateTox(nodeService, "Adult RO Hair", 110.0, true, true,
				List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.SystemicIngredient)));
		toxList.add(toxListDataItem);
		
		toxListDataItem = new ToxListDataItem();
		toxListDataItem.setTox(CharactTestHelper.getOrCreateTox(nodeService, "Adult RO Body", 220.0, true, true,
				List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.SystemicIngredient)));
		toxList.add(toxListDataItem);
		return toxList;
	}

	/**
	 * Adds the optional lists the builder enables: packaging, process, labeling rules, claims, microbiological criteria
	 * and regulatory markets. Each list comes from its creation method, which a subclass overrides with its own data.
	 * The allergens belong to the raw materials, so each product sets them where it creates its raw materials.
	 *
	 * @param product the product, not created yet
	 */
	protected void addOptionalLists(ProductData product) {
		if (isWithPackaging) {
			product.getPackagingListView().setPackagingList(createPackagingList());
		}
		if (isWithProcess) {
			product.getProcessListView().setProcessList(createProcessList());
		}
		if (isWithLabeling) {
			product.getLabelingListView().setLabelingRuleList(createLabelingRules());
		}
		if (isWithClaim) {
			product.setLabelClaimList(createClaimList());
		}
		if (isWithMicrobio) {
			product.setMicrobioList(createMicrobioList());
		}
		if (isWithRegulatory) {
			product.setRegulatoryList(createMarketList());
		}
	}

	/**
	 * Creates the packaging of the soap bar: a kraft paper wrapper, packed by 24 in a corrugated case.
	 *
	 * @return the packaging list
	 */
	protected List<PackagingListDataItem> createPackagingList() {
		return buildPackagingList(SOAP_PACKAGING);
	}

	/**
	 * Creates the cold process manufacturing steps of the soap.
	 *
	 * @return the process list
	 */
	protected List<ProcessListDataItem> createProcessList() {
		return buildProcessList(SOAP_PROCESS);
	}

	/**
	 * Creates the labeling rule rendering the INCI list.
	 *
	 * @return the labeling rules
	 */
	protected List<LabelingRuleListDataItem> createLabelingRules() {
		return new ArrayList<>(List.of(createInciRenderRule()));
	}

	/**
	 * Creates the manual claims of the soap.
	 *
	 * @return the claim list
	 */
	protected List<LabelClaimListDataItem> createClaimList() {
		return new ArrayList<>(List.of(createClaim(CLAIM_COLD_PROCESS_CODE, CLAIM_COLD_PROCESS, null),
				createClaim(CLAIM_PALM_OIL_FREE_CODE, CLAIM_PALM_OIL_FREE, null)));
	}

	/**
	 * Creates the microbiological limits of a low risk product, as an anhydrous soap of high pH is under ISO 29621.
	 *
	 * @return the microbiological list
	 */
	protected List<MicrobioListDataItem> createMicrobioList() {
		return buildMicrobioList(SOAP_MICROBIOLOGICAL_LIMITS);
	}

	/**
	 * Creates the fragrance allergens declared by the supplier of the essential oils.
	 *
	 * @return the allergen list of the raw material holding the fragrance
	 */
	protected List<AllergenListDataItem> createAllergenList() {
		return buildAllergenList(ESSENTIAL_OILS_ALLERGENS);
	}

	/**
	 * Creates the market of the soap: body and hand soap sold in France and in the European Union.
	 *
	 * @return the regulatory list
	 */
	protected List<RegulatoryListDataItem> createMarketList() {
		return buildMarketList(List.of(getOrCreateCountryRef(FRANCE_CODE, FRANCE), getOrCreateCountryRef(EU_CODE, EUROPEAN_UNION)),
				List.of(getOrCreateUsageRef(USAGE_BODY_SOAP, USAGE_BODY_SOAP_CODE), getOrCreateUsageRef(USAGE_HAND_SOAP, USAGE_HAND_SOAP_CODE)));
	}

	/**
	 * Creates the packaging materials and the packaging lines they are used in.
	 *
	 * @param specs the packaging materials
	 * @return the packaging list
	 */
	protected List<PackagingListDataItem> buildPackagingList(List<PackagingSpec> specs) {
		List<PackagingListDataItem> packagingList = new ArrayList<>();
		for (PackagingSpec spec : specs) {
			packagingList.add(PackagingListDataItem.build().withQty(spec.qty()).withUnit(ProductUnit.P).withPkgLevel(spec.level())
					.withProduct(createPackagingMaterial(spec)));
		}
		return packagingList;
	}

	/**
	 * Creates a packaging material with its tare and constitutive material.
	 *
	 * @param spec the packaging material description
	 * @return the created packaging material
	 */
	private NodeRef createPackagingMaterial(PackagingSpec spec) {
		PackagingMaterialData packagingMaterial = PackagingMaterialData.build().withName(uniqueName(spec.name())).withQty(1d)
				.withUnit(ProductUnit.P).withTare(spec.tare(), TareUnit.g).withPackMaterialList(List.of(PackMaterialListDataItem.build()
						.withMaterial(getOrCreatePackMaterial(spec.material())).withPerc(100d).withPkgLevel(spec.level())));
		packagingMaterial.setReportLocales(List.of(REPORT_LOCALE_ENGLISH));
		return alfrescoRepository.create(destFolder, packagingMaterial).getNodeRef();
	}

	/**
	 * Creates the process lines, one for each described step.
	 *
	 * @param specs the process steps
	 * @return the process list
	 */
	protected List<ProcessListDataItem> buildProcessList(List<ProcessStepSpec> specs) {
		List<ProcessListDataItem> processList = new ArrayList<>();
		for (ProcessStepSpec spec : specs) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, spec.name());
			properties.put(ContentModel.PROP_DESCRIPTION, spec.description());
			processList.add(ProcessListDataItem.build().withStep(
					CharactTestHelper.getOrCreateNode(nodeService, CHARACTS_PATH + PROCESS_STEPS_LIST, spec.name(), MPMModel.TYPE_PROCESSSTEP, properties)));
		}
		return processList;
	}

	/**
	 * Creates the rule rendering the INCI list in English and in French; without explicit locales, it is only rendered
	 * in the server default language.
	 *
	 * @return the render rule
	 */
	protected LabelingRuleListDataItem createInciRenderRule() {
		LabelingRuleListDataItem render = LabelingRuleListDataItem.build().withName(INCI_LIST_RULE).withFormula(RENDER_FORMULA)
				.withLabelingRuleType(LabelingRuleType.Render);
		render.setLocales(LABELING_LOCALES);
		return render;
	}

	/**
	 * Creates a manual claim, claimed on the product: formulation does not derive it from the raw materials.
	 *
	 * @param code the claim code
	 * @param name the claim name
	 * @param percentClaim the claimed percentage, or null when the claim carries none
	 * @return the claim line
	 */
	protected LabelClaimListDataItem createClaim(String code, String name, Double percentClaim) {
		LabelClaimListDataItem claim = LabelClaimListDataItem.build().withLabelClaim(CharactTestHelper.getOrCreateClaim(nodeService, code, name))
				.withIsClaimed(true).withPercentClaim(percentClaim);
		claim.setIsManual(true);
		return claim;
	}

	/**
	 * Creates the microbiological lines, counted in CFU/g.
	 *
	 * @param specs the microbiological criteria
	 * @return the microbiological list
	 */
	protected List<MicrobioListDataItem> buildMicrobioList(List<MicrobioSpec> specs) {
		List<MicrobioListDataItem> microbioList = new ArrayList<>();
		for (MicrobioSpec spec : specs) {
			MicrobioListDataItem item = new MicrobioListDataItem();
			item.setMicrobio(getOrCreateListedCharact(spec.name(), PLMModel.TYPE_MICROBIO, MICROBIOS_LIST));
			item.setUnit(getOrCreateUnit(MICROBIO_UNITS_LIST, CFU_PER_G));
			item.setMaxi(spec.maxi());
			if (spec.textCriteria() != null) {
				item.setTextCriteria(new MLText(spec.textCriteria()));
			}
			microbioList.add(item);
		}
		return microbioList;
	}

	/**
	 * Creates the allergen lines of a raw material holding a fragrance, as voluntary allergens.
	 *
	 * @param allergens the allergens and their shares
	 * @return the allergen list
	 */
	protected List<AllergenListDataItem> buildAllergenList(List<AllergenShare> allergens) {
		List<AllergenListDataItem> allergenList = new ArrayList<>();
		for (AllergenShare allergen : allergens) {
			allergenList.add(AllergenListDataItem.build().withAllergen(getOrCreateFragranceAllergen(allergen.name()))
					.withQtyPerc(allergen.qtyPerc()).withVoluntary(true).withInVoluntary(false));
		}
		return allergenList;
	}

	/**
	 * Gets or creates a fragrance allergen with its labeling threshold.
	 *
	 * @param name the allergen name
	 * @return the allergen
	 */
	private NodeRef getOrCreateFragranceAllergen(String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(PLMModel.PROP_ALLERGEN_CODE, name.toUpperCase(Locale.ROOT));
		properties.put(PLMModel.PROP_ALLERGEN_TYPE, AllergenType.Minor.toString());
		properties.put(PLMModel.PROP_ALLERGEN_REGULATORY_THRESHOLD, ALLERGEN_LABELING_THRESHOLD);
		return CharactTestHelper.getOrCreateNode(nodeService, CHARACTS_PATH + ALLERGENS_LIST, name, PLMModel.TYPE_ALLERGEN, properties);
	}

	/**
	 * Creates a market line: the countries the product is sold in and its usages there.
	 *
	 * @param countries the countries
	 * @param usages the regulatory usages
	 * @return the regulatory list
	 */
	protected List<RegulatoryListDataItem> buildMarketList(List<NodeRef> countries, List<NodeRef> usages) {
		RegulatoryListDataItem market = new RegulatoryListDataItem();
		market.setRegulatoryCountriesRef(countries);
		market.setRegulatoryUsagesRef(usages);
		return new ArrayList<>(List.of(market));
	}

	/**
	 * <p>getOrCreateUsageRef.</p>
	 *
	 * @param name           the usage charact name
	 * @param regulatoryCode the regulatory code ({@code <beCPGcode>,DECERNIS_<phrase>})
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	protected NodeRef getOrCreateUsageRef(String name, String regulatoryCode) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(PLMModel.PROP_REGULATORY_CODE, regulatoryCode);
		properties.put(PLMModel.PROP_REGULATORY_MODULE, REGULATORY_MODULE_COSMETICS);
		return CharactTestHelper.getOrCreateNode(nodeService, REGULATORY_USAGES_PATH, name, PLMModel.TYPE_REGULATORY_USAGE, properties);
	}

	/**
	 * <p>getOrCreateCountryRef.</p>
	 *
	 * @param code a {@link java.lang.String} object
	 * @param name a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	protected NodeRef getOrCreateCountryRef(String code, String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PLMModel.PROP_REGULATORY_CODE, name); // so decernis still works
		return CharactTestHelper.getOrCreateGeo(nodeService, code, name, properties);
	}

	/**
	 * Gets or creates a characteristic of a system list.
	 *
	 * @param name the characteristic name
	 * @param type the characteristic type
	 * @param listName the name of the system list
	 * @return the characteristic
	 */
	protected NodeRef getOrCreateListedCharact(String name, QName type, String listName) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		return CharactTestHelper.getOrCreateNode(nodeService, CHARACTS_PATH + listName, name, type, properties);
	}

	/**
	 * Gets or creates a unit of a system list, as the unit properties only accept the values of their list. No code is
	 * set, so that an existing value is never given a code another value already holds.
	 *
	 * @param listName the name of the unit list
	 * @param unit the unit
	 * @return the unit, ready to be set on a property
	 */
	protected String getOrCreateUnit(String listName, String unit) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_VALUE, unit);
		CharactTestHelper.getOrCreateNode(nodeService, LISTS_PATH + listName, unit, BeCPGModel.TYPE_LIST_VALUE, properties);
		return unit;
	}

	/**
	 * Gets or creates a packaging material of the system list.
	 *
	 * @param name the material name
	 * @return the packaging material
	 */
	private NodeRef getOrCreatePackMaterial(String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_VALUE, name);
		return CharactTestHelper.getOrCreateNode(nodeService, LISTS_PATH + PACK_MATERIALS_LIST, name, PackModel.TYPE_PACKAGING_MATERIAL, properties);
	}

	/**
	 * <p>saveEntityAssociations.</p>
	 *
	 * @param soapProduct a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	private void saveEntityAssociations(FinishedProductData soapProduct) {

		if (soapProduct.getProductSpecifications() != null) {
			for (ProductSpecificationData productSpecificationData : soapProduct.getProductSpecifications()) {
				nodeService.createAssociation(soapProduct.getNodeRef(), productSpecificationData.getNodeRef(), PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			}
		}

		if (soapProduct.getClients() != null) {
			for (ClientData clientData : soapProduct.getClients()) {
				nodeService.createAssociation(soapProduct.getNodeRef(), clientData.getNodeRef(), PLMModel.ASSOC_CLIENTS);
			}
		}

	}

	/**
	 * <p>createPhysicoChems.</p>
	 *
	 * @param soapProduct a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	protected void createPhysicoChems(ProductData soapProduct) {
		// Add physico-chemical properties
		addPhysicoChemProperty(soapProduct, "Boiling point", HazardClassificationFormulaContext.BOILING_POINT, 78.0); // Boiling point
		addPhysicoChemProperty(soapProduct, "Flash point", HazardClassificationFormulaContext.FLASH_POINT, 23.0); // Flash point
		addPhysicoChemProperty(soapProduct, "Hydrocarbon", HazardClassificationFormulaContext.HYDROCARBON_PERC, 15.0); // Hydrocarbon percentage
	}

	/**
	 * <p>createProductSpecifications.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	protected List<ProductSpecificationData> createProductSpecifications() {

		ProductSpecificationData productSpecification = ProductSpecificationData.build().withName(uniqueName("🧼 Soap products specification 📋"))
				.withHcList(List.of(
						HazardClassificationListDataItem.build().withHazardStatement(CharactTestHelper.getOrCreateH(nodeService, "H226"))
								.withRegulatoryMessage(H226_FORBIDDEN),

						HazardClassificationListDataItem.build().withHazardStatement(CharactTestHelper.getOrCreateH(nodeService, "H290"))
								.withSignalWord(SignalWord.Danger.toString()).withRegulatoryMessage(H290_DANGER_FORBIDDEN),

						HazardClassificationListDataItem.build().withSignalWord(SignalWord.Danger.toString()).withRegulatoryMessage(DANGER_FORBIDDEN),

						HazardClassificationListDataItem.build().withPictogram(CharactTestHelper.getOrCreatePicto(nodeService, "GHS07"))
								.withRegulatoryMessage(GHS07_FORBIDDEN)))
				.withForbiddenIngList(List.of(ForbiddenIngListDataItem

						.build().withQtyPercMaxi(2d).withIngs(List.of(CharactTestHelper.getOrCreateIng(nodeService, SODIUM_CHLORIDE)))));

		alfrescoRepository.create(destFolder, productSpecification);
		return List.of(productSpecification);
	}

	/**
	 * <p>initRawMaterialsWithIngredients.</p>
	 */
	public void initRawMaterialsWithIngredients() {
		// Create Sodium Hydroxide raw material with ingredients
		RawMaterialData sodiumHydroxide = RawMaterialData.build().withName(uniqueName(SODIUM_HYDROXIDE)).withQty(100d).withUnit(ProductUnit.kg)
				.withIngList(createSodiumHydroxideIngredients())
				.withGeoOrigins(List.of(CharactTestHelper.getOrCreateGeo(nodeService, "DE", "Germany")));

		if (isWithScore) {
			
			nodeService.setProperty(CharactTestHelper.getOrCreateScoreCriterion(nodeService, EPI_SCORE),ProjectModel.PROP_SCORE_CRITERION_FORMULATED, true);
			nodeService.setProperty(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SPI_SCORE),ProjectModel.PROP_SCORE_CRITERION_FORMULATED, true);
			nodeService.setProperty(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SUPPLIER_TRANSPORT_IMPACT),ProjectModel.PROP_SCORE_CRITERION_FORMULATED, true);

			sodiumHydroxide = sodiumHydroxide.withScoreList(List.of(
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, EPI_SCORE))
							.withScore(62.4d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SPI_SCORE))
							.withScore(77.4d),
				 ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SUPPLIER_TRANSPORT_IMPACT))));
			addLCAProperty(sodiumHydroxide, CLIMATE_CHANGE, "CLIMATE_CHANGE", 2.51d);

		}

		sodiumHydroxideNodeRef = alfrescoRepository.create(destFolder, sodiumHydroxide).getNodeRef();

		// Create Olive Oil raw material with ingredients
		RawMaterialData oliveOil = RawMaterialData.build().withName(uniqueName(OLIVE_OIL)).withQty(500d).withUnit(ProductUnit.kg)
				.withIngList(createOliveOilIngredients()).withGeoOrigins(List.of(CharactTestHelper.getOrCreateGeo(nodeService, "France", "FR")));

		if (isWithScore) {

			oliveOil = oliveOil.withScoreList(List.of(
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, EPI_SCORE))
							.withScore(62.5d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SPI_SCORE))
							.withScore(77.5d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SUPPLIER_TRANSPORT_IMPACT))));
			addLCAProperty(sodiumHydroxide, CLIMATE_CHANGE, "CLIMATE_CHANGE", 109.99d);

		}

		oliveOilNodeRef = alfrescoRepository.create(destFolder, oliveOil).getNodeRef();

		// Create Essential Oils raw material with ingredients
		RawMaterialData essentialOils = RawMaterialData.build().withName(uniqueName(ESSENTIAL_OILS)).withQty(50d).withUnit(ProductUnit.kg)
				.withIngList(createEssentialOilsIngredients()).withGeoOrigins(List.of(CharactTestHelper.getOrCreateGeo(nodeService, "China", "CN")));

		if (isWithScore) {

			essentialOils = essentialOils.withScoreList(List.of(
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, EPI_SCORE))
							.withScore(28.4d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SPI_SCORE))
							.withScore(43.4d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SUPPLIER_TRANSPORT_IMPACT))));
			addLCAProperty(sodiumHydroxide, CLIMATE_CHANGE, "CLIMATE_CHANGE", 300d);

		}

		if (isWithAllergens) {
			essentialOils.setAllergenList(createAllergenList());
		}

		essentialOilsNodeRef = alfrescoRepository.create(destFolder, essentialOils).getNodeRef();
	}

	/**
	 * <p>createSodiumHydroxideIngredients.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	private List<IngListDataItem> createSodiumHydroxideIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Sodium Hydroxide ingredients
		ingredients.add(createIngListItem(SODIUM_HYDROXIDE, 80.0, "1310-73-2", "Skin Corr. 1A:H314, Met. Corr. 1:H290", 500.0, 1000.0, 1.0, true, null, null));
		ingredients.add(createIngListItem(SODIUM_CARBONATE, 10.0, "497-19-8", "Eye Irrit. 2:H319", 2800.0, 2000.0, null, false, null, null));
		ingredients.add(createIngListItem(SODIUM_CHLORIDE, 10.0, "7647-14-5", "Eye Irrit. 2:H319", 3000.0, null, null, false, null, null));

		if (isWithToxicology) {
			NodeRef ing = CharactTestHelper.getOrCreateIng(nodeService, SODIUM_HYDROXIDE);
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(PLMModel.PROP_ING_TOX_POD_SYSTEMIC, 10000);
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 18);
			properties.put(PLMModel.PROP_ING_TOX_MOS_MOE, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 80);
			properties.put(PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION, 50);
			properties.put(PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC, 85);
			nodeService.addProperties(ing, properties);
			
			ing = CharactTestHelper.getOrCreateIng(nodeService, SODIUM_CARBONATE);
			properties.put(PLMModel.PROP_ING_TOX_POD_SYSTEMIC, 1200);
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 50);
			properties.put(PLMModel.PROP_ING_TOX_MOS_MOE, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 5);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION, 0.2);
			properties.put(PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC, 100);
			nodeService.addProperties(ing, properties);
			
			ing = CharactTestHelper.getOrCreateIng(nodeService, SODIUM_CHLORIDE);
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 50);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 5);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 1);
			properties.put(PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION, 5);
			properties.put(PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC, 5);
			nodeService.addProperties(ing, properties);
		}
		
		return ingredients;
	}

	/**
	 * <p>createOliveOilIngredients.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	private List<IngListDataItem> createOliveOilIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Olive Oil ingredients
		ingredients.add(createIngListItem("Oleic Acid", 55.0, "112-80-1", "Skin Irrit. 2:H315", null, null, null, false, null, null));
		ingredients.add(createIngListItem("Linoleic Acid", 20.0, "60-33-3", "Skin Sens. 1:H317", null, null, 1.0, true, null, null));
		ingredients.add(createIngListItem("Palmitic Acid", 15.0, "57-10-3", "Eye Irrit. 2:H319", null, null, null, false, null, null));

		return ingredients;
	}

	/**
	 * <p>createEssentialOilsIngredients.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	private List<IngListDataItem> createEssentialOilsIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Essential Oils ingredients
		ingredients.add(createIngListItem("Lavender Oil", 40.0, "8000-28-0", "Skin Sens. 1:H317, Aquatic Chronic 3:H412", null, null, 1.0, true, null, null));
		ingredients.add(createIngListItem("Eucalyptus Oil", 30.0, "8000-48-4", "Flam. Liq. 3:H226, Skin Sens. 1:H317", null, null, 1.0, true, null, null));
		ingredients.add(createIngListItem("Tea Tree Oil", 30.0, "68647-73-4", "Flam. Liq. 3:H226, Acute Tox. 4:H302, Skin Sens. 1:H317", 1900.0, null,
				1.0, true, null, null));

		return ingredients;
	}

	/**
	 * <p>createIngListItem.</p>
	 *
	 * @param ingName          a {@link String} object
	 * @param percentage       a {@link Double} object
	 * @param casNumber        a {@link String} object
	 * @param hazardClass      a {@link String} object
	 * @param toxicityOral     a {@link Double} object
	 * @param toxicityDermal   a {@link Double} object
	 * @param mFactor          a {@link Double} object
	 * @param superSensitizing a {@link Boolean} object
	 * @param ceNumber
	 * @param ecNumber
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 */
	protected IngListDataItem createIngListItem(String ingName, Double percentage, String casNumber, String hazardClass,
	                                            Double toxicityOral, Double toxicityDermal, Double mFactor,
	                                            Boolean superSensitizing, String ceNumber, String ecNumber) {

		NodeRef ing = CharactTestHelper.getOrCreateIng(nodeService, ingName);

		Map<QName, Serializable> properties = new HashMap<>();
		if (casNumber != null)
			properties.put(PLMModel.PROP_CAS_NUMBER, casNumber);
		if (hazardClass != null)
			properties.put(GHSModel.PROP_SDS_HAZARD_CLASSIFICATIONS, hazardClass);
		if (ceNumber != null)
			properties.put(PLMModel.PROP_CE_NUMBER, ceNumber);
		if (ecNumber != null)
			properties.put(PLMModel.PROP_EC_NUMBER, ecNumber);
		properties.put(BeCPGModel.PROP_ING_TOX_ACUTE_ORAL, toxicityOral);
		properties.put(BeCPGModel.PROP_ING_TOX_ACUTE_DERMAL, toxicityDermal);
		properties.put(BeCPGModel.PROP_ING_TOX_AQUATIC_MFACTOR, mFactor);
		properties.put(BeCPGModel.PROP_ING_TOX_IS_SUPER_SENSITIZING, superSensitizing);

		nodeService.addProperties(ing, properties);

		return IngListDataItem.build().withQtyPerc(percentage).withIngredient(ing);
	}

	/**
	 * <p>addIngredient.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param ingName a {@link java.lang.String} object
	 * @param percentage a {@link java.lang.Double} object
	 * @param hazardClass a {@link java.lang.String} object
	 * @param toxicityOral a {@link java.lang.Double} object
	 * @param mFactor a {@link java.lang.Double} object
	 * @param superSensitizing a {@link java.lang.Boolean} object
	 */
	public void addIngredient(ProductData product, String ingName, Double percentage, String hazardClass, Double toxicityOral, Double mFactor,
			Boolean superSensitizing) {
		if (product.getIngList() == null) {
			product.setIngList(new ArrayList<>());
		}

		product.getIngList().add(createIngListItem(ingName, percentage, null, hazardClass, toxicityOral, null, mFactor, superSensitizing, null, null));

	}

	/**
	 * <p>addPhysicoChemProperty.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param name a {@link java.lang.String} object
	 * @param code a {@link java.lang.String} object
	 * @param value a {@link java.lang.Double} object
	 */
	public void addPhysicoChemProperty(ProductData product, String name, String code, Double value) {
		PhysicoChemListDataItem physicoChemList = new PhysicoChemListDataItem();

		NodeRef physicoChem = CharactTestHelper.getOrCreatePhysico(nodeService, name);

		Map<QName, Serializable> props = new HashMap<>();
		props.put(PLMModel.PROP_PHYSICO_CHEM_CODE, code);
		props.put(PLMModel.PROP_PHYSICO_CHEM_UNIT, "%");
		nodeService.addProperties(physicoChem, props);

		if (product.getPhysicoChemList() == null) {
			product.setPhysicoChemList(new ArrayList<>());
		}

		physicoChemList.setPhysicoChem(physicoChem);
		physicoChemList.setValue(value);
		product.getPhysicoChemList().add(physicoChemList);
	}

	/**
	 * <p>addLCAProperty.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param name a {@link java.lang.String} object
	 * @param code a {@link java.lang.String} object
	 * @param value a {@link java.lang.Double} object
	 */
	public void addLCAProperty(ProductData product, String name, String code, Double value) {
		LCAListDataItem lcaListDataItem = new LCAListDataItem();

		NodeRef lca = CharactTestHelper.getOrCreateLCA(nodeService, name);

		Map<QName, Serializable> props = new HashMap<>();
		props.put(PLMModel.PROP_LCA_CODE, code);
		props.put(PLMModel.PROP_LCAUNIT, CharactTestHelper.getOrCreateLCAUnit(nodeService, "kg CO2 eq"));
		nodeService.addProperties(lca, props);

		if (product.getLcaList() == null) {
			product.setLcaList(new ArrayList<>());
		}

		lcaListDataItem.setLca(lca);
		lcaListDataItem.setValue(value);
		product.getLcaList().add(lcaListDataItem);

	}

	// Getters
	/**
	 * <p>Getter for the field <code>sodiumHydroxideNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSodiumHydroxideNodeRef() {
		return sodiumHydroxideNodeRef;
	}

	/**
	 * <p>Getter for the field <code>oliveOilNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getOliveOilNodeRef() {
		return oliveOilNodeRef;
	}

	/**
	 * <p>Getter for the field <code>essentialOilsNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getEssentialOilsNodeRef() {
		return essentialOilsNodeRef;
	}

}
