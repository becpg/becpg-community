package fr.becpg.repo.repository.model;

import java.util.Set;

import org.alfresco.service.namespace.QName;

public interface AspectAwareDataItem {

	/**
	 * Optional Set to add extra aspects
	 * @return
	 */
	public Set<QName> getAspects();
	public void setAspects(Set<QName> aspects);
	
}
