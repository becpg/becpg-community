package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

// Removed unused import

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LogisticUnitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.impl.SurveyServiceImpl.ResponseType;
import org.alfresco.service.cmr.repository.MLText;

/**
 * <p>StandardChocolateEclairTestProduct class.</p>
 *
 * @author matthieu
 */
public class StandardChocolateEclairTestProduct extends SampleProductBuilder {

	/** Constant <code>WATER_NAME="Eau"</code> */
	public static final String WATER_NAME = "Eau";
	/** Constant <code>MILK_NAME="Lait"</code> */
	public static final String MILK_NAME = "Lait";
	/** Constant <code>CLAIM_EU_ORGANIC="EU_ORGANIC"</code> */
	public static final String CLAIM_EU_ORGANIC = "EU_ORGANIC";
	/** Constant <code>CLAIM_EU_ORGANIC_LABEL="EU organic agriculture"</code> */
	public static final String CLAIM_EU_ORGANIC_LABEL = "EU organic agriculture";
	/** Constant <code>CLAIM_KOSHER="KOSHER"</code> */
	public static final String CLAIM_KOSHER = "KOSHER";
	/** Constant <code>CLAIM_KOSHER_LABEL="Kosher"</code> */
	public static final String CLAIM_KOSHER_LABEL = "Kosher";
	/** Constant <code>CLAIM_HALAL="HALAL"</code> */
	public static final String CLAIM_HALAL = "HALAL";
	/** Constant <code>CLAIM_HALAL_LABEL="Halal"</code> */
	public static final String CLAIM_HALAL_LABEL = "Halal";

	public static final String CERT_ISO_9001 = "ISO 9001";
	public static final String CERT_BRC = "BRC";
	public static final String CERT_IFS = "IFS";
	public static final String CERT_ISO_22000 = "ISO 22000";
	public static final String CERT_FSSC_22000 = "FSSC 22000";
	
	/** Constant <code>SUGAR_NAME="Sucre"</code> */
	public static final String SUGAR_NAME = "Sucre";
	/** Constant <code>SUGAR_SUPPLIER_1_NAME="Sucre - Fournisseur 1"</code> */
	public static final String SUGAR_SUPPLIER_1_NAME = "Sucre - Fournisseur 1";
	/** Constant <code>SUGAR_SUPPLIER_2_NAME="Sucre - Fournisseur 2"</code> */
	public static final String SUGAR_SUPPLIER_2_NAME = "Sucre - Fournisseur 2";
	/** Constant <code>SUGAR_SUPPLIER_3_NAME="Sucre - Fournisseur 3"</code> */
	public static final String SUGAR_SUPPLIER_3_NAME = "Sucre - Fournisseur 3";
	/** Constant <code>SUGAR_GEN_USINE_1_NAME="Sucre - GEN - Usine 1"</code> */
	public static final String SUGAR_GEN_USINE_1_NAME = "Sucre - GEN - Usine 1";
	/** Constant <code>SUGAR_GEN_USINE_2_NAME="Sucre - GEN - Usine 2"</code> */
	public static final String SUGAR_GEN_USINE_2_NAME = "Sucre - GEN - Usine 2";
	/** Constant <code>FLOUR_NAME="Farine"</code> */
	public static final String FLOUR_NAME = "Farine";
	/** Constant <code>EGG_NAME="Oeuf"</code> */
	public static final String EGG_NAME = "Oeuf";
	/** Constant <code>CHOCOLATE_NAME="Chocolat"</code> */
	public static final String CHOCOLATE_NAME = "Chocolat";
	/** Constant <code>PATE_CHOUX_NAME="P&acirc;te &agrave; choux"</code> */
	public static final String PATE_CHOUX_NAME = "Pâte à choux";
	/** Constant <code>CREME_PATISSIERE_NAME="Cr&egrave;me p&acirc;tissi&egrave;re"</code> */
	public static final String CREME_PATISSIERE_NAME = "Crème pâtissière";
	/** Constant <code>NAPPAGE_NAME="Nappage"</code> */
	public static final String NAPPAGE_NAME = "Nappage";
	/** Constant <code>SUPPLIER_1="Fournisseur 1"</code> */
	public static final String SUPPLIER_1 = "Fournisseur 1";
	/** Constant <code>SUPPLIER_2="Fournisseur 2"</code> */
	public static final String SUPPLIER_2 = "Fournisseur 2";
	/** Constant <code>SUPPLIER_3="Fournisseur 3"</code> */
	public static final String SUPPLIER_3 = "Fournisseur 3";
	/** Constant <code>LABORATORY_1="Laboratoire 1"</code> */
	public static final String LABORATORY_1 = "Laboratoire 1";
	/** Constant <code>LABORATORY_2="Laboratoire 2"</code> */
	public static final String LABORATORY_2 = "Laboratoire 2";
	/** Constant <code>PLANT_USINE_1="Usine 1"</code> */
	public static final String PLANT_USINE_1 = "Usine 1";
	/** Constant <code>PLANT_USINE_2="Usine 2"</code> */
	public static final String PLANT_USINE_2 = "Usine 2";
	/** Constant <code>PASTRY_QUALITY="Pastry Quality"</code> */
    public static final String PASTRY_QUALITY = "Pastry Quality";
    /** Constant <code>FILLING_QUALITY="Filling Quality"</code> */
    public static final String FILLING_QUALITY = "Filling Quality";
    /** Constant <code>CCP_COMPLIANCE="CCP Compliance"</code> */
    public static final String CCP_COMPLIANCE = "CCP Compliance";

