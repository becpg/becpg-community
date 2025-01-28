package fr.becpg.test.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepoHealthChecker;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GHSModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.SurveyModel;

/**
 * @author matthieu
 * Utility class to help create or retrieve specific nodes.
 */
public class CharactTestHelper {

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
	private static NodeRef getOrCreateNode(NodeService nodeService, String path, String nodeName, QName type, Map<QName, Serializable> properties) {
		NodeRef folder = BeCPGQueryBuilder.createQuery().selectNodeByPath(path);
		NodeRef node = nodeService.getChildByName(folder, ContentModel.ASSOC_CONTAINS, nodeName);
		properties.put(ContentModel.PROP_NAME, PropertiesHelper.cleanName(nodeName));
		
	  

		if (node == null) {
			ChildAssociationRef childAssocRef = nodeService.createNode(folder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, PropertiesHelper.cleanName(nodeName)), type, properties);
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

	public static NodeRef getOrCreateScoreCriterion(NodeService nodeService, String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:ProjectLists/bcpg:entityLists/cm:ScoreCriteria", name,
				ProjectModel.TYPE_SCORE_CRITERION, properties);
	}

	public static NodeRef getOrCreateSurveyQuestion(NodeService nodeService, String label) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(SurveyModel.PROP_SURVEY_QUESTION_LABEL, label);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:SurveyQuestions", label,
				SurveyModel.TYPE_SURVEY_QUESTION, properties);

	}

}
