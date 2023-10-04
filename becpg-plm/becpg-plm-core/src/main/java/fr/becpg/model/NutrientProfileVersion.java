package fr.becpg.model;

public enum NutrientProfileVersion {
	
	VERSION_2017("2017"),
	VERSION_2023("2023");
	
	private String version;

	NutrientProfileVersion(String version) {
		this.version = version;
	}
	
	@Override
	public String toString() {
		return version;
	}
}
