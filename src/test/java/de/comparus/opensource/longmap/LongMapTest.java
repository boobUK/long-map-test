package de.comparus.opensource.longmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

public class LongMapTest {
	private final long MAX_LONG = Long.MAX_VALUE;
	private final long MIN_LONG = Long.MIN_VALUE;
	private final int MAX_INTEGER = Integer.MAX_VALUE;
	private final int MIN_INTEGER = Integer.MIN_VALUE;
	private final int MINUS_ONE = -1;;
	private final int DEFAULT_AMOUNT = 100;
	private final int TEST_LOW_CAPACITY = 5;

	private final int RESTRICTED = 1000;

	private LongMap<Integer> longMap;

	@Before
	public void setUp() {
		longMap = new LongMapImpl<>(TEST_LOW_CAPACITY);
	}

	@Test
	public void whenPutIntegerKeyShouldContainAllNegativeData() {
		putIntegerAmountValues(RESTRICTED);
		List<Integer> currentValue = getValues(RESTRICTED, MIN_INTEGER);
		List<Integer> expectedValue = generateIntegerList(RESTRICTED * MINUS_ONE - 1, MINUS_ONE);
		assertEquals(currentValue.size(), RESTRICTED + 1);
		assertTrue(expectedValue.containsAll(currentValue) && currentValue.containsAll(expectedValue));
	}

	@Test
	public void whenPutIntegerKeyShouldContainAllPositiveData() {
		putIntegerAmountValues(RESTRICTED);
		List<Integer> currentValue = getValues(RESTRICTED, MAX_INTEGER);
		List<Integer> expectedValue = generateIntegerList(0, RESTRICTED);
		assertEquals(currentValue.size(), RESTRICTED + 1);
		assertTrue(expectedValue.containsAll(currentValue) && currentValue.containsAll(expectedValue));
	}

	@Test
	public void whenPutLongKeyShouldContainAllNegativeData() {
		putLongAmountValues(RESTRICTED);
		List<Integer> currentValue = getValues(RESTRICTED, MIN_LONG);
		List<Integer> expectedValue = generateIntegerList(RESTRICTED * MINUS_ONE - 1, MINUS_ONE);
		assertEquals(currentValue.size(), RESTRICTED + 1);
		assertTrue(expectedValue.containsAll(currentValue) && currentValue.containsAll(expectedValue));
	}

	@Test
	public void whenPutLongKeyShouldContainAllPositiveData() {
		putLongAmountValues(RESTRICTED);
		List<Integer> currentValue = getValues(RESTRICTED, MAX_LONG);
		List<Integer> expectedValue = generateIntegerList(0, RESTRICTED);
		assertEquals(currentValue.size(), RESTRICTED + 1);
		assertTrue(expectedValue.containsAll(currentValue) && currentValue.containsAll(expectedValue));
	}

	@Test
	public void whenKeysShouldReturnKeysArray() {
		putIntegerAmountValues(RESTRICTED);
		long[] values = longMap.keys();
		assertEquals(values.length, RESTRICTED * 2 + 2);
	}

	@Test
	public void whenValuesShouldReturnAllValues() {
		putIntegerAmountValues(RESTRICTED);
		Object[] values = longMap.values();
		assertEquals(values.length, RESTRICTED * 2 + 2);
	}

	@Test
	public void whenContainsValueAbsentShouldFalse() {
		longMap.put(MAX_INTEGER, DEFAULT_AMOUNT * MINUS_ONE);
		assertFalse(longMap.containsValue(DEFAULT_AMOUNT));
	}

	@Test
	public void whenRemoveKeyShouldFalseContentKey() {
		putIntegerAmountValues(DEFAULT_AMOUNT);
		longMap.put(MAX_INTEGER, DEFAULT_AMOUNT);
		assertTrue(longMap.containsKey(MAX_INTEGER));
		assertTrue(longMap.remove(MAX_INTEGER).equals(DEFAULT_AMOUNT));
		assertFalse(longMap.containsKey(MAX_INTEGER));
	}

	@Test
	public void whenGetExistKeyShouldReturnValue() {
		putIntegerAmountValues(DEFAULT_AMOUNT);
		Integer oneValue = longMap.get(MAX_INTEGER);
		assertTrue(oneValue != null && oneValue.equals(0));
	}

	@Test
	public void whenPutThanShouldTrueIfContainKey() {
		putIntegerAmountValues(DEFAULT_AMOUNT);
		assertTrue(longMap.containsKey(MAX_INTEGER - 1));
		assertTrue(longMap.containsValue(1));
	}

	@Test
	public void whenClearThanIsEmpty() {
		putIntegerAmountValues(DEFAULT_AMOUNT);
		assertTrue(longMap.containsKey(MAX_INTEGER));
		longMap.clear();
		assertTrue(longMap.isEmpty());
		assertFalse(longMap.containsKey(MAX_INTEGER));
	}

	@Test
	public void whenCreatedThanIsEmpty() {
		longMap = new LongMapImpl<>();
		assertTrue(longMap.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void whenInitialCapacityBelowZeroThanExeption() {
		longMap = new LongMapImpl<>(MINUS_ONE);
	}

	private void putLongAmountValues(int amount) {
		long max = MAX_LONG;
		long min = MIN_LONG;
		for (int i = 0; i <= amount; i++) {
			longMap.put(max--, i);
			longMap.put(min++, i * MINUS_ONE - 1);
		}
	}

	private void putIntegerAmountValues(int amount) {
		int max = MAX_INTEGER;
		int min = MIN_INTEGER;
		for (int i = 0; i <= amount; i++) {
			longMap.put(max--, i);
			longMap.put(min++, i * MINUS_ONE - 1);
		}
	}

	private List<Integer> getValues(int amount, long keyIteration) {
		List<Integer> positiveValues = new ArrayList<>();
		for (int i = 0; i <= amount; i++) {
			Integer entry = longMap.get(keyIteration < 0 ? keyIteration++ : keyIteration--);
			if (entry != null) {
				positiveValues.add(entry);
			}
		}
		return positiveValues;
	}

	private List<Integer> generateIntegerList(int from, int to) {
		return IntStream.rangeClosed(from, to).boxed().collect(Collectors.toList());
	}

}
