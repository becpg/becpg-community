package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem;
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem.SignalWord;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * <p>ClaimRequirementScanner class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class HazardClassificationRequirementScanner extends AbstractRequirementScanner<HazardClassificationListDataItem> {

	/** Constant <code>MESSAGE_NOT_CLAIM="message.formulate.labelClaim.notClaimed"</code> */
	public static final String MESSAGE_FORBIDDEN_CLASSIFICATION = "message.formulate.clp.forbidden";

	private static Log logger = LogFactory.getLog(HazardClassificationRequirementScanner.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, 
	        List<ProductSpecificationData> specifications) {
	    List<ReqCtrlListDataItem> results = new ArrayList<>();
	    
	    List<HazardClassificationListDataItem> visitedDataList = getDataListVisited(formulatedProduct);
	    if (visitedDataList == null || visitedDataList.isEmpty()) {
	        return results;
	    }

	    Map<ProductSpecificationData, List<HazardClassificationListDataItem>> specRequirements = 
	            extractRequirements(specifications);
	    
	    for (Map.Entry<ProductSpecificationData, List<HazardClassificationListDataItem>> entry : 
	            specRequirements.entrySet()) {
	        ProductSpecificationData specification = entry.getKey();
	        List<HazardClassificationListDataItem> requirements = entry.getValue();
	        
	        for (HazardClassificationListDataItem specDataItem : requirements) {
	            for (HazardClassificationListDataItem listDataItem : visitedDataList) {
	                if (matchesHazardStatement(specDataItem, listDataItem) ||
	                    matchesSignalWord(specDataItem, listDataItem) ||
	                    matchesPictogram(specDataItem, listDataItem)) {
	                    
	                    processRequirement(results, specDataItem, listDataItem, specification);
	                }
	            }
	        }
	    }
	    
	    return results;
	}

	private boolean matchesHazardStatement(HazardClassificationListDataItem spec, 
	        HazardClassificationListDataItem product) {
	    return spec.getHazardStatement() != null &&
	           spec.getHazardStatement().equals(product.getHazardStatement()) &&
	           (spec.getSignalWord() == null || 
	            product.getSignalWord() == null ||
	            isSignalWordLevelEqualOrHigher(spec.getSignalWord(), product.getSignalWord()));
	}

	private boolean matchesSignalWord(HazardClassificationListDataItem spec, 
	        HazardClassificationListDataItem product) {
	    return spec.getHazardStatement() == null && spec.getSignalWord() != null &&
	           product.getSignalWord() != null &&
	           isSignalWordLevelEqualOrHigher(spec.getSignalWord(), product.getSignalWord());
	}

	private boolean matchesPictogram(HazardClassificationListDataItem spec, 
	        HazardClassificationListDataItem product) {
	    return spec.getHazardStatement() == null && spec.getPictogram() != null &&
	           spec.getPictogram().equals(product.getPictogram());
	}

	
	/**
	 * Determines if the product's signal word level is equal to or higher than the specification's signal word level.
	 * @param specSignalWord The signal word from the specification.
	 * @param productSignalWord The signal word from the product.
	 * @return true if the product's signal word level is equal to or higher, false otherwise.
	 */
	private boolean isSignalWordLevelEqualOrHigher(String specSignalWord, String productSignalWord) {
	    // Define signal word hierarchy
	    Map<String, Integer> signalWordLevels = Map.of(
	    		SignalWord.Danger.toString(), 2,
	    		SignalWord.Warning.toString(), 1
	    );

	    // Get levels for the signal words
	    Integer specLevel = signalWordLevels.get(specSignalWord);
	    Integer productLevel = signalWordLevels.get(productSignalWord);

	    // Compare levels (null-safe)
	    return productLevel != null && specLevel != null && productLevel >= specLevel;
	}
	
	/**
	 * Helper method to process and add a requirement control item to the result list.
	 */
	private void processRequirement(List<ReqCtrlListDataItem> ret, 
	                                HazardClassificationListDataItem specDataItem,
	                                HazardClassificationListDataItem listDataItem,
	                                ProductSpecificationData specification) {

	    if (logger.isDebugEnabled()) {
	        logger.debug(extractName(specDataItem.getHazardStatement()) + " has been visited");
	    }

	    MLText message = MLTextHelper.getI18NMessage(MESSAGE_FORBIDDEN_CLASSIFICATION, extractName(listDataItem.getHazardStatement()));
	    String regulatoryId = extractRegulatoryId(specDataItem, specification);
	    RequirementType reqType = (specDataItem.getRegulatoryType() != null) ? specDataItem.getRegulatoryType() : RequirementType.Forbidden;

	    if (specDataItem.getRegulatoryMessage() != null && !MLTextHelper.isEmpty(specDataItem.getRegulatoryMessage())) {
	        message = specDataItem.getRegulatoryMessage();
	    }

	    ReqCtrlListDataItem reqCtrl = ReqCtrlListDataItem.build()
	        .ofType(reqType)
	        .withMessage(message)
	        .withSources(Arrays.asList(listDataItem.getHazardStatement()))
	        .ofDataType(RequirementDataType.Specification)
	        .withRegulatoryCode(regulatoryId);
	    
	    ret.add(reqCtrl);
	}

	/** {@inheritDoc} */
	@Override
	protected void mergeRequirements(List<HazardClassificationListDataItem> ret, List<HazardClassificationListDataItem> toAdd) {
		ret.addAll(toAdd);
	}

	/** {@inheritDoc} */
	@Override
	protected List<HazardClassificationListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getHcList() != null ? partProduct.getHcList() : new ArrayList<>();
	}

}
