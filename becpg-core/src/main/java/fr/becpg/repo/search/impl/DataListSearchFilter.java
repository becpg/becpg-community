package fr.becpg.repo.search.impl;

import java.util.ArrayList;
import java.util.List;

public class DataListSearchFilter {

	private String name;
	private List<DataListSearchFilterField> assocsFilters = new ArrayList<>();
	private List<DataListSearchFilterField> propFilters = new ArrayList<>();

	public DataListSearchFilter(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<DataListSearchFilterField> getAssocsFilters() {
		return assocsFilters;
	}

	public List<DataListSearchFilterField> getPropFilters() {
		return propFilters;
	}
	
	
}
