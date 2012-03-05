package fr.becpg.repo.product.report;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.impl.AbstractEntityReportExtractor;

public class DefaultProductReportExtractor extends AbstractEntityReportExtractor {

	/** The Constant KEY_PRODUCT_IMAGE. */
	private static final String KEY_PRODUCT_IMAGE = "productImage";

	/** The Constant TAG_PRODUCT. */
	protected static final String TAG_PRODUCT = "product";

	/** The Constant TAG_DATALISTS. */
	protected static final String TAG_DATALISTS = "dataLists";
	protected static final String TAG_ATTRIBUTES = "attributes";
	protected static final String TAG_ATTRIBUTE = "attribute";
	protected static final String ATTR_SET = "set";
	protected static final String ATTR_NAME = "name";
	protected static final String ATTR_VALUE = "value";

	/** The Constant TAG_ALLERGENLIST. */
	protected static final String TAG_ALLERGENLIST = "allergenList";

	/** The Constant TAG_COMPOLIST. */
	protected static final String TAG_COMPOLIST = "compoList";

	/** The Constant TAG_COSTLIST. */
	protected static final String TAG_COSTLIST = "costList";

	/** The Constant TAG_INGLIST. */
	protected static final String TAG_INGLIST = "ingList";

	/** The Constant TAG_INGLABELINGLIST. */
	protected static final String TAG_INGLABELINGLIST = "ingLabelingList";

	/** The Constant TAG_NUTLIST. */
	protected static final String TAG_NUTLIST = "nutList";

	/** The Constant TAG_ORGANOLIST. */
	protected static final String TAG_ORGANOLIST = "organoList";

	/** The Constant TAG_MICROBIOLIST. */
	protected static final String TAG_MICROBIOLIST = "microbioList";

	/** The Constant TAG_PHYSICOCHEMLIST. */
	protected static final String TAG_PHYSICOCHEMLIST = "physicoChemList";

	/** The Constant TAG_ALLERGEN. */
	protected static final String TAG_ALLERGEN = "allergen";

	/** The Constant TAG_COST. */
	protected static final String TAG_COST = "cost";

	/** The Constant TAG_ING. */
	protected static final String TAG_ING = "ing";

	/** The Constant TAG_INGLABELING. */
	protected static final String TAG_INGLABELING = "ingLabeling";

	/** The Constant TAG_NUT. */
	protected static final String TAG_NUT = "nut";

	/** The Constant TAG_ORGANO. */
	protected static final String TAG_ORGANO = "organo";

	/** The Constant TAG_MICROBIO. */
	protected static final String TAG_MICROBIO = "microbio";

	/** The Constant TAG_PHYSICOCHEM. */
	protected static final String TAG_PHYSICOCHEM = "physicoChem";

	/** The Constant SUFFIX_LOCALE_FRENCH. */
	protected static final String SUFFIX_LOCALE_FRENCH = "_fr";

	/** The Constant SUFFIX_LOCALE_ENGLISH. */
	protected static final String SUFFIX_LOCALE_ENGLISH = "_en";

	protected static final String REPORT_FORM_CONFIG_PATH = "beCPG/birt/document/becpg-report-form-config.xml";

	protected ProductDAO productDAO;

	protected ProductDictionaryService productDictionaryService;

	/**
	 * @param productDAO
	 *            the productDAO to set
	 */
	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	/**
	 * @param productDictionaryService
	 *            the productDictionaryService to set
	 */
	public void setProductDictionaryService(ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}

