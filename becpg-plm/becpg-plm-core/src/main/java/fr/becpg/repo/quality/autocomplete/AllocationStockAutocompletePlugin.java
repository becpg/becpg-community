package fr.becpg.repo.quality.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompletePlugin;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.helper.BeCPGQueryHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.quality.data.dataList.AllocationListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.quality.formulation.BatchFormulationHandler;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

@Service
public class AllocationStockAutocompletePlugin implements AutoCompletePlugin {

	@Autowired
	private BatchFormulationHandler batchFormulationHandler;
	
	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { "stocks" };
	}
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

	    NodeRef entityNodeRef = new NodeRef((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF));

	    NodeRef itemId = null;
	    @SuppressWarnings("unchecked")
	    Map<String, String> extras = (HashMap<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);
	    if (extras != null && extras.get("itemId") != null) {
	        itemId = new NodeRef(extras.get("itemId"));
	    }

	    // Fetch the necessary data
	    AllocationListDataItem allocationListDataItem = (AllocationListDataItem) alfrescoRepository.findOne(itemId);
	    BatchData batchData = (BatchData) alfrescoRepository.findOne(entityNodeRef);
	    ProductData productData = (ProductData) alfrescoRepository.findOne(allocationListDataItem.getProduct());

	    // Extract the filtered stock list
	    List<StockListDataItem> items = batchFormulationHandler.extractFilteredStockList(batchData, productData, new ArrayList<>());

	    // Filter by query and batchId
	    List<StockListDataItem> filteredItems = items.stream()
	        .filter(item -> matchesQuery(item, query) )
	        .toList();

	    // Return the filtered items wrapped in AutoCompletePage
	    return new AutoCompletePage(filteredItems, pageNum, pageSize, list -> {

	        List<AutoCompleteEntry> suggestions = new ArrayList<>();
	        if (list != null) {
	            for (StockListDataItem item : list) {
	                suggestions.add(new AutoCompleteEntry(item.getNodeRef().toString(), item.getBatchId(), QualityModel.TYPE_STOCK_LIST.getLocalName()));
	            }
	        }
	        return suggestions;
	    });
	}

	private boolean matchesQuery(StockListDataItem item, String query) {
	    return  BeCPGQueryHelper.isAllQuery(query) ||  BeCPGQueryHelper.isQueryMatch(query,  item.getBatchId());
	}

	
	
}
