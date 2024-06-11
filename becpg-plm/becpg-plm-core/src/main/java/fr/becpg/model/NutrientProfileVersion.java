package fr.becpg.model;

/**
 * <p>NutrientProfileVersion class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public enum NutrientProfileVersion {
	
	VERSION_2017("2017"),
	VERSION_2023("2023");
	
	private String version;

	NutrientProfileVersion(String version) {
		this.version = version;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return version;
	}
}
