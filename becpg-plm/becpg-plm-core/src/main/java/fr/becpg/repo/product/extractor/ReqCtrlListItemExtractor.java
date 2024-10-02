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
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>ReqCtrlListItemExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReqCtrlListItemExtractor implements DataListItemExtractor {

	private static final Log logger = LogFactory.getLog(ReqCtrlListItemExtractor.class);

	private NodeService nodeService;
	
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private EntityListDAO entityListDAO;
	
	private AssociationService associationService;
	
	/**
	 * <p>Constructor for ReqCtrlListItemExtractor.</p>
	 */
	public ReqCtrlListItemExtractor() {
		SimpleExtractor.registerDataListItemExtractor(PLMModel.TYPE_REQCTRLLIST, this);
	}
	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}
	
	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}
	
	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<NodeRef> extractItems(NodeRef nodeRef) {
		
		List<NodeRef> extractedItems = new ArrayList<>();
			
			try {
				NodeRef charact = null;

				RepositoryEntity item = alfrescoRepository.findOne(nodeRef);

				if (item instanceof IngListDataItem simpleItem) {
					charact = simpleItem.getNodeRef();
				} else if (item instanceof SimpleCharactDataItem simpleItem) {
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
							if (item instanceof IngRegulatoryListDataItem ingRegItem) {
								String reqCtrlCode = (String) nodeService.getProperty(reqCtrl, PLMModel.PROP_REGULATORY_CODE);
								if (reqCtrlCode != null && ingRegItem.getRegulatoryCountries().stream().anyMatch(u -> reqCtrlCode.contains(((String) nodeService.getProperty(u, PLMModel.PROP_REGULATORY_CODE))))) {
									extractedItems.add(reqCtrl);
								}
							} else {
								extractedItems.add(reqCtrl);
							}
						}
					}
				}
			} catch (StackOverflowError e) {
				logger.debug("Infinity loop : " + nodeRef, e);
			}
			
			return extractedItems;
	}

}
