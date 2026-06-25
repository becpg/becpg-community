package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.extensions.surf.util.I18NUtil;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

@Service("labelingGroupAutoCompletePlugin")
public class LabelingGroupAutoCompletePlugin extends TargetAssocAutoCompletePlugin {
    private static final String SOURCE_TYPE_LABELING_GROUP = "labelingGroup";

    @Autowired
    private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

    @Override
    public String[] getHandleSourceTypes() {
        return new String[] { SOURCE_TYPE_LABELING_GROUP };
    }

    @Override
    public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
        if (props != null) {
            NodeRef entityNodeRef = extractEntityNodeRef(props);
            if (entityNodeRef != null) {
                ProductData productData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
                if (productData != null && productData.getLabelingListView() != null) {
                    List<NodeRef> uniqueRuleNodeRefs = new ArrayList<>();
                    if (productData.getLabelingListView().getIngLabelingList() != null) {
                        for (IngLabelingListDataItem illItem : productData.getLabelingListView().getIngLabelingList()) {
                            NodeRef grp = illItem.getGrp();
                            if (grp != null && !uniqueRuleNodeRefs.contains(grp)) {
                                uniqueRuleNodeRefs.add(grp);
                            }
                        }
                    }
                    if (productData.getEntityTpl() != null && !productData.getEntityTpl().equals(productData)
                            && productData.getEntityTpl().getLabelingListView() != null
                            && productData.getEntityTpl().getLabelingListView().getLabelingRuleList() != null) {
                        for (LabelingRuleListDataItem rule : productData.getEntityTpl().getLabelingListView().getLabelingRuleList()) {
                            if (rule.getNodeRef() != null && !uniqueRuleNodeRefs.contains(rule.getNodeRef())) {
                                uniqueRuleNodeRefs.add(rule.getNodeRef());
                            }
                        }
                    }
                    if (productData.getProductSpecifications() != null) {
                        for (ProductSpecificationData spec : productData.getProductSpecifications()) {
                            if (spec.getLabelingRuleList() != null) {
                                for (LabelingRuleListDataItem rule : spec.getLabelingRuleList()) {
                                    if (rule.getNodeRef() != null && !uniqueRuleNodeRefs.contains(rule.getNodeRef())) {
                                        uniqueRuleNodeRefs.add(rule.getNodeRef());
                                    }
                                }
                            }
                        }
                    }
                    List<NodeRef> filteredNodeRefs = new ArrayList<>();
                    for (NodeRef ruleRef : uniqueRuleNodeRefs) {
                        String ruleName = (String) nodeService.getProperty(ruleRef, ContentModel.PROP_NAME);
                        Serializable titleProp = nodeService.getProperty(ruleRef, ContentModel.PROP_TITLE);
                        String ruleTitle = null;
                        if (titleProp instanceof MLText mlText) {
                            ruleTitle = mlText.get(I18NUtil.getLocale());
                            if (ruleTitle == null) {
                                ruleTitle = mlText.getDefaultValue();
                            }
                            if (ruleTitle == null && !mlText.isEmpty()) {
                                ruleTitle = mlText.values().iterator().next();
                            }
                        } else if (titleProp instanceof String stringProp) {
                            ruleTitle = stringProp;
                        }

                        boolean isMatch = isAllQuery(query) 
                                || (ruleName != null && isQueryMatch(query, ruleName)) 
                                || (ruleTitle != null && isQueryMatch(query, ruleTitle));

                        if (isMatch) {
                            filteredNodeRefs.add(ruleRef);
                        }
                    }
                    return new AutoCompletePage(filteredNodeRefs, pageNum, pageSize, getTargetAssocValueExtractor());
                }
            }
        }
        return new AutoCompletePage(new ArrayList<>(), pageNum, pageSize, getTargetAssocValueExtractor());
    }
}