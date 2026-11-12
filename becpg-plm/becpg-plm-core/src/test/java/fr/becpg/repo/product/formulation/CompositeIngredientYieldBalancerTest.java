package fr.becpg.repo.product.formulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Test;

import fr.becpg.repo.product.data.productList.IngListDataItem;

/**
 * #34702 : the evaporation rate of a composite ingredient prevails over those of its sub-ingredients, which keep
 * their declared proportions once it evaporates. Under a composite without rate, each sub-ingredient keeps its own
 * loss and the composite loses the sum of them.
 */
public class CompositeIngredientYieldBalancerTest {

	private static final double DELTA = 0.0001d;

	private final List<IngListDataItem> ingList = new ArrayList<>();

	private final Set<IngListDataItem> withRate = Collections.newSetFromMap(new IdentityHashMap<>());

	@Test
	public void parentRatePrevailsOverSubIngredientRate() {
		IngListDataItem mozzarella = ing(40d, 40d, null, true);
		IngListDataItem milk = ing(40d, 40d, mozzarella, true);

		CompositeIngredientYieldBalancer balancer = balancer();

		assertTrue(balancer.isEvaporating(mozzarella));
		assertFalse(balancer.isEvaporating(milk));
	}

	@Test
	public void subIngredientRateAppliesWhenNoAncestorCarriesOne() {
		IngListDataItem puree = ing(40d, 40d, null, false);
		IngListDataItem tomato = ing(39.6d, 39.6d, puree, true);
		IngListDataItem oil = ing(0.4d, 0.4d, puree, false);

		CompositeIngredientYieldBalancer balancer = balancer();

		assertFalse(balancer.isEvaporating(puree));
		assertTrue(balancer.isEvaporating(tomato));
	}

	@Test
	public void grandParentRatePrevailsAtAnyDepth() {
		IngListDataItem sauce = ing(50d, 50d, null, true);
		IngListDataItem puree = ing(50d, 50d, sauce, false);
		IngListDataItem tomato = ing(50d, 50d, puree, true);

		CompositeIngredientYieldBalancer balancer = balancer();

		assertTrue(balancer.isEvaporating(sauce));
		assertFalse(balancer.isEvaporating(tomato));
	}

	@Test
	public void ingredientWithoutCharacteristicNeverEvaporates() {
		IngListDataItem orphan = ing(10d, 10d, null, true);
		orphan.setIng(null);

		assertFalse(balancer().isEvaporating(orphan));
	}

	@Test
	public void evaporatingCompositeKeepsItsWaterLossOnAggregation() {
		IngListDataItem mozzarella = ing(50d, 30d, null, true);
		ing(30d, 33.33d, mozzarella, false);
		ing(20d, 22.22d, mozzarella, false);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.aggregateParentQtyPercWithYield();

		assertEquals(30d, mozzarella.getQtyPercWithYield(), DELTA);
	}

	@Test
	public void evaporatingCompositeSpreadsItsLossOverSubIngredientsByDeclaredProportions() {
		IngListDataItem mozzarella = ing(50d, 30d, null, true);
		IngListDataItem milk = ing(30d, 33.33d, mozzarella, false);
		IngListDataItem salt = ing(20d, 22.22d, mozzarella, false);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.aggregateParentQtyPercWithYield();
		balancer.spreadEvaporationOverSubIngredients();

		assertEquals(18d, milk.getQtyPercWithYield(), DELTA);
		assertEquals(12d, salt.getQtyPercWithYield(), DELTA);
	}

	@Test
	public void subIngredientRateIsIgnoredInTheSpreadOfAnEvaporatingComposite() {
		IngListDataItem mozzarella = ing(50d, 30d, null, true);
		IngListDataItem milk = ing(30d, 33.33d, mozzarella, true);
		IngListDataItem salt = ing(20d, 22.22d, mozzarella, false);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.aggregateParentQtyPercWithYield();
		balancer.spreadEvaporationOverSubIngredients();

		assertEquals(18d, milk.getQtyPercWithYield(), DELTA);
		assertEquals(12d, salt.getQtyPercWithYield(), DELTA);
	}

	@Test
	public void spreadCascadesIntoCompositesBelowAnEvaporatingOne() {
		IngListDataItem sauce = ing(50d, 30d, null, true);
		IngListDataItem puree = ing(40d, 44.44d, sauce, false);
		IngListDataItem salt = ing(10d, 11.11d, sauce, false);
		IngListDataItem tomato = ing(30d, 33.33d, puree, false);
		IngListDataItem oil = ing(10d, 11.11d, puree, false);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.aggregateParentQtyPercWithYield();
		balancer.spreadEvaporationOverSubIngredients();

		assertEquals(24d, puree.getQtyPercWithYield(), DELTA);
		assertEquals(6d, salt.getQtyPercWithYield(), DELTA);
		assertEquals(18d, tomato.getQtyPercWithYield(), DELTA);
		assertEquals(6d, oil.getQtyPercWithYield(), DELTA);
	}