    // Survey question labels
    public static final String SURVEY_PASTRY_QUESTION = "Evaluate the choux pastry characteristics:";
    public static final String SURVEY_FILLING_QUESTION = "Assess the pastry cream filling:";
    public static final String SURVEY_CHOCOLATE_QUESTION = "Evaluate the chocolate glaze characteristics:";

    // Survey answer labels - Pastry
    public static final String ANSWER_PASTRY_PERFECT = "Perfect - Golden brown, hollow, crisp exterior (18-20cm length)";
    public static final String ANSWER_PASTRY_MINOR_DEFECTS = "Minor defects - Slight color variation, size within 17-21cm";
    public static final String ANSWER_PASTRY_MAJOR_DEFECTS = "Major defects - Improper rise, inconsistent texture";
    
    // Survey answer labels - Filling
    public static final String ANSWER_FILLING_PERFECT = "Perfect - Smooth, creamy, vanilla flavor (5-7°C)";
    public static final String ANSWER_FILLING_MINOR_ISSUES = "Minor issues - Slight graininess or temperature deviation (4-8°C)";
    public static final String ANSWER_FILLING_MAJOR_ISSUES = "Major issues - Curdled, separated, or incorrect temperature";
    
    // Survey answer labels - Chocolate
    public static final String ANSWER_CHOCOLATE_CORRECT = "Correct viscosity (65-70% chocolate content), uniform texture";
    public static final String ANSWER_CHOCOLATE_DEVIATION = "Slight viscosity deviation (60-75% content), minor texture issues";
    public static final String ANSWER_CHOCOLATE_OUT_OF_SPEC = "Out of specification - improper viscosity or crystallization";
	
	protected FinishedProductData product;

	protected NodeRef pateChouxNodeRef;
	protected NodeRef cremePatissiereNodeRef;
	protected NodeRef nappageNodeRef;

	protected NodeRef sugarPlants1NodeRef;
	protected NodeRef sugarPlants2NodeRef;
	protected NodeRef sugarSupplier1NodeRef;
	protected NodeRef sugarSupplier2NodeRef;
	protected NodeRef sugarSupplier3NodeRef;

	protected NodeRef waterNodeRef;
	protected NodeRef milkNodeRef;
	protected NodeRef sugarNodeRef;
	protected NodeRef flourNodeRef;
	protected NodeRef eggNodeRef;
	protected NodeRef chocolateNodeRef;

	protected NodeRef ingWaterNodeRef;
	protected NodeRef ingMilkNodeRef;
	protected NodeRef ingSugarNodeRef;
	protected NodeRef ingFlourNodeRef;
	protected NodeRef ingEggNodeRef;
	protected NodeRef ingChocolateNodeRef;

	protected NodeRef euOrganicClaim;
	protected NodeRef kosherClaim;
	protected NodeRef halalClaim;

	protected NodeRef iso9001Cert;	
	protected NodeRef brcCert;
	protected NodeRef ifsCert;
	protected NodeRef iso22000Cert;
	protected NodeRef fssc22000Cert;
	

