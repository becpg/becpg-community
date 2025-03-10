package fr.becpg.repo.report.entity;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>EntityImageInfo class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityImageInfo {
	String id;
	String name;
	String title;
	String description;
	NodeRef imageNodeRef;
	

	/**
	 * <p>Constructor for EntityImageInfo.</p>
	 *
	 * @param imgId a {@link java.lang.String} object
	 * @param imageNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public EntityImageInfo(String imgId, NodeRef imageNodeRef) {
		super();
		this.id = imgId;
		this.imageNodeRef = imageNodeRef;
	}

	
	/**
	 * <p>Getter for the field <code>imageNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getImageNodeRef() {
		return imageNodeRef;
	}


	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() {
		return id;
	}

	/**
	 * <p>Setter for the field <code>id</code>.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 */
	public void setId(String id) {
		this.id = id;
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
	 * <p>Setter for the field <code>name</code>.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>Getter for the field <code>title</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * <p>Setter for the field <code>title</code>.</p>
	 *
	 * @param title a {@link java.lang.String} object.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * <p>Getter for the field <code>description</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * <p>Setter for the field <code>description</code>.</p>
	 *
	 * @param description a {@link java.lang.String} object.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(description, id, imageNodeRef, name, title);
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
		EntityImageInfo other = (EntityImageInfo) obj;
		return Objects.equals(description, other.description) && Objects.equals(id, other.id) && Objects.equals(imageNodeRef, other.imageNodeRef)
				&& Objects.equals(name, other.name) && Objects.equals(title, other.title);
	}

	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "EntityImageInfo [id=" + id + ", name=" + name + ", title=" + title + ", description=" + description + ", imageNodeRef=" + imageNodeRef
				+ "]";
	}

}
