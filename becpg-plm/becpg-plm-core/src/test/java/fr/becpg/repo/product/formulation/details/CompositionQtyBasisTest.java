package fr.becpg.repo.product.formulation.details;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

/**
 * Unit tests of the positive lines quantity basis used by the ingredient details (see #37100).
 */
public class CompositionQtyBasisTest {

	private static final double DELTA = 1e-9;

	private static CompoListDataItem line(double qty) {
		return CompoListDataItem.build().withQtyUsed(qty).withQty(qty);
	}

	@Test
	public void keepsTheBasisWithoutNegativeLine() {
		List<CompoListDataItem> compoList = List.of(line(60d), line(40d));

		assertEquals(1d, CompositionQtyBasis.computePositiveLinesFactor(compoList), DELTA);
	}

	@Test
	public void relatesTheSignedBasisToThePositiveLines() {
		List<CompoListDataItem> compoList = List.of(line(100d), line(-6d));

		assertEquals(100d / 94d, CompositionQtyBasis.computePositiveLinesFactor(compoList), DELTA);
	}

	@Test
	public void appliesTheLineYield() {
		List<CompoListDataItem> compoList = List.of(line(100d).withYieldPerc(50d), line(-10d));

		assertEquals(50d / 40d, CompositionQtyBasis.computePositiveLinesFactor(compoList), DELTA);
	}

	@Test
	public void ignoresOmittedLines() {
		List<CompoListDataItem> compoList = List.of(line(100d), line(-6d).withDeclarationType(DeclarationType.Omit));

		assertEquals(1d, CompositionQtyBasis.computePositiveLinesFactor(compoList), DELTA);
	}

	@Test
	public void ignoresLinesUnderAnOmittedParent() {
		CompoListDataItem omittedParent = line(10d).withDeclarationType(DeclarationType.Omit);
		List<CompoListDataItem> compoList = List.of(line(100d), omittedParent, line(-6d).withParent(omittedParent));

		assertEquals(1d, CompositionQtyBasis.computePositiveLinesFactor(compoList), DELTA);
	}

	@Test
	public void countsLeafLinesOnly() {
		CompoListDataItem parent = line(50d);
		List<CompoListDataItem> compoList = List.of(parent, line(60d).withParent(parent), line(-10d).withParent(parent));

		assertEquals(60d / 50d, CompositionQtyBasis.computePositiveLinesFactor(compoList), DELTA);
	}

	@Test
	public void keepsTheBasisWhenTheSignedQuantityIsNotPositive() {
		List<CompoListDataItem> compoList = List.of(line(10d), line(-10d));

		assertEquals(1d, CompositionQtyBasis.computePositiveLinesFactor(compoList), DELTA);
	}

	@Test
	public void keepsTheBasisOfAnEmptyComposition() {
		assertEquals(1d, CompositionQtyBasis.computePositiveLinesFactor(Collections.emptyList()), DELTA);
		assertEquals(1d, CompositionQtyBasis.computePositiveLinesFactor(null), DELTA);
	}

	@Test
	public void detailsPositiveLinesOnly() {
		assertTrue(CompositionQtyBasis.hasPositiveQty(line(6d)));
		assertFalse(CompositionQtyBasis.hasPositiveQty(line(-6d)));
		assertFalse(CompositionQtyBasis.hasPositiveQty(line(0d)));
		assertFalse(CompositionQtyBasis.hasPositiveQty(CompoListDataItem.build()));
	}
}