	/**
	 * <p>Getter for the field <code>pateChouxNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getPateChouxNodeRef() {
		return pateChouxNodeRef;
	}

	/**
	 * <p>Getter for the field <code>cremePatissiereNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getCremePatissiereNodeRef() {
		return cremePatissiereNodeRef;
	}

	/**
	 * <p>Getter for the field <code>nappageNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getNappageNodeRef() {
		return nappageNodeRef;
	}

	/**
	 * <p>Getter for the field <code>waterNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getWaterNodeRef() {
		return waterNodeRef;
	}

	/**
	 * <p>Getter for the field <code>milkNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getMilkNodeRef() {
		return milkNodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarNodeRef() {
		return sugarNodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarPlants1NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarPlants1NodeRef() {
		return sugarPlants1NodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarPlants2NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarPlants2NodeRef() {
		return sugarPlants2NodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarSupplier1NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarSupplier1NodeRef() {
		return sugarSupplier1NodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarSupplier2NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarSupplier2NodeRef() {
		return sugarSupplier2NodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarSupplier3NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarSupplier3NodeRef() {
		return sugarSupplier3NodeRef;
	}

	/**
	 * <p>Getter for the field <code>flourNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getFlourNodeRef() {
		return flourNodeRef;
	}

	/**
	 * <p>Getter for the field <code>eggNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getEggNodeRef() {
		return eggNodeRef;
	}

	/**
	 * <p>Getter for the field <code>chocolateNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getChocolateNodeRef() {
		return chocolateNodeRef;
	}

	/**
	 * <p>getNodeService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * <p>getDestFolder.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getDestFolder() {
		return destFolder;
	}
	
	

	public FinishedProductData getProduct() {
		return product;
	}
	


	private boolean isWithCompo = true;
	private boolean isWithLabeling = true;
	private boolean isWithGenericRawMaterial = true;
	private boolean isWithStocks = true;
	private boolean isWithIngredients = false;
	private boolean isWithSurvey = false;
	private boolean isWithScoreList = false;
	private boolean isWithClaim = false;
	private boolean isWithSpecification = false;

	// Private constructor to enforce usage of the builder
	private StandardChocolateEclairTestProduct(Builder builder) {
		super(builder);
		this.isWithCompo = builder.isWithCompo;
		this.isWithLabeling = builder.isWithLabeling;
		this.isWithGenericRawMaterial = builder.isWithGenericRawMaterial;
		this.isWithStocks = builder.isWithStocks;
		this.isWithIngredients = builder.isWithIngredients;
		this.isWithSurvey = builder.isWithSurvey;
		this.isWithScoreList = builder.isWithScoreList;
		this.isWithClaim = builder.isWithClaim;
		this.isWithSpecification = builder.isWithSpecification;
	}

	// Static inner Builder class
	public static class Builder extends SampleProductBuilder.Builder<Builder> {

		private boolean isWithCompo = true;
		private boolean isWithLabeling = true;
		private boolean isWithGenericRawMaterial = true;
		private boolean isWithStocks = true;
		private boolean isWithIngredients = false;
		private boolean isWithSurvey = false;
		private boolean isWithScoreList = false;
		private boolean isWithClaim = false;
		private boolean isWithSpecification = false;

		public Builder withClaim(boolean isWithClaim) {
			this.isWithClaim = isWithClaim;
			return this;
		}

		public Builder withSurvey(boolean isWithSurvey) {
			this.isWithSurvey = isWithSurvey;
			return this;
		}

		public Builder withScoreList(boolean isWithScoreList) {
			this.isWithScoreList = isWithScoreList;
			return this;
		}

		public Builder withCompo(boolean isWithCompo) {
			this.isWithCompo = isWithCompo;
			return this;
		}

		public Builder withLabeling(boolean isWithLabeling) {
			this.isWithLabeling = isWithLabeling;
			return this;
		}

		public Builder withGenericRawMaterial(boolean isWithGenericRawMaterial) {
			this.isWithGenericRawMaterial = isWithGenericRawMaterial;
			return this;
		}

		public Builder withStocks(boolean isWithStocks) {
			this.isWithStocks = isWithStocks;
			return this;
		}
	
		public Builder withIngredients(boolean isWithIngredients) {
			this.isWithIngredients = isWithIngredients;
			return this;
		}
	
		public Builder withSpecification(boolean isWithSpecification) {
			this.isWithSpecification = isWithSpecification;
			return this;
		}
		
		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public StandardChocolateEclairTestProduct build() {
			return new StandardChocolateEclairTestProduct(this);
		}
	}

	@Override
	public FinishedProductData createTestProduct() {
		 product = FinishedProductData.build().withName("Éclair au chocolat").withUnit(ProductUnit.kg).withQty(550d)
				.withDensity(1d);

       	initCertifications();
		
		if (isWithClaim) {
			initClaims();
			product.withLabelClaimList(createClaimList());
		}

		if (isWithCompo) {

			if (pateChouxNodeRef == null) {
				initCompoProduct();
			}

			product = product.withCompoList(List.of(
					CompoListDataItem.build().withQtyUsed(250d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(pateChouxNodeRef),
					CompoListDataItem.build().withQtyUsed(200d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(cremePatissiereNodeRef),
					CompoListDataItem.build().withQtyUsed(100d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(nappageNodeRef)));

		}

		if (isWithLabeling) {

			product = product.withLabelingRuleList(
					List.of(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("Rendu as HTML").withFormula("renderAsHtmlTable()")
									.withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
									.withLabelingRuleType(LabelingRuleType.Format),
							LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
									.withLabelingRuleType(LabelingRuleType.Prefs)));

		}

		if (isWithSurvey) {
			product.withSurveyList(createEclairQMSSurveyList());
		}

		if (isWithScoreList) {
			product.withScoreList(createScoreList());
		}

		if (isWithSpecification) {
			product.setProductSpecifications(createProductSpecifications());
		}

		alfrescoRepository.create(destFolder, product);

		
		return product;

	}
	

	/**
	 * Creates product specifications for the test product.
	 * 
	 * @return List of product specifications
	 */
	protected List<ProductSpecificationData> createProductSpecifications() {
		ProductSpecificationData specification = createSpecificationWithSurveyRequirements();
		return List.of(specification);
	}
	
