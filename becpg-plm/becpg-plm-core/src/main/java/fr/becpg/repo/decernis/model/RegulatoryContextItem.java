package fr.becpg.repo.decernis.model;

import java.util.HashSet;
import java.util.Set;

import fr.becpg.repo.repository.model.BeCPGDataObject;

public class RegulatoryContextItem {

	private BeCPGDataObject item;
	private Set<String> countries = new HashSet<>();
	private Set<String> usages = new HashSet<>();
	private Integer moduleId;
	private String analysisResult;
	public BeCPGDataObject getItem() {
		return item;
	}
	public void setItem(BeCPGDataObject item) {
		this.item = item;
	}
	public Set<String> getCountries() {
		return countries;
	}
	public void setCountries(Set<String> countries) {
		this.countries = countries;
	}
	public Set<String> getUsages() {
		return usages;
	}
	public void setUsages(Set<String> usages) {
		this.usages = usages;
	}
	public Integer getModuleId() {
		return moduleId;
	}
	public void setModuleId(Integer moduleId) {
		this.moduleId = moduleId;
	}
	public String getAnalysisResult() {
		return analysisResult;
	}
	public void setAnalysisResult(String analysisResult) {
		this.analysisResult = analysisResult;
	}
	
	public boolean isTreatable() {
		return !countries.isEmpty() && !usages.isEmpty();
	}

}