package fr.becpg.repo.repository.filters;

import org.apache.commons.collections.Predicate;

public interface DataListFilter<T> {
	
	
	public Predicate createPredicate(T data);
	
	
}
