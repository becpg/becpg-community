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
import fr.becpg.repo.regulatory.RegulatoryEntityItem;

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
	 * @param regulatoryEntityItem a {@link fr.becpg.repo.regulatory.RegulatoryEntityItem} object
	 * @param specification a {@link fr.becpg.repo.product.data.ProductSpecificationData} object
	 * @return a {@link java.lang.String} object
	 */
	protected String extractRegulatoryId(RegulatoryEntityItem regulatoryEntityItem, ProductSpecificationData specification) {
		if ((regulatoryEntityItem != null)
				&& ((regulatoryEntityItem.getRegulatoryCountriesRef() != null) && !regulatoryEntityItem.getRegulatoryCountriesRef().isEmpty())) {
			String countryCode = (String) mlNodeService.getProperty(regulatoryEntityItem.getRegulatoryCountriesRef().get(0),
					PLMModel.PROP_REGULATORY_CODE);
			if ((regulatoryEntityItem.getRegulatoryUsagesRef() != null) && !regulatoryEntityItem.getRegulatoryUsagesRef().isEmpty()) {
				return (countryCode != null ? countryCode + " - " : "")
						+ (String) mlNodeService.getProperty(regulatoryEntityItem.getRegulatoryUsagesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			}
		}
		if ((specification.getRegulatoryCountriesRef() != null) && !specification.getRegulatoryCountriesRef().isEmpty()) {
			String countryCode = (String) mlNodeService.getProperty(specification.getRegulatoryCountriesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			if ((specification.getRegulatoryUsagesRef() != null) && !specification.getRegulatoryUsagesRef().isEmpty()) {
				return (countryCode != null ? countryCode + " - " : "")
						+ (String) mlNodeService.getProperty(specification.getRegulatoryUsagesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			}
		}
		if ((specification.getRegulatoryCode() != null) && !specification.getRegulatoryCode().isBlank()) {
			return specification.getRegulatoryCode();
		}
		return specification.getName();
	}
	

	/**
	 * Check if the requirement matches the product's regulatory usage and regulatory country
	 *
	 * @param requirementItem the requirement item (must implement RegulatoryEntityItem)
	 * @param productData the product data
	 * @return true if the requirement should apply to this product
	 */
	protected boolean checkRegulatoryUsageMatch(T requirementItem, ProductData productData) {
		if (!(requirementItem instanceof RegulatoryEntityItem regulatoryItem)) {
			return true; // If not a regulatory entity, no filtering applies
		}

		// Check regulatory usage filtering
		if (!regulatoryItem.getRegulatoryUsagesRef().isEmpty()) {
			boolean hasMatchingUsage = false;
			for (NodeRef productUsage : productData.getRegulatoryUsagesRef()) {
				if (regulatoryItem.getRegulatoryUsagesRef().contains(productUsage)) {
					hasMatchingUsage = true;
					break;
				}
			}
			if (!hasMatchingUsage) {
				return false;
			}
		}

		// Check regulatory country filtering
		if (!regulatoryItem.getRegulatoryCountriesRef().isEmpty()) {
			boolean hasMatchingCountry = false;
			for (NodeRef productCountry : productData.getRegulatoryCountriesRef()) {
				if (regulatoryItem.getRegulatoryCountriesRef().contains(productCountry)) {
					hasMatchingCountry = true;
					break;
				}
			}
			if (!hasMatchingCountry) {
				return false;
			}
		}

		return true;
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
