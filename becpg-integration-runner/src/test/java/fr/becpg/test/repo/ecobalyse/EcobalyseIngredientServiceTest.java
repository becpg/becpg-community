package fr.becpg.test.repo.ecobalyse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredient;
import fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredientService;

/**
 * Checks the Ecobalyse ingredient reference shipped with beCPG.
 *
 * @author matthieu
 */
public class EcobalyseIngredientServiceTest {

	private static final double PRECISION = 0.0001d;

	private final EcobalyseIngredientService service = new EcobalyseIngredientService();

	@Test
	public void testReferenceIsLoaded() {
		assertTrue("The Ecobalyse reference should hold more than a thousand ingredients", service.getIngredients().size() > 1000);
	}

	@Test
	public void testIngredientIsFoundByAlias() {
		Optional<EcobalyseIngredient> barley = service.findByCode("barley-whole-fr");

		assertTrue("barley-whole-fr should be part of the reference", barley.isPresent());
		assertEquals("Orge entière FR", barley.get().getName());
		assertEquals("ORGE", barley.get().getCropGroup());
		assertEquals("reference", barley.get().getScenario());
		assertEquals(1.533d, barley.get().getLandOccupation(), PRECISION);
	}

	@Test
	public void testIngredientIsAlsoFoundByIdentifier() {
		Optional<EcobalyseIngredient> byId = service.findByCode("00150a93-dea5-55d1-ba56-990d4fa43592");

		assertTrue("The reference is indexed by identifier as well as by alias", byId.isPresent());
		assertEquals("barley-whole-fr", byId.get().getAlias());
	}

	@Test
	public void testEcosystemicServicesAreSummed() {
		EcobalyseIngredient barley = service.findByCode("barley-whole-fr").orElseThrow();

		// hedges 2.5, plot size 4.09, crop diversity 0, no permanent pasture
		assertEquals(6.59d, barley.totalEcosystemicServices(), PRECISION);
	}

	@Test
	public void testMissingServicesCountAsZero() {
		EcobalyseIngredient bare = EcobalyseIngredient.builder().withIdentity("id", "alias", "name")
				.withEcosystemicServices(null, null, null, null).build();

		assertEquals(0d, bare.totalEcosystemicServices(), PRECISION);
	}

	@Test
	public void testUnknownCodeIsEmpty() {
		assertTrue(service.findByCode("does-not-exist").isEmpty());
		assertTrue(service.findByCode(null).isEmpty());
		assertTrue(service.findByCode("  ").isEmpty());
	}

}
