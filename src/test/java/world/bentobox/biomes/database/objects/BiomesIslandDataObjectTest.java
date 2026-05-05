package world.bentobox.biomes.database.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BiomesIslandDataObject}.
 */
class BiomesIslandDataObjectTest {

    private BiomesIslandDataObject obj;

    @BeforeEach
    void setUp() {
        obj = new BiomesIslandDataObject();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(obj);
    }

    @Test
    void testGetSetUniqueId() {
        obj.setUniqueId("island-123");
        assertEquals("island-123", obj.getUniqueId());
    }

    @Test
    void testGetOwnerBundleNullByDefault() {
        assertNull(obj.getOwnerBundle());
    }

    @Test
    void testGetSetOwnerBundle() {
        obj.setOwnerBundle("bundle-a");
        assertEquals("bundle-a", obj.getOwnerBundle());
    }

    @Test
    void testSetOwnerBundleNull() {
        obj.setOwnerBundle("bundle-a");
        obj.setOwnerBundle(null);
        assertNull(obj.getOwnerBundle());
    }

    @Test
    void testGetIslandBundleNullByDefault() {
        assertNull(obj.getIslandBundle());
    }

    @Test
    void testGetSetIslandBundle() {
        obj.setIslandBundle("bundle-b");
        assertEquals("bundle-b", obj.getIslandBundle());
    }

    @Test
    void testGetUnlockedBiomesDefaultEmpty() {
        assertNotNull(obj.getUnlockedBiomes());
        assertTrue(obj.getUnlockedBiomes().isEmpty());
    }

    @Test
    void testGetPurchasedBiomesDefaultEmpty() {
        assertNotNull(obj.getPurchasedBiomes());
        assertTrue(obj.getPurchasedBiomes().isEmpty());
    }

    @Test
    void testGetBiomeChangeCounterDefaultEmpty() {
        assertNotNull(obj.getBiomeChangeCounter());
        assertTrue(obj.getBiomeChangeCounter().isEmpty());
    }

    @Test
    void testIsUnlockedByString() {
        obj.getUnlockedBiomes().add("biome-a");
        assertTrue(obj.isUnlocked("biome-a"));
        assertFalse(obj.isUnlocked("biome-b"));
    }

    @Test
    void testUnlockBiomeByString() {
        obj.unlockBiome("biome-x");
        assertTrue(obj.isUnlocked("biome-x"));
    }

    @Test
    void testUnlockBiomeByObject() {
        BiomesObject biomesObject = new BiomesObject();
        biomesObject.setUniqueId("biome-obj");
        obj.unlockBiome(biomesObject);
        assertTrue(obj.isUnlocked(biomesObject));
        assertTrue(obj.isUnlocked("biome-obj"));
    }

    @Test
    void testIsPurchasedByString() {
        obj.getPurchasedBiomes().add("biome-p");
        assertTrue(obj.isPurchased("biome-p"));
        assertFalse(obj.isPurchased("biome-q"));
    }

    @Test
    void testPurchaseBiomeByString() {
        obj.purchaseBiome("biome-y");
        assertTrue(obj.isPurchased("biome-y"));
    }

    @Test
    void testPurchaseBiomeByObject() {
        BiomesObject biomesObject = new BiomesObject();
        biomesObject.setUniqueId("biome-obj-2");
        obj.purchaseBiome(biomesObject);
        assertTrue(obj.isPurchased(biomesObject));
        assertTrue(obj.isPurchased("biome-obj-2"));
    }

    @Test
    void testIncreaseBiomeChangeCounterByString() {
        obj.increaseBiomeChangeCounter("biome-c");
        obj.increaseBiomeChangeCounter("biome-c");
        assertEquals(2, obj.getBiomeChangeCounter("biome-c"));
    }

    @Test
    void testIncreaseBiomeChangeCounterByObject() {
        BiomesObject biomesObject = new BiomesObject();
        biomesObject.setUniqueId("biome-d");
        obj.increaseBiomeChangeCounter(biomesObject);
        assertEquals(1, obj.getBiomeChangeCounter(biomesObject));
    }

    @Test
    void testAdjustBiomeChangeCounter() {
        BiomesObject biomesObject = new BiomesObject();
        biomesObject.setUniqueId("biome-e");
        obj.adjustBiomeChangeCounter(biomesObject, 7);
        assertEquals(7, obj.getBiomeChangeCounter(biomesObject));
    }

    @Test
    void testGetBiomeChangeCounterUnknownKeyReturnsZero() {
        assertEquals(0, obj.getBiomeChangeCounter("nonexistent"));
    }

    @Test
    void testClearResetsIslandBundleAndCounters() {
        obj.setIslandBundle("some-bundle");
        obj.unlockBiome("biome-f");
        obj.purchaseBiome("biome-g");
        obj.increaseBiomeChangeCounter("biome-f");
        obj.setOwnerBundle("owner-bundle");

        obj.clear();

        assertNull(obj.getIslandBundle());
        assertTrue(obj.getUnlockedBiomes().isEmpty());
        assertTrue(obj.getPurchasedBiomes().isEmpty());
        assertTrue(obj.getBiomeChangeCounter().isEmpty());
        // ownerBundle is not cleared
        assertEquals("owner-bundle", obj.getOwnerBundle());
    }

    @Test
    void testSetUnlockedBiomes() {
        Set<String> biomes = new HashSet<>();
        biomes.add("biome-h");
        obj.setUnlockedBiomes(biomes);
        assertEquals(biomes, obj.getUnlockedBiomes());
    }

    @Test
    void testSetPurchasedBiomes() {
        Set<String> biomes = new HashSet<>();
        biomes.add("biome-i");
        obj.setPurchasedBiomes(biomes);
        assertEquals(biomes, obj.getPurchasedBiomes());
    }

    @Test
    void testSetBiomeChangeCounter() {
        Map<String, AtomicInteger> counter = new ConcurrentHashMap<>();
        counter.put("biome-j", new AtomicInteger(3));
        obj.setBiomeChangeCounter(counter);
        assertEquals(counter, obj.getBiomeChangeCounter());
        assertEquals(3, obj.getBiomeChangeCounter("biome-j"));
    }
}
