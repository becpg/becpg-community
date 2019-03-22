package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;

public abstract  class AbstractRequirementScanner<T> implements RequirementScanner {

	protected NodeService nodeService;
	

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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

	protected abstract List<T> getDataListVisited(ProductData productData);
	
	protected abstract void  mergeRequirements(List<T> ret, List<T> toAdd);
	
}
