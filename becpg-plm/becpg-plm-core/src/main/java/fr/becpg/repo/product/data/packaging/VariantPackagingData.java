package fr.becpg.repo.product.data.packaging;

import java.math.BigDecimal;

/**
 * Manage variant
 *
 * @author quere
 *
 */
public class VariantPackagingData {

	private BigDecimal tarePrimary = new BigDecimal(0d);
	private BigDecimal tareSecondary = new BigDecimal(0d);
	private BigDecimal tareTertiary = new BigDecimal(0d);
	private Integer productPerBoxes;
	private Integer boxesPerPallet;

	private Integer palletLayers;
	private Integer palletBoxesPerLayer;
	private Integer palletHeight;
	private Integer palletNumberOnGround;
	private Integer palletBoxesPerLastLayer;
	private Double palletStackingMaxWeight;
	private String palletTypeCode;

	private Double height;
	private Double width;
	private Double depth;

	private Double secondaryHeight;
	private Double secondaryWidth;
	private Double secondaryDepth;

	private Float tertiaryWidth;
	private Float tertiaryDepth;

	private boolean isManualPrimary = true;
	private boolean isManualTertiary = true;
	private boolean isManualSecondary = true;

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

	public Integer getProductPerPallet() {
		if ((this.productPerBoxes != null) && (this.boxesPerPallet != null)) {
			return this.productPerBoxes * this.boxesPerPallet;
		}
		return null;
	}

	public Integer getPalletLayers() {
		return palletLayers;
	}

	public void setPalletLayers(Integer palletLayers) {
		this.palletLayers = palletLayers;
	}

	public Integer getPalletBoxesPerLayer() {
		return palletBoxesPerLayer;
	}

	public void setPalletBoxesPerLayer(Integer palletBoxesPerLayer) {
		this.palletBoxesPerLayer = palletBoxesPerLayer;
	}

	public Integer getPalletHeight() {
		return palletHeight;
	}

