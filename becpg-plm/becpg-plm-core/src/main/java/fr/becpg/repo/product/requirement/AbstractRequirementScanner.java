package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;

/**
 * <p>Abstract AbstractRequirementScanner class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractRequirementScanner<T> implements RequirementScanner {

	protected NodeService mlNodeService;

	/**
	 * <p>Setter for the field <code>mlNodeService</code>.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/**
	 * <p>extractRequirements.</p>
	 *
	 * @param specifications a {@link java.util.List} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<ProductSpecificationData, List<T>> extractRequirements(List<ProductSpecificationData> specifications) {
		Map<ProductSpecificationData, List<T>> ret = new HashMap<>();
		if (specifications != null) {
			for (ProductSpecificationData specification : specifications) {
				List<T> tmp = new ArrayList<>();

				mergeRequirements(tmp, extractRequirementsFromParent(specification.getProductSpecifications()));
				List<T> dataListVisited = getDataListVisited(specification);
				if (dataListVisited != null) {
					mergeRequirements(tmp, dataListVisited);
				}

				ret.put(specification, tmp);

			}
		}
		return ret;

	}

	private List<T> extractRequirementsFromParent(List<ProductSpecificationData> specifications) {
		List<T> ret = new ArrayList<>();
		if (specifications != null) {
			for (ProductSpecificationData specification : specifications) {
				mergeRequirements(ret, extractRequirementsFromParent(specification.getProductSpecifications()));
				List<T> dataListVisited = getDataListVisited(specification);
				if (dataListVisited != null) {
					mergeRequirements(ret, dataListVisited);
				}
			}
		}
		return ret;
	}

	/**
	 * <p>extractName.</p>
	 *
	 * @param charactRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	protected MLText extractName(NodeRef charactRef) {
		return (MLText) mlNodeService.getProperty(charactRef, BeCPGModel.PROP_CHARACT_NAME);
	}

	/**
	 * <p>getDataListVisited.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.util.List} object.
	 */
	protected abstract List<T> getDataListVisited(ProductData productData);

	/**
	 * <p>mergeRequirements.</p>
	 *
	 * @param ret a {@link java.util.List} object.
	 * @param toAdd a {@link java.util.List} object.
	 */
	protected abstract void mergeRequirements(List<T> ret, List<T> toAdd);

}
