package fr.becpg.repo.product.formulation;

public class FormulatedQties {
	
	Double qty;
	Double volume;
	Double netWeight;
	Double netQty;
	
	
	public FormulatedQties(Double qty, Double volume, Double netWeight, Double netQty) {
		super();
		this.qty = qty;
		this.volume = volume;
		this.netWeight = netWeight;
		this.netQty = netQty;
	}


	public Double getQtyUsed(boolean formulateInVol) {
		return  formulateInVol ? volume : qty;
	}


	public Double getNetQty(boolean forceWeight) {
		return forceWeight ? netWeight : netQty;
	}


	public boolean isNotNull() {
		return qty!=null;
	}
	

}
