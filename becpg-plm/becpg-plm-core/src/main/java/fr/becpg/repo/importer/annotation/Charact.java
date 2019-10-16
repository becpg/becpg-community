package fr.becpg.repo.importer.annotation;

public class Charact extends Annotation {

	private String dataListQName;
	private String charactQName;
	private String charactKeyValue;
	private String charactName;
	private String charactKeyQName;
	private String charactNodeRef;
	private String dataListAttribute;

	public Charact() {
		super();
	}

	public String getDataListQName() {
		return dataListQName;
	}

	public void setDataListQName(String dataListQName) {
		this.dataListQName = dataListQName;
	}

	public String getCharactQName() {
		return charactQName;
	}

	public void setCharactQName(String charactQName) {
		this.charactQName = charactQName;
	}

	public String getCharactKeyQName() {
		return charactKeyQName;
	}

	public void setCharactKeyQName(String charactKeyQName) {
		this.charactKeyQName = charactKeyQName;
	}

	public String getCharactNodeRef() {
		return charactNodeRef;
	}

	public void setCharactNodeRef(String charactNodeRef) {
		this.charactNodeRef = charactNodeRef;
	}

	public String getDataListAttribute() {
		return dataListAttribute;
	}

	public void setDataListAttribute(String dataListAttribute) {
		this.dataListAttribute = dataListAttribute;
	}

	public String getCharactKeyValue() {
		return charactKeyValue != null ? charactKeyValue : getCharactName();
	}

	public void setCharactKeyValue(String charactKeyValue) {
		this.charactKeyValue = charactKeyValue;
	}

	public String getCharactName() {
		return charactName != null ? charactName : getId();
	}

	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}


	@Override
	public String toString() {
		return "Charact [dataListQName=" + dataListQName + ", charactQName=" + charactQName + ", charactKeyValue="
				+ charactKeyValue + ", charactName=" + charactName + ", charactKeyQName=" + charactKeyQName
				+ ", charactNodeRef=" + charactNodeRef + ", dataListAttribute=" + dataListAttribute + ", id=" + id
				+ ", attribute=" + attribute + ", targetClass=" + targetClass + ", targetKey=" + targetKey + "]";
	}

	
	

	
	 

	
	

}
