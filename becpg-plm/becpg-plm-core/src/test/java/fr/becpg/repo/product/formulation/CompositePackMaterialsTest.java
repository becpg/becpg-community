package fr.becpg.repo.product.formulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;

/**
 * Unit tests of the composite packaging materials: a composite line is computed from
 * its components and counts as one material (#38305).
 */
public class CompositePackMaterialsTest {

	private static final NodeRef PP = new NodeRef("workspace://SpacesStore/pp");
	private static final NodeRef FILM = new NodeRef("workspace://SpacesStore/film");
	private static final NodeRef PET = new NodeRef("workspace://SpacesStore/pet");
	private static final NodeRef PE = new NodeRef("workspace://SpacesStore/pe");
	private static final NodeRef FILM_LINE = new NodeRef("workspace://SpacesStore/filmLine");

	private static final double DELTA = 0.001d;

	private final PackMaterialListDataItem tray = PackMaterialListDataItem.build().withMaterial(PP).withWeight(18d);
	private final PackMaterialListDataItem film = PackMaterialListDataItem.build().withMaterial(FILM);
	private final PackMaterialListDataItem pet = PackMaterialListDataItem.build().withMaterial(PET).withWeight(1.5d).withParent(film);
	private final PackMaterialListDataItem pe = PackMaterialListDataItem.build().withMaterial(PE).withWeight(2d).withRecycledPerc(30d)
			.withParent(film);

	@Test
	public void compositeWeightIsTheSumOfItsComponents() {
		CompositePackMaterials.rollUp(lidAndTray());

		assertEquals(3.5d, film.getPmlWeight(), DELTA);
	}

	@Test
	public void compositeRecycledShareIsWeightedByComponentWeight() {
		CompositePackMaterials.rollUp(lidAndTray());

		assertEquals(17.143d, film.getPmlRecycledPercentage(), DELTA);
	}

	@Test
	public void monoMaterialIsLeftUntouched() {
		tray.setPmlRecycledPercentage(12d);

		CompositePackMaterials.rollUp(lidAndTray());

		assertEquals(18d, tray.getPmlWeight(), DELTA);
		assertEquals(12d, tray.getPmlRecycledPercentage(), DELTA);
	}

	@Test
	public void compositeWithoutComponentWeightHasNoShare() {
		pet.setPmlWeight(null);
		pe.setPmlWeight(null);

		CompositePackMaterials.rollUp(lidAndTray());

		assertEquals(0d, film.getPmlWeight(), DELTA);
		assertNull(film.getPmlRecycledPercentage());
	}

	@Test
	public void componentsAreNotCountedAsMaterials() {
		List<PackMaterialListDataItem> materials = CompositePackMaterials.materialsOf(lidAndTray());

		assertEquals(List.of(tray, film), materials);
	}

	@Test
	public void missingListHoldsNoMaterial() {
		assertTrue(CompositePackMaterials.materialsOf(null).isEmpty());
	}

	@Test
	public void componentsAreMatchedOnTheNodeOfTheirParent() {
		film.setNodeRef(FILM_LINE);
		PackMaterialListDataItem reloadedFilm = PackMaterialListDataItem.build().withMaterial(FILM);
		reloadedFilm.setNodeRef(FILM_LINE);
		pet.setParent(reloadedFilm);
		pe.setParent(reloadedFilm);

		assertEquals(List.of(pet, pe), CompositePackMaterials.componentsOf(film, lidAndTray()));
	}

	@Test
	public void depthFollowsTheParent() {
		film.withParent(null);

		assertEquals(Integer.valueOf(1), film.getDepthLevel());
		assertFalse(CompositePackMaterials.isComponent(film));
		assertTrue(CompositePackMaterials.isComponent(pet));
	}

	private List<PackMaterialListDataItem> lidAndTray() {
		List<PackMaterialListDataItem> packMaterials = new ArrayList<>();
		packMaterials.add(tray);
		packMaterials.add(film);
		packMaterials.add(pet);
		packMaterials.add(pe);
		return packMaterials;
	}
}
