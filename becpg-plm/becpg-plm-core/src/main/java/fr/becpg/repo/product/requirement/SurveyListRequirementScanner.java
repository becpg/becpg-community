package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.collections4.CollectionUtils;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
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

    /** Constant <code>MESSAGE_SURVEY_INCORRECT_ANSWER="message.formulate.survey.incorrect.answer"</code> */
    public static final String MESSAGE_SURVEY_INCORRECT_ANSWER = "message.formulate.survey.incorrect.answer";
    
    /** Constant <code>MESSAGE_SURVEY_MISSING_CHOICE="message.formulate.survey.missing.choice"</code> */
    public static final String MESSAGE_SURVEY_MISSING_CHOICE = "message.formulate.survey.missing.choice";
    
    /** {@inheritDoc} */
    @Override
    public List<RequirementListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
        List<RequirementListDataItem> ret = new LinkedList<>();

        if (formulatedProduct.getSurveyList() != null && !formulatedProduct.getSurveyList().isEmpty()) {
            
            for (Map.Entry<ProductSpecificationData, List<SurveyListDataItem>> entry : extractRequirements(specifications).entrySet()) {
                List<SurveyListDataItem> requirements = entry.getValue();
                ProductSpecificationData specification = entry.getKey();

                requirements.forEach(specDataItem -> {
                    formulatedProduct.getSurveyList().forEach(listDataItem -> {
                        // Check if the questions match
                        if (listDataItem.getQuestion() != null && specDataItem.getQuestion() != null 
                                && listDataItem.getQuestion().equals(specDataItem.getQuestion())) {
                            
                            boolean isAnswerCorrect = checkCorrectAnswer(listDataItem, specDataItem);
                            
                            if (!isAnswerCorrect || Boolean.TRUE.equals(addInfoReqCtrl)) {
                                MLText message;
                                
                                // Determine if choice is missing or incorrect
                                if (CollectionUtils.isEmpty(listDataItem.getChoices())) {
                                    message = MLTextHelper.getI18NMessage(MESSAGE_SURVEY_MISSING_CHOICE, 
                                            extractName(listDataItem.getQuestion()));
                                } else {
                                    message = MLTextHelper.getI18NMessage(MESSAGE_SURVEY_INCORRECT_ANSWER, 
                                            extractName(listDataItem.getQuestion()));
                                }
                                
                                RequirementType reqType = isAnswerCorrect ? RequirementType.Info : RequirementType.Forbidden;
                                
                                // Check if specDataItem can be cast to RegulatoryEntityItem
                                if (!isAnswerCorrect ) {
                                    if (specDataItem.getRegulatoryType() != null) {
                                        reqType = specDataItem.getRegulatoryType();
                                    }
                                    if (specDataItem.getRegulatoryMessage() != null) {
                                        message = specDataItem.getRegulatoryMessage();
                                    }
                                }
                                
                                RequirementListDataItem rclDataItem = RequirementListDataItem.build()
                                        .ofType(reqType).withMessage(message)
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
     * Check if the survey item's choices match the required choices from the specification
     * 
     * @param actualItem The survey item from the formulatedProduct
     * @param requiredItem The survey item from the specification
     * @return true if the answer is correct, false otherwise
     */
    private boolean checkCorrectAnswer(SurveyListDataItem actualItem, SurveyListDataItem requiredItem) {
        // If spec has no choices defined, any choice is valid
        if (CollectionUtils.isEmpty(requiredItem.getChoices())) {
            return true;
        }
        
        // If no choices selected but required, it's invalid
        if (CollectionUtils.isEmpty(actualItem.getChoices())) {
            return false;
        }
        
        // Check if all required choices are included in the actual choices
        return requiredItem.getChoices().stream()
                .allMatch(reqChoice -> actualItem.getChoices().stream()
                        .anyMatch(choice -> choice.equals(reqChoice)));
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
}
