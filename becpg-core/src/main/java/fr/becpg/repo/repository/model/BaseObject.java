package fr.becpg.repo.repository.model;



/**
 * Base class for Model objects.  Child objects should implement toString(), 
 * equals() and hashCode();
 * 
 * @author matthieu
 */
public abstract class BaseObject /* TODO implements Serializable */{    

	public abstract String toString();
    public abstract boolean equals(Object o);
    public abstract int hashCode();
}
