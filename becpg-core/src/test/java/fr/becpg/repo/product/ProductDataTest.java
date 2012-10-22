package fr.becpg.repo.product;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

public class ProductDataTest extends TestCase {
	
	Date now = new Date();
	Date nowplus1h = new Date(now.getTime()+1000*60*60);
	Date nowplus2h = new Date(now.getTime()+2000*60*60);
	Date nowminus1h = new Date(now.getTime()-1000*60*60);
	Date nowminus2h = new Date(now.getTime()-1000*60*60);
	
	@Test
	public void testEffectiveFilter(){
		
		ProductData productData = new ProductData();
		
		//Test NPE
		Assert.assertFalse(productData.hasCompoListEl(EffectiveFilters.ALL));
		
		List<CompoListDataItem> compoList = new LinkedList<CompoListDataItem>();
		
		
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
		
		productData.setCompoList(compoList);
		
		Assert.assertTrue(productData.hasCompoListEl(EffectiveFilters.ALL));
		
		Assert.assertEquals(5, productData.getCompoList().size());
		
		Assert.assertEquals(5, productData.getCompoList(EffectiveFilters.ALL).size());
		
		Assert.assertEquals(3, productData.getCompoList(EffectiveFilters.EFFECTIVE).size());
		
		Assert.assertEquals(4, productData.getCompoList(EffectiveFilters.FUTUR).size());
		
		productData.setStartEffectivity(nowminus2h);
		productData.setEndEffectivity(now);
		
		compoList.remove(0);
		
		Assert.assertEquals(3, productData.getCompoList(EffectiveFilters.EFFECTIVE).size());
		
		Assert.assertEquals(4, productData.getCompoList(EffectiveFilters.ALL).size());
		
		Assert.assertEquals(4, productData.getCompoList(EffectiveFilters.FUTUR).size());
		
		productData.setStartEffectivity(nowminus2h);
		productData.setEndEffectivity(nowminus1h);
		
		Assert.assertTrue(productData.hasCompoListEl(EffectiveFilters.ALL));
		Assert.assertTrue(productData.hasCompoListEl(EffectiveFilters.EFFECTIVE));
		
	}
	

}
