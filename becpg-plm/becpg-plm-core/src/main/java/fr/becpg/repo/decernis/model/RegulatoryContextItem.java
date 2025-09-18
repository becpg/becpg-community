package fr.becpg.repo.decernis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.regulatory.RegulatoryEntity;

/**
 * <p>RegulatoryContextItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class RegulatoryContextItem {

	private RegulatoryEntity item;
	private Map<String, NodeRef> countries = new HashMap<>();
	private List<UsageContext> usages = new ArrayList<>();

	/**
	 * <p>Getter for the field <code>item</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.regulatory.RegulatoryEntity} object
	 */
	public RegulatoryEntity getItem() {
		return item;
	}

	/**
	 * <p>Setter for the field <code>item</code>.</p>
	 *
	 * @param item a {@link fr.becpg.repo.regulatory.RegulatoryEntity} object
	 */
	public void setItem(RegulatoryEntity item) {
		this.item = item;
	}

	/**
	 * <p>Getter for the field <code>countries</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<String, NodeRef> getCountries() {
		return countries;
	}

	/**
	 * <p>Setter for the field <code>countries</code>.</p>
	 *
	 * @param countries a {@link java.util.Map} object
	 */
	public void setCountries(Map<String, NodeRef> countries) {
		this.countries = countries;
	}

	/**
	 * <p>Getter for the field <code>usages</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<UsageContext> getUsages() {
		return usages;
	}

	/**
	 * <p>Setter for the field <code>usages</code>.</p>
	 *
	 * @param usages a {@link java.util.List} object
	 */
	public void setUsages(List<UsageContext> usages) {
		this.usages = usages;
	}

	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean
	 */
	public boolean isEmpty() {
		return countries.isEmpty() || usages.isEmpty();
	}

}
