package fr.becpg.repo.data.hierarchicalList;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a hierarchical data list
 * @author quere
 *
 * @param <T>
 */
public class Composite<T> extends AbstractComponent<T> {

	private List<AbstractComponent<T>> children = new ArrayList<AbstractComponent<T>>();
	
	public List<AbstractComponent<T>> getChildren() {
		return children;
	}

	public void setChildren(List<AbstractComponent<T>> children) {
		this.children = children;
	}

	public void addChild(AbstractComponent<T> component){
		children.add(component);
	}
	
	public void removeChild(AbstractComponent<T> component){
		children.remove(component);
	}
	
	public Composite(){
		
	}
	
	public Composite(T data){
		super(data);
	}	
}
