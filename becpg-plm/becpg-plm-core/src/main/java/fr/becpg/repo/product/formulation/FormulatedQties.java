package fr.becpg.repo.product.formulation;

/**
 * <p>FormulatedQties class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FormulatedQties {
	
	Double qty;
	Double volume;
	Double netWeight;
	Double netQty;
	
	
	/**
	 * <p>Constructor for FormulatedQties.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @param volume a {@link java.lang.Double} object
	 * @param netQty a {@link java.lang.Double} object
	 * @param netWeight a {@link java.lang.Double} object
	 */
	public FormulatedQties(Double qty, Double volume, Double netQty, Double netWeight) {
		super();
		this.qty = qty;
		this.volume = volume;
		this.netWeight = netWeight;
		this.netQty = netQty;
	}


	/**
	 * <p>getQtyUsed.</p>
	 *
	 * @param formulateInVol a boolean
	 * @return a {@link java.lang.Double} object
	 */
	public Double getQtyUsed(boolean formulateInVol) {
		return  formulateInVol ? volume : qty;
	}


	/**
	 * <p>Getter for the field <code>netQty</code>.</p>
	 *
	 * @param forceWeight a boolean
	 * @return a {@link java.lang.Double} object
	 */
	public Double getNetQty(boolean forceWeight) {
		return forceWeight ? netWeight : netQty;
	}


	/**
	 * <p>isNotNull.</p>
	 *
	 * @return a boolean
	 */
	public boolean isNotNull() {
		return qty!=null;
	}
	

}
