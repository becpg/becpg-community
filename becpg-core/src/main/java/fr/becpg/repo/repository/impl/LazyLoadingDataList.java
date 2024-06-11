/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.repository.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>LazyLoadingDataList class.</p>
 *
 * @author matthieu
 * @param <E>
 * @version $Id: $Id
 */
public class LazyLoadingDataList<E extends RepositoryEntity> implements List<E>, Collection<E>  {

	
	List<E> backedList = null;
	
	public interface DataProvider<E> {
		List<E> getData();
		String getFieldName();
	}

	DataProvider<E> dataProvider; 

	private boolean loaded = false;
	
	final Set<E> deletedNodes  = new HashSet<>();

	/**
	 * <p>Setter for the field <code>dataProvider</code>.</p>
	 *
	 * @param dataProvider a {@link fr.becpg.repo.repository.impl.LazyLoadingDataList.DataProvider} object.
	 */
	public void setDataProvider(DataProvider<E> dataProvider) {
		this.dataProvider = dataProvider;
	}
	

	/**
	 * <p>Getter for the field <code>deletedNodes</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<E> getDeletedNodes() {
		return deletedNodes;
	}
	
	
	
	/**
	 * <p>isLoaded.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isLoaded() {
		return loaded;
	}

	
	private List<E> getList() {
		if(backedList==null){
			backedList  = dataProvider.getData();
			loaded = true;
		} 
		
		
		return backedList;
	}
	
	
	/**
	 * <p>refresh.</p>
	 */
	public void  refresh() {
		backedList = null;
		loaded = false;
		deletedNodes.clear();
	}
	

	// List interfaces methods
	
	/** {@inheritDoc} */
	@Override
	public int size() {
		return  getList().size();
	}


	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return getList().isEmpty();
	}


	/** {@inheritDoc} */
	@Override
	public boolean contains(Object o) {
		return getList().contains(o);
	}


	/** {@inheritDoc} */
	@Override
	public Iterator<E> iterator() {
		return getList().iterator();
	}


	/** {@inheritDoc} */
	@Override
	public Object[] toArray() {
		return getList().toArray();
	}


	/** {@inheritDoc} */
	@Override
	public <T> T[] toArray(T[] a) {
		return getList().toArray( a);
	}


	/** {@inheritDoc} */
	@Override
	public boolean add(E e) {
		deletedNodes.remove(e);
		return getList().add(e);
	}


	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		 deletedNodes.add((E) o);
		 return getList().remove(o);
	}


	/** {@inheritDoc} */
	@Override
	public boolean containsAll(Collection<?> c) {
		return getList().containsAll(c);
	}


	/** {@inheritDoc} */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		deletedNodes.removeAll(c);
		return getList().addAll(c);
	}


	/** {@inheritDoc} */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		deletedNodes.removeAll(c);
		return getList().addAll(index, c);
	}


	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> c) {
		deletedNodes.addAll((Collection<? extends E>) c);
		return getList().removeAll(c);
	}


	/** {@inheritDoc} */
	@Override
	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Iterator<E> e = iterator();
		while (e.hasNext()) {
			E elt = e.next();
		    if (!c.contains(elt)) {
			    deletedNodes.add(elt);
				e.remove();
				modified = true;
		    }
		}
		return modified;
		
	}


	/** {@inheritDoc} */
	@Override
	public void clear() {
		deletedNodes.addAll(getList());
		getList().clear();
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<E> each = iterator();
        while (each.hasNext()) {
            E next = each.next();
			if (filter.test(next)) {
            	deletedNodes.add(next);
                each.remove();
                removed = true;
            }
        }
        return removed;
    }


	/** {@inheritDoc} */
	@Override
	public E get(int index) {
		return getList().get(index);
	}


	/** {@inheritDoc} */
	@Override
	public E set(int index, E element) {
		return getList().set(index, element);
	}

	/** {@inheritDoc} */
	@Override
	public void add(int index, E element) {
		deletedNodes.remove(element);
		getList().add(index, element);
		
	}

	/** {@inheritDoc} */
	@Override
	public E remove(int index) {
		
		deletedNodes.add( getList().get(index));
		return getList().remove(index);
	}


	/** {@inheritDoc} */
	@Override
	public int indexOf(Object o) {
		return getList().indexOf(o);
	}


	/** {@inheritDoc} */
	@Override
	public int lastIndexOf(Object o) {
		return getList().lastIndexOf(o);
	}


	/** {@inheritDoc} */
	@Override
	public ListIterator<E> listIterator() {
		return getList().listIterator();
	}


	/** {@inheritDoc} */
	@Override
	public ListIterator<E> listIterator(int index) {
		return getList().listIterator(index);
	}


	/** {@inheritDoc} */
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return getList().subList(fromIndex, toIndex);
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LazyLoadingDataList [backedList=" + backedList + ", loaded=" + loaded + ", deletedNodes=" + deletedNodes + "]";
	}


	
	
	
}
