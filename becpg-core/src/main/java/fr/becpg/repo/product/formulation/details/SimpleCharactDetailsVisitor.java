package fr.becpg.repo.product.formulation.details;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.CharactDetailsVisitor;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

@Service
public class SimpleCharactDetailsVisitor implements CharactDetailsVisitor {

	private static Log logger = LogFactory.getLog(SimpleCharactDetailsVisitor.class);

	protected AlfrescoRepository<SimpleCharactDataItem> alfrescoRepository;
	
	private NodeService nodeService;
	
	protected QName dataListType;

	public void setAlfrescoRepository(AlfrescoRepository<SimpleCharactDataItem> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void setDataListType(QName dataListType) {
		this.dataListType = dataListType;
	}

	@Override
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems) throws FormulateException {

		CharactDetails ret = new CharactDetails(extractCharacts(dataListItems));
		Double netWeight = FormulationHelper.getNetWeight(productData.getNodeRef(), nodeService);

		if (productData.hasCompoListEl(EffectiveFilters.EFFECTIVE)) {
			for (CompoListDataItem compoListDataItem : productData.getCompoList(EffectiveFilters.EFFECTIVE)) {
				Double qty = FormulationHelper.getQty(compoListDataItem);			
				visitPart(compoListDataItem.getProduct(), ret, qty, netWeight);
			}
		}		

		return ret;
	}

	protected List<NodeRef> extractCharacts(List<NodeRef> dataListItems) {

		List<NodeRef> ret = new ArrayList<NodeRef>();
		if (dataListItems != null) {
			for (NodeRef dataListItem : dataListItems) {

				SimpleCharactDataItem o = alfrescoRepository.findOne(dataListItem);
				if (o != null ) {
					ret.add(o.getCharactNodeRef());
				}
			}
		}

		return ret;
	}

	protected void visitPart(NodeRef entityNodeRef, CharactDetails charactDetails, Double qty, Double netWeight)
			throws FormulateException {

		if(entityNodeRef == null){
			return;
		}

		if (!alfrescoRepository.hasDataList(entityNodeRef,dataListType)) {
			logger.debug("no datalist for this product, exit. dataListType: " + dataListType + " entity: " + entityNodeRef);
			return;
		}
		
		List<SimpleCharactDataItem> simpleCharactDataList = alfrescoRepository.loadDataList(entityNodeRef,dataListType,  dataListType);

		for (SimpleCharactDataItem simpleCharact : simpleCharactDataList) {
			if (simpleCharact != null && charactDetails.hasElement(simpleCharact.getCharactNodeRef())) {

				Double value = (simpleCharact.getValue() != null ? simpleCharact.getValue() : 0d);
				value = value * qty;
				if (netWeight != 0.0d) {
					value = value / netWeight;
				}
				
				if (logger.isDebugEnabled()) {
					logger.debug("Add new charact detail. Charact: " + simpleCharact.getCharactNodeRef() + " - entityNodeRef: " + entityNodeRef + " - value: " + value);
				}
				charactDetails.addKeyValue(simpleCharact.getCharactNodeRef(), entityNodeRef, value);
			}
		}
	}
}