	@Override
	public EntityReportData extract(NodeRef entityNodeRef) {

		EntityReportData ret = new EntityReportData();

		Document document = DocumentHelper.createDocument();
		Element entityElt = document.addElement(TAG_PRODUCT);

		Element attributesElt = entityElt.addElement(TAG_ATTRIBUTES);

		// add attributes at <product/> tag
		Map<ClassAttributeDefinition, String> attributes = loadNodeAttributes(entityNodeRef);

		for (Map.Entry<ClassAttributeDefinition, String> attrKV : attributes.entrySet()) {

			entityElt.addAttribute(attrKV.getKey().getName().getLocalName(), attrKV.getValue());
		}

		// add attributes at <product><attributes/></product> and group them by
		// set
		Map<String, List<String>> fieldsBySets = getFieldsBySets(entityNodeRef, REPORT_FORM_CONFIG_PATH);

		// set
		for (Map.Entry<String, List<String>> kv : fieldsBySets.entrySet()) {

			// field
			for (String fieldId : kv.getValue()) {

				// look for value
				Map.Entry<ClassAttributeDefinition, String> attrKV = null;
				for (Map.Entry<ClassAttributeDefinition, String> a : attributes.entrySet()) {

					if (a.getKey().getName().getPrefixString().equals(fieldId)) {
						attrKV = a;
						break;
					}
				}

				if (attrKV != null) {

					Element attributeElt = attributesElt.addElement(TAG_ATTRIBUTE);
					attributeElt.addAttribute(ATTR_SET, kv.getKey());
					attributeElt.addAttribute(ATTR_NAME, attrKV.getKey().getTitle());
					attributeElt.addAttribute(ATTR_VALUE, attrKV.getValue());
				}
			}
		}

		// render data lists
		Element dataListsElt = entityElt.addElement(TAG_DATALISTS);
		dataListsElt = loadDataLists(entityNodeRef, dataListsElt);

		ret.setXmlDataSource(entityElt);
		ret.setDataObjects(extractImages(entityNodeRef));

		return ret;
	}

