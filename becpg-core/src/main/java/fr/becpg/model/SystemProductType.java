/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

import java.security.InvalidParameterException;

import org.alfresco.service.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * Product types.
 *
 * @author querephi
 */
public enum SystemProductType {
	
	/** The Unknown. */
	Unknown,
	
	/** The Raw material. */
	RawMaterial, 
	
	/** The Packaging material. */
	PackagingMaterial,
	
	/** The Semi finished product. */
	SemiFinishedProduct,	
	
	/** The Finished product. */
	FinishedProduct,
	
	/** The Local semi finished product. */
	LocalSemiFinishedProduct,
	
	/** The Packaging kit. */
	PackagingKit;
	
	/**
	 * Gets the product type.
	 *
	 * @param productType the product type
	 * @return the product type
	 */
	public static SystemProductType valueOf(QName productType){		
		
		SystemProductType systemProductType = SystemProductType.Unknown;
	
		//Check
		if(productType == null){
			throw new InvalidParameterException("null value is not allowed for productType.");
		}
		
		if(productType.getLocalName().equals(BeCPGModel.TYPE_FINISHEDPRODUCT.getLocalName())){
			systemProductType = SystemProductType.FinishedProduct;
		}
		else if(productType.getLocalName().equals(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT.getLocalName())){
			systemProductType = SystemProductType.LocalSemiFinishedProduct;
		}
		else if(productType.getLocalName().equals(BeCPGModel.TYPE_PACKAGINGKIT.getLocalName())){
			systemProductType = SystemProductType.PackagingKit;
		}
		else if(productType.getLocalName().equals(BeCPGModel.TYPE_PACKAGINGMATERIAL.getLocalName())){
			systemProductType = SystemProductType.PackagingMaterial;
		}
		else if(productType.getLocalName().equals(BeCPGModel.TYPE_RAWMATERIAL.getLocalName())){
			systemProductType = SystemProductType.RawMaterial;
		}
		else if(productType.getLocalName().equals(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT.getLocalName())){
			systemProductType = SystemProductType.SemiFinishedProduct;
		}	
		else{
			//must return Unknown, otherwise there is no way to verify an node is a product. We would have to test every type (RawMarterial,...)
			//throw new InvalidParameterException("this value is not allowed for productType. productType" + productType);
		}
					
		return systemProductType;
	}
}
