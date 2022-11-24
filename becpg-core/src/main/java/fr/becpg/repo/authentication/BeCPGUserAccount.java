package fr.becpg.repo.authentication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

public class BeCPGUserAccount {

	private String userName;
	private String password;

	private Map<QName,Serializable> extraProps = new HashMap<>();

	private Set<String> authorities = new HashSet<>();

	private Boolean notify = false;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return  (String) extraProps.get(ContentModel.PROP_FIRSTNAME);
	}

	public void setFirstName(String firstName) {
		extraProps.put(ContentModel.PROP_FIRSTNAME, firstName);
	}

	public String getLastName() {
		return (String) extraProps.get(ContentModel.PROP_LASTNAME);
	}

	public void setLastName(String lastName) {
		extraProps.put(ContentModel.PROP_LASTNAME, lastName);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return (String) extraProps.get(ContentModel.PROP_EMAIL);
	}

	public void setEmail(String email) {
		extraProps.put(ContentModel.PROP_EMAIL, email);
	}

	public Boolean getNotify() {
		return notify;
	}

	public void setNotify(Boolean notify) {
		this.notify = notify;
	}

	public Set<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Set<String> authorities) {
		this.authorities = authorities;
	}

	public Map<QName, Serializable> getExtraProps() {
		return extraProps;
	}

	public void setExtraProps(Map<QName, Serializable> extraProps) {
		this.extraProps = extraProps;
	}

	@Override
	public int hashCode() {
		return Objects.hash(authorities, extraProps, notify, password, userName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		BeCPGUserAccount other = (BeCPGUserAccount) obj;
		return Objects.equals(authorities, other.authorities) && Objects.equals(extraProps, other.extraProps) && Objects.equals(notify, other.notify)
				&& Objects.equals(password, other.password) && Objects.equals(userName, other.userName);
	}

	@Override
	public String toString() {
		return "BeCPGUserAccount [userName=" + userName + ", password=" + password + ", extraProps=" + extraProps + ", authorities=" + authorities
				+ ", notify=" + notify + "]";
	}



}
