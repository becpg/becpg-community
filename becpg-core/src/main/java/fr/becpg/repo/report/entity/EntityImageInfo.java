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
	byte[] content;
	String mimeType;


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
	 * <p>Constructor for an image generated on the fly rather than read from a content node, so that
	 * a report can carry a picture that exists nowhere in the repository.</p>
	 *
	 * @param imgId a {@link java.lang.String} object
	 * @param content an array of {@link byte} objects
	 * @param mimeType a {@link java.lang.String} object
	 */
	public EntityImageInfo(String imgId, byte[] content, String mimeType) {
		super();
		this.id = imgId;
		this.content = content;
		this.mimeType = mimeType;
	}

	/**
	 * <p>Tells whether this image carries its bytes instead of pointing at a content node.</p>
	 *
	 * @return a boolean
	 */
	public boolean isInMemory() {
		return (imageNodeRef == null) && (content != null) && (content.length > 0);
	}

	/**
	 * <p>Getter for the field <code>content</code>.</p>
	 *
	 * @return an array of {@link byte} objects
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * <p>Getter for the field <code>mimeType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getMimeType() {
		return mimeType;
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

	
	/**
	 * {@inheritDoc}
	 *
	 * Images are held in a set and reach the report engine keyed by their id, so the id alone
	 * decides identity. Comparing the bytes as well would let two generated images with the same
	 * id both survive, and the report would then pick either one.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		return Objects.equals(id, ((EntityImageInfo) obj).id);
	}

	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "EntityImageInfo [id=" + id + ", name=" + name + ", title=" + title + ", description=" + description + ", imageNodeRef=" + imageNodeRef
				+ "]";
	}

}
