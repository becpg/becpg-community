package fr.becpg.repo.autocomplete.impl.plugins;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.impl.extractors.NodeRefAutoCompleteExtractor;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>ListValueAutoCompletePlugin class.</p>
 * 
 * becpg/autocomplete/listvalue
 *
 */
@Service
public class ListValueAutoCompletePlugin extends TargetAssocAutoCompletePlugin {


	/** Constant <code>SOURCE_TYPE_LIST_VALUE="listvalue"</code> */
	protected static final String SOURCE_TYPE_LIST_VALUE = "listvalue";
	
	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_LIST_VALUE };
	}
	
	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String path = (String) props.get(AutoCompleteService.PROP_PATH);

		return suggestListValue(path, query, pageNum, pageSize);

	}
	

	/**
	 * Suggest list value according to query
	 *
	 * Query path : +PATH:"/app:company_home/cm:System/cm:Lists/cm:Nuts/*"
	 * +TYPE:"bcpg:nut" +@cm\:name:"Nut1*".
	 *
	 * @param path
	 *            the path
	 * @param query
	 *            the query
	 * @return the map
	 */
	private AutoCompletePage suggestListValue(String path, String query, Integer pageNum, Integer pageSize) {

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();

		queryBuilder.inPath(path);
		queryBuilder.ofType(BeCPGModel.TYPE_LIST_VALUE);

		if (!isAllQuery(query)) {
			queryBuilder.andPropQuery(BeCPGModel.PROP_LV_VALUE, prepareQuery(query));
			queryBuilder.andOperator();
		} else {
			queryBuilder.addSort(BeCPGModel.PROP_LV_VALUE, true);
		}

		List<NodeRef> ret = queryBuilder.ftsLanguage().list();

		return new AutoCompletePage(ret, pageNum, pageSize, new NodeRefAutoCompleteExtractor(BeCPGModel.PROP_LV_VALUE, nodeService));

	}


	

}
