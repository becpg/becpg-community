package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.data.SurveyListDataItem;

/**
 * <p>SurveyListRequirementScanner class.</p>
 *
 * <p>This scanner checks that a question in a survey list matches the correct answer
 * according to specifications.</p>
 *
 * @author matthieu
 */
public class SurveyListRequirementScanner extends AbstractRequirementScanner<SurveyListDataItem> {

    /** Constant `MESSAGE_SURVEY_INCORRECT_ANSWER="message.formulate.survey.incorrect.answer"` */
    public static final String MESSAGE_SURVEY_INCORRECT_ANSWER = "message.formulate.survey.incorrect.answer";
    
    /** Constant `MESSAGE_SURVEY_MISSING_CHOICE="message.formulate.survey.missing.choice"` */
    public static final String MESSAGE_SURVEY_MISSING_CHOICE = "message.formulate.survey.missing.choice";
    
    /** Constant `MESSAGE_SURVEY_FORBIDDEN_VALUE="message.formulate.survey.forbidden.value"` */
    public static final String MESSAGE_SURVEY_FORBIDDEN_VALUE = "message.formulate.survey.forbidden.value";
    
    /** Constant `MESSAGE_SURVEY_INFO_VALUE="message.formulate.survey.info.value"` */
    public static final String MESSAGE_SURVEY_INFO_VALUE = "message.formulate.survey.info.value";
    
    private static final Log logger = LogFactory.getLog(SurveyListRequirementScanner.class);
    
    /**
     * Result class to hold the requirement check result
     */
    private static class RequirementCheckResult {
        private final RequirementType requirementType;
        private final MLText message;
        private final boolean shouldCreateRequirement;
        
        public RequirementCheckResult(RequirementType requirementType, MLText message, boolean shouldCreateRequirement) {
            this.requirementType = requirementType;
            this.message = message;
            this.shouldCreateRequirement = shouldCreateRequirement;
        }
        
        public RequirementType getRequirementType() {
            return requirementType;
        }
        
        public MLText getMessage() {
            return message;
        }
        
        public boolean shouldCreateRequirement() {
            return shouldCreateRequirement;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public List<RequirementListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
        List<RequirementListDataItem> ret = new ArrayList<>();

        if (formulatedProduct.getSurveyList() != null && !formulatedProduct.getSurveyList().isEmpty()) {
            
            for (Map.Entry<ProductSpecificationData, List<SurveyListDataItem>> entry : extractRequirements(specifications).entrySet()) {
                List<SurveyListDataItem> requirements = entry.getValue();
                ProductSpecificationData specification = entry.getKey();

                requirements.forEach(specDataItem -> {
                    formulatedProduct.getSurveyList().forEach(listDataItem -> {
                        // Check if the questions match
                        if (listDataItem.getQuestion() != null && specDataItem.getQuestion() != null 
                                && listDataItem.getQuestion().equals(specDataItem.getQuestion())) {
                            
                            RequirementCheckResult checkResult = checkAnswer(listDataItem, specDataItem);
                            
                            if (checkResult.shouldCreateRequirement() || Boolean.TRUE.equals(addInfoReqCtrl)) {
                                MLText message = checkResult.getMessage();
                                if (message == null) {
                                    // Determine if choice is missing or incorrect for default message
                                    if (CollectionUtils.isEmpty(listDataItem.getChoices())) {
                                        message = MLTextHelper.getI18NMessage(MESSAGE_SURVEY_MISSING_CHOICE, 
                                                extractName(listDataItem.getQuestion()));
                                    } else {
                                        message = MLTextHelper.getI18NMessage(MESSAGE_SURVEY_INCORRECT_ANSWER, 
                                                extractName(listDataItem.getQuestion()));
                                    }
                                }
                                
                                RequirementListDataItem rclDataItem = RequirementListDataItem.build()
                                        .ofType(checkResult.getRequirementType()).withMessage(message)
                                        .withCharact(listDataItem.getQuestion()).ofDataType(RequirementDataType.Specification)
										.withRegulatoryCode(extractRegulatoryId(specDataItem, specification));
                         
                               
                                ret.add(rclDataItem);
                            }
                        }
                    });
                });
            }
        }

        return ret;
    }

