package fr.becpg.repo.decernis.model;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.RegulatoryEntity;

public class RegulatoryContextItem {

	private RegulatoryEntity item;
	private Map<String, NodeRef> countries = new HashMap<>();
	private Map<String, NodeRef> usages = new HashMap<>();
	private Integer moduleId;

	public RegulatoryEntity getItem() {
		return item;
	}

	public void setItem(RegulatoryEntity item) {
		this.item = item;
	}

	public Map<String, NodeRef> getCountries() {
		return countries;
	}

	public void setCountries(Map<String, NodeRef> countries) {
		this.countries = countries;
	}

	public Map<String, NodeRef> getUsages() {
		return usages;
	}

	public void setUsages(Map<String, NodeRef> usages) {
		this.usages = usages;
	}

	public Integer getModuleId() {
		return moduleId;
	}

	public void setModuleId(Integer moduleId) {
		this.moduleId = moduleId;
	}

	public boolean isEmpty() {
		return countries.isEmpty() || usages.isEmpty();
	}

}