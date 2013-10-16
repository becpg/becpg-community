package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleType;
import fr.becpg.repo.repository.AlfrescoRepository;

public class IngLabelingPatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(IngLabelingPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.ingLabelingPatch.result";

	private EntityTplService entityTplService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	protected String applyInternal() throws Exception {
		NodeRef entityTplNodeRef = entityTplService.getEntityTpl(BeCPGModel.TYPE_FINISHEDPRODUCT);

		ProductData entityTpl = alfrescoRepository.findOne(entityTplNodeRef);
		NodeRef renderRuleNodeRef = null;
		for (LabelingRuleListDataItem labelingRuleListDataItem : entityTpl.getLabelingListView().getLabelingRuleList()) {
			if (LabelingRuleType.Render.equals(labelingRuleListDataItem.getLabelingRuleType()) && labelingRuleListDataItem.getName().contains("1")) {
				renderRuleNodeRef = labelingRuleListDataItem.getNodeRef();
				break;
			}
		}

		if (renderRuleNodeRef == null) {
			logger.info("No default labeling Rule found creating one");
			LabelingRuleListDataItem labelingRuleListDataItem = new LabelingRuleListDataItem("Étiquetage 1", "render()", LabelingRuleType.Render);
			labelingRuleListDataItem.setIsManual(false);
			labelingRuleListDataItem.setIsActive(true);
			entityTpl.getLabelingListView().getLabelingRuleList().add(labelingRuleListDataItem);
			alfrescoRepository.save(entityTpl);
			renderRuleNodeRef = labelingRuleListDataItem.getNodeRef();
		}

		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(entityTplNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);

		if (renderRuleNodeRef == null) {
			throw new RuntimeException("No rule created");
		}
		for (AssociationRef assocRef : assocRefs) {
			if (!nodeService.hasAspect(assocRef.getSourceRef(), BeCPGModel.ASPECT_COMPOSITE_VERSION) && !entityTplNodeRef.equals(assocRef.getSourceRef()) ) {

				
				try {
					ProductData entity = alfrescoRepository.findOne(assocRef.getSourceRef());

					logger.info("Patch ingLabeling for :" + entity.getName());

					entity.getLabelingListView().getLabelingRuleList().clear();

					for (IngLabelingListDataItem ingLabelingListDataItem : entity.getLabelingListView().getIngLabelingList()) {
						ingLabelingListDataItem.setGrp(renderRuleNodeRef);
						break;
					}

					alfrescoRepository.save(entity);

				} catch (Exception e) {
					logger.error(e, e);
				}
			}
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
