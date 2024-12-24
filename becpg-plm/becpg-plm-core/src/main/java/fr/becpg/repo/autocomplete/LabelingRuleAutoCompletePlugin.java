package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

@Service("labelingRuleAutoCompletePlugin")
public class LabelingRuleAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

    private static final String SOURCE_TYPE_LABELING = "labeling";

    private static final Log logger = LogFactory.getLog(LabelingRuleAutoCompletePlugin.class);

    @Autowired
    private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

    @Override
    public String[] getHandleSourceTypes() {
        return new String[] { SOURCE_TYPE_LABELING };
    }

    @Override
    public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
        logger.info("Executing suggest method with sourceType: " + sourceType + ", query: " + query);

        if (props != null) {

            List<NodeRef> ret = new ArrayList<>();
            String propParent = (String) props.get(AutoCompleteService.PROP_PARENT);
            NodeRef entityNodeRef = extractEntityNodeRef(props);

            if (propParent != null && !propParent.isEmpty() && entityNodeRef != null) {
                String[] splitted = propParent.split(",");
                List<NodeRef> components = new ArrayList<>();

                for (String node : splitted) {
                    if (NodeRef.isNodeRef(node)) {
                        components.add(new NodeRef(node));
                    }
                }

                for (NodeRef component : components) {
                    logger.debug("Processing component: " + component);
                    List<NodeRef> alias = new ArrayList<>();

                    if (entityDictionaryService.isSubClass(nodeService.getType(component), PLMModel.TYPE_ING)) {
                        alias.addAll(findAliasInComposition(component, entityNodeRef));
                    }

                    if (alias.isEmpty() && nodeService.hasAspect(component, BeCPGModel.ASPECT_LINKED_SEARCH)) {
                        alias.addAll(associationService.getTargetAssocs(component, BeCPGModel.ASSOC_LINKED_SEARCH_ASSOCIATION));
                    }

                    ret.addAll(alias);
                }
            }

            if (!ret.isEmpty()) {
                logger.info("Suggestions found: " + ret.size());
                return new AutoCompletePage(ret, pageNum, pageSize, getTargetAssocValueExtractor());
            }
        }

        logger.info("No suggestions found, delegating to superclass.");
        return super.suggest(sourceType, query, pageNum, pageSize, props);
    }

    private List<NodeRef> findAliasInComposition(NodeRef component, NodeRef entityNodeRef) {
        logger.debug("Finding alias in composition for component: " + component + ", entityNodeRef: " + entityNodeRef);
        ProductData productData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
        List<IngListDataItem> ingList = new ArrayList<>();
        extractIngList(productData, ingList);
        List<NodeRef> alias = new ArrayList<>();

        for (IngListDataItem ingListDataItem : ingList) {
            if (ingListDataItem.getIng() != null && ingListDataItem.getIng().equals(component)) {
                logger.debug("Match found for ingredient: " + ingListDataItem.getIng());

                if (ingListDataItem.getNodeRef()!=null && nodeService.hasAspect(ingListDataItem.getNodeRef(), BeCPGModel.ASPECT_LINKED_SEARCH)) {
                    alias.addAll(associationService.getTargetAssocs(ingListDataItem.getNodeRef(), BeCPGModel.ASSOC_LINKED_SEARCH_ASSOCIATION));
                }
            }
        }

        logger.debug("Aliases found: " + alias.size());
        return alias;
    }

    private void extractIngList(ProductData productData, List<IngListDataItem> ingList) {
        logger.debug("Extracting ingredient list from product data.");

        for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
            NodeRef productNodeRef = compoList.getProduct();
            if (productNodeRef != null && !DeclarationType.Omit.equals(compoList.getDeclType())) {
                ProductData subProductData = (ProductData) alfrescoRepository.findOne(productNodeRef);

                if (subProductData.isRawMaterial() || subProductData.isGeneric() || !subProductData.hasCompoListEl()) {
                    ingList.addAll(subProductData.getIngList());
                } else if (!(subProductData instanceof LocalSemiFinishedProductData)) {
                    extractIngList(subProductData, ingList);
                }
            }
        }

        logger.debug("Ingredient list size: " + ingList.size());
    }
}
