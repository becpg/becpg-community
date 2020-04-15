package fr.becpg.repo.product.data.productList;

import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:contactList")
public class ContactListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = 7013902261053544166L;

	private String firstName;
	private String lastName;
	private String email;

	@AlfProp
	@AlfQname(qname = "bcpg:contactListFirstName")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:contactListLastName")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:contactListEmail")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + Objects.hash(email, firstName, lastName);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ContactListDataItem other = (ContactListDataItem) obj;
		return Objects.equals(email, other.email) && Objects.equals(firstName, other.firstName) && Objects.equals(lastName, other.lastName);
	}

	@Override
	public String toString() {
		return "ContactListDataItem [firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + "]";
	}

}