	private Map<String, byte[]> extractImages(NodeRef entityNodeRef) {
		Map<String, byte[]> images = new HashMap<String, byte[]>();
		/*
		 * get the product image
		 */
		try {
			String productImageFileName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_IMAGE).toLowerCase();
			NodeRef imgNodeRef;

			imgNodeRef = entityService.getImage(entityNodeRef, productImageFileName);

			byte[] imageBytes = null;

			if (imgNodeRef != null) {
				imageBytes = entityService.getImage(imgNodeRef);
				images.put(KEY_PRODUCT_IMAGE, imageBytes);
			}
		} catch (BeCPGException e) {
			//DO Nothing here
		}
		return images;
	}

	/**
	 * load the datalists of the product data.
	 * 
	 * @param productData
	 *            the product data
	 * @param dataListsElt
	 *            the data lists elt
	 * @return the element
	 */
	protected Element loadDataLists(NodeRef entityNodeRef, Element dataListsElt) {

		// TODO make it more generic!!!!
		ProductData productData = productDAO.find(entityNodeRef, productDictionaryService.getDataLists());

		// allergen
		if (productData.getAllergenList() != null) {
			Element allergenListElt = dataListsElt.addElement(TAG_ALLERGENLIST);

			for (AllergenListDataItem dataItem : productData.getAllergenList()) {

				String voluntarySources = VALUE_NULL;
				for (NodeRef nodeRef : dataItem.getVoluntarySources()) {
					if (voluntarySources.isEmpty())
						voluntarySources = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
					else
						voluntarySources += RepoConsts.LABEL_SEPARATOR + (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}

				String inVoluntarySources = VALUE_NULL;
				for (NodeRef nodeRef : dataItem.getInVoluntarySources()) {
					if (inVoluntarySources.isEmpty())
						inVoluntarySources = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
					else
						inVoluntarySources += RepoConsts.LABEL_SEPARATOR + (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}

				String allergen = (String) nodeService.getProperty(dataItem.getAllergen(), ContentModel.PROP_NAME);
				String allergenType = (String) nodeService.getProperty(dataItem.getAllergen(), BeCPGModel.PROP_ALLERGEN_TYPE);

				Element AllergenElt = allergenListElt.addElement(TAG_ALLERGEN);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_ALLERGEN.getLocalName(), allergen);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGEN_TYPE.getLocalName(), allergenType);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(), Boolean.toString(dataItem.getVoluntary()));
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(), Boolean.toString(dataItem.getInVoluntary()));
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES.getLocalName(), voluntarySources);
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES.getLocalName(), inVoluntarySources);
			}
		}

		// compoList
		if (productData.getCompoList() != null) {
			Element compoListElt = dataListsElt.addElement(TAG_COMPOLIST);

			for (CompoListDataItem dataItem : productData.getCompoList()) {

				String partName = (String) nodeService.getProperty(dataItem.getProduct(), ContentModel.PROP_NAME);

				Element partElt = compoListElt.addElement(TAG_PRODUCT);
				partElt.addAttribute(BeCPGModel.ASSOC_COMPOLIST_PRODUCT.getLocalName(), partName);
				partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(dataItem.getDepthLevel()));
				partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY.getLocalName(), dataItem.getQty() == null ? VALUE_NULL : Float.toString(dataItem.getQty()));
				partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA.getLocalName(),
						dataItem.getQtySubFormula() == null ? VALUE_NULL : Float.toString(dataItem.getQtySubFormula()));
				partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_LOSS_PERC.getLocalName(), dataItem.getLossPerc() == null ? VALUE_NULL : Float.toString(dataItem.getLossPerc()));
				partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_DECL_TYPE.getLocalName(), dataItem.getDeclType());
			}
		}

		// CostList
		if (productData.getCostList() != null) {
			Element costListElt = dataListsElt.addElement(TAG_COSTLIST);

			for (CostListDataItem dataItem : productData.getCostList()) {

				String cost = (String) nodeService.getProperty(dataItem.getCost(), ContentModel.PROP_NAME);

				Element costElt = costListElt.addElement(TAG_COST);
				costElt.addAttribute(BeCPGModel.ASSOC_COSTLIST_COST.getLocalName(), cost);
				costElt.addAttribute(BeCPGModel.PROP_COSTLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Float.toString(dataItem.getValue()));
				costElt.addAttribute(BeCPGModel.PROP_COSTLIST_UNIT.getLocalName(), dataItem.getUnit());
			}
		}

		// IngList
		if (productData.getIngList() != null) {
			Element ingListElt = dataListsElt.addElement(TAG_INGLIST);

			for (IngListDataItem dataItem : productData.getIngList()) {

				String ing = (String) nodeService.getProperty(dataItem.getIng(), ContentModel.PROP_NAME);
				String ingCEECode = (String) nodeService.getProperty(dataItem.getIng(), BeCPGModel.PROP_ING_CEECODE);

				String geoOrigins = VALUE_NULL;
				for (NodeRef nodeRef : dataItem.getGeoOrigin()) {
					if (geoOrigins.isEmpty())
						geoOrigins = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString();
					else
						geoOrigins += RepoConsts.LABEL_SEPARATOR + (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}

				String bioOrigins = VALUE_NULL;
				for (NodeRef nodeRef : dataItem.getBioOrigin()) {
					if (bioOrigins.isEmpty())
						bioOrigins = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
					else
						bioOrigins += RepoConsts.LABEL_SEPARATOR + (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}

				Element ingElt = ingListElt.addElement(TAG_ING);
				ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_ING.getLocalName(), ing);
				ingElt.addAttribute(BeCPGModel.PROP_INGLIST_QTY_PERC.getLocalName(), dataItem.getQtyPerc() == null ? VALUE_NULL : Float.toString(dataItem.getQtyPerc()));
				ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_GEO_ORIGIN.getLocalName(), geoOrigins);
				ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_BIO_ORIGIN.getLocalName(), bioOrigins);
				ingElt.addAttribute(BeCPGModel.PROP_INGLIST_IS_GMO.getLocalName(), Boolean.toString(dataItem.isGMO()));
				ingElt.addAttribute(BeCPGModel.PROP_INGLIST_IS_IONIZED.getLocalName(), Boolean.toString(dataItem.isIonized()));
				ingElt.addAttribute(BeCPGModel.PROP_ING_CEECODE.getLocalName(), ingCEECode);
			}
		}

		// IngLabelingList
		if (productData.getIngLabelingList() != null) {
			Element ingListElt = dataListsElt.addElement(TAG_INGLABELINGLIST);
			String frenchILLGrpName = BeCPGModel.PROP_ILL_GRP.getLocalName() + SUFFIX_LOCALE_FRENCH;
			String englishILLGrpName = BeCPGModel.PROP_ILL_GRP.getLocalName() + SUFFIX_LOCALE_ENGLISH;
			String frenchILLValueName = BeCPGModel.PROP_ILL_VALUE.getLocalName() + SUFFIX_LOCALE_FRENCH;
			String englishILLValueName = BeCPGModel.PROP_ILL_VALUE.getLocalName() + SUFFIX_LOCALE_ENGLISH;

			for (IngLabelingListDataItem dataItem : productData.getIngLabelingList()) {

				Element ingLabelingElt = ingListElt.addElement(TAG_INGLABELING);
				ingLabelingElt.addAttribute(frenchILLGrpName, dataItem.getGrp());
				ingLabelingElt.addAttribute(englishILLGrpName, dataItem.getGrp());

				ingLabelingElt.addAttribute(frenchILLValueName, dataItem.getValue().getValue(Locale.FRENCH));
				ingLabelingElt.addAttribute(englishILLValueName, dataItem.getValue().getValue(Locale.ENGLISH));
			}
		}

		// NutList
		if (productData.getNutList() != null) {

			Element nutListElt = dataListsElt.addElement(TAG_NUTLIST);

			for (NutListDataItem dataItem : productData.getNutList()) {

				String nut = nodeService.getProperty(dataItem.getNut(), ContentModel.PROP_NAME).toString();

				Element nutElt = nutListElt.addElement(TAG_NUT);
				nutElt.addAttribute(BeCPGModel.ASSOC_NUTLIST_NUT.getLocalName(), nut);
				nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : String.valueOf(dataItem.getValue()));
				nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_UNIT.getLocalName(), dataItem.getUnit());
				nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_GROUP.getLocalName(), dataItem.getGroup());
			}
		}

		// OrganoList
		if (productData.getOrganoList() != null) {

			Element organoListElt = dataListsElt.addElement(TAG_ORGANOLIST);

			for (OrganoListDataItem dataItem : productData.getOrganoList()) {

				String organo = nodeService.getProperty(dataItem.getOrgano(), ContentModel.PROP_NAME).toString();

				Element organoElt = organoListElt.addElement(TAG_ORGANO);
				organoElt.addAttribute(BeCPGModel.ASSOC_ORGANOLIST_ORGANO.getLocalName(), organo);
				organoElt.addAttribute(BeCPGModel.PROP_ORGANOLIST_VALUE.getLocalName(), dataItem.getValue());
			}
		}

		// MicrobioList
		if (productData.getMicrobioList() != null) {

			Element organoListElt = dataListsElt.addElement(TAG_MICROBIOLIST);

			for (MicrobioListDataItem dataItem : productData.getMicrobioList()) {

				String microbio = nodeService.getProperty(dataItem.getMicrobio(), ContentModel.PROP_NAME).toString();

				Element microbioElt = organoListElt.addElement(TAG_MICROBIO);
				microbioElt.addAttribute(BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO.getLocalName(), microbio);
				microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Float.toString(dataItem.getValue()));
				microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_UNIT.getLocalName(), dataItem.getUnit());
				microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_MAXI.getLocalName(), dataItem.getMaxi() == null ? VALUE_NULL : Float.toString(dataItem.getMaxi()));
				microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_TEXT_CRITERIA.getLocalName(), dataItem.getTextCriteria());
			}
		}

		// PhysicoChemList
		if (productData.getPhysicoChemList() != null) {

			Element physicoChemListElt = dataListsElt.addElement(TAG_PHYSICOCHEMLIST);

			for (PhysicoChemListDataItem dataItem : productData.getPhysicoChemList()) {

				String physicoChem = nodeService.getProperty(dataItem.getPhysicoChem(), ContentModel.PROP_NAME).toString();

				Element physicoChemElt = physicoChemListElt.addElement(TAG_PHYSICOCHEM);
				physicoChemElt.addAttribute(BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM.getLocalName(), physicoChem);
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Float.toString(dataItem.getValue()));
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_UNIT.getLocalName(), dataItem.getUnit());
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_MINI.getLocalName(), dataItem.getMini() == null ? VALUE_NULL : Float.toString(dataItem.getMini()));
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_MAXI.getLocalName(), dataItem.getMaxi() == null ? VALUE_NULL : Float.toString(dataItem.getMaxi()));
			}
		}

		return dataListsElt;
	}

}
