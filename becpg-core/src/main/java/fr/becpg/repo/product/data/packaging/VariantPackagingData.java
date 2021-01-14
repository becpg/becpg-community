package fr.becpg.repo.product.data.packaging;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Manage variant
 *
 * @author quere
 * @version $Id: $Id
 */
public class VariantPackagingData implements Serializable {

	private static final long serialVersionUID = -3460131741476715569L;
	
	private BigDecimal tarePrimary = BigDecimal.valueOf(0d);
	private BigDecimal tareSecondary = BigDecimal.valueOf(0d);
	private BigDecimal tareTertiary = BigDecimal.valueOf(0d);
	private Integer productPerBoxes;
	private Integer boxesPerPallet;

	private Integer palletLayers;
	private Integer palletBoxesPerLayer;
	private Double palletHeight;
	private Integer palletNumberOnGround;
	private Integer palletBoxesPerLastLayer;
	private Double palletStackingMaxWeight;
	private String palletTypeCode;
	private String platformTermsAndConditionsCode;

	private Float height;
	private Float width;
	private Float depth;

	private Float secondaryHeight;
	private Float secondaryWidth;
	private Float secondaryDepth;

	private Float tertiaryWidth;
	private Float tertiaryDepth;

	private boolean isManualPrimary = true;
	private boolean isManualTertiary = true;
	private boolean isManualSecondary = true;
	private boolean isManualPalletInformations = true;

	/**
	 * <p>Getter for the field <code>tarePrimary</code>.</p>
	 *
	 * @return a {@link java.math.BigDecimal} object.
	 */
	public BigDecimal getTarePrimary() {
		return tarePrimary;
	}

	/**
	 * <p>Setter for the field <code>tarePrimary</code>.</p>
	 *
	 * @param tarePrimary a {@link java.math.BigDecimal} object.
	 */
	public void setTarePrimary(BigDecimal tarePrimary) {
		this.tarePrimary = tarePrimary;
	}

	/**
	 * <p>Getter for the field <code>tareSecondary</code>.</p>
	 *
	 * @return a {@link java.math.BigDecimal} object.
	 */
	public BigDecimal getTareSecondary() {
		return tareSecondary;
	}

	/**
	 * <p>Setter for the field <code>tareSecondary</code>.</p>
	 *
	 * @param tareSecondary a {@link java.math.BigDecimal} object.
	 */
	public void setTareSecondary(BigDecimal tareSecondary) {
		this.tareSecondary = tareSecondary;
	}

	/**
	 * <p>Getter for the field <code>tareTertiary</code>.</p>
	 *
	 * @return a {@link java.math.BigDecimal} object.
	 */
	public BigDecimal getTareTertiary() {
		return tareTertiary;
	}

	/**
	 * <p>Setter for the field <code>tareTertiary</code>.</p>
	 *
	 * @param tareTertiary a {@link java.math.BigDecimal} object.
	 */
	public void setTareTertiary(BigDecimal tareTertiary) {
		this.tareTertiary = tareTertiary;
	}

	/**
	 * <p>Getter for the field <code>productPerBoxes</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getProductPerBoxes() {
		return productPerBoxes;
	}

	/**
	 * <p>Setter for the field <code>productPerBoxes</code>.</p>
	 *
	 * @param productPerBoxes a {@link java.lang.Integer} object.
	 */
	public void setProductPerBoxes(Integer productPerBoxes) {
		this.productPerBoxes = productPerBoxes;
	}

	/**
	 * <p>Getter for the field <code>boxesPerPallet</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getBoxesPerPallet() {
		return boxesPerPallet;
	}

	/**
	 * <p>Setter for the field <code>boxesPerPallet</code>.</p>
	 *
	 * @param boxesPerPallet a {@link java.lang.Integer} object.
	 */
	public void setBoxesPerPallet(Integer boxesPerPallet) {
		this.boxesPerPallet = boxesPerPallet;
	}

	/**
	 * <p>Getter for the field <code>palletNumberOnGround</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getPalletNumberOnGround() {
		return palletNumberOnGround;
	}

	/**
	 * <p>getProductPerPallet.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getProductPerPallet() {
		if ((this.productPerBoxes != null) && (this.boxesPerPallet != null)) {
			return this.productPerBoxes * this.boxesPerPallet;
		}
		return null;
	}

	/**
	 * <p>Getter for the field <code>palletLayers</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getPalletLayers() {
		return palletLayers;
	}

	/**
	 * <p>Setter for the field <code>palletLayers</code>.</p>
	 *
	 * @param palletLayers a {@link java.lang.Integer} object.
	 */
	public void setPalletLayers(Integer palletLayers) {
		this.palletLayers = palletLayers;
	}

