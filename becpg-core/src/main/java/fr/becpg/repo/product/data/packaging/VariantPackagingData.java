package fr.becpg.repo.product.data.packaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

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
	
	private String packagingTypeCode;
	private String packagingTermsAndConditionsCode;
	private String secondaryPackagingTypeCode;
	private String tertiaryPackagingTypeCode;

	
	private Double height;
	private Double width;
	private Double depth;

	private Double secondaryHeight;
	private Double secondaryWidth;
	private Double secondaryDepth;

	private Double tertiaryWidth;
	private Double tertiaryDepth;
	
	private Double volume;
	private Double secondaryVolume;
	private Double tertiaryVolume;

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
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getHeight() {
		return height;
	}

	/**
	 * <p>Setter for the field <code>height</code>.</p>
	 *
	 * @param height a {@link java.lang.Double} object.
	 */
	public void setHeight(Double height) {
		this.height = height;
	}

	/**
	 * <p>Getter for the field <code>width</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getWidth() {
		return width;
	}

	/**
	 * <p>Setter for the field <code>width</code>.</p>
	 *
	 * @param width a {@link java.lang.Double} object.
	 */
	public void setWidth(Double width) {
		this.width = width;
	}

	/**
	 * <p>Getter for the field <code>depth</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getDepth() {
		return depth;
	}

	/**
	 * <p>Setter for the field <code>depth</code>.</p>
	 *
	 * @param depth a {@link java.lang.Double} object.
	 */
	public void setDepth(Double depth) {
		this.depth = depth;
	}

	/**
	 * <p>Getter for the field <code>secondaryHeight</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getSecondaryHeight() {
		return secondaryHeight;
	}

	/**
	 * <p>Setter for the field <code>secondaryHeight</code>.</p>
	 *
	 * @param secondaryHeight a {@link java.lang.Double} object.
	 */
	public void setSecondaryHeight(Double secondaryHeight) {
		this.secondaryHeight = secondaryHeight;
	}

	/**
	 * <p>Getter for the field <code>secondaryWidth</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getSecondaryWidth() {
		return secondaryWidth;
	}

	/**
	 * <p>Setter for the field <code>secondaryWidth</code>.</p>
	 *
	 * @param secondaryWidth a {@link java.lang.Double} object.
	 */
	public void setSecondaryWidth(Double secondaryWidth) {
		this.secondaryWidth = secondaryWidth;
	}

	/**
	 * <p>Getter for the field <code>secondaryDepth</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getSecondaryDepth() {
		return secondaryDepth;
	}

	/**
	 * <p>Setter for the field <code>secondaryDepth</code>.</p>
	 *
	 * @param secondaryDepth a {@link java.lang.Double} object.
	 */
	public void setSecondaryDepth(Double secondaryDepth) {
		this.secondaryDepth = secondaryDepth;
	}

	/**
	 * <p>Getter for the field <code>tertiaryWidth</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getTertiaryWidth() {
		return tertiaryWidth;
	}

	/**
	 * <p>Setter for the field <code>tertiaryWidth</code>.</p>
	 *
	 * @param tertiaryWidth a {@link java.lang.Double} object.
	 */
	public void setTertiaryWidth(Double tertiaryWidth) {
		this.tertiaryWidth = tertiaryWidth;
	}

	/**
	 * <p>Getter for the field <code>tertiaryDepth</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getTertiaryDepth() {
		return tertiaryDepth;
	}

	/**
	 * <p>Setter for the field <code>tertiaryDepth</code>.</p>
	 *
	 * @param tertiaryDepth a {@link java.lang.Double} object.
	 */
	public void setTertiaryDepth(Double tertiaryDepth) {
		this.tertiaryDepth = tertiaryDepth;
	}
	
	/**
	 * <p>Getter for the field <code>volume</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getVolume() {
		return volume;
	}

	/**
	 * <p>Setter for the field <code>volume</code>.</p>
	 *
	 * @param volume a {@link java.lang.Double} object.
	 */
	public void setVolume(Double volume) {
		this.volume = volume;
	}
	
	/**
	 * <p>Getter for the field <code>secondaryVolume</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getSecondaryVolume() {
		return secondaryVolume;
	}

	/**
	 * <p>Setter for the field <code>secondaryVolume</code>.</p>
	 *
	 * @param secondaryVolume a {@link java.lang.Double} object.
	 */
	public void setSecondaryVolume(Double secondaryVolume) {
		this.secondaryVolume = secondaryVolume;
	}
	
	/**
	 * <p>Getter for the field <code>tertiaryVolume</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getTertiaryVolume() {
		return tertiaryVolume;
	}

	/**
	 * <p>Setter for the field <code>tertiaryVolume</code>.</p>
	 *
	 * @param tertiaryVolume a {@link java.lang.Double} object.
	 */
	public void setTertiaryVolume(Double tertiaryVolume) {
		this.tertiaryVolume = tertiaryVolume;
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

	
	/**
	 * <p>isManualPalletInformations.</p>
	 *
	 * @return a boolean
	 */
	public boolean isManualPalletInformations() {
		return isManualPalletInformations;
	}

	/**
	 * <p>setManualPalletInformations.</p>
	 *
	 * @param isManualPalletInformations a boolean
	 */
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
	
	

	/**
	 * <p>Getter for the field <code>packagingTypeCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getPackagingTypeCode() {
		return packagingTypeCode;
	}

	/**
	 * <p>Setter for the field <code>packagingTypeCode</code>.</p>
	 *
	 * @param packagingTypeCode a {@link java.lang.String} object
	 */
	public void setPackagingTypeCode(String packagingTypeCode) {
		this.packagingTypeCode = packagingTypeCode;
	}

	/**
	 * <p>Getter for the field <code>packagingTermsAndConditionsCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getPackagingTermsAndConditionsCode() {
		return packagingTermsAndConditionsCode;
	}

	/**
	 * <p>Setter for the field <code>packagingTermsAndConditionsCode</code>.</p>
	 *
	 * @param packagingTermsAndConditionsCode a {@link java.lang.String} object
	 */
	public void setPackagingTermsAndConditionsCode(String packagingTermsAndConditionsCode) {
		this.packagingTermsAndConditionsCode = packagingTermsAndConditionsCode;
	}

	/**
	 * <p>Getter for the field <code>secondaryPackagingTypeCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getSecondaryPackagingTypeCode() {
		return secondaryPackagingTypeCode;
	}

	/**
	 * <p>Setter for the field <code>secondaryPackagingTypeCode</code>.</p>
	 *
	 * @param secondaryPackagingTypeCode a {@link java.lang.String} object
	 */
	public void setSecondaryPackagingTypeCode(String secondaryPackagingTypeCode) {
		this.secondaryPackagingTypeCode = secondaryPackagingTypeCode;
	}
	
	/**
	 * <p>Getter for the field <code>tertiaryPackagingTypeCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getTertiaryPackagingTypeCode() {
		return tertiaryPackagingTypeCode;
	}

	/**
	 * <p>Setter for the field <code>tertiaryPackagingTypeCode</code>.</p>
	 *
	 * @param secondaryPackagingTypeCode a {@link java.lang.String} object
	 */
	public void setTertiaryPackagingTypeCode(String tertiaryPackagingTypeCode) {
		this.tertiaryPackagingTypeCode = tertiaryPackagingTypeCode;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "VariantPackagingData [tarePrimary=" + tarePrimary + ", tareSecondary=" + tareSecondary + ", tareTertiary=" + tareTertiary
				+ ", productPerBoxes=" + productPerBoxes + ", boxesPerPallet=" + boxesPerPallet + ", palletLayers=" + palletLayers
				+ ", palletBoxesPerLayer=" + palletBoxesPerLayer + ", palletHeight=" + palletHeight + ", palletNumberOnGround=" + palletNumberOnGround
				+ ", palletBoxesPerLastLayer=" + palletBoxesPerLastLayer + ", palletStackingMaxWeight=" + palletStackingMaxWeight
				+ ", palletTypeCode=" + palletTypeCode + ", platformTermsAndConditionsCode=" + platformTermsAndConditionsCode + ", packagingTypeCode="
				+ packagingTypeCode + ", packagingTermsAndConditionsCode=" + packagingTermsAndConditionsCode + ", secondaryPackagingTypeCode="
				+ secondaryPackagingTypeCode + ", height=" + height + ", width=" + width + ", depth=" + depth + ", secondaryHeight=" + secondaryHeight
				+ ", secondaryWidth=" + secondaryWidth + ", secondaryDepth=" + secondaryDepth + ", tertiaryPackagingTypeCode=" + tertiaryPackagingTypeCode 
				+ ", tertiaryWidth=" + tertiaryWidth + ", tertiaryDepth=" + tertiaryDepth + ", isManualPrimary=" + isManualPrimary + ", isManualTertiary=" 
				+ isManualTertiary + ", isManualSecondary=" + isManualSecondary + ", isManualPalletInformations=" + isManualPalletInformations + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(boxesPerPallet, depth, height, isManualPalletInformations, isManualPrimary, isManualSecondary, isManualTertiary,
				packagingTermsAndConditionsCode, packagingTypeCode, palletBoxesPerLastLayer, palletBoxesPerLayer, palletHeight, palletLayers,
				palletNumberOnGround, palletStackingMaxWeight, palletTypeCode, platformTermsAndConditionsCode, productPerBoxes, secondaryDepth,
				secondaryHeight, secondaryPackagingTypeCode, secondaryWidth, tarePrimary, tareSecondary, tareTertiary, tertiaryPackagingTypeCode,
				tertiaryDepth, tertiaryWidth, width);
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
		VariantPackagingData other = (VariantPackagingData) obj;
		return Objects.equals(boxesPerPallet, other.boxesPerPallet) && Objects.equals(depth, other.depth) && Objects.equals(height, other.height)
				&& isManualPalletInformations == other.isManualPalletInformations && isManualPrimary == other.isManualPrimary
				&& isManualSecondary == other.isManualSecondary && isManualTertiary == other.isManualTertiary
				&& Objects.equals(packagingTermsAndConditionsCode, other.packagingTermsAndConditionsCode)
				&& Objects.equals(packagingTypeCode, other.packagingTypeCode)
				&& Objects.equals(palletBoxesPerLastLayer, other.palletBoxesPerLastLayer)
				&& Objects.equals(palletBoxesPerLayer, other.palletBoxesPerLayer) && Objects.equals(palletHeight, other.palletHeight)
				&& Objects.equals(palletLayers, other.palletLayers) && Objects.equals(palletNumberOnGround, other.palletNumberOnGround)
				&& Objects.equals(palletStackingMaxWeight, other.palletStackingMaxWeight) && Objects.equals(palletTypeCode, other.palletTypeCode)
				&& Objects.equals(platformTermsAndConditionsCode, other.platformTermsAndConditionsCode)
				&& Objects.equals(productPerBoxes, other.productPerBoxes) && Objects.equals(secondaryDepth, other.secondaryDepth)
				&& Objects.equals(secondaryHeight, other.secondaryHeight)
				&& Objects.equals(secondaryPackagingTypeCode, other.secondaryPackagingTypeCode)
				&& Objects.equals(tertiaryPackagingTypeCode, other.tertiaryPackagingTypeCode)
				&& Objects.equals(secondaryWidth, other.secondaryWidth) && Objects.equals(tarePrimary, other.tarePrimary)
				&& Objects.equals(tareSecondary, other.tareSecondary) && Objects.equals(tareTertiary, other.tareTertiary)
				&& Objects.equals(tertiaryDepth, other.tertiaryDepth) && Objects.equals(tertiaryWidth, other.tertiaryWidth)
				&& Objects.equals(width, other.width);
	}

}
