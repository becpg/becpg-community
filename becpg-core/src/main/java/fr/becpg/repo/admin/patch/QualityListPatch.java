package fr.becpg.repo.admin.patch;

import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.InitVisitor;

/**
 * QualityListPatch
 * 
 * @author matthieu
 * 
 */
public class QualityListPatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(QualityListPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.qualityListPatch.result";

	private InitVisitor initRepoVisitor;

	private FileFolderService fileFolderService;

	public void setInitRepoVisitor(InitVisitor initRepoVisitor) {
		this.initRepoVisitor = initRepoVisitor;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	@Override
	protected String applyInternal() throws Exception {

		NodeRef systemNodeRef = getFolder(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM);
		if (systemNodeRef != null) {
		NodeRef productHierarchyNodeRef = getFolder(systemNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);
		

			NodeRef oldDatalistFolder = getCharactDataList(productHierarchyNodeRef, RepoConsts.PATH_CLAIM_ORIGIN_HIERARCHY);
			
			if (oldDatalistFolder != null) {

				initRepoVisitor.visitContainer(repository.getCompanyHome());

				NodeRef qualityListNodeRef = getFolder(systemNodeRef, RepoConsts.PATH_QUALITY_LISTS);
				NodeRef newDatalistFolder = getCharactDataList(qualityListNodeRef, RepoConsts.PATH_CLAIM_ORIGIN_HIERARCHY);

				migratePath(oldDatalistFolder, newDatalistFolder);

				nodeService.deleteNode(oldDatalistFolder);
			}
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void migratePath(NodeRef sourceRef, NodeRef destRef) throws FileExistsException, InvalidNodeRefException, FileNotFoundException {

			for (FileInfo file : fileFolderService.list(sourceRef)) {
				logger.info("Move : " + file.getName() + " to " + nodeService.getPath(destRef).toPrefixString(namespaceService));
				fileFolderService.move(file.getNodeRef(), destRef, file.getName());
			}

	}

}
