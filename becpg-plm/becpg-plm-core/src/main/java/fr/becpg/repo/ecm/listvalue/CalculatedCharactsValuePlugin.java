package fr.becpg.repo.ecm.listvalue;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("calculatedCharactsValuePlugin")
public class CalculatedCharactsValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_ECO = "eco";

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_ECO };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		List<NodeRef> ret = new LinkedList<>();
		
		
		List<NodeRef> tplsNodeRef = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_PRODUCT).withAspect(BeCPGModel.ASPECT_ENTITY_TPL).inDB().list();
		
				for(NodeRef tplNodeRef : tplsNodeRef){
			
					ret.addAll(BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_DYNAMICCHARACTLIST).andPropQuery(PLMModel.PROP_DYNAMICCHARACT_TITLE, prepareQuery(query))
					    .isNull(PLMModel.PROP_DYNAMICCHARACT_COLUMN)
					    .inPath(nodeService.getPath(tplNodeRef).toPrefixString(namespaceService) + "/*/*")
					    .ftsLanguage()
						.maxResults(RepoConsts.MAX_SUGGESTIONS).list());
					
					ret.addAll(BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_LABELINGRULELIST).andPropQuery(PLMModel.PROP_LABELINGRULELIST_LABEL, prepareQuery(query))
						    .andPropEquals(PLMModel.PROP_LABELINGRULELIST_TYPE, LabelingRuleType.Render.toString())
						    .inPath(nodeService.getPath(tplNodeRef).toPrefixString(namespaceService) + "/*/*")
						    .ftsLanguage()
							.maxResults(RepoConsts.MAX_SUGGESTIONS).list());
		
				}
				
				ret.addAll(BeCPGQueryBuilder.createQuery().inType(PLMModel.TYPE_NUT).inType(PLMModel.TYPE_COST).inType(PLMModel.TYPE_PHYSICO_CHEM)
						.inType(PLMModel.TYPE_LABEL_CLAIM).andPropQuery(BeCPGModel.PROP_CHARACT_NAME, prepareQuery(query)).ftsLanguage().list());
				

		
		
		return new ListValuePage(ret, pageNum, pageSize, getTargetAssocValueExtractor() );
		
		
	}


}