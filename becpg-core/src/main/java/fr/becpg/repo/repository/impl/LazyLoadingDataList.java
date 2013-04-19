package fr.becpg.repo.repository.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author matthieu
 * @param <E>
 */
public class LazyLoadingDataList<E> implements List<E>  {

	
	List<E> backedList = null;

	private static Log logger = LogFactory.getLog(LazyLoadingDataList.class);
	
	public interface DataProvider<E> {
		public List<E> getData();
		public String getFieldName();
	}

	DataProvider<E> dataProvider; 

	private boolean loaded = false;
	
	Set<E> deletedNodes  = new HashSet<E>();

	public void setDataProvider(DataProvider<E> dataProvider) {
		this.dataProvider = dataProvider;
	}
	

	public Set<E> getDeletedNodes() {		
		return deletedNodes;
	}
	
	
	
	public boolean isLoaded() {
		return loaded;
	}

	
	private List<E> getList() {
		if(backedList==null){
			logger.debug("Lazy load list :"+dataProvider.getFieldName());
			backedList  = dataProvider.getData();
			loaded = true;
		} 
		
		
		return backedList;
	}
	

	// List interfaces methods
	
	@Override
	public int size() {
		return  getList().size();
	}


	@Override
	public boolean isEmpty() {
		return getList().isEmpty();
	}


	@Override
	public boolean contains(Object o) {
		return getList().contains(o);
	}


	@Override
	public Iterator<E> iterator() {
		return getList().iterator();
	}


	@Override
	public Object[] toArray() {
		return getList().toArray();
	}


	@Override
	public <T> T[] toArray(T[] a) {
		return getList().toArray( a);
	}


	@Override
	public boolean add(E e) {
		return getList().add(e);
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		 deletedNodes.add((E) o);
		 return getList().remove(o);
	}


	@Override
	public boolean containsAll(Collection<?> c) {
		return getList().containsAll(c);
	}


	@Override
	public boolean addAll(Collection<? extends E> c) {
		return getList().addAll(c);
	}


	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return getList().addAll(index, c);
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> c) {
		deletedNodes.addAll((Collection<? extends E>) c);
		return getList().removeAll(c);
	}


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


	@Override
	public void clear() {
		deletedNodes.addAll(getList());
		getList().clear();
	}


	@Override
	public E get(int index) {
		return getList().get(index);
	}


	@Override
	public E set(int index, E element) {
		return getList().set(index, element);
	}

	@Override
	public void add(int index, E element) {
		getList().add(index, element);
		
	}

	@Override
	public E remove(int index) {
		
		deletedNodes.add( getList().get(index));
		return getList().remove(index);
	}


	@Override
	public int indexOf(Object o) {
		return getList().indexOf(o);
	}


	@Override
	public int lastIndexOf(Object o) {
		return getList().lastIndexOf(o);
	}


	@Override
	public ListIterator<E> listIterator() {
		return getList().listIterator();
	}


	@Override
	public ListIterator<E> listIterator(int index) {
		return getList().listIterator(index);
	}


	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return getList().subList(fromIndex, toIndex);
	}

	
}