	/**
	 * <p>Getter for the field <code>palletBoxesPerLayer</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getPalletBoxesPerLayer() {
		return palletBoxesPerLayer;
	}

	/**
	 * <p>Setter for the field <code>palletBoxesPerLayer</code>.</p>
	 *
	 * @param palletBoxesPerLayer a {@link java.lang.Integer} object.
	 */
	public void setPalletBoxesPerLayer(Integer palletBoxesPerLayer) {
		this.palletBoxesPerLayer = palletBoxesPerLayer;
	}

	/**
	 * <p>Getter for the field <code>palletHeight</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getPalletHeight() {
		return palletHeight;
	}

	/**
	 * <p>Setter for the field <code>palletHeight</code>.</p>
	 *
	 * @param palletHeight a {@link java.lang.Double} object.
	 */
	public void setPalletHeight(Double palletHeight) {
		this.palletHeight = palletHeight;
	}

	/**
	 * <p>Getter for the field <code>height</code>.</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getHeight() {
		return height;
	}

	/**
	 * <p>Setter for the field <code>height</code>.</p>
	 *
	 * @param height a {@link java.lang.Float} object.
	 */
	public void setHeight(Float height) {
		this.height = height;
	}

	/**
	 * <p>Getter for the field <code>width</code>.</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getWidth() {
		return width;
	}

	/**
	 * <p>Setter for the field <code>width</code>.</p>
	 *
	 * @param width a {@link java.lang.Float} object.
	 */
	public void setWidth(Float width) {
		this.width = width;
	}

	/**
	 * <p>Getter for the field <code>depth</code>.</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getDepth() {
		return depth;
	}

	/**
	 * <p>Setter for the field <code>depth</code>.</p>
	 *
	 * @param depth a {@link java.lang.Float} object.
	 */
	public void setDepth(Float depth) {
		this.depth = depth;
	}

	/**
	 * <p>Getter for the field <code>secondaryHeight</code>.</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getSecondaryHeight() {
		return secondaryHeight;
	}

	/**
	 * <p>Setter for the field <code>secondaryHeight</code>.</p>
	 *
	 * @param secondaryHeight a {@link java.lang.Float} object.
	 */
	public void setSecondaryHeight(Float secondaryHeight) {
		this.secondaryHeight = secondaryHeight;
	}

	/**
	 * <p>Getter for the field <code>secondaryWidth</code>.</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getSecondaryWidth() {
		return secondaryWidth;
	}

	/**
	 * <p>Setter for the field <code>secondaryWidth</code>.</p>
	 *
	 * @param secondaryWidth a {@link java.lang.Float} object.
	 */
	public void setSecondaryWidth(Float secondaryWidth) {
		this.secondaryWidth = secondaryWidth;
	}

	/**
	 * <p>Getter for the field <code>secondaryDepth</code>.</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getSecondaryDepth() {
		return secondaryDepth;
	}

	/**
	 * <p>Setter for the field <code>secondaryDepth</code>.</p>
	 *
	 * @param secondaryDepth a {@link java.lang.Float} object.
	 */
	public void setSecondaryDepth(Float secondaryDepth) {
		this.secondaryDepth = secondaryDepth;
	}

	/**
	 * <p>Getter for the field <code>tertiaryWidth</code>.</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getTertiaryWidth() {
		return tertiaryWidth;
	}

	/**
	 * <p>Setter for the field <code>tertiaryWidth</code>.</p>
	 *
	 * @param tertiaryWidth a {@link java.lang.Float} object.
	 */
	public void setTertiaryWidth(Float tertiaryWidth) {
		this.tertiaryWidth = tertiaryWidth;
	}

	/**
	 * <p>Getter for the field <code>tertiaryDepth</code>.</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getTertiaryDepth() {
		return tertiaryDepth;
	}

	/**
	 * <p>Setter for the field <code>tertiaryDepth</code>.</p>
	 *
	 * @param tertiaryDepth a {@link java.lang.Float} object.
	 */
	public void setTertiaryDepth(Float tertiaryDepth) {
		this.tertiaryDepth = tertiaryDepth;
	}

	/**
	 * <p>Setter for the field <code>palletNumberOnGround</code>.</p>
	 *
	 * @param palletNumberOnGround a {@link java.lang.Integer} object.
	 */
	public void setPalletNumberOnGround(Integer palletNumberOnGround) {
		this.palletNumberOnGround = palletNumberOnGround;
	}

