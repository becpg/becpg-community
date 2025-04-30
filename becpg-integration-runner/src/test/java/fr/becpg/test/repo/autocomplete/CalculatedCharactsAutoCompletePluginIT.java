/*
 *
 */
package fr.becpg.test.repo.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.ecm.autocomplete.CalculatedCharactsAutoCompletePlugin;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;

/**
 * Integration test for the CalculatedCharactsAutoCompletePlugin.
 *
 * @author matthieu
 */
public class CalculatedCharactsAutoCompletePluginIT extends AbstractAutoCompletePluginTest {

    @Autowired
    private CalculatedCharactsAutoCompletePlugin calculatedCharactsAutoCompletePlugin;
    


    private static final Log logger = LogFactory.getLog(CalculatedCharactsAutoCompletePluginIT.class);

    /**
     * Test the autocomplete functionality for calculated characteristics.
     */
    @Test
    public void testCalculatedCharactsAutoCompletePlugin() {
        final NodeRef templateNodeRef = createTemplateWithCharacteristics();

        waitForSolr();

        inWriteTx(() -> {
            Map<String, Serializable> props = new HashMap<>();
            props.put(AutoCompleteService.PROP_LOCALE, Locale.FRENCH);
             
            logger.info("Using template node: " + templateNodeRef);
        
            // Test empty query to get all dynamic characteristics
            AutoCompletePage autoCompletePage = calculatedCharactsAutoCompletePlugin.suggest("eco", "", null, 
                    AutoCompleteService.SUGGEST_PAGE_SIZE, props);

            logger.info("Found " + autoCompletePage.getResults().size() + " results");
            for (AutoCompleteEntry entry : autoCompletePage.getResults()) {
                logger.info("AutoCompleteEntry: " + entry.getName() + " - " + entry.getValue());
            }
            
            // Should find all our dynamic characteristics (at least 10)
            assertTrue("Should find at least 3 characteristics", autoCompletePage.getResults().size() >= 3);
            
            // Test with specific query to find calculated characteristics
            autoCompletePage = calculatedCharactsAutoCompletePlugin.suggest("eco", "Calculated", null, 
                    AutoCompleteService.SUGGEST_PAGE_SIZE, props);

            logger.info("Found " + autoCompletePage.getResults().size() + " results for 'Calculated' query");
            for (AutoCompleteEntry entry : autoCompletePage.getResults()) {
                logger.info("AutoCompleteEntry: " + entry.getName() + " - " + entry.getValue());
            }
            
            // Should find our calculated characteristics (at least 4)
            assertTrue("Should find at least 3 calculated characteristics", autoCompletePage.getResults().size() >= 3);
            
            // Test with render labeling rule query
            autoCompletePage = calculatedCharactsAutoCompletePlugin.suggest("eco", "LBL 1", null, 
                    AutoCompleteService.SUGGEST_PAGE_SIZE, props);

            logger.info("Found " + autoCompletePage.getResults().size() + " results for 'Test Render' query");
            for (AutoCompleteEntry entry : autoCompletePage.getResults()) {
                logger.info("AutoCompleteEntry: " + entry.getName() + " - " + entry.getValue());
            }
           
            assertTrue("Should find at least 1 render labeling rules",  autoCompletePage.getResults().size() >= 1);
            
            boolean foundLBL1 = false;
            for (AutoCompleteEntry entry : autoCompletePage.getResults()) {
                if (entry.getName().equals("CalculatedCharactsAutoCompletePluginIT - LBL 1")) {
                    foundLBL1 = true;
                    break;
                }
            }
            assertTrue("At least one entry should be of type LBL 1", foundLBL1);
            
            // Verify all found entries are of type Render
            boolean foundLBL2 = false;
            for (AutoCompleteEntry entry : autoCompletePage.getResults()) {
                if (entry.getName().equals("CalculatedCharactsAutoCompletePluginIT - LBL 2")) {
                    foundLBL2 = true;
                    logger.warn("Found LBL 2 entry: " + entry.getName());
                    break;
                }
            }
            assertFalse("No entry should be of type LBL 2", foundLBL2);

            return null;
        });
    }

 
    /**
     * Creates a template product with dynamic characteristics and labeling rules for testing.
     * 
     * @return NodeRef of the created template
     */
    private NodeRef createTemplateWithCharacteristics() {
        return inWriteTx(() -> {
            logger.info("Creating template product with characteristics");
            
            // Create a template product
            FinishedProductData templateProduct = new FinishedProductData();
            templateProduct.setName("Test Template Product");
            templateProduct.setLegalName("Test Template Product Legal Name");
            templateProduct.setUnit(ProductUnit.kg);
            templateProduct.setQty(1.0d);
            templateProduct.setDensity(1.0d);
            
            // Add dynamic characteristics
            List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<>();
            // Product properties
            dynamicCharactListItems.add(DynamicCharactListItem.build()
                .withTitle("CalculatedCharactsAutoCompletePluginIT - DYN 1")
                .withFormula("qty"));
                
            dynamicCharactListItems.add(DynamicCharactListItem.build()
                .withTitle("CalculatedCharactsAutoCompletePluginIT- DYN 2")
                .withFormula("name"));
            
        
            // Dynamic column
            dynamicCharactListItems.add(DynamicCharactListItem.build()
                .withTitle("CalculatedCharactsAutoCompletePluginIT- DYN 3")
                .withColumnName("bcpg_dynamicCharactColumn1"));
            
            templateProduct.getCompoListView().setDynamicCharactList(dynamicCharactListItems);
            
            // Add labeling rules
            List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();
            
            labelingRuleList.add(LabelingRuleListDataItem.build()
                    .withName("CalculatedCharactsAutoCompletePluginIT - LBL 1")
                    .withFormula("render()")
                    .withLabelingRuleType(LabelingRuleType.Render));
                    
            labelingRuleList.add(LabelingRuleListDataItem.build()
                    .withName("CalculatedCharactsAutoCompletePluginIT - LBL 2")
                    .withFormula("{0} {1,number,0.#%}")
                    .withLabelingRuleType(LabelingRuleType.Format));
                    
            labelingRuleList.add(LabelingRuleListDataItem.build()
                    .withName("CalculatedCharactsAutoCompletePluginIT - LBL 3")
                    .withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
                    .withLabelingRuleType(LabelingRuleType.Prefs));
            
            templateProduct.getLabelingListView().setLabelingRuleList(labelingRuleList);
            
            // Create the product and add template aspect
            NodeRef templateNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), templateProduct).getNodeRef();
            nodeService.addAspect(templateNodeRef, BeCPGModel.ASPECT_ENTITY_TPL, null);
            
            logger.info("Template product created successfully: " + templateNodeRef);
            return templateNodeRef;
        });
    }
    

    

}
