/*
 *
 */
package fr.becpg.repo.product.data.constraints;


/**
 * The Enum ProductUnit.
 *
 * @author querephi
 * @version $Id: $Id
 */
public enum ProductUnit {

	kg, g, mg, µg, lb, oz , L, cL, mL, fl_oz,cp,gal, in, ft, mm, cm , m, m2, m3, h, P, PP, Box, Perc;
	
	


	/**
	 * <p>getUnit.</p>
	 *
	 * @param productUnit a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 */
	public static ProductUnit getUnit(String productUnit) {
		return ((productUnit != null) && !productUnit.isEmpty()) ? ProductUnit.valueOf(productUnit) : ProductUnit.kg;
	}
	
	/**
	 * <p>isVolume.</p>
	 *
	 * @return a boolean.
	 */
	public  boolean isVolume() {
		return L.equals(this) || mL.equals(this) || cL.equals(this) || isGal();
	}

	/**
	 * <p>isWeight.</p>
	 *
	 * @return a boolean.
	 */
	public  boolean isWeight() {
		return kg.equals(this) || g.equals(this) || mg.equals(this)  || µg.equals(this) || isLb();
	}

	/**
	 * <p>isP.</p>
	 *
	 * @return a boolean.
	 */
	public  boolean isP() {
		return P.equals(this) || m2.equals(this)|| m3.equals(this) || isM();
	}

	
	/**
	 * <p>isM.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isM() {
		return m.equals(this)  || in.equals(this)|| ft.equals(this) || mm.equals(this);
	}

	/**
	 * <p>isLb.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isLb() {
		return lb.equals(this) || oz.equals(this);
	}
	
	/**
	 * <p>isGal.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isGal() {
		return fl_oz.equals(this) || cp.equals(this) || gal.equals(this);
	}
	

	/**
	 * <p>isPerc.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPerc() {
		return Perc.equals(this);
	}
	
	/**
	 * Convert factor to Kg L or M
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public  Double getUnitFactor() {
		if (this.equals(ProductUnit.mL) || this.equals(ProductUnit.g)) {
			return  1000d;
		} else if (this.equals(ProductUnit.cL)) {
			return 100d;
		}  else if (this.equals(ProductUnit.cm)) {
			return 100d;
		} else if (this.equals(ProductUnit.mg)) {
			return 1000000d;
		} else if (this.equals(ProductUnit.µg)) {
			return 1000000000d;
		} else if (this.equals(ProductUnit.lb)) {
			return 2.204622622d;
		} else if (this.equals(ProductUnit.oz)) {
			return 35.27396195d;
		} else if (this.equals(ProductUnit.fl_oz)) {
			return 33.814d;
		} else if (this.equals(ProductUnit.cp)) {
			return 4.16667d;
		} else if (this.equals(ProductUnit.gal)) {
			return 0.264172d;
		} else if (this.equals(ProductUnit.mm)) {
			return 1000d;
		}  else if (this.equals(ProductUnit.ft)) {
			return 3.28084d;
		}  else if (this.equals(ProductUnit.in)) {
			return 39.3701d;
		}  
		
		
		return 1d;
	}
	

	/**
	 * <p>kgToLb.</p>
	 *
	 * @param kgValue a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double kgToLb(Double kgValue) {
		if(kgValue !=null) {
			return kgValue * 2.204622622d;
		}
		return null;
	}
	
	/**
	 * <p>lbToKg.</p>
	 *
	 * @param kgValue a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double lbToKg(Double kgValue) {
		if(kgValue !=null) {
			return kgValue / 2.204622622d;
		}
		return null;
	}
	

	/**
	 * <p>LToGal.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double LToGal(Double value) {
		if(value !=null) {
			return value * 0.264172d;
		}
		return null;
	}
	
	/**
	 * <p>GalToL.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double GalToL(Double value) {
		if(value !=null) {
			return value / 0.264172d;
		}
		return null;
	}
	

	/**
	 * <p>lbToOz.</p>
	 *
	 * @param lbValue a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double lbToOz(Double lbValue) {
		if(lbValue!=null) {
			return lbValue * 16d;
		}	
		return null;
	}
	
	
	/**
	 * <p>getMainUnit.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 */
	public ProductUnit getMainUnit() {
		if (this.equals(ProductUnit.lb) || this.equals(ProductUnit.oz)) {
			return ProductUnit.lb;
		} else if (this.equals(ProductUnit.kg) || this.equals(ProductUnit.g) || this.equals(ProductUnit.mg) || this.equals(ProductUnit.µg) ) {
			return ProductUnit.kg;
		} else if (this.equals(ProductUnit.L) || this.equals(ProductUnit.cL) || this.equals(ProductUnit.mL)) {
			return ProductUnit.L;
		} if (this.equals(ProductUnit.fl_oz) || this.equals(ProductUnit.cp) || this.equals(ProductUnit.gal)) {
			return ProductUnit.gal;
		}  if (this.equals(ProductUnit.mm) || this.equals(ProductUnit.m) ) {
			return ProductUnit.m;
		}  if (this.equals(ProductUnit.in) || this.equals(ProductUnit.ft) ) {
			return ProductUnit.ft;
		} 
		return this;
	}
	

	/**
	 * <p>extractUnit.</p>
	 *
	 * @param unit a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 */
	public static ProductUnit extractUnit(String unit) {

			switch (unit.trim()) {
			case "kg":
				return kg;
			case "g":
			case "gr":
				return g;
			case "l":
				return L;
			case "ml":
				return mL;
			case "cl":
				return cL;
			case "mg":
				return mg;
			case "µg":	
				return µg;
			case "p":
				return P;
			case "m":
				return m;
			case "m2":
				return m2;
			case "perc":
			case "%":
				return Perc;
			default:
				return P;
			}
	}


}
