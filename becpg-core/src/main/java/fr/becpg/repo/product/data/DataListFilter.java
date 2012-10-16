package fr.becpg.repo.product.data;

import org.apache.commons.collections.Predicate;

public interface DataListFilter<T> {
	
	
	public Predicate createPredicate(T data);
	
	
}
