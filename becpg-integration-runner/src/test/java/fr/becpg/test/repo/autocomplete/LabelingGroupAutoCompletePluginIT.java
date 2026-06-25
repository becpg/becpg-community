package fr.becpg.test.repo.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.LabelingGroupAutoCompletePlugin;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;

public class LabelingGroupAutoCompletePluginIT extends AbstractAutoCompletePluginTest
{
    @Autowired
    private LabelingGroupAutoCompletePlugin labelingGroupAutoCompletePlugin;

    @Test
    public void testLabelingGroupPlugin()
    {
        NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() ->
        {
            authenticationComponent.setSystemUserAsCurrentUser();

            FinishedProductData templateProduct = new FinishedProductData();
            templateProduct.setName("FP-Template");
            NodeRef templateRef = alfrescoRepository.create(getTestFolderNodeRef(), templateProduct).getNodeRef();
            nodeService.addAspect(templateRef, fr.becpg.model.BeCPGModel.ASPECT_ENTITY_TPL, null);

            FinishedProductData savedTemplate = (FinishedProductData) alfrescoRepository.findOne(templateRef);
            List<LabelingRuleListDataItem> tplRules = new ArrayList<>();
            LabelingRuleListDataItem ruleTpl = new LabelingRuleListDataItem();
            ruleTpl.setName("SecondRuleTpl");
            
            MLText titleTpl = new MLText();
            titleTpl.addValue(java.util.Locale.ENGLISH, "SecondRuleTpl");
            titleTpl.addValue(java.util.Locale.FRENCH, "SecondRuleTpl");
            titleTpl.addValue(java.util.Locale.getDefault(), "SecondRuleTpl");
            if (I18NUtil.getLocale() != null)
            {
                titleTpl.addValue(I18NUtil.getLocale(), "SecondRuleTpl");
            }
            ruleTpl.setMlTitle(titleTpl);
            tplRules.add(ruleTpl);
            
            if (savedTemplate.getLabelingListView().getLabelingRuleList() == null)
            {
                savedTemplate.getLabelingListView().setLabelingRuleList(new ArrayList<>());
            }
            savedTemplate.getLabelingListView().getLabelingRuleList().clear();
            savedTemplate.getLabelingListView().getLabelingRuleList().addAll(tplRules);
            alfrescoRepository.save(savedTemplate);

            FinishedProductData reloadedTemplate = (FinishedProductData) alfrescoRepository.findOne(templateRef);

            FinishedProductData finishedProduct = new FinishedProductData();
            finishedProduct.setName("FP-Test");
            NodeRef pRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
            nodeService.addAspect(pRef, fr.becpg.model.BeCPGModel.ASPECT_ENTITY_TPL_REF, null);

            FinishedProductData createdProduct = (FinishedProductData) alfrescoRepository.findOne(pRef);
            createdProduct.setEntityTpl(reloadedTemplate);
            List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();
            LabelingRuleListDataItem rule1 = new LabelingRuleListDataItem();
            rule1.setName("FirstRule");
            
            MLText title1 = new MLText();
            title1.addValue(java.util.Locale.ENGLISH, "FirstRule");
            title1.addValue(java.util.Locale.FRENCH, "FirstRule");
            title1.addValue(java.util.Locale.getDefault(), "FirstRule");
            if (I18NUtil.getLocale() != null)
            {
                title1.addValue(I18NUtil.getLocale(), "FirstRule");
            }
            rule1.setMlTitle(title1);
            labelingRuleList.add(rule1);
            
            if (createdProduct.getLabelingListView().getLabelingRuleList() == null)
            {
                createdProduct.getLabelingListView().setLabelingRuleList(new ArrayList<>());
            }
            createdProduct.getLabelingListView().getLabelingRuleList().clear();
            createdProduct.getLabelingListView().getLabelingRuleList().addAll(labelingRuleList);
            alfrescoRepository.save(createdProduct);

            FinishedProductData reloadedProduct = (FinishedProductData) alfrescoRepository.findOne(pRef);
            NodeRef ruleNodeRef = reloadedProduct.getLabelingListView().getLabelingRuleList().get(0).getNodeRef();

            List<IngLabelingListDataItem> ingLabelingList = new ArrayList<>();
            IngLabelingListDataItem line1 = new IngLabelingListDataItem();
            line1.setGrp(ruleNodeRef);
            ingLabelingList.add(line1);
            
            if (reloadedProduct.getLabelingListView().getIngLabelingList() == null)
            {
                reloadedProduct.getLabelingListView().setIngLabelingList(new ArrayList<>());
            }
            reloadedProduct.getLabelingListView().getIngLabelingList().clear();
            reloadedProduct.getLabelingListView().getIngLabelingList().addAll(ingLabelingList);
            alfrescoRepository.save(reloadedProduct);

            return pRef;
        }, false, true);

        transactionService.getRetryingTransactionHelper().doInTransaction(() ->
        {
            Map<String, Serializable> props = new HashMap<>();
            props.put(AutoCompleteService.PROP_NODEREF, finishedProductNodeRef.toString());

            List<AutoCompleteEntry> suggestions = labelingGroupAutoCompletePlugin.suggest("labelingGroup", "*", 1, 10, props).getResults();
            boolean templateRulePresent = false;
            for (AutoCompleteEntry entry : suggestions)
            {
                if ("SecondRuleTpl".equals(entry.getName()))
                {
                    templateRulePresent = true;
                    break;
                }
            }
            assertTrue(templateRulePresent);

            return null;
        }, false, true);
    }
}