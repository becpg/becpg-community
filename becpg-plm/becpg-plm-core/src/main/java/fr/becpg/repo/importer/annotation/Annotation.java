package fr.becpg.repo.importer.annotation;

public class Annotation {

	protected String id;
	protected String attribute;
	protected String targetClass;
	protected String targetKey;
	protected String type;
	protected String key;
	

	public Annotation() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	public String getType() {
		return type != null ? type : getTargetClass();
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getTargetKey() {
		return targetKey != null ? targetKey : getAttribute();
	}

	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}
	
	public String getKey() {
		return key != null ? key : getTargetKey();
	}

	public void setKey(String key) {
		this.key = key;
	}

	


	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [id=" + id + ", attribute=" + attribute + ", targetClass="
				+ targetClass + ", targetKey=" + targetKey + "]";
	}

}
