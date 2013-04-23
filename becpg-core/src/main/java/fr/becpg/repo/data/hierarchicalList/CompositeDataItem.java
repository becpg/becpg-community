package fr.becpg.repo.data.hierarchicalList;

public interface CompositeDataItem {

	Integer getDepthLevel();
     
	CompositeDataItem getParent();
	
}
