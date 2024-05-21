package fr.becpg.repo.product.extractor;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.DataListItemExtractor;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

public class ReqCtrlListItemExtractor implements DataListItemExtractor {

	private static final Log logger = LogFactory.getLog(ReqCtrlListItemExtractor.class);

	private NodeService nodeService;
	
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private EntityListDAO entityListDAO;
	
	private AssociationService associationService;
	
	public ReqCtrlListItemExtractor() {
		SimpleExtractor.registerDataListItemExtractor(PLMModel.TYPE_REQCTRLLIST, this);
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	@Override
	public List<NodeRef> extractItems(NodeRef nodeRef) {
		
		List<NodeRef> extractedItems = new ArrayList<>();
			
			try {
				NodeRef charact = null;

				RepositoryEntity item = alfrescoRepository.findOne(nodeRef);

				if (item instanceof SimpleCharactDataItem simpleItem) {
					charact = simpleItem.getCharactNodeRef();
				} else if (item instanceof LabelClaimListDataItem labelClaimItem) {
					charact = labelClaimItem.getLabelClaim();
				}  else if (item instanceof IngRegulatoryListDataItem ingRegItem) {
					charact = ingRegItem.getIng();
				}

				NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityListDAO.getEntity(nodeRef));
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_REQCTRLLIST);

				if (listNodeRef != null) {
					List<NodeRef> reqCtrlList = entityListDAO.getListItems(listNodeRef, PLMModel.TYPE_REQCTRLLIST);

					for (NodeRef reqCtrl : reqCtrlList) {
						
						@SuppressWarnings("unchecked")
						List<NodeRef> sources = (List<NodeRef>) nodeService.getProperty(reqCtrl, PLMModel.PROP_RCL_SOURCES_V2);

						if (((charact != null) && charact.equals(associationService.getTargetAssoc(reqCtrl, PLMModel.ASSOC_RCL_CHARACT))
								|| (sources!=null && sources.contains(charact)))) {
							extractedItems.add(reqCtrl);
						}
					}
				}
			} catch (StackOverflowError e) {
				logger.debug("Infinity loop : " + nodeRef, e);
			}
			
			return extractedItems;
	}

}
