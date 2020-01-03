package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;

public abstract  class AbstractRequirementScanner<T> implements RequirementScanner {

	protected NodeService mlNodeService;
	

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	public List<T> extractRequirements(List<ProductSpecificationData> specifications) {
		List<T> ret = new ArrayList<>();
		if (specifications != null) {
			for (ProductSpecificationData specification : specifications) {
				mergeRequirements(ret, extractRequirements(specification.getProductSpecifications()));
				if (getDataListVisited(specification) != null) {
					mergeRequirements(ret, getDataListVisited(specification));
				}
			}
		}
		return ret;
	}


	protected MLText extractName(NodeRef charactRef) {
		return (MLText) mlNodeService.getProperty(charactRef, BeCPGModel.PROP_CHARACT_NAME);
	}

	
	
	protected abstract List<T> getDataListVisited(ProductData productData);
	
	protected abstract void  mergeRequirements(List<T> ret, List<T> toAdd);
	
}
