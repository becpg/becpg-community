package fr.becpg.repo.importer.annotation;

/**
 * <p>Charact class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class Charact extends Annotation {

	private String dataListQName;
	private String charactQName;
	private String charactKeyValue;
	private String charactName;
	private String charactKeyQName;
	private String charactNodeRef;
	private String dataListAttribute;

	/**
	 * <p>Constructor for Charact.</p>
	 */
	public Charact() {
		super();
	}

	/**
	 * <p>Getter for the field <code>dataListQName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDataListQName() {
		return dataListQName;
	}

	/**
	 * <p>Setter for the field <code>dataListQName</code>.</p>
	 *
	 * @param dataListQName a {@link java.lang.String} object.
	 */
	public void setDataListQName(String dataListQName) {
		this.dataListQName = dataListQName;
	}

	/**
	 * <p>Getter for the field <code>charactQName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCharactQName() {
		return charactQName;
	}

	/**
	 * <p>Setter for the field <code>charactQName</code>.</p>
	 *
	 * @param charactQName a {@link java.lang.String} object.
	 */
	public void setCharactQName(String charactQName) {
		this.charactQName = charactQName;
	}

	/**
	 * <p>Getter for the field <code>charactKeyQName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCharactKeyQName() {
		return charactKeyQName;
	}

	/**
	 * <p>Setter for the field <code>charactKeyQName</code>.</p>
	 *
	 * @param charactKeyQName a {@link java.lang.String} object.
	 */
	public void setCharactKeyQName(String charactKeyQName) {
		this.charactKeyQName = charactKeyQName;
	}

	/**
	 * <p>Getter for the field <code>charactNodeRef</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCharactNodeRef() {
		return charactNodeRef;
	}

	/**
	 * <p>Setter for the field <code>charactNodeRef</code>.</p>
	 *
	 * @param charactNodeRef a {@link java.lang.String} object.
	 */
	public void setCharactNodeRef(String charactNodeRef) {
		this.charactNodeRef = charactNodeRef;
	}

	/**
	 * <p>Getter for the field <code>dataListAttribute</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDataListAttribute() {
		return dataListAttribute;
	}

	/**
	 * <p>Setter for the field <code>dataListAttribute</code>.</p>
	 *
	 * @param dataListAttribute a {@link java.lang.String} object.
	 */
	public void setDataListAttribute(String dataListAttribute) {
		this.dataListAttribute = dataListAttribute;
	}

	/**
	 * <p>Getter for the field <code>charactKeyValue</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCharactKeyValue() {
		return charactKeyValue != null ? charactKeyValue : getCharactName();
	}

	/**
	 * <p>Setter for the field <code>charactKeyValue</code>.</p>
	 *
	 * @param charactKeyValue a {@link java.lang.String} object.
	 */
	public void setCharactKeyValue(String charactKeyValue) {
		this.charactKeyValue = charactKeyValue;
	}

	/**
	 * <p>Getter for the field <code>charactName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCharactName() {
		return charactName != null ? charactName : getId();
	}

	/**
	 * <p>Setter for the field <code>charactName</code>.</p>
	 *
	 * @param charactName a {@link java.lang.String} object.
	 */
	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Charact [dataListQName=" + dataListQName + ", charactQName=" + charactQName + ", charactKeyValue="
				+ charactKeyValue + ", charactName=" + charactName + ", charactKeyQName=" + charactKeyQName
				+ ", charactNodeRef=" + charactNodeRef + ", dataListAttribute=" + dataListAttribute + ", id=" + id
				+ ", attribute=" + attribute + ", targetClass=" + targetClass + ", targetKey=" + targetKey + "]";
	}

	
	

	
	 

	
	

}
