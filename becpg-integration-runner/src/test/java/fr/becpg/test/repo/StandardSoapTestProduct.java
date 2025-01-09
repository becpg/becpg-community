package fr.becpg.test.repo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GHSModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.formulation.cpl.HazardClassificationFormulaContext;

public class StandardSoapTestProduct extends StandardProductBuilder {

	protected StandardSoapTestProduct(Builder builder) {
		super(builder);
	}

	// Static inner Builder class
	public static class Builder extends StandardProductBuilder.Builder<Builder> {

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
	public static final String SODIUM_HYDROXIDE = "Sodium Hydroxide";
	public static final String OLIVE_OIL = "Olive Oil";
	public static final String ESSENTIAL_OILS = "Essential Oils Mix";

	protected NodeRef sodiumHydroxideNodeRef;
	protected NodeRef oliveOilNodeRef;
	protected NodeRef essentialOilsNodeRef;

	@Override
	public FinishedProductData createTestProduct() {
		// Initialize raw materials if not already done
		if (sodiumHydroxideNodeRef == null) {
			initRawMaterialsWithIngredients();
		}

		// Create the soap finished product
		FinishedProductData soapProduct = FinishedProductData.build().withName("Natural Olive Soap").withUnit(ProductUnit.kg).withQty(1000d)
				.withDensity(1.2d)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(130d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
								.withProduct(sodiumHydroxideNodeRef),
						CompoListDataItem.build().withQtyUsed(800d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
								.withProduct(oliveOilNodeRef),
						CompoListDataItem.build().withQtyUsed(70d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
								.withProduct(essentialOilsNodeRef)));
		
        // Add physico-chemical properties
        addPhysicoChemProperty(soapProduct,"Boiling point",  HazardClassificationFormulaContext.BOILING_POINT, 78.0);  // Boiling point
        addPhysicoChemProperty(soapProduct,"Flash point", HazardClassificationFormulaContext.FLASH_POINT, 23.0);  // Flash point
        addPhysicoChemProperty(soapProduct,"Hydrocarbon", HazardClassificationFormulaContext.HYDROCARBON_PERC, 15.0);  // Hydrocarbon percentage

		alfrescoRepository.create(destFolder, soapProduct);

		return soapProduct;
	}

	public void initRawMaterialsWithIngredients() {
		// Create Sodium Hydroxide raw material with ingredients
		RawMaterialData sodiumHydroxide = RawMaterialData.build().withName(SODIUM_HYDROXIDE).withQty(100d).withUnit(ProductUnit.kg)
				.withIngList(createSodiumHydroxideIngredients());

		sodiumHydroxideNodeRef = alfrescoRepository.create(destFolder, sodiumHydroxide).getNodeRef();

		// Create Olive Oil raw material with ingredients
		RawMaterialData oliveOil = RawMaterialData.build().withName(OLIVE_OIL).withQty(500d).withUnit(ProductUnit.kg)
				.withIngList(createOliveOilIngredients());

		oliveOilNodeRef = alfrescoRepository.create(destFolder, oliveOil).getNodeRef();

		// Create Essential Oils raw material with ingredients
		RawMaterialData essentialOils = RawMaterialData.build().withName(ESSENTIAL_OILS).withQty(50d).withUnit(ProductUnit.kg)
				.withIngList(createEssentialOilsIngredients());

		essentialOilsNodeRef = alfrescoRepository.create(destFolder, essentialOils).getNodeRef();
	}

	private List<IngListDataItem> createSodiumHydroxideIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Sodium Hydroxide ingredients
		ingredients.add(createIngListItem("Sodium Hydroxide", 80.0, "1310-73-2", "Skin Corr. 1A:H314, Met. Corr. 1:H290", 500.0, 1000.0, 1.0, true));
		ingredients.add(createIngListItem("Sodium Carbonate", 10.0, "497-19-8", "Eye Irrit. 2:H319", 2800.0, 2000.0, null, false));
		ingredients.add(createIngListItem("Sodium Chloride", 10.0, "7647-14-5", "Eye Irrit. 2:H319", 3000.0, null, null, false));

		return ingredients;
	}

	private List<IngListDataItem> createOliveOilIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Olive Oil ingredients
		ingredients.add(createIngListItem("Oleic Acid", 55.0, "112-80-1", "Skin Irrit. 2:H315", null, null, null, false));
		ingredients.add(createIngListItem("Linoleic Acid", 20.0, "60-33-3", "Skin Sens. 1:H317", null, null, 1.0, true));
		ingredients.add(createIngListItem("Palmitic Acid", 15.0, "57-10-3", "Eye Irrit. 2:H319", null, null, null, false));

		return ingredients;
	}

	private List<IngListDataItem> createEssentialOilsIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Essential Oils ingredients
		ingredients.add(createIngListItem("Lavender Oil", 40.0, "8000-28-0", "Skin Sens. 1:H317, Aquatic Chronic 3:H412", null, null, 1.0, true));
		ingredients.add(createIngListItem("Eucalyptus Oil", 30.0, "8000-48-4", "Flam. Liq. 3:H226, Skin Sens. 1:H317", null, null, 1.0, true));
		ingredients.add(createIngListItem("Tea Tree Oil", 30.0, "68647-73-4", "Flam. Liq. 3:H226, Acute Tox. 4:H302, Skin Sens. 1:H317", 1900.0, null,
				1.0, true));

		return ingredients;
	}

	private IngListDataItem createIngListItem(String ingName, Double percentage, String casNumber, String hazardClass, Double toxicityOral,
			Double toxicityDermal, Double mFactor, Boolean superSensitizing) {

		NodeRef ing = getOrCreateIng(ingName);

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PLMModel.PROP_CAS_NUMBER, casNumber);
		properties.put(GHSModel.PROP_SDS_HAZARD_CLASSIFICATIONS, hazardClass);
		properties.put(BeCPGModel.PROP_ING_TOX_ACUTE_ORAL, toxicityOral);
		properties.put(BeCPGModel.PROP_ING_TOX_ACUTE_DERMAL, toxicityDermal);
		properties.put(BeCPGModel.PROP_M_FACTOR, mFactor);
		properties.put(BeCPGModel.PROP_SUPER_SENSITIZING, superSensitizing);

		nodeService.setProperties(ing, properties);

		return IngListDataItem.build().withQtyPerc(percentage).withIngredient(ing);
	}
	
	private void addPhysicoChemProperty(ProductData product,String name,  String code, Double value) {
        PhysicoChemListDataItem physicoChemList = new PhysicoChemListDataItem();
        
        NodeRef physicoChem = getOrCreatePhysico(name);
        
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PLMModel.PROP_PHYSICO_CHEM_CODE, code);
        
        nodeService.setProperties(physicoChem, props);
      
        
        
        physicoChemList.setPhysicoChem(physicoChem);
        physicoChemList.setValue(value);
        product.getPhysicoChemList().add(physicoChemList);
    }


	// Getters
	public NodeRef getSodiumHydroxideNodeRef() {
		return sodiumHydroxideNodeRef;
	}

	public NodeRef getOliveOilNodeRef() {
		return oliveOilNodeRef;
	}

	public NodeRef getEssentialOilsNodeRef() {
		return essentialOilsNodeRef;
	}
}