	@Test
	public void secondaryYieldColumnIsSpreadToo() {
		IngListDataItem mozzarella = ing(50d, 30d, null, true);
		IngListDataItem milk = ing(30d, 33.33d, mozzarella, false);
		IngListDataItem salt = ing(20d, 22.22d, mozzarella, false);
		mozzarella.setQtyPercWithSecondaryYield(25d);
		milk.setQtyPercWithSecondaryYield(40d);
		salt.setQtyPercWithSecondaryYield(26d);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.aggregateParentQtyPercWithYield();
		balancer.spreadEvaporationOverSubIngredients();

		assertEquals(15d, milk.getQtyPercWithSecondaryYield(), DELTA);
		assertEquals(10d, salt.getQtyPercWithSecondaryYield(), DELTA);
	}

	@Test
	public void compositeWithoutRateIsTheSumOfItsSubIngredients() {
		IngListDataItem puree = ing(40d, 42.1d, null, false);
		IngListDataItem tomato = ing(39.6d, 36d, puree, true);
		IngListDataItem oil = ing(0.4d, 0.42d, puree, false);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.aggregateParentQtyPercWithYield();

		assertEquals(36.42d, puree.getQtyPercWithYield(), DELTA);
	}

	@Test
	public void evaporatingSubIngredientKeepsItsOwnLossUnderACompositeWithoutRate() {
		IngListDataItem puree = ing(40d, 42.1d, null, false);
		IngListDataItem tomato = ing(30d, 26d, puree, true);
		IngListDataItem oil = ing(10d, 10.53d, puree, false);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.aggregateParentQtyPercWithYield();
		balancer.spreadEvaporationOverSubIngredients();

		assertEquals(26d, tomato.getQtyPercWithYield(), DELTA);
		assertEquals(10.53d, oil.getQtyPercWithYield(), DELTA);
		assertEquals(36.53d, puree.getQtyPercWithYield(), DELTA);
	}

	@Test
	public void partiallyDeclaredCompositeLosesTheWaterOfItsSubIngredients() {
		IngListDataItem puree = ing(40d, 40d / 0.95d, null, false);
		IngListDataItem tomato = ing(30d, 26d, puree, true);
		ing(0d, 0d, puree, false).setQtyPerc(null);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.deductEvaporationFromAncestors(valuesBeforeEvaporation(tomato, 28.5d), 0.95d, IngListDataItem::getQtyPercWithYield,
				IngListDataItem::setQtyPercWithYield);

		assertEquals((40d / 0.95d) - 4d, puree.getQtyPercWithYield(), DELTA);
	}

	@Test
	public void lossOfASubIngredientIsTakenOffEveryAncestor() {
		IngListDataItem sauce = ing(50d, 50d, null, false);
		IngListDataItem puree = ing(40d, 40d, sauce, false);
		IngListDataItem tomato = ing(30d, 25d, puree, true);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.deductEvaporationFromAncestors(valuesBeforeEvaporation(tomato, 30d), 1d, IngListDataItem::getQtyPercWithYield,
				IngListDataItem::setQtyPercWithYield);

		assertEquals(35d, puree.getQtyPercWithYield(), DELTA);
		assertEquals(45d, sauce.getQtyPercWithYield(), DELTA);
	}

	@Test
	public void partiallyDeclaredEvaporatingCompositeLeavesItsSubIngredientsUntouched() {
		IngListDataItem mozzarella = ing(50d, 30d, null, true);
		IngListDataItem milk = ing(30d, 33.33d, mozzarella, false);
		CompositeIngredientYieldBalancer balancer = balancer();

		balancer.aggregateParentQtyPercWithYield();
		balancer.spreadEvaporationOverSubIngredients();

		assertEquals(30d, mozzarella.getQtyPercWithYield(), DELTA);
		assertEquals(33.33d, milk.getQtyPercWithYield(), DELTA);
	}

	@Test
	public void fullyDeclaredCompositeToleratesRounding() {
		IngListDataItem parent = ing(100d, 100d, null, false);
		IngListDataItem first = ing(60.4d, 60.4d, parent, false);
		IngListDataItem second = ing(39.4d, 39.4d, parent, false);

		assertTrue(CompositeIngredientYieldBalancer.isFullyDeclaredBySubIngredients(parent, List.of(first, second)));
	}

	@Test
	public void zeroPercentWrapperIsNeverFullyDeclared() {
		IngListDataItem wrapper = ing(0d, 0d, null, false);
		IngListDataItem salt = ing(0d, 0d, wrapper, false);

		assertFalse(CompositeIngredientYieldBalancer.isFullyDeclaredBySubIngredients(wrapper, List.of(salt)));
	}

	private static Map<IngListDataItem, Double> valuesBeforeEvaporation(IngListDataItem item, double value) {
		Map<IngListDataItem, Double> values = new IdentityHashMap<>();
		values.put(item, value);
		return values;
	}

	private CompositeIngredientYieldBalancer balancer() {
		return new CompositeIngredientYieldBalancer(ingList, withRate::contains);
	}

	private IngListDataItem ing(double qtyPerc, double qtyPercWithYield, IngListDataItem parent, boolean evaporating) {
		IngListDataItem item = IngListDataItem.build().withQtyPerc(qtyPerc)
				.withIngredient(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, UUID.randomUUID().toString()));
		item.setQtyPercWithYield(qtyPercWithYield);
		item.setParent(parent);
		if (evaporating) {
			withRate.add(item);
		}
		ingList.add(item);
		return item;
	}
}
