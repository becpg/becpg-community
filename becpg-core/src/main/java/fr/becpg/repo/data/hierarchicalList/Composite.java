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
package fr.becpg.repo.data.hierarchicalList;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing a hierarchical data list
 *
 * @author quere
 * @version $Id: $Id
 */
public class Composite<T extends CompositeDataItem<T>> {

	private T data;

	private List<Composite<T>> children = new LinkedList<>();

	/**
	 * <p>Getter for the field <code>data</code>.</p>
	 *
	 * @return a T object.
	 */
	public T getData() {
		return data;
	}

	/**
	 * <p>Setter for the field <code>data</code>.</p>
	 *
	 * @param data a T object.
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * <p>Constructor for Composite.</p>
	 */
	public Composite() {

	}

	/**
	 * <p>Constructor for Composite.</p>
	 *
	 * @param data a T object.
	 */
	public Composite(T data) {

		this.data = data;
	}

	/**
	 * <p>Getter for the field <code>children</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Composite<T>> getChildren() {
		return children;
	}

	/**
	 * <p>Setter for the field <code>children</code>.</p>
	 *
	 * @param children a {@link java.util.List} object.
	 */
	public void setChildren(List<Composite<T>> children) {
		this.children = children;
	}

	/**
	 * <p>addChild.</p>
	 *
	 * @param component a {@link fr.becpg.repo.data.hierarchicalList.Composite} object.
	 */
	public void addChild(Composite<T> component) {
		children.add(component);
	}

	/**
	 * <p>removeChild.</p>
	 *
	 * @param component a {@link fr.becpg.repo.data.hierarchicalList.Composite} object.
	 */
	public void removeChild(Composite<T> component) {
		children.remove(component);
	}

	/**
	 * <p>isLeaf.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isLeaf() {
		return children == null || children.isEmpty();
	}

	/**
	 * <p>isRoot.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isRoot() {
		return data == null;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Composite other = (Composite) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		print(sb, "",true);
		return sb.toString();
	}

	private void print(StringBuilder sb, String prefix, boolean isTail) {
		sb.append(prefix).append(isTail ? "└──[" : "├──[").append(data == null ? "root" : data).append("]\n");
        for (Iterator<Composite<T>> iterator = children.iterator(); iterator.hasNext(); ) {
            iterator.next().print(sb, prefix + (isTail ? "    " : "│   "), !iterator.hasNext());
        }
    }


}
