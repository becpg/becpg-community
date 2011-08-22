package fr.becpg.repo.data.hierarchicalList;

public abstract class AbstractComponent<T> {

	private T data;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
	public AbstractComponent(){
		
	}
	
	public AbstractComponent(T data){
		
		this.data = data;
	}
}
