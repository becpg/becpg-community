package fr.becpg.repo.decernis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.RegulatoryEntity;

public class RegulatoryContextItem {

	private RegulatoryEntity item;
	private Map<String, NodeRef> countries = new HashMap<>();
	private List<UsageContext> usages = new ArrayList<>();

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

	public List<UsageContext> getUsages() {
		return usages;
	}

	public void setUsages(List<UsageContext> usages) {
		this.usages = usages;
	}

	public boolean isEmpty() {
		return countries.isEmpty() || usages.isEmpty();
	}

}