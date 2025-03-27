package fr.becpg.repo.authentication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

/**
 * <p>BeCPGUserAccount class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGUserAccount {

	private String userName;
	private String password;
	private Boolean generatePassword;
	private Boolean synchronizeWithIDS;
	private Boolean disable;
	private String newUserName;

	private Map<QName,Serializable> extraProps = new HashMap<>();

	private Set<String> authorities = new HashSet<>();

	private Boolean notify = false;

	public Boolean getSynchronizeWithIDS() {
		return synchronizeWithIDS;
	}

	public void setSynchronizeWithIDS(Boolean synchronizeWithIDS) {
		this.synchronizeWithIDS = synchronizeWithIDS;
	}

	public Boolean getGeneratePassword() {
		return generatePassword;
	}

	public void setGeneratePassword(Boolean generatePassword) {
		this.generatePassword = generatePassword;
	}

	public Boolean getDisable() {
		return disable;
	}

	public void setDisable(Boolean disable) {
		this.disable = disable;
	}

	public String getNewUserName() {
		return newUserName;
	}

	public void setNewUserName(String newUserName) {
		this.newUserName = newUserName;
	}

	/**
	 * <p>Getter for the field <code>userName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * <p>Setter for the field <code>userName</code>.</p>
	 *
	 * @param userName a {@link java.lang.String} object
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * <p>getFirstName.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getFirstName() {
		return  (String) extraProps.get(ContentModel.PROP_FIRSTNAME);
	}

	/**
	 * <p>setFirstName.</p>
	 *
	 * @param firstName a {@link java.lang.String} object
	 */
	public void setFirstName(String firstName) {
		extraProps.put(ContentModel.PROP_FIRSTNAME, firstName);
	}

	/**
	 * <p>getLastName.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getLastName() {
		return (String) extraProps.get(ContentModel.PROP_LASTNAME);
	}

	/**
	 * <p>setLastName.</p>
	 *
	 * @param lastName a {@link java.lang.String} object
	 */
	public void setLastName(String lastName) {
		extraProps.put(ContentModel.PROP_LASTNAME, lastName);
	}

	/**
	 * <p>Getter for the field <code>password</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * <p>Setter for the field <code>password</code>.</p>
	 *
	 * @param password a {@link java.lang.String} object
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * <p>getEmail.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getEmail() {
		return (String) extraProps.get(ContentModel.PROP_EMAIL);
	}

	/**
	 * <p>setEmail.</p>
	 *
	 * @param email a {@link java.lang.String} object
	 */
	public void setEmail(String email) {
		extraProps.put(ContentModel.PROP_EMAIL, email);
	}

	/**
	 * <p>Getter for the field <code>notify</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean getNotify() {
		return notify;
	}

	/**
	 * <p>Setter for the field <code>notify</code>.</p>
	 *
	 * @param notify a {@link java.lang.Boolean} object
	 */
	public void setNotify(Boolean notify) {
		this.notify = notify;
	}

	/**
	 * <p>Getter for the field <code>authorities</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<String> getAuthorities() {
		return authorities;
	}

	/**
	 * <p>Setter for the field <code>authorities</code>.</p>
	 *
	 * @param authorities a {@link java.util.Set} object
	 */
	public void setAuthorities(Set<String> authorities) {
		this.authorities = authorities;
	}

	/**
	 * <p>Getter for the field <code>extraProps</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<QName, Serializable> getExtraProps() {
		return extraProps;
	}

	/**
	 * <p>Setter for the field <code>extraProps</code>.</p>
	 *
	 * @param extraProps a {@link java.util.Map} object
	 */
	public void setExtraProps(Map<QName, Serializable> extraProps) {
		this.extraProps = extraProps;
	}

	@Override
	public int hashCode() {
		return Objects.hash(authorities, disable, extraProps, generatePassword, newUserName, notify, password, synchronizeWithIDS, userName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeCPGUserAccount other = (BeCPGUserAccount) obj;
		return Objects.equals(authorities, other.authorities) && Objects.equals(disable, other.disable)
				&& Objects.equals(extraProps, other.extraProps) && Objects.equals(generatePassword, other.generatePassword)
				&& Objects.equals(newUserName, other.newUserName) && Objects.equals(notify, other.notify) && Objects.equals(password, other.password)
				&& Objects.equals(synchronizeWithIDS, other.synchronizeWithIDS) && Objects.equals(userName, other.userName);
	}

	@Override
	public String toString() {
		return "BeCPGUserAccount [userName=" + userName + ", password=" + password + ", generatePassword=" + generatePassword
				+ ", synchronizeWithIDS=" + synchronizeWithIDS + ", disable=" + disable + ", newUserName=" + newUserName
				+ ", extraProps=" + extraProps + ", authorities=" + authorities + ", notify=" + notify + "]";
	}

}