	/**
     * Creates a product specification with survey requirements that will 
     * create a mix of matching and non-matching requirements when compared to a product
     * 
     * @return A product specification with survey requirements
     */
    private ProductSpecificationData createSpecificationWithSurveyRequirements() {
        ProductSpecificationData specification = new ProductSpecificationData();
        specification.setName("Chocolate Eclair Quality Specification");
        
        // Create survey list for the specification with requirements
        List<SurveyListDataItem> specSurveyList = new ArrayList<>();
        
        // Get references to the questions
        NodeRef pastryQuestionRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, SURVEY_PASTRY_QUESTION);
        NodeRef fillingQuestionRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, SURVEY_FILLING_QUESTION);
        
        // Get references to the answers
        NodeRef pastryPerfectRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, ANSWER_PASTRY_PERFECT);
        NodeRef fillingPerfectRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, ANSWER_FILLING_PERFECT);
        
        // Setup specification survey requirements
        // 1. Pastry quality - require perfect (product uses minor defects, should generate a Forbidden requirement)
        SurveyListDataItem specQ1 = new SurveyListDataItem(pastryQuestionRef, true);
        specQ1.setChoices(List.of(pastryPerfectRef));
        specQ1.setRegulatoryType(RequirementType.Forbidden);
        MLText pastryMessage = new MLText();
        pastryMessage.addValue(Locale.ENGLISH, "Pastry quality must be perfect for this specification");
        specQ1.setRegulatoryMessage(pastryMessage);
        specSurveyList.add(specQ1);
        
        // 2. Filling quality - require perfect (product uses minor issues, should generate a Forbidden requirement)
        SurveyListDataItem specQ2 = new SurveyListDataItem(fillingQuestionRef, true);
        specQ2.setChoices(List.of(fillingPerfectRef));
        specQ2.setRegulatoryType(RequirementType.Forbidden);
        MLText fillingMessage = new MLText();
        fillingMessage.addValue(Locale.ENGLISH, "Filling quality must be perfect for this specification");
        specQ2.setRegulatoryMessage(fillingMessage);
        specSurveyList.add(specQ2);
        
        // Set the survey list on the specification
        specification.setSurveyList(specSurveyList);
        specification = (ProductSpecificationData) alfrescoRepository.create(destFolder,specification);
        
        return specification;
    }


	private void initClaims() {
		euOrganicClaim = CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_EU_ORGANIC, CLAIM_EU_ORGANIC_LABEL);
		kosherClaim = CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_KOSHER, CLAIM_KOSHER_LABEL);
		halalClaim = CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_HALAL, CLAIM_HALAL_LABEL);
	}

	private void initCertifications() {
		iso9001Cert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_ISO_9001, "ISO 9001 Quality Management System");
		brcCert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_BRC, "BRC Global Standard for Food Safety");
		ifsCert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_IFS, "International Featured Standards");
		iso22000Cert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_ISO_22000, "ISO 22000 Food Safety Management");
		fssc22000Cert = CharactTestHelper.getOrCreateCertification(nodeService, StandardChocolateEclairTestProduct.CERT_FSSC_22000, "FSSC 22000 Food Safety System Certification");
	}
	

	private List<LabelClaimListDataItem> createClaimList() {
		LabelClaimListDataItem euOrganicItem = LabelClaimListDataItem.build().withLabelClaim(euOrganicClaim);

		LabelClaimListDataItem kosherItem = LabelClaimListDataItem.build().withLabelClaim(kosherClaim);

		LabelClaimListDataItem halalItem = LabelClaimListDataItem.build().withLabelClaim(halalClaim);

		return new ArrayList<>(List.of(euOrganicItem, kosherItem, halalItem));
	}

	/**
	 * <p>createCaseLogisticUnit.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
	public LogisticUnitData createCaseLogisticUnit() {
		FinishedProductData eclairAuChocolat = createTestProduct();

		PackagingMaterialData packagingMaterialData = PackagingMaterialData.build().withName("Colis en carton");
		NodeRef packagingMaterialNodeRef = alfrescoRepository.create(destFolder, packagingMaterialData).getNodeRef();

		LogisticUnitData logisticUnitData = LogisticUnitData.build().withName("Colis d'éclairs au chocolat").withSecondaryWidth(200d)
				.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
						.withProduct(eclairAuChocolat.getNodeRef())))
				.withPackagingList(List.of(PackagingListDataItem.build().withQty(500d).withUnit(ProductUnit.g).withPkgLevel(PackagingLevel.Secondary)
						.withProduct(packagingMaterialNodeRef)));

		alfrescoRepository.create(destFolder, logisticUnitData);

		return logisticUnitData;
	}

	/**
	 * <p>createPalletLogisticUnit.</p>
	 *
	 * @param caseNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
	public LogisticUnitData createPalletLogisticUnit(NodeRef caseNodeRef) {
		PackagingMaterialData packagingMaterialData = PackagingMaterialData.build().withName("Palette en bois");
		NodeRef packagingMaterialNodeRef = alfrescoRepository.create(destFolder, packagingMaterialData).getNodeRef();
		LogisticUnitData palletLogisticUnit =

				LogisticUnitData.build().withName("Palette d'éclairs au chocolat").withTertiaryWidth(500d)
						.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
								.withDeclarationType(DeclarationType.Detail).withProduct(caseNodeRef)))
						.withPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.kg)
								.withPkgLevel(PackagingLevel.Tertiary).withProduct(packagingMaterialNodeRef)));

		alfrescoRepository.create(destFolder, palletLogisticUnit);

		return palletLogisticUnit;
	}

	/**
	 * <p>initCompoProduct.</p>
	 */
	public void initCompoProduct() {
		// Creating raw materials
		RawMaterialData water = RawMaterialData.build().withName(WATER_NAME).withQty(100d).withUnit(ProductUnit.kg);

		if (isWithStocks) {
			addStocks(water, 1000d, new ArrayList<>());
		}

		if (isWithIngredients) {
			initIngredients();
			water.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingWaterNodeRef)));
		}

		waterNodeRef = alfrescoRepository.create(destFolder, water).getNodeRef();
		nodeService.addAspect(waterNodeRef, PLMModel.ASPECT_WATER, new HashMap<>());

		RawMaterialData milk = RawMaterialData.build().withName(MILK_NAME).withQty(20d).withUnit(ProductUnit.kg);

		if (isWithStocks) {
			addStocks(milk, 1000d, new ArrayList<>());
		}

		milkNodeRef = alfrescoRepository.create(destFolder, milk).getNodeRef();

		nodeService.setProperty(milkNodeRef, PLMModel.PROP_EVAPORATED_RATE, 90d);

		RawMaterialData sugar = RawMaterialData.build().withName(SUGAR_NAME).withQty(20d).withUnit(ProductUnit.kg);

		if (isWithGenericRawMaterial) {
			RawMaterialData sugarSupplier1 = RawMaterialData.build().withName(SUGAR_SUPPLIER_1_NAME).withUnit(ProductUnit.kg)
					.withPlants(List.of(getOrCreateCharact(PLANT_USINE_1, PLMModel.TYPE_PLANT)));
			sugarSupplier1.setSuppliers(List.of(getOrCreateCharact(SUPPLIER_1, PLMModel.TYPE_SUPPLIER)));

			if (isWithIngredients) {
				sugarSupplier1.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingSugarNodeRef)));
			}

			if (isWithClaim) {
				// EU_ORGANIC claim for sugarSupplier1 - 100% applicable, 100% claimed, TRUE
				sugarSupplier1.withLabelClaimList(List.of(LabelClaimListDataItem.build().withPercentApplicable(100d).withPercentClaim(100d)
						.withLabelClaim(CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_EU_ORGANIC,CLAIM_EU_ORGANIC_LABEL))
						.withIsCertified(Boolean.TRUE)));
			}

			RawMaterialData sugarSupplier2 = RawMaterialData.build().withName(SUGAR_SUPPLIER_2_NAME).withUnit(ProductUnit.kg).withPlants(
					List.of(getOrCreateCharact(PLANT_USINE_1, PLMModel.TYPE_PLANT), getOrCreateCharact(PLANT_USINE_2, PLMModel.TYPE_PLANT)));
			sugarSupplier2.setSuppliers(List.of(getOrCreateCharact(SUPPLIER_2, PLMModel.TYPE_SUPPLIER)));

			if (isWithIngredients) {
				sugarSupplier2.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingSugarNodeRef)));
			}

			if (isWithClaim) {
				// KOSHER claim for sugarSupplier2 - 75% applicable, 50% claimed, FALSE
				sugarSupplier2.withLabelClaimList(List.of(LabelClaimListDataItem.build().withPercentApplicable(75d).withPercentClaim(50d)
						.withLabelClaim(CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_KOSHER, CLAIM_KOSHER_LABEL)).withIsClaimed(Boolean.FALSE)));
			}

			RawMaterialData sugarSupplier3 = RawMaterialData.build().withName(SUGAR_SUPPLIER_3_NAME).withUnit(ProductUnit.kg)
					.withPlants(List.of(getOrCreateCharact(PLANT_USINE_2, PLMModel.TYPE_PLANT)));
			sugarSupplier3.setSuppliers(List.of(getOrCreateCharact(SUPPLIER_3, PLMModel.TYPE_SUPPLIER)));

			if (isWithIngredients) {
				sugarSupplier3.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingSugarNodeRef)));
			}

			if (isWithClaim) {
				// HALAL claim for sugarSupplier3 - 60% applicable, 40% claimed, TRUE
				sugarSupplier3.withLabelClaimList(List.of(LabelClaimListDataItem.build().withPercentApplicable(60d).withPercentClaim(40d)
						.withLabelClaim(CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_HALAL, CLAIM_HALAL_LABEL)).withIsClaimed(Boolean.TRUE)

				));
			}

			if (isWithStocks) {
				addStocks(sugarSupplier1, 100d, List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY),
						getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
				addStocks(sugarSupplier2, 100d, List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY)));
				addStocks(sugarSupplier3, 100d, List.of(getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
			}

			sugarSupplier1NodeRef = alfrescoRepository.create(destFolder, sugarSupplier1).getNodeRef();
			sugarSupplier2NodeRef = alfrescoRepository.create(destFolder, sugarSupplier2).getNodeRef();
			sugarSupplier3NodeRef = alfrescoRepository.create(destFolder, sugarSupplier3).getNodeRef();

			RawMaterialData sugarGen1 = RawMaterialData.build().withName(SUGAR_GEN_USINE_1_NAME).withUnit(ProductUnit.kg)
					.withPlants(List.of(getOrCreateCharact(PLANT_USINE_1, PLMModel.TYPE_PLANT)));

			sugarGen1.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarSupplier1NodeRef),
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarSupplier2NodeRef)));

			if (isWithClaim) {
				sugarGen1.withLabelClaimList(createClaimList());
			}
			
			sugarPlants1NodeRef = alfrescoRepository.create(destFolder, sugarGen1).getNodeRef();

			RawMaterialData sugarGen2 = RawMaterialData.build().withName(SUGAR_GEN_USINE_2_NAME).withUnit(ProductUnit.kg)
					.withPlants(List.of(getOrCreateCharact(PLANT_USINE_2, PLMModel.TYPE_PLANT)));

			sugarGen2.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarSupplier2NodeRef),
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarSupplier3NodeRef)));

			if (isWithClaim) {
				sugarGen2.withLabelClaimList(createClaimList());
			}
			
			sugarPlants2NodeRef = alfrescoRepository.create(destFolder, sugarGen2).getNodeRef();

			sugar.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarPlants1NodeRef),
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarPlants2NodeRef)));
			
			if (isWithClaim) {
				sugar.withLabelClaimList(createClaimList());
			}
		}

		sugarNodeRef = alfrescoRepository.create(destFolder, sugar).getNodeRef();

		RawMaterialData flour = RawMaterialData.build().withName(FLOUR_NAME).withQty(30d).withUnit(ProductUnit.kg);

		if (isWithIngredients) {
			flour.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingFlourNodeRef)));
		}

		if (isWithStocks) {
			addStocks(flour, 100d,
					List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY), getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
		}
		
		if (isWithClaim) {
			flour.withLabelClaimList(List.of(LabelClaimListDataItem.build().withPercentApplicable(100d).withPercentClaim(100d)
					.withLabelClaim(CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_EU_ORGANIC,CLAIM_EU_ORGANIC_LABEL))
					.withIsClaimed(Boolean.TRUE)));
		}


		flourNodeRef = alfrescoRepository.create(destFolder, flour).getNodeRef();

		RawMaterialData egg = RawMaterialData.build().withName(EGG_NAME).withQty(40d).withUnit(ProductUnit.kg);

		if (isWithIngredients) {
			egg.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingEggNodeRef)));
		}

		if (isWithStocks) {
			addStocks(egg, 100d,
					List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY), getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
		}

		eggNodeRef = alfrescoRepository.create(destFolder, egg).getNodeRef();

		RawMaterialData chocolate = RawMaterialData.build().withName(CHOCOLATE_NAME).withQty(50d).withUnit(ProductUnit.kg);

		if (isWithIngredients) {
			chocolate.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingChocolateNodeRef)));
		}
		
		if (isWithClaim) {
			chocolate.withLabelClaimList(List.of(LabelClaimListDataItem.build().withPercentApplicable(100d).withPercentClaim(100d)
					.withLabelClaim(CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_EU_ORGANIC,CLAIM_EU_ORGANIC_LABEL))
					.withIsClaimed(Boolean.TRUE)));
		}

		if (isWithStocks) {
			addStocks(chocolate, 100d,
					List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY), getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
		}

		chocolateNodeRef = alfrescoRepository.create(destFolder, chocolate).getNodeRef();

		// Creating semi-finished product (Pâte à choux)
		SemiFinishedProductData pateChoux = SemiFinishedProductData.build().withName(PATE_CHOUX_NAME).withQty(22d).withUnit(ProductUnit.kg)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(100d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(waterNodeRef),
						CompoListDataItem.build().withQtyUsed(20d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(milkNodeRef),
						CompoListDataItem.build().withQtyUsed(20d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(sugarNodeRef)));

		if (isWithLabeling) {
			pateChoux = pateChoux.withLabelingRuleList(
					List.of(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("Rendu as HTML").withFormula("renderAsHtmlTable()")
									.withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
									.withLabelingRuleType(LabelingRuleType.Format),
							LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
									.withLabelingRuleType(LabelingRuleType.Prefs)));
		}

		// Set label claims for pateChoux
		if (isWithClaim) {
			pateChoux.setLabelClaimList(createClaimList());
		}

		pateChouxNodeRef = alfrescoRepository.create(destFolder, pateChoux).getNodeRef();

		// Creating semi-finished product (Crème pâtissière)
		SemiFinishedProductData cremePatissiere = SemiFinishedProductData.build().withName(CREME_PATISSIERE_NAME).withQty(200d)
				.withUnit(ProductUnit.kg)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(sugarNodeRef),
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(milkNodeRef),
						CompoListDataItem.build().withQtyUsed(30d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(flourNodeRef),
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(eggNodeRef),
						CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(chocolateNodeRef)));

		// Set label claims for cremePatissiere
		if (isWithClaim) {
			cremePatissiere.setLabelClaimList(createClaimList());
		}

		cremePatissiereNodeRef = alfrescoRepository.create(destFolder, cremePatissiere).getNodeRef();

		// Creating semi-finished product (Nappage)
		SemiFinishedProductData nappage = SemiFinishedProductData.build().withName(NAPPAGE_NAME).withQty(100d).withUnit(ProductUnit.kg)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(eggNodeRef),
						CompoListDataItem.build().withQtyUsed(30d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(sugarNodeRef),
						CompoListDataItem.build().withQtyUsed(30d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(chocolateNodeRef)));

		// Set label claims for nappage
		if (isWithClaim) {
			nappage.setLabelClaimList(createClaimList());
		}

		nappageNodeRef = alfrescoRepository.create(destFolder, nappage).getNodeRef();
	}

	private void initIngredients() {

		ingWaterNodeRef = CharactTestHelper.getOrCreateIng(nodeService, WATER_NAME);

		nodeService.addAspect(ingWaterNodeRef, PLMModel.ASPECT_WATER, new HashMap<>());

		ingMilkNodeRef = CharactTestHelper.getOrCreateIng(nodeService, MILK_NAME);

		nodeService.setProperty(ingMilkNodeRef, PLMModel.PROP_EVAPORATED_RATE, 90d);

		ingSugarNodeRef = CharactTestHelper.getOrCreateIng(nodeService, SUGAR_NAME);
		ingFlourNodeRef = CharactTestHelper.getOrCreateIng(nodeService, FLOUR_NAME);
		ingEggNodeRef = CharactTestHelper.getOrCreateIng(nodeService, EGG_NAME);

		nodeService.setProperty(ingEggNodeRef, PLMModel.PROP_EVAPORATED_RATE, 10d);

		ingChocolateNodeRef = CharactTestHelper.getOrCreateIng(nodeService, CHOCOLATE_NAME);

	}

	private void addStocks(RawMaterialData rawMaterial, Double qty, List<NodeRef> laboratories) {

		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_WEEK, 10);

		List<StockListDataItem> stocks = new ArrayList<>();

		for (NodeRef laboratory : laboratories) {
			StockListDataItem stockListDataItem = new StockListDataItem();
			stockListDataItem.setBatchId(rawMaterial.getName() + " - " + nodeService.getProperty(laboratory, ContentModel.PROP_NAME) + "-"
					+ now.get(Calendar.DAY_OF_WEEK));
			stockListDataItem.setLaboratories(List.of(laboratory));
			stockListDataItem.setUseByDate(now.getTime());
			stockListDataItem.setBatchQty(qty);
			stocks.add(stockListDataItem);
		}

		now.add(Calendar.DAY_OF_WEEK, 5);

		StockListDataItem stockListDataItem = new StockListDataItem();
		stockListDataItem.setBatchId(rawMaterial.getName() + " - " + now.get(Calendar.DAY_OF_WEEK));
		stockListDataItem.setUseByDate(now.getTime());
		stockListDataItem.setBatchQty(qty / 10);
		stocks.add(stockListDataItem);

		rawMaterial.setStockList(stocks);

	}

	Map<Pair<QName, String>, NodeRef> characts = new HashMap<>();

	/**
	 * <p>getOrCreateCharact.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getOrCreateCharact(String name, QName type) {

		return characts.computeIfAbsent(new Pair<>(type, name), p -> {
			Map<QName, Serializable> prop = Map.of(ContentModel.PROP_NAME, name);

			return nodeService
					.createNode(destFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) prop.get(ContentModel.PROP_NAME)), type, prop)
					.getChildRef();

		});
	}

	private List<SurveyListDataItem> createEclairQMSSurveyList() {
        // Question 1: Choux Pastry Quality Check
        final SurveyQuestion question1 = getOrCreateSurveyQuestion(SURVEY_PASTRY_QUESTION, PASTRY_QUALITY,
                ResponseType.multiChoicelist.name(), null);

        getOrCreateSurveyAnswer(question1, ANSWER_PASTRY_PERFECT, 100d);
        final SurveyQuestion q1Answer1 = getOrCreateSurveyAnswer(question1, ANSWER_PASTRY_MINOR_DEFECTS, 20d);
        getOrCreateSurveyAnswer(question1, ANSWER_PASTRY_MAJOR_DEFECTS, 0d);

        final SurveyListDataItem survey1 = new SurveyListDataItem();
        survey1.setQuestion(question1.getNodeRef());
        survey1.setChoices(List.of(q1Answer1.getNodeRef()));

        // Question 2: Pastry Cream Filling
        final SurveyQuestion question2 = getOrCreateSurveyQuestion(SURVEY_FILLING_QUESTION, FILLING_QUALITY,
                ResponseType.multiChoicelist.name(), null);

        getOrCreateSurveyAnswer(question2, ANSWER_FILLING_PERFECT, 100d);
        final SurveyQuestion q2Answer2 = getOrCreateSurveyAnswer(question2, ANSWER_FILLING_MINOR_ISSUES, 30d);
        getOrCreateSurveyAnswer(question2, ANSWER_FILLING_MAJOR_ISSUES, 0d);

        final SurveyListDataItem survey2 = new SurveyListDataItem();
        survey2.setQuestion(question2.getNodeRef());
        survey2.setChoices(List.of(q2Answer2.getNodeRef()));

        // Question 3: Chocolate Glaze
        final SurveyQuestion question3 = getOrCreateSurveyQuestion(SURVEY_CHOCOLATE_QUESTION, CCP_COMPLIANCE,
                ResponseType.multiChoicelist.name(), null);

        getOrCreateSurveyAnswer(question3, ANSWER_CHOCOLATE_CORRECT, 100d);
        getOrCreateSurveyAnswer(question3, ANSWER_CHOCOLATE_DEVIATION, 0d);
        final SurveyQuestion q3Answer3 = getOrCreateSurveyAnswer(question3, ANSWER_CHOCOLATE_OUT_OF_SPEC, 50d);

        final SurveyListDataItem survey3 = new SurveyListDataItem();
        survey3.setQuestion(question3.getNodeRef());
        survey3.setChoices(List.of(q3Answer3.getNodeRef()));

        return List.of(survey1, survey2, survey3);
    }

	private List<ScoreListDataItem> createScoreList() {
		return new ArrayList<>(
				List.of(ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, PASTRY_QUALITY)),
						ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, FILLING_QUALITY)),
						ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, CCP_COMPLIANCE))
								.withScore(80d)));
	}

	private SurveyQuestion getOrCreateSurveyQuestion(String label, String scoreCriterion, String responseType, Double questionScore) {

		// Create new question if not found
		final SurveyQuestion question = (SurveyQuestion) alfrescoRepository.findOne(CharactTestHelper.getOrCreateSurveyQuestion(nodeService, label));
		question.setLabel(label);
		question.setScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, scoreCriterion));

		if (responseType != null) {
			question.setResponseType(responseType);
		}

		if (questionScore != null) {
			question.setQuestionScore(questionScore);
		}

		return (SurveyQuestion) alfrescoRepository.save(question);
	}

	private SurveyQuestion getOrCreateSurveyAnswer(SurveyQuestion parentQuestion, String label, Double score) {
		final SurveyQuestion answer = (SurveyQuestion) alfrescoRepository.findOne(CharactTestHelper.getOrCreateSurveyQuestion(nodeService, label));
		answer.setParent(parentQuestion);
		answer.setLabel(label);

		if (score != null) {
			answer.setQuestionScore(score);
		}

		return (SurveyQuestion) alfrescoRepository.save(answer);
	}

}
