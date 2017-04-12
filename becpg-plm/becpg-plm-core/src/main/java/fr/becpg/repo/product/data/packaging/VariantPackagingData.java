package fr.becpg.repo.product.data.packaging;

import java.math.BigDecimal;

/**
 * Manage variant
 * @author quere
 *
 */
public class VariantPackagingData {
	
	private BigDecimal tarePrimary = new BigDecimal(0d);
	private BigDecimal tareSecondary = new BigDecimal(0d);
	private BigDecimal tareTertiary = new BigDecimal(0d);
	private Integer productPerBoxes;
	private Integer boxesPerPallet;
	private Integer palletNumberOnGround;

	public BigDecimal getTarePrimary() {
		return tarePrimary;
	}

	public void setTarePrimary(BigDecimal tarePrimary) {
		this.tarePrimary = tarePrimary;
	}

	public BigDecimal getTareSecondary() {
		return tareSecondary;
	}

	public void setTareSecondary(BigDecimal tareSecondary) {
		this.tareSecondary = tareSecondary;
	}

	public BigDecimal getTareTertiary() {
		return tareTertiary;
	}

	public void setTareTertiary(BigDecimal tareTertiary) {
		this.tareTertiary = tareTertiary;
	}

	public Integer getProductPerBoxes() {
		return productPerBoxes;
	}

	public void setProductPerBoxes(Integer productPerBoxes) {
		this.productPerBoxes = productPerBoxes;
	}

	public Integer getBoxesPerPallet() {
		return boxesPerPallet;
	}

	public void setBoxesPerPallet(Integer boxesPerPallet) {
		this.boxesPerPallet = boxesPerPallet;
	}

	public Integer getPalletNumberOnGround() {
		return palletNumberOnGround;
	}
	
	public Integer getProductPerPallet(){
		if(this.productPerBoxes != null && this.boxesPerPallet != null){
			return this.productPerBoxes * this.boxesPerPallet;
		}
		return null;
	}

	public void setPalletNumberOnGround(Integer palletNumberOnGround) {
		this.palletNumberOnGround = palletNumberOnGround;
	}

	@Override
	public String toString() {
		return "VariantPackagingData [tareSecondary=" + tareSecondary + ", tareTertiary=" + tareTertiary
				+ ", productPerBoxes=" + productPerBoxes + ", boxesPerPallet=" + boxesPerPallet + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((boxesPerPallet == null) ? 0 : boxesPerPallet.hashCode());
		result = prime * result + ((palletNumberOnGround == null) ? 0 : palletNumberOnGround.hashCode());
		result = prime * result + ((productPerBoxes == null) ? 0 : productPerBoxes.hashCode());
		result = prime * result + ((tarePrimary == null) ? 0 : tarePrimary.hashCode());
		result = prime * result + ((tareSecondary == null) ? 0 : tareSecondary.hashCode());
		result = prime * result + ((tareTertiary == null) ? 0 : tareTertiary.hashCode());
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
		VariantPackagingData other = (VariantPackagingData) obj;
		if (boxesPerPallet == null) {
			if (other.boxesPerPallet != null)
				return false;
		} else if (!boxesPerPallet.equals(other.boxesPerPallet))
			return false;
		if (palletNumberOnGround == null) {
			if (other.palletNumberOnGround != null)
				return false;
		} else if (!palletNumberOnGround.equals(other.palletNumberOnGround))
			return false;
		if (productPerBoxes == null) {
			if (other.productPerBoxes != null)
				return false;
		} else if (!productPerBoxes.equals(other.productPerBoxes))
			return false;
		if (tarePrimary == null) {
			if (other.tarePrimary != null)
				return false;
		} else if (!tarePrimary.equals(other.tarePrimary))
			return false;
		if (tareSecondary == null) {
			if (other.tareSecondary != null)
				return false;
		} else if (!tareSecondary.equals(other.tareSecondary))
			return false;
		if (tareTertiary == null) {
			if (other.tareTertiary != null)
				return false;
		} else if (!tareTertiary.equals(other.tareTertiary))
			return false;
		return true;
	}
}
