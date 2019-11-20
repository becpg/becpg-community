package fr.becpg.repo.importer.annotation;

public class Assoc extends Annotation {
	private String path;
	
	public Assoc() {
		super();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "Assoc [path=" + path + ", id=" + id + ", attribute=" + attribute + ", targetClass=" + targetClass
				+ ", targetKey=" + targetKey + ", type=" + type + ", key=" + key + "]";
	}

	

	
}
