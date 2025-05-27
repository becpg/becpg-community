package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GHSModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.ToxType;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.SurveyModel;

/**
 * <p>CharactTestHelper class.</p>
 *
 * @author matthieu
 * Utility class to help create or retrieve specific nodes.
 */
public class CharactTestHelper {

	private CharactTestHelper() {
		//Do Nothing
	}
	
	/**
	 * Generic method to get or create a node based on the provided path, type, and properties.
	 * 
	 * @param nodeService the NodeService instance
	 * @param path the path to the parent folder
	 * @param nodeName the name of the node to find or create
	 * @param type the type of the node to create if it does not exist
	 * @param properties the properties of the node to create
	 * @return the NodeRef of the found or created node
	 */
	public static NodeRef getOrCreateNode(NodeService nodeService, String path, String nodeName, QName type, Map<QName, Serializable> properties) {
		String name = PropertiesHelper.cleanName(nodeName);
		
		NodeRef folder = BeCPGQueryBuilder.createQuery().selectNodeByPath(path);
		NodeRef node = nodeService.getChildByName(folder, ContentModel.ASSOC_CONTAINS,name);
		properties.put(ContentModel.PROP_NAME,name);

		if (node == null) {
			ChildAssociationRef childAssocRef = nodeService.createNode(folder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,name), type, properties);
			node = childAssocRef.getChildRef();
		}

		return node;
	}

	/**
	 * Get or create a Hazard Statement node.
	 *
	 * @param nodeService the NodeService instance
	 * @param hCode the hazard statement code
	 * @return the NodeRef of the Hazard Statement node
	 */
	public static NodeRef getOrCreateH(NodeService nodeService, String hCode) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, hCode);
		properties.put(GHSModel.PROP_HAZARD_CODE, hCode);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:SecurityLists/bcpg:entityLists/cm:HazardStatements", hCode,
				GHSModel.TYPE_HAZARD_STATEMENT, properties);
	}

	/**
	 * Get or create a Pictogram node.
	 *
	 * @param nodeService the NodeService instance
	 * @param hCode the pictogram code
	 * @return the NodeRef of the Pictogram node
	 */
	public static NodeRef getOrCreatePicto(NodeService nodeService, String hCode) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, hCode);
		properties.put(GHSModel.PROP_PICTOGRAM_CODE, hCode);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:SecurityLists/bcpg:entityLists/cm:Pictograms", hCode,
				GHSModel.TYPE_PICTOGRAM, properties);
	}

	/**
	 * Get or create an Ingredient node.
	 *
	 * @param nodeService the NodeService instance
	 * @param ingName the ingredient name
	 * @return the NodeRef of the Ingredient node
	 */
	public static NodeRef getOrCreateIng(NodeService nodeService, String ingName) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, ingName);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:Ings", ingName, PLMModel.TYPE_ING,
				properties);
	}

	/**
	 * Get or create a Physico-Chemical Characteristics node.
	 *
	 * @param nodeService the NodeService instance
	 * @param name the name of the physico-chemical characteristic
	 * @return the NodeRef of the Physico-Chemical Characteristics node
	 */
	public static NodeRef getOrCreatePhysico(NodeService nodeService, String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:PhysicoChems", name,
				PLMModel.TYPE_PHYSICO_CHEM, properties);
	}

	/**
	 * <p>getOrCreateScoreCriterion.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param name a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public static NodeRef getOrCreateScoreCriterion(NodeService nodeService, String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:ProjectLists/bcpg:entityLists/cm:ScoreCriteria", name,
				ProjectModel.TYPE_SCORE_CRITERION, properties);
	}

	/**
	 * <p>getOrCreateSurveyQuestion.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param label a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public static NodeRef getOrCreateSurveyQuestion(NodeService nodeService, String label) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(SurveyModel.PROP_SURVEY_QUESTION_LABEL, label);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:SurveyQuestions", label,
				SurveyModel.TYPE_SURVEY_QUESTION, properties);

	}

	/**
	 * <p>getOrCreateLCA.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param name a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public static NodeRef getOrCreateLCA(NodeService nodeService, String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:LifeCycleAnalysis", name, PLMModel.TYPE_LCA,
				properties);
	}

	/**
	 * <p>getOrCreateGeo.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param name a {@link java.lang.String} object
	 * @param code a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public static NodeRef getOrCreateGeo(NodeService nodeService, String name, String code) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(PLMModel.PROP_GEO_ORIGIN_ISOCODE,code);
		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:GeoOrigins", name, PLMModel.TYPE_GEO_ORIGIN,
				properties);
	}

	public static NodeRef getOrCreateClaim(NodeService nodeService, String name, String code) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(PLMModel.PROP_LABEL_CLAIM_CODE,code);
		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:LabelClaims", name, PLMModel.TYPE_LABEL_CLAIM,
				properties);
	}


	/**
	 * <p>getOrCreateLCAUnit.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param name a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String getOrCreateLCAUnit(NodeService nodeService,String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_CODE, name);
		properties.put(BeCPGModel.PROP_LV_VALUE, name);
	    getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Lists/bcpg:entityLists/cm:LCAUnits", name, BeCPGModel.TYPE_LIST_VALUE,
				properties);
	    return name;
	}

	/**
	 * Get or create a Certification node.
	 *
	 * @param nodeService the NodeService instance
	 * @param name the certification name
	 * @param description the certification description
	 * @return the NodeRef of the Certification node
	 */
	public static NodeRef getOrCreateCertification(NodeService nodeService, String name, String description) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(ContentModel.PROP_DESCRIPTION, description);
		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:Certifications", name, PLMModel.TYPE_CERTIFICATION,
				properties);
	}
	
	/**
	 * <p>getOrCreateTox.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param name a {@link java.lang.String} object
	 * @param toxValue a {@link java.lang.Double} object
	 * @param calculateSystemic a boolean
	 * @param calculateMax a boolean
	 * @param toxTypes a {@link java.util.List} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public static NodeRef getOrCreateTox(NodeService nodeService, String name, Double toxValue, boolean calculateSystemic, boolean calculateMax,
			List<ToxType> toxTypes) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(PLMModel.PROP_TOX_VALUE, toxValue);
		properties.put(PLMModel.PROP_TOX_CALCULATE_SYSTEMIC, calculateSystemic);
		properties.put(PLMModel.PROP_TOX_CALCULATE_MAX, calculateMax);
		properties.put(PLMModel.PROP_TOX_TYPES, new ArrayList<>(toxTypes));

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:Toxicities", name,
				PLMModel.TYPE_TOX, properties);
	}

}
