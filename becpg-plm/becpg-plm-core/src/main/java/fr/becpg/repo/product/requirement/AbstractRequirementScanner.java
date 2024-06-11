package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RegulatoryEntityItem;

/**
 * <p>Abstract AbstractRequirementScanner class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractRequirementScanner<T> implements RequirementScanner {

	protected NodeService mlNodeService;
	
	protected Boolean addInfoReqCtrl;
	
	/**
	 * <p>Setter for the field <code>addInfoReqCtrl</code>.</p>
	 *
	 * @param addInfoReqCtrl a {@link java.lang.Boolean} object
	 */
	public void setAddInfoReqCtrl(Boolean addInfoReqCtrl) {
		this.addInfoReqCtrl = addInfoReqCtrl;
	}


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
	 * <p>extractRegulatoryId.</p>
	 *
	 * @param regulatoryEntityItem a {@link fr.becpg.repo.product.data.RegulatoryEntityItem} object
	 * @param specification a {@link fr.becpg.repo.product.data.ProductSpecificationData} object
	 * @return a {@link java.lang.String} object
	 */
	protected String extractRegulatoryId(RegulatoryEntityItem regulatoryEntityItem, ProductSpecificationData specification) {
		if (regulatoryEntityItem.getRegulatoryCountriesRef() != null && !regulatoryEntityItem.getRegulatoryCountriesRef().isEmpty()) {
			String countryCode = (String) mlNodeService.getProperty(regulatoryEntityItem.getRegulatoryCountriesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			if (regulatoryEntityItem.getRegulatoryUsagesRef() != null && !regulatoryEntityItem.getRegulatoryUsagesRef().isEmpty()) {
				return countryCode + " - " + (String) mlNodeService.getProperty(regulatoryEntityItem.getRegulatoryUsagesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			}
		}
		if (specification.getRegulatoryCountriesRef() != null && !specification.getRegulatoryCountriesRef().isEmpty()) {
			String countryCode = (String) mlNodeService.getProperty(specification.getRegulatoryCountriesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			if (specification.getRegulatoryUsagesRef() != null && !specification.getRegulatoryUsagesRef().isEmpty()) {
				return countryCode + " - " + (String) mlNodeService.getProperty(specification.getRegulatoryUsagesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			}
		}
		if (specification.getRegulatoryCode() != null && !specification.getRegulatoryCode().isBlank()) {
			return specification.getRegulatoryCode();
		}
		return specification.getName();
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
