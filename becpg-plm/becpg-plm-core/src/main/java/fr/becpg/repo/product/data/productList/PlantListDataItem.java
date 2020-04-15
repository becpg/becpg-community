package fr.becpg.repo.product.data.productList;

import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:plant")
public class PlantListDataItem extends BeCPGDataObject{

	
	private static final long serialVersionUID = 5761731974557143651L;
	
	private String packerCode;
	private String approvalNumbers;
	
	
	
	@AlfProp
	@AlfQname(qname="bcpg:plantPackerCode")
	public String getPackerCode() {
		return packerCode;
	}

	public void setPackerCode(String packerCode) {
		this.packerCode = packerCode;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:plantApprovalNumbers")
	public String getApprovalNumbers() {
		return approvalNumbers;
	}

	public void setApprovalNumbers(String approvalNumbers) {
		this.approvalNumbers = approvalNumbers;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(approvalNumbers, packerCode);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlantListDataItem other = (PlantListDataItem) obj;
		return Objects.equals(approvalNumbers, other.approvalNumbers) && Objects.equals(packerCode, other.packerCode);
	}

	@Override
	public String toString() {
		return "PlantListDataItem [packerCode=" + packerCode + ", approvalNumbers=" + approvalNumbers + "]";
	}


	
	
	
}
