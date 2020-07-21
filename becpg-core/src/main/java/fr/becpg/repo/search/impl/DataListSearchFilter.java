package fr.becpg.repo.search.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>DataListSearchFilter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DataListSearchFilter {

	private String name;
	private List<DataListSearchFilterField> assocsFilters = new ArrayList<>();
	private List<DataListSearchFilterField> propFilters = new ArrayList<>();

	/**
	 * <p>Constructor for DataListSearchFilter.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public DataListSearchFilter(String name) {
		super();
		this.name = name;
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Getter for the field <code>assocsFilters</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<DataListSearchFilterField> getAssocsFilters() {
		return assocsFilters;
	}

	/**
	 * <p>Getter for the field <code>propFilters</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<DataListSearchFilterField> getPropFilters() {
		return propFilters;
	}
	
	
}
