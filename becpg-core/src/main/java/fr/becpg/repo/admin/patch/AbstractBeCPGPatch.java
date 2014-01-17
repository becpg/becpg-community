package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;

public abstract class AbstractBeCPGPatch extends AbstractPatch {

	private static Log logger = LogFactory.getLog(AbstractBeCPGPatch.class);

	protected Repository repository;
	
	protected RepoService repoService;
	
	protected EntitySystemService entitySystemService;

	protected ContentHelper contentHelper;

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setContentHelper(ContentHelper contentHelper) {
		this.contentHelper = contentHelper;
	}
	
	public void setEntitySystemService(EntitySystemService entitySystemService) {
		this.entitySystemService = entitySystemService;
	}

	
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	protected NodeRef searchFolder(String xpath) {
		List<NodeRef> nodeRefs = searchService.selectNodes(repository.getRootHome(), xpath, null, namespaceService,
				false);
		if (nodeRefs.size() > 1) {
			throw new PatchException("XPath returned too many results: \n" + "   xpath: " + xpath + "\n"
					+ "   results: " + nodeRefs);
		} else if (nodeRefs.size() == 0) {
			// the node does not exist
			return null;
		} else {
			return nodeRefs.get(0);
		}
	}

	protected void updateResource(String xPath, String resourcePath) {
		NodeRef nodeRef = searchFolder(xPath);
		if (nodeRef != null) {
			logger.info("Update resource xPath: " + xPath + " with resourcePath: " + resourcePath);
			contentHelper.addFilesResources(nodeRef, resourcePath, true);
		}
	}
	


	public NodeRef getSystemCharactsEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_CHARACTS);
	}

	public NodeRef getSystemListValuesEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_LISTS);
	}

	public NodeRef getSystemHierachiesEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);
	}

	public NodeRef getCharactDataList(NodeRef systemEntityNodeRef, String dataListPath) {
		return entitySystemService.getSystemEntityDataList(systemEntityNodeRef, dataListPath);
	}

	public NodeRef getFolder(NodeRef parentNodeRef, String folderPath) {
		String folderName = TranslateHelper.getTranslatedPath(folderPath);
		if (folderName == null) {
			folderName = folderPath;
		}
		return repoService.getFolderByPath(parentNodeRef, folderPath);
	}


}