	/**
	 * <p>Getter for the field <code>palletBoxesPerLastLayer</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getPalletBoxesPerLastLayer() {
		return palletBoxesPerLastLayer;
	}

	/**
	 * <p>Setter for the field <code>palletBoxesPerLastLayer</code>.</p>
	 *
	 * @param palletBoxesPerLastLayer a {@link java.lang.Integer} object.
	 */
	public void setPalletBoxesPerLastLayer(Integer palletBoxesPerLastLayer) {
		this.palletBoxesPerLastLayer = palletBoxesPerLastLayer;
	}

	/**
	 * <p>Getter for the field <code>palletStackingMaxWeight</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getPalletStackingMaxWeight() {
		return palletStackingMaxWeight;
	}

	/**
	 * <p>Setter for the field <code>palletStackingMaxWeight</code>.</p>
	 *
	 * @param palletStackingMaxWeight a {@link java.lang.Double} object.
	 */
	public void setPalletStackingMaxWeight(Double palletStackingMaxWeight) {
		this.palletStackingMaxWeight = palletStackingMaxWeight;
	}

	/**
	 * <p>Getter for the field <code>palletTypeCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPalletTypeCode() {
		return palletTypeCode;
	}

	/**
	 * <p>Setter for the field <code>palletTypeCode</code>.</p>
	 *
	 * @param palletTypeCode a {@link java.lang.String} object.
	 */
	public void setPalletTypeCode(String palletTypeCode) {
		this.palletTypeCode = palletTypeCode;
	}

	/**
	 * <p>isManualPrimary.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isManualPrimary() {
		return isManualPrimary;
	}

	/**
	 * <p>setManualPrimary.</p>
	 *
	 * @param isManualPrimary a boolean.
	 */
	public void setManualPrimary(boolean isManualPrimary) {
		this.isManualPrimary = isManualPrimary;
	}

	/**
	 * <p>isManualTertiary.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isManualTertiary() {
		return isManualTertiary;
	}

	/**
	 * <p>setManualTertiary.</p>
	 *
	 * @param isManualTertiary a boolean.
	 */
	public void setManualTertiary(boolean isManualTertiary) {
		this.isManualTertiary = isManualTertiary;
	}

	
	public boolean isManualPalletInformations() {
		return isManualPalletInformations;
	}

	public void setManualPalletInformations(boolean isManualPalletInformations) {
		this.isManualPalletInformations = isManualPalletInformations;
	}

	/**
	 * <p>isManualSecondary.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isManualSecondary() {
		return isManualSecondary;
	}

	/**
	 * <p>setManualSecondary.</p>
	 *
	 * @param isManualSecondary a boolean.
	 */
	public void setManualSecondary(boolean isManualSecondary) {
		this.isManualSecondary = isManualSecondary;
	}
	
	

	/**
	 * <p>Getter for the field <code>platformTermsAndConditionsCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPlatformTermsAndConditionsCode() {
		return platformTermsAndConditionsCode;
	}

	/**
	 * <p>Setter for the field <code>platformTermsAndConditionsCode</code>.</p>
	 *
	 * @param platformTermsAndConditionsCode a {@link java.lang.String} object.
	 */
	public void setPlatformTermsAndConditionsCode(String platformTermsAndConditionsCode) {
		this.platformTermsAndConditionsCode = platformTermsAndConditionsCode;
	}

	/**
	 * <p>addTarePrimary.</p>
	 *
	 * @param value a {@link java.math.BigDecimal} object.
	 */
	public void addTarePrimary(BigDecimal value) {
		if (getTarePrimary() != null) {
			setTarePrimary(getTarePrimary().add(value));
		} else {
			setTarePrimary(value);
		}

	}

	/**
	 * <p>addTareTertiary.</p>
	 *
	 * @param value a {@link java.math.BigDecimal} object.
	 */
	public void addTareTertiary(BigDecimal value) {
		if (getTareTertiary() != null) {
			setTareTertiary(getTareTertiary().add(value));
		} else {
			setTareTertiary(value);
		}

	}

	/**
	 * <p>addTareSecondary.</p>
	 *
	 * @param value a {@link java.math.BigDecimal} object.
	 */
	public void addTareSecondary(BigDecimal value) {
		if (getTareSecondary() != null) {
			setTareSecondary(getTareSecondary().add(value));
		} else {
			setTareSecondary(value);
		}

	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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
