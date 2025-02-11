package fr.becpg.repo.entity.comparison;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

@Service
public class DoNothingCompareEntityServicePlugin implements CompareEntityServicePlugin {

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public void compareEntities(NodeRef entity1NodeRef, NodeRef entity2NodeRef, int nbEntities, int comparisonPosition,
			Map<String, CompareResultDataItem> comparisonMap, Map<String, List<StructCompareResultDataItem>> structCompareResults) {
		//Do Nothing
	}

	@Override
	public boolean isComparableProperty(QName qName, boolean isDataList) {
		return false;
	}

	@Override
	public void compareStructDatalist(NodeRef entity1NodeRef, NodeRef entity2NodeRef, QName datalistType,
			Map<String, List<StructCompareResultDataItem>> structCompareResults) {
		//Do Nothing
	}

	@Override
	public boolean applyTo(QName entityType) {
		return false;
	}

}