    /**
     * Check the survey answer against the specification and return the appropriate requirement result
     * 
     * @param actualItem The survey item from the formulatedProduct
     * @param requiredItem The survey item from the specification
     * @return RequirementCheckResult with the appropriate requirement type and message
     */
    private RequirementCheckResult checkAnswer(SurveyListDataItem actualItem, SurveyListDataItem requiredItem) {
        // If spec has no choices defined, any choice is valid - no requirement needed
        if (CollectionUtils.isEmpty(requiredItem.getChoices())) {
            if (logger.isDebugEnabled()) {
                logger.debug("No choices defined in specification for question: " + extractName(actualItem.getQuestion()));
            }
            return new RequirementCheckResult(null, null, false);
        }
        
        // If no choices selected but required, it's invalid
        if (CollectionUtils.isEmpty(actualItem.getChoices())) {
            if (logger.isDebugEnabled()) {
                logger.debug("No choices selected in product for required question: " + extractName(actualItem.getQuestion()));
            }
            MLText message = MLTextHelper.getI18NMessage(MESSAGE_SURVEY_MISSING_CHOICE, 
                    extractName(actualItem.getQuestion()));
            return new RequirementCheckResult(RequirementType.Forbidden, message, true);
        }
        
        // Get the regulatory type from specification
        RequirementType specRegulatoryType = requiredItem.getRegulatoryType();
        
        if (logger.isDebugEnabled()) {
            logger.debug("Checking answer for question: " + extractName(actualItem.getQuestion()) + 
                       ", spec type: " + specRegulatoryType + 
                       ", actual choices: " + actualItem.getChoices().size() + 
                       ", required choices: " + requiredItem.getChoices().size());
            
            // Log details for debugging
            logger.debug("Actual choices: " + actualItem.getChoices().stream()
                        .map(this::extractName).toList());
            logger.debug("Required choices: " + requiredItem.getChoices().stream()
                        .map(this::extractName).toList());
        }
        
        // Case 1: Forbidden value in CDC -> red alert when value is selected
        if (specRegulatoryType == RequirementType.Forbidden) {
            // Check if any forbidden value is selected in the product
            boolean hasForbiddenChoice = requiredItem.getChoices().stream()
                    .anyMatch(reqChoice -> actualItem.getChoices().stream()
                            .anyMatch(choice -> choice.equals(reqChoice)));
            
            if (hasForbiddenChoice) {
                MLText message;
                if (requiredItem.getRegulatoryMessage() != null && !MLTextHelper.isEmpty(requiredItem.getRegulatoryMessage())) {
                    // Use only custom message without question context
                    message = requiredItem.getRegulatoryMessage();
                } else {
                    // Use default message with question context
                    message = MLTextHelper.getI18NMessage(MESSAGE_SURVEY_FORBIDDEN_VALUE, extractName(actualItem.getQuestion()).getDefaultValue());
                }
                return new RequirementCheckResult(RequirementType.Forbidden, message, true);
            }
        }
        
        // Case 2: Tolerated value in CDC -> orange alert when value is selected
        if (specRegulatoryType == RequirementType.Tolerated) {
            // Check if any tolerated value is selected
            boolean hasToleratedChoice = requiredItem.getChoices().stream()
                    .anyMatch(reqChoice -> actualItem.getChoices().stream()
                            .anyMatch(choice -> choice.equals(reqChoice)));
            
            if (hasToleratedChoice) {
                MLText message;
                if (requiredItem.getRegulatoryMessage() != null && !MLTextHelper.isEmpty(requiredItem.getRegulatoryMessage())) {
                    // Use only custom message without question context
                    message = requiredItem.getRegulatoryMessage();
                } else {
                    // Use default message with question context
                    message = MLTextHelper.getI18NMessage(MESSAGE_SURVEY_INCORRECT_ANSWER, extractName(actualItem.getQuestion()).getDefaultValue());
                }
                return new RequirementCheckResult(RequirementType.Tolerated, message, true);
            }
        }
        
        // Case 3: Authorized value in CDC -> red alert with "forbidden" mention when other value is selected
        if (specRegulatoryType == RequirementType.Authorized) {
            // Check if only authorized values are selected
            boolean hasOnlyAuthorizedChoices = actualItem.getChoices().stream()
                    .allMatch(actualChoice -> requiredItem.getChoices().stream()
                            .anyMatch(reqChoice -> reqChoice.equals(actualChoice)));
            
            if (!hasOnlyAuthorizedChoices) {
                MLText message;
                if (requiredItem.getRegulatoryMessage() != null && !MLTextHelper.isEmpty(requiredItem.getRegulatoryMessage())) {
                    // Use only custom message without question context
                    message = requiredItem.getRegulatoryMessage();
                } else {
                    // Use default message with question context
                    message = MLTextHelper.getI18NMessage(MESSAGE_SURVEY_FORBIDDEN_VALUE, extractName(actualItem.getQuestion()).getDefaultValue());
                }
                return new RequirementCheckResult(RequirementType.Forbidden, message, true);
            }
        }
        
        // Case 4: Information value in CDC -> blue alert with "info" mention when value is selected
        if (specRegulatoryType == RequirementType.Info) {
            // Check if any info value is selected
            boolean hasInfoChoice = requiredItem.getChoices().stream()
                    .anyMatch(reqChoice -> actualItem.getChoices().stream()
                            .anyMatch(choice -> choice.equals(reqChoice)));
            
            if (hasInfoChoice) {
                MLText message;
                if (requiredItem.getRegulatoryMessage() != null && !MLTextHelper.isEmpty(requiredItem.getRegulatoryMessage())) {
                    // Use only custom message without question context
                    message = requiredItem.getRegulatoryMessage();
                } else {
                    // Use default message with question context
                    message = MLTextHelper.getI18NMessage(MESSAGE_SURVEY_INFO_VALUE, extractName(actualItem.getQuestion()).getDefaultValue());
                }
                return new RequirementCheckResult(RequirementType.Info, message, true);
            }
        }
        
        // Default case - no requirement needed
        return new RequirementCheckResult(null, null, false);
    }

