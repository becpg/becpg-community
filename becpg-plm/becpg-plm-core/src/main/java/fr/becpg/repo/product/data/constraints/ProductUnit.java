/*
 *
 */
package fr.becpg.repo.product.data.constraints;


/**
 * The Enum ProductUnit.
 *
 * @author querephi
 */
public enum ProductUnit {

	kg, g, mg, lb, oz , L, cL, mL, fl_oz,cp,gal, in, ft, mm, m, m2, m3, h, P, PP, Box, Perc;
	
	


	public static ProductUnit getUnit(String productUnit) {
		return ((productUnit != null) && !productUnit.isEmpty()) ? ProductUnit.valueOf(productUnit) : ProductUnit.kg;
	}
	
	public  boolean isVolume() {
		return L.equals(this) || mL.equals(this) || cL.equals(this) || isGal();
	}

	public  boolean isWeight() {
		return kg.equals(this) || g.equals(this) || mg.equals(this) || isLb();
	}

	public  boolean isP() {
		return P.equals(this) || m2.equals(this)|| m.equals(this)  || in.equals(this)|| ft.equals(this) || mm.equals(this) || m3.equals(this);
	}


	public boolean isLb() {
		return lb.equals(this) || oz.equals(this);
	}
	
	public boolean isGal() {
		return fl_oz.equals(this) || cp.equals(this) || gal.equals(this);
	}
	
	
   /**
    * Convert factor to Kg or L
    * @return
    */
	public  Double getUnitFactor() {
		if (this.equals(ProductUnit.mL) || this.equals(ProductUnit.g)) {
			return  1000d;
		} else if (this.equals(ProductUnit.cL)) {
			return 100d;
		} else if (this.equals(ProductUnit.mg)) {
			return 1000000d;
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
		}
		return 1d;
	}
	

	public static Double kgToLb(Double kgValue) {
		if(kgValue !=null) {
			return kgValue * 2.204622622d;
		}
		return null;
	}
	
	public static Double lbToKg(Double kgValue) {
		if(kgValue !=null) {
			return kgValue / 2.204622622d;
		}
		return null;
	}
	

	public static Double LToGal(Double value) {
		if(value !=null) {
			return value * 0.264172d;
		}
		return null;
	}
	
	public static Double GalToL(Double value) {
		if(value !=null) {
			return value / 0.264172d;
		}
		return null;
	}
	

	public static Double lbToOz(Double lbValue) {
		if(lbValue!=null) {
			return lbValue * 16d;
		}	
		return null;
	}
	
	
	public ProductUnit getMainUnit() {
		if (this.equals(ProductUnit.lb) || this.equals(ProductUnit.oz)) {
			return ProductUnit.lb;
		} else if (this.equals(ProductUnit.kg) || this.equals(ProductUnit.g) || this.equals(ProductUnit.mg)) {
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
				return mL;	
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
