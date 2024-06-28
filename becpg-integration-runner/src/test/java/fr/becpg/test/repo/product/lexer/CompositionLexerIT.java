/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
package fr.becpg.test.repo.product.lexer;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.lexer.CompositionLexer;
import fr.becpg.test.PLMBaseTestCase;

public class CompositionLexerIT extends PLMBaseTestCase {

	protected static Log logger = LogFactory.getLog(CompositionLexerIT.class);

	protected NodeRef rawMaterial1NodeRef;

	protected NodeRef rawMaterial2NodeRef;

	protected NodeRef rawMaterial3NodeRef;

	protected NodeRef rawMaterial5NodeRef;

	@Autowired
	ProductService productService;

	Date startTime;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		startTime = new Date();

		// create RM and lSF
		inWriteTx(() -> {

			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("lexer material 1" + startTime.getTime());

			alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1);

			rawMaterial1NodeRef = rawMaterial1.getNodeRef();

			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("lexer material 2" + startTime.getTime());

			alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2);

			rawMaterial2NodeRef = rawMaterial2.getNodeRef();

			RawMaterialData rawMaterial3 = new RawMaterialData();
			rawMaterial3.setName("lexer material 3" + startTime.getTime());

			alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial3);

			rawMaterial3NodeRef = rawMaterial3.getNodeRef();

			RawMaterialData rawMaterial5 = new RawMaterialData();
			rawMaterial5.setName("lexer material 5" + startTime.getTime());

			alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial5);

			rawMaterial5NodeRef = rawMaterial5.getNodeRef();

			return true;
		});
		// Wait for Solr

		waitForSolr();

	}

	@Test
	public void testCompositionLexer() throws Exception {

		inWriteTx(() -> {

			String recipe = "5 P Lexer material 1" + startTime.getTime() + "\n 12,9 gr Lexer material 2"
					+ startTime.getTime() + "\n 25.5005 g Lexer material 3" + startTime.getTime()
					+ "\n50 Lexer material 5" + startTime.getTime() + "";

			ProductData productData = productService.formulateText(recipe);

			org.junit.Assert.assertNotNull(productData);
			return null;

		});

		inWriteTx(() -> {
			String recipe = "5 P Lexer material 1" + startTime.getTime() + "\n 12,9 gr Lexer material 2"
					+ startTime.getTime() + "\n 25.5005 g Lexer material 3" + startTime.getTime()
					+ "\n50 Lexer material 5" + startTime.getTime() + "";

			logger.debug("Lex: " + recipe);

			List<CompoListDataItem> ret = CompositionLexer.lexMultiLine(recipe);

			int check = 0;
			for (CompoListDataItem item : ret) {

				logger.debug("Item: " + item.toString());

				if (item.getProduct().equals(rawMaterial1NodeRef) && item.getQtySubFormula().equals(5d)
						&& item.getCompoListUnit().equals(ProductUnit.P)) {
					check++;
				}
				if (item.getProduct().equals(rawMaterial2NodeRef) && item.getQtySubFormula().equals(12.9d)
						&& item.getCompoListUnit().equals(ProductUnit.g)) {
					check++;
				}
				if (item.getProduct().equals(rawMaterial3NodeRef) && item.getQtySubFormula().equals(25.5005d)
						&& item.getCompoListUnit().equals(ProductUnit.g)) {
					check++;
				}
				if (item.getProduct().equals(rawMaterial5NodeRef) && item.getQtySubFormula().equals(50d)
						&& item.getCompoListUnit().equals(ProductUnit.P)) {
					check++;
				}
			}

			org.junit.Assert.assertEquals(4, check);
			return true;
		});
	}

}
