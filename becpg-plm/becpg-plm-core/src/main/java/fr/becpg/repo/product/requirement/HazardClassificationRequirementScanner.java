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

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, 
	        List<ProductSpecificationData> specifications) {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Starting checkRequirements for product: " + 
	                (formulatedProduct != null ? formulatedProduct.getNodeRef() : "null") + 
	                " with " + (specifications != null ? specifications.size() : 0) + " specifications");
	    }
	    
	    List<ReqCtrlListDataItem> results = new ArrayList<>();
	    
	    List<HazardClassificationListDataItem> visitedDataList = getDataListVisited(formulatedProduct);
	    if (visitedDataList == null || visitedDataList.isEmpty()) {
	        if (logger.isDebugEnabled()) {
	            logger.debug("No hazard classification data found for product");
	        }
	        return results;
	    }
	    
	    if (logger.isDebugEnabled()) {
	        logger.debug("Found " + visitedDataList.size() + " hazard classifications for product");
	    }

	    Map<ProductSpecificationData, List<HazardClassificationListDataItem>> specRequirements = 
	            extractRequirements(specifications);
	    
	    if (logger.isDebugEnabled()) {
	        logger.debug("Extracted " + specRequirements.size() + " specifications with hazard classification requirements");
	    }
	    
	    for (Map.Entry<ProductSpecificationData, List<HazardClassificationListDataItem>> entry : 
	            specRequirements.entrySet()) {
	        ProductSpecificationData specification = entry.getKey();
	        List<HazardClassificationListDataItem> requirements = entry.getValue();
	        
	        if (logger.isDebugEnabled()) {
	            logger.debug("Processing specification: " + specification.getNodeRef() + 
	                    " with " + requirements.size() + " requirements");
	        }
	        
	        for (HazardClassificationListDataItem specDataItem : requirements) {
	            if (logger.isDebugEnabled()) {
	                logger.debug("Checking spec requirement: hazardStatement=" + specDataItem.getHazardStatement() + 
	                        ", signalWord=" + specDataItem.getSignalWord() + 
	                        ", pictogram=" + specDataItem.getPictogram());
	            }
	            
	            for (HazardClassificationListDataItem listDataItem : visitedDataList) {
	                if (logger.isDebugEnabled()) {
	                    logger.debug("Against product hazard: hazardStatement=" + listDataItem.getHazardStatement() + 
	                            ", signalWord=" + listDataItem.getSignalWord() + 
	                            ", pictogram=" + listDataItem.getPictogram());
	                }
	                
	                boolean hazardMatch = matchesHazardStatement(specDataItem, listDataItem);
	                boolean signalWordMatch = matchesSignalWord(specDataItem, listDataItem);
	                boolean pictogramMatch = matchesPictogram(specDataItem, listDataItem);
	                
	                if (logger.isDebugEnabled()) {
	                    logger.debug("Match results: hazard=" + hazardMatch + 
	                            ", signalWord=" + signalWordMatch + 
	                            ", pictogram=" + pictogramMatch);
	                }
	                
	                if (hazardMatch || signalWordMatch || pictogramMatch) {
	                    processRequirement(results, specDataItem, listDataItem, specification);
	                }
	            }
	        }
	    }
	    
	    if (logger.isDebugEnabled()) {
	        logger.debug("Completed checkRequirements with " + results.size() + " requirements found");
	    }
	    
	    return results;
	}

	private boolean matchesHazardStatement(HazardClassificationListDataItem spec, 
	        HazardClassificationListDataItem product) {
	    boolean hasHazardStatement = spec.getHazardStatement() != null;
	    boolean hazardStatementsEqual = hasHazardStatement && 
	            spec.getHazardStatement().equals(product.getHazardStatement());
	    boolean signalWordCheck = spec.getSignalWord() == null || 
	            product.getSignalWord() == null ||
	            isSignalWordLevelEqualOrHigher(spec.getSignalWord(), product.getSignalWord());
	            
	    boolean result = hasHazardStatement && hazardStatementsEqual && signalWordCheck;
	    
	    if (logger.isDebugEnabled()) {
	        logger.debug("matchesHazardStatement: hasHazardStatement=" + hasHazardStatement + 
	                ", hazardStatementsEqual=" + hazardStatementsEqual + 
	                ", signalWordCheck=" + signalWordCheck + 
	                ", result=" + result);
	    }
	    
	    return result;
	}

	private boolean matchesSignalWord(HazardClassificationListDataItem spec, 
	        HazardClassificationListDataItem product) {
	    boolean noHazardStatement = spec.getHazardStatement() == null;
	    boolean hasSpecSignalWord = spec.getSignalWord() != null;
	    boolean hasProductSignalWord = product.getSignalWord() != null;
	    boolean signalWordLevelCheck = hasSpecSignalWord && hasProductSignalWord && 
	            isSignalWordLevelEqualOrHigher(spec.getSignalWord(), product.getSignalWord());
	            
	    boolean result = noHazardStatement && signalWordLevelCheck;
	    
	    if (logger.isDebugEnabled()) {
	        logger.debug("matchesSignalWord: noHazardStatement=" + noHazardStatement + 
	                ", hasSpecSignalWord=" + hasSpecSignalWord + 
	                ", hasProductSignalWord=" + hasProductSignalWord + 
	                ", signalWordLevelCheck=" + (hasSpecSignalWord && hasProductSignalWord ? 
	                        isSignalWordLevelEqualOrHigher(spec.getSignalWord(), product.getSignalWord()) : "N/A") + 
	                ", result=" + result);
	    }
	    
	    return result;
	}

	private boolean matchesPictogram(HazardClassificationListDataItem spec, 
	        HazardClassificationListDataItem product) {
	    boolean noHazardStatement = spec.getHazardStatement() == null;
	    boolean hasSpecPictogram = spec.getPictogram() != null;
	    boolean pictogramsEqual = hasSpecPictogram && product.getPictogram() != null && 
	            spec.getPictogram().equals(product.getPictogram());
	            
	    boolean result = noHazardStatement && hasSpecPictogram && pictogramsEqual;
	    
	    if (logger.isDebugEnabled()) {
	        logger.debug("matchesPictogram: noHazardStatement=" + noHazardStatement + 
	                ", hasSpecPictogram=" + hasSpecPictogram + 
	                ", pictogramsEqual=" + pictogramsEqual + 
	                ", result=" + result);
	    }
	    
	    return result;
	}

	
	/**
	 * Determines if the product's signal word level is equal to or higher than the specification's signal word level.
	 * @param specSignalWord The signal word from the specification.
	 * @param productSignalWord The signal word from the product.
	 * @return true if the product's signal word level is equal to or higher, false otherwise.
	 */
	private boolean isSignalWordLevelEqualOrHigher(String specSignalWord, String productSignalWord) {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Comparing signal words: spec=\"" + specSignalWord + "\", product=\"" + productSignalWord + "\"");
	    }
	    
	    // Handle null values
	    if (specSignalWord == null) {
	        // If spec signal word is null, any product signal word is considered higher
	        if (logger.isDebugEnabled()) {
	            logger.debug("Spec signal word is null, returning true");
	        }
	        return true;
	    }
	    if (productSignalWord == null) {
	        // If product signal word is null but spec has a signal word, it's not higher
	        if (logger.isDebugEnabled()) {
	            logger.debug("Product signal word is null but spec has a signal word, returning false");
	        }
	        return false;
	    }
	    
	    // Define signal word hierarchy (higher number = higher severity)
	    Map<String, Integer> signalWordLevels = Map.of(
	    		SignalWord.Danger.toString(), 2,
	    		SignalWord.Warning.toString(), 1,
	    		"", 0  // Empty string as lowest level
	    );

	    // Get levels for the signal words with default value of 0 for unknown signal words
	    Integer specLevel = signalWordLevels.getOrDefault(specSignalWord, 0);
	    Integer productLevel = signalWordLevels.getOrDefault(productSignalWord, 0);

	    if (logger.isDebugEnabled()) {
	        logger.debug("Signal word levels: spec=" + specLevel + " (" + specSignalWord + "), " + 
	                "product=" + productLevel + " (" + productSignalWord + ")");
	    }

	    // Compare levels
	    boolean result = productLevel >= specLevel;
	    
	    if (logger.isDebugEnabled()) {
	        logger.debug("Signal word comparison result: " + result);
	    }
	    
	    return result;
	}
	
	/**
	 * Helper method to process and add a requirement control item to the result list.
	 */
	private void processRequirement(List<ReqCtrlListDataItem> ret, 
	                                HazardClassificationListDataItem specDataItem,
	                                HazardClassificationListDataItem listDataItem,
	                                ProductSpecificationData specification) {

	    if (logger.isDebugEnabled()) {
	        logger.debug("Processing requirement for hazard statement: " + 
	                extractName(specDataItem.getHazardStatement()) + 
	                " matched with product hazard: " + 
	                extractName(listDataItem.getHazardStatement()));
	    }

	    MLText message = MLTextHelper.getI18NMessage(MESSAGE_FORBIDDEN_CLASSIFICATION, extractName(listDataItem.getHazardStatement()));
	    String regulatoryId = extractRegulatoryId(specDataItem, specification);
	    RequirementType reqType = (specDataItem.getRegulatoryType() != null) ? specDataItem.getRegulatoryType() : RequirementType.Forbidden;

	    if (logger.isDebugEnabled()) {
	        logger.debug("Requirement details: regulatoryId=" + regulatoryId + 
	                ", requirementType=" + reqType);
	    }

	    if (specDataItem.getRegulatoryMessage() != null && !MLTextHelper.isEmpty(specDataItem.getRegulatoryMessage())) {
	        message = specDataItem.getRegulatoryMessage();
	        if (logger.isDebugEnabled()) {
	            logger.debug("Using custom regulatory message from specification");
	        }
	    }

	    ReqCtrlListDataItem reqCtrl = ReqCtrlListDataItem.build()
	        .ofType(reqType)
	        .withMessage(message)
	        .withSources(Arrays.asList(listDataItem.getHazardStatement()))
	        .ofDataType(RequirementDataType.Specification)
	        .withRegulatoryCode(regulatoryId);
	    
	    if (logger.isDebugEnabled()) {
	        logger.debug("Created requirement control item: type=" + reqType + 
	                ", dataType=" + RequirementDataType.Specification + 
	                ", regulatoryCode=" + regulatoryId);
	    }
	    
	    ret.add(reqCtrl);
	}

	/** {@inheritDoc} */
	@Override
	protected void mergeRequirements(List<HazardClassificationListDataItem> ret, List<HazardClassificationListDataItem> toAdd) {
		if (logger.isDebugEnabled()) {
			logger.debug("Merging " + toAdd.size() + " hazard classification requirements into list of " + ret.size());
		}
		ret.addAll(toAdd);
		if (logger.isDebugEnabled()) {
			logger.debug("After merge, total hazard classification requirements: " + ret.size());
		}
	}

	/** {@inheritDoc} */
	@Override
	protected List<HazardClassificationListDataItem> getDataListVisited(ProductData partProduct) {
		List<HazardClassificationListDataItem> result = partProduct.getHcList() != null ? 
				partProduct.getHcList() : new ArrayList<>();
				
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieved " + result.size() + " hazard classifications for product" + 
					(partProduct != null ? " " + partProduct.getNodeRef() : ""));
		}
		
		return result;
	}

}
