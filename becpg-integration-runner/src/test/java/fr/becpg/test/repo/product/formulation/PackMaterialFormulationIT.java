package fr.becpg.test.repo.product.formulation;

import static fr.becpg.repo.product.formulation.SurveyQuestionFormulationHandler.SURVEY_LIST_NAMES;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.test.PLMBaseTestCase;

public class PackMaterialFormulationIT extends PLMBaseTestCase {
	
	protected static final Log logger = LogFactory.getLog(PackMaterialFormulationIT.class);
	
	@Autowired
	protected AlfrescoRepository<SurveyQuestion> surveyQuestionRepository;

	/** The product service. */
	@Autowired
	protected ProductService productService;
	
	private NodeRef packMaterial0NodeRef;
	private NodeRef packMaterial1NodeRef;
	private NodeRef packMaterial2NodeRef;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		logger.info("Initializing test data");
		initParts();
	}

	private PackagingMaterialData createPackagingMaterial() {
		return PackagingMaterialData.build().withName("My Packaging")
				.withPackMaterialList(List.of(
						PackMaterialListDataItem.build().withMaterial(packMaterial0NodeRef),
								PackMaterialListDataItem.build().withMaterial(packMaterial1NodeRef),
								PackMaterialListDataItem.build().withMaterial(packMaterial2NodeRef)));
	}

	@Test
	public void test_formulate() throws Exception {
		inWriteTx(() -> {
			logger.info("Creating packaging material");
			PackagingMaterialData packagingMaterialData = createPackagingMaterial();
			//productService.formulate(packagingMaterialData);
			return packagingMaterialData;
		});

		
	}

	/**
	 * Inits the parts.
	 */
	protected void initParts() {

		inWriteTx(() -> {

			/*-- characteristics --*/
			// PackMaterial
			List<Pair<Map<QName, Serializable>, Consumer<NodeRef>>> propertiesList = Arrays.asList(
					Pair.of(Map.of(BeCPGModel.PROP_LV_VALUE, "Packaging Material 0"),
							nodeRef -> packMaterial0NodeRef = nodeRef),
					Pair.of(Map.of(BeCPGModel.PROP_LV_VALUE, "Packaging Material 1"),
							nodeRef -> packMaterial1NodeRef = nodeRef),
					Pair.of(Map.of(BeCPGModel.PROP_LV_VALUE, "Packaging Material 2"),
							nodeRef -> packMaterial2NodeRef = nodeRef)
			);
			propertiesList
					.forEach(
							properties -> properties
									.getRight()
									.accept(nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
											ContentModel.ASSOC_CONTAINS, PackModel.TYPE_PACKAGING_MATERIAL,
											properties.getLeft()).getChildRef()));
			return null;
		});

		inWriteTx(() -> {

			/*-- Create survey question 0 --*/
			logger.debug("/*-- Create survey question 0 --*/");
			var surveyQuestion = new SurveyQuestion();
			surveyQuestion.setName("Survey Question 0");
			surveyQuestion.setLabel("Yes or No ?");
			surveyQuestion.setFsLinkedCharactRefs(Collections.singletonList(packMaterial0NodeRef));
			surveyQuestion.setFsLinkedTypes(Collections.singletonList(PLMModel.TYPE_PACKAGINGMATERIAL.toString()));
			surveyQuestion.setFsSurveyListName(SURVEY_LIST_NAMES.get(1));

			surveyQuestionRepository.create(getTestFolderNodeRef(), surveyQuestion);

			return null;
		});

		inWriteTx(() -> {

			/*-- Create survey question 1 --*/
			logger.debug("/*-- Create survey question 1 --*/");
			var surveyQuestion = new SurveyQuestion();
			surveyQuestion.setName("Survey Question 1");
			surveyQuestion.setLabel("Hot or Cold ?");
			surveyQuestion.setFsLinkedCharactRefs(Collections.singletonList(packMaterial1NodeRef));
			surveyQuestion.setFsLinkedTypes(Collections.singletonList(PLMModel.TYPE_PACKAGINGMATERIAL.toString()));
			surveyQuestion.setFsSurveyListName(SURVEY_LIST_NAMES.get(1));

			surveyQuestionRepository.create(getTestFolderNodeRef(), surveyQuestion);

			return null;
		});

		inWriteTx(() -> {

			/*-- Create survey question 2 --*/
			logger.debug("/*-- Create survey question 2 --*/");
			var surveyQuestion = new SurveyQuestion();
			surveyQuestion.setName("Survey Question 22");
			surveyQuestion.setLabel("High or Low ?");
			surveyQuestion.setFsLinkedCharactRefs(Collections.singletonList(packMaterial2NodeRef));
			surveyQuestion.setFsLinkedTypes(Collections.singletonList(PLMModel.TYPE_PACKAGINGMATERIAL.toString()));
			surveyQuestion.setFsSurveyListName(SURVEY_LIST_NAMES.get(1));
			surveyQuestionRepository.create(getTestFolderNodeRef(), surveyQuestion);

			return null;
		});
	}
}
