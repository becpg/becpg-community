/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.product;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

public class ProductDataTest {
	
	final Date now = new Date();
	final Date nowplus1h = new Date(now.getTime()+1000*60*60);
	final Date nowplus2h = new Date(now.getTime()+2000*60*60);
	final Date nowminus1h = new Date(now.getTime()-1000*60*60);
	final Date nowminus2h = new Date(now.getTime()-1000*60*60);
	
	@Test
	public void testEffectiveFilter(){
		
		ProductData productData = new ProductData();
		
		//Test NPE
		Assert.assertFalse(productData.hasCompoListEl());
		
		List<CompoListDataItem> compoList = new LinkedList<>();
		
		
		CompoListDataItem item = new CompoListDataItem();
		compoList.add(item);
		//active
	    item = new CompoListDataItem();
		item.setStartEffectivity(now);
		compoList.add(item);
		//active
		item = new CompoListDataItem();
		item.setStartEffectivity(now);
		item.setEndEffectivity(nowplus1h);
		compoList.add(item);
		//passed
		item = new CompoListDataItem();
		item.setStartEffectivity(nowminus1h);
		item.setEndEffectivity(now);
		compoList.add(item);
		//futur
		item = new CompoListDataItem();
		item.setStartEffectivity(nowplus1h);
		item.setEndEffectivity(nowplus2h);
		compoList.add(item);
		
		productData.getCompoListView().setCompoList(compoList);
		
		Assert.assertTrue(productData.hasCompoListEl());
		
		Assert.assertEquals(5, productData.getCompoListView().getCompoList().size());
		
		Assert.assertEquals(5, productData.getCompoList().size());
		
		Assert.assertEquals(3, productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).size());
		
		Assert.assertEquals(4, productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.FUTUR)).size());
		
		productData.setStartEffectivity(nowminus2h);
		productData.setEndEffectivity(now);
		
		compoList.remove(0);
		
		Assert.assertEquals(3, productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).size());
		
		Assert.assertEquals(4, productData.getCompoList().size());
		
		Assert.assertEquals(4, productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.FUTUR)).size());
		
		productData.setStartEffectivity(nowminus2h);
		productData.setEndEffectivity(nowminus1h);
		
		Assert.assertTrue(productData.hasCompoListEl());
		Assert.assertTrue(productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)));
		
	}
	

}
