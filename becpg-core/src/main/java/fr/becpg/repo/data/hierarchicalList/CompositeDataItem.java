package fr.becpg.repo.data.hierarchicalList;

public interface CompositeDataItem<T> {

	Integer getDepthLevel();
     
	T getParent();
	
	void setParent(T parent);
	
}
