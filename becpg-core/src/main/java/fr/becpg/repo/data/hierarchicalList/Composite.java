package fr.becpg.repo.data.hierarchicalList;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing a hierarchical data list
 * 
 * @author quere
 * 
 * @param <T>
 */
public class Composite<T extends CompositeDataItem<T>> {

	private T data;

	private List<Composite<T>> children = new LinkedList<Composite<T>>();

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Composite() {

	}

	public Composite(T data) {

		this.data = data;
	}

	public List<Composite<T>> getChildren() {
		return children;
	}

	public void setChildren(List<Composite<T>> children) {
		this.children = children;
	}

	public void addChild(Composite<T> component) {
		children.add(component);
	}

	public void removeChild(Composite<T> component) {
		children.remove(component);
	}

	public boolean isLeaf() {
		return children == null || children.isEmpty();
	}

	public boolean isRoot() {
		return data == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		print(sb, "",true);
		return sb.toString();
	}

	private void print(StringBuilder sb, String prefix, boolean isTail) {
		sb.append(prefix + (isTail ? "└──[" : "├──[")+ (data==null ? "root" : data)  +"]\n");
        for (Iterator<Composite<T>> iterator = children.iterator(); iterator.hasNext(); ) {
            iterator.next().print(sb, prefix + (isTail ? "    " : "│   "), !iterator.hasNext());
        }
    }


}
