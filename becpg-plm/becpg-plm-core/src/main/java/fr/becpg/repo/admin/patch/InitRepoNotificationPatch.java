package fr.becpg.repo.admin.patch;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;

public class InitRepoNotificationPatch extends AbstractBeCPGPatch {

	private static final String MSG_SUCCESS = "patch.bcpg.plm.InitRepoNotificationPatch.result";

	@Override
	protected String applyInternal() throws Exception {
		
		NodeRef companyHomeNodeRef = repository.getCompanyHome();
		
		if (companyHomeNodeRef != null) {
			NodeRef systemNodeRef = repoService.getFolderByPath(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);
			NodeRef charactsNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);
			NodeRef listContainer = charactsNodeRef == null ? null : nodeService.getChildByName(charactsNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
			NodeRef notificationFolder = listContainer == null ? null : nodeService.getChildByName(listContainer, ContentModel.ASSOC_CONTAINS, RepoConsts.PATH_NOTIFICATIONS);
			NodeRef requirementsNotification = nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.FORMULATION_ERRORS_NOTIFICATION);
			
			if (requirementsNotification != null) {
				nodeService.setProperty(requirementsNotification, QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"), "{\"query\":\"+@{http://www.bcpg.fr/model/becpg/1.0}rclDataType:\\\"Formulation\\\"\"}");
			}
			
			NodeRef inProgressProjectsNotification = nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.IN_PROGRESS_PROJECTS_NOTIFICATION);
			
			if (inProgressProjectsNotification != null) {
				nodeService.setProperty(inProgressProjectsNotification, QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"), "{\"query\":\"+@{http://www.bcpg.fr/model/project/1.0}projectState:\\\"InProgress\\\"\"}");
			}
			
			NodeRef validatedProductsNotification = nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.VALIDATED_PRODUCTS_NOTIFICATION);
			
			if (validatedProductsNotification != null) {
				nodeService.setProperty(validatedProductsNotification, QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"), "{\"query\":\"+@{http://www.bcpg.fr/model/becpg/1.0}productState:\\\"Valid\\\"\"}");
			}
			
			NodeRef validatedAndUpdatedProductsNotification = nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.VALIDATED_AND_UPDATED_PRODUCTS_NOTIFICATION);
			
			if (validatedAndUpdatedProductsNotification != null) {
				nodeService.setProperty(validatedAndUpdatedProductsNotification, QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"), "{\"query\":\"+@{http://www.bcpg.fr/model/becpg/1.0}productState:\\\"Valid\\\"\"}");
			}
			
			NodeRef archivedProductsNotification = nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.ARCHIVED_PRODUCTS_NOTIFICATION);
			
			if (archivedProductsNotification != null) {
				nodeService.setProperty(archivedProductsNotification, QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"), "{\"query\":\"+@{http://www.bcpg.fr/model/becpg/1.0}productState:\\\"Archived\\\"\"}");
			}
		}
		
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