    /** {@inheritDoc} */
    @Override
    protected void mergeRequirements(List<SurveyListDataItem> ret, List<SurveyListDataItem> toAdd) {
        toAdd.forEach(item -> {
            if (item.getQuestion() != null) {
                boolean isFound = false;
                for (SurveyListDataItem sl : ret) {
                    if (item.getQuestion().equals(sl.getQuestion())) {
                        isFound = true;
                        
                        // Merge choices - keep the most restrictive set
                        // If both have choices, only keep common choices
                        if (CollectionUtils.isNotEmpty(sl.getChoices()) && CollectionUtils.isNotEmpty(item.getChoices())) {
                            sl.getChoices().retainAll(item.getChoices());
                        } else if (CollectionUtils.isNotEmpty(item.getChoices())) {
                            sl.setChoices(new ArrayList<>(item.getChoices()));
                        }
                        
                        break;
                    }
                }
                if (!isFound) {
                    ret.add(item);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected List<SurveyListDataItem> getDataListVisited(ProductData partProduct) {
        return partProduct.getSurveyList() != null ? partProduct.getSurveyList() : new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    protected MLText extractName(NodeRef charactRef) {
        return (MLText) mlNodeService.getProperty(charactRef, SurveyModel.PROP_SURVEY_QUESTION_LABEL);
    }
}