	public void setPalletHeight(Integer palletHeight) {
		this.palletHeight = palletHeight;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getDepth() {
		return depth;
	}

	public void setDepth(Double depth) {
		this.depth = depth;
	}

	public Double getSecondaryHeight() {
		return secondaryHeight;
	}

	public void setSecondaryHeight(Double secondaryHeight) {
		this.secondaryHeight = secondaryHeight;
	}

	public Double getSecondaryWidth() {
		return secondaryWidth;
	}

	public void setSecondaryWidth(Double secondaryWidth) {
		this.secondaryWidth = secondaryWidth;
	}

	public Double getSecondaryDepth() {
		return secondaryDepth;
	}

	public void setSecondaryDepth(Double secondaryDepth) {
		this.secondaryDepth = secondaryDepth;
	}

	public Float getTertiaryWidth() {
		return tertiaryWidth;
	}

	public void setTertiaryWidth(Float tertiaryWidth) {
		this.tertiaryWidth = tertiaryWidth;
	}

	public Float getTertiaryDepth() {
		return tertiaryDepth;
	}

	public void setTertiaryDepth(Float tertiaryDepth) {
		this.tertiaryDepth = tertiaryDepth;
	}

	public void setPalletNumberOnGround(Integer palletNumberOnGround) {
		this.palletNumberOnGround = palletNumberOnGround;
	}

	public Integer getPalletBoxesPerLastLayer() {
		return palletBoxesPerLastLayer;
	}

	public void setPalletBoxesPerLastLayer(Integer palletBoxesPerLastLayer) {
		this.palletBoxesPerLastLayer = palletBoxesPerLastLayer;
	}

	public Double getPalletStackingMaxWeight() {
		return palletStackingMaxWeight;
	}

	public void setPalletStackingMaxWeight(Double palletStackingMaxWeight) {
		this.palletStackingMaxWeight = palletStackingMaxWeight;
	}

	public String getPalletTypeCode() {
		return palletTypeCode;
	}

	public void setPalletTypeCode(String palletTypeCode) {
		this.palletTypeCode = palletTypeCode;
	}

	public boolean isManualPrimary() {
		return isManualPrimary;
	}

	public void setManualPrimary(boolean isManualPrimary) {
		this.isManualPrimary = isManualPrimary;
	}

	public boolean isManualTertiary() {
		return isManualTertiary;
	}

	public void setManualTertiary(boolean isManualTertiary) {
		this.isManualTertiary = isManualTertiary;
	}

	public boolean isManualSecondary() {
		return isManualSecondary;
	}

	public void setManualSecondary(boolean isManualSecondary) {
		this.isManualSecondary = isManualSecondary;
	}

	public void addTarePrimary(BigDecimal value) {
		if (getTarePrimary() != null) {
			setTarePrimary(getTarePrimary().add(value));
		} else {
			setTarePrimary(value);
		}

	}

	public void addTareTertiary(BigDecimal value) {
		if (getTareTertiary() != null) {
			setTareTertiary(getTareTertiary().add(value));
		} else {
			setTareTertiary(value);
		}

	}

	public void addTareSecondary(BigDecimal value) {
		if (getTareSecondary() != null) {
			setTareSecondary(getTareSecondary().add(value));
		} else {
			setTareSecondary(value);
		}

	}

	@Override
	public String toString() {
		return "VariantPackagingData [tarePrimary=" + tarePrimary + ", tareSecondary=" + tareSecondary + ", tareTertiary=" + tareTertiary
				+ ", productPerBoxes=" + productPerBoxes + ", boxesPerPallet=" + boxesPerPallet + ", palletLayers=" + palletLayers
				+ ", palletBoxesPerLayer=" + palletBoxesPerLayer + ", palletHeight=" + palletHeight + ", palletNumberOnGround=" + palletNumberOnGround
				+ ", palletBoxesPerLastLayer=" + palletBoxesPerLastLayer + ", palletStackingMaxWeight=" + palletStackingMaxWeight
				+ ", palletTypeCode=" + palletTypeCode + ", height=" + height + ", width=" + width + ", depth=" + depth + ", secondaryHeight="
				+ secondaryHeight + ", secondaryWidth=" + secondaryWidth + ", secondaryDepth=" + secondaryDepth + ", tertiaryWidth=" + tertiaryWidth
				+ ", tertiaryDepth=" + tertiaryDepth + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((boxesPerPallet == null) ? 0 : boxesPerPallet.hashCode());
		result = (prime * result) + ((depth == null) ? 0 : depth.hashCode());
		result = (prime * result) + ((height == null) ? 0 : height.hashCode());
		result = (prime * result) + ((palletBoxesPerLastLayer == null) ? 0 : palletBoxesPerLastLayer.hashCode());
		result = (prime * result) + ((palletBoxesPerLayer == null) ? 0 : palletBoxesPerLayer.hashCode());
		result = (prime * result) + ((palletHeight == null) ? 0 : palletHeight.hashCode());
		result = (prime * result) + ((palletLayers == null) ? 0 : palletLayers.hashCode());
		result = (prime * result) + ((palletNumberOnGround == null) ? 0 : palletNumberOnGround.hashCode());
		result = (prime * result) + ((palletStackingMaxWeight == null) ? 0 : palletStackingMaxWeight.hashCode());
		result = (prime * result) + ((palletTypeCode == null) ? 0 : palletTypeCode.hashCode());
		result = (prime * result) + ((productPerBoxes == null) ? 0 : productPerBoxes.hashCode());
		result = (prime * result) + ((secondaryDepth == null) ? 0 : secondaryDepth.hashCode());
		result = (prime * result) + ((secondaryHeight == null) ? 0 : secondaryHeight.hashCode());
		result = (prime * result) + ((secondaryWidth == null) ? 0 : secondaryWidth.hashCode());
		result = (prime * result) + ((tarePrimary == null) ? 0 : tarePrimary.hashCode());
		result = (prime * result) + ((tareSecondary == null) ? 0 : tareSecondary.hashCode());
		result = (prime * result) + ((tareTertiary == null) ? 0 : tareTertiary.hashCode());
		result = (prime * result) + ((tertiaryDepth == null) ? 0 : tertiaryDepth.hashCode());
		result = (prime * result) + ((tertiaryWidth == null) ? 0 : tertiaryWidth.hashCode());
		result = (prime * result) + ((width == null) ? 0 : width.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		VariantPackagingData other = (VariantPackagingData) obj;
		if (boxesPerPallet == null) {
			if (other.boxesPerPallet != null) {
				return false;
			}
		} else if (!boxesPerPallet.equals(other.boxesPerPallet)) {
			return false;
		}
		if (depth == null) {
			if (other.depth != null) {
				return false;
			}
		} else if (!depth.equals(other.depth)) {
			return false;
		}
		if (height == null) {
			if (other.height != null) {
				return false;
			}
		} else if (!height.equals(other.height)) {
			return false;
		}
		if (palletBoxesPerLastLayer == null) {
			if (other.palletBoxesPerLastLayer != null) {
				return false;
			}
		} else if (!palletBoxesPerLastLayer.equals(other.palletBoxesPerLastLayer)) {
			return false;
		}
		if (palletBoxesPerLayer == null) {
			if (other.palletBoxesPerLayer != null) {
				return false;
			}
		} else if (!palletBoxesPerLayer.equals(other.palletBoxesPerLayer)) {
			return false;
		}
		if (palletHeight == null) {
			if (other.palletHeight != null) {
				return false;
			}
		} else if (!palletHeight.equals(other.palletHeight)) {
			return false;
		}
		if (palletLayers == null) {
			if (other.palletLayers != null) {
				return false;
			}
		} else if (!palletLayers.equals(other.palletLayers)) {
			return false;
		}
		if (palletNumberOnGround == null) {
			if (other.palletNumberOnGround != null) {
				return false;
			}
		} else if (!palletNumberOnGround.equals(other.palletNumberOnGround)) {
			return false;
		}
		if (palletStackingMaxWeight == null) {
			if (other.palletStackingMaxWeight != null) {
				return false;
			}
		} else if (!palletStackingMaxWeight.equals(other.palletStackingMaxWeight)) {
			return false;
		}
		if (palletTypeCode == null) {
			if (other.palletTypeCode != null) {
				return false;
			}
		} else if (!palletTypeCode.equals(other.palletTypeCode)) {
			return false;
		}
		if (productPerBoxes == null) {
			if (other.productPerBoxes != null) {
				return false;
			}
		} else if (!productPerBoxes.equals(other.productPerBoxes)) {
			return false;
		}
		if (secondaryDepth == null) {
			if (other.secondaryDepth != null) {
				return false;
			}
		} else if (!secondaryDepth.equals(other.secondaryDepth)) {
			return false;
		}
		if (secondaryHeight == null) {
			if (other.secondaryHeight != null) {
				return false;
			}
		} else if (!secondaryHeight.equals(other.secondaryHeight)) {
			return false;
		}
		if (secondaryWidth == null) {
			if (other.secondaryWidth != null) {
				return false;
			}
		} else if (!secondaryWidth.equals(other.secondaryWidth)) {
			return false;
		}
		if (tarePrimary == null) {
			if (other.tarePrimary != null) {
				return false;
			}
		} else if (!tarePrimary.equals(other.tarePrimary)) {
			return false;
		}
		if (tareSecondary == null) {
			if (other.tareSecondary != null) {
				return false;
			}
		} else if (!tareSecondary.equals(other.tareSecondary)) {
			return false;
		}
		if (tareTertiary == null) {
			if (other.tareTertiary != null) {
				return false;
			}
		} else if (!tareTertiary.equals(other.tareTertiary)) {
			return false;
		}
		if (tertiaryDepth == null) {
			if (other.tertiaryDepth != null) {
				return false;
			}
		} else if (!tertiaryDepth.equals(other.tertiaryDepth)) {
			return false;
		}
		if (tertiaryWidth == null) {
			if (other.tertiaryWidth != null) {
				return false;
			}
		} else if (!tertiaryWidth.equals(other.tertiaryWidth)) {
			return false;
		}
		if (width == null) {
			if (other.width != null) {
				return false;
			}
		} else if (!width.equals(other.width)) {
			return false;
		}
		return true;
	}

}
