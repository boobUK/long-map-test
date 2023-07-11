package de.comparus.opensource.longmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the LongMap interface that provides a data structure for
 * storing key-value pairs with long keys and generic values.
 *
 * @param <V> the type of values stored in the map
 * @author valentyn.ushych
 */
public class LongMapImpl<V> implements LongMap<V> {

	private static final int DEFAULT_CAPACITY = 50;
	private final float DEFAULT_LOAD_FACTOR = 0.75f;

	private Entry<V>[] table;
	private long size;
	private int capacity;

	public LongMapImpl() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Constructs a LongMapImpl object with the specified initial capacity.
	 *
	 * @param initialCapacity the initial capacity of the map
	 * @throws IllegalArgumentException if the initial capacity is less than or
	 *                                  equal to 0
	 */
	public LongMapImpl(int initialCapacity) {
		if (initialCapacity <= 0) {
			throw new IllegalArgumentException("Invalid initial capacity: " + initialCapacity);
		}
		capacity = initialCapacity;
		table = new Entry[initialCapacity];
	}

	/**
	 * Associates the specified value with the specified key in this map. If the map
	 * previously contained a mapping for the key, the old value is replaced by the
	 * specified value.
	 *
	 * @param key   the key with which the specified value is to be associated
	 * @param value the value to be associated with the specified key
	 * @return the previous value associated with the key, or null if there was no
	 *         mapping for the key
	 */
	@Override
	public V put(long key, V value) {
		ensureCapacity();
		int index = getIndex(key);
		return table[index] == null ? assignTableIndex(key, value, index)
				: putToBasket(table[index], new Entry<>(key, value));
	}

	/**
	 * Returns the value to which the specified key is mapped in this map, or null
	 * if the map contains no mapping for the key.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or null if the map
	 *         contains no mapping for the key
	 */
	@Override
	public V get(long key) {
		int index = getIndex(key);
		Entry<V> entry = getEntry(index, key);
		return entry == null ? null : entry.getValue();
	}

	/**
	 * Removes the mapping for the specified key from this map if present.
	 *
	 * @param key the key whose mapping is to be removed from the map
	 * @return the value associated with the key, or null if there was no mapping
	 *         for the key
	 */
	@Override
	public V remove(long key) {
		int index = getIndex(key);
		Entry<V> entry = table[index];
		return entry == null ? null : removeEntry(entry, key, index);
	}

	/**
	 * Returns true if this map contains no key-value mappings.
	 *
	 * @return true if this map contains no key-value mappings, false otherwise
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Returns true if this map contains a mapping for the specified key.
	 *
	 * @param key the key whose presence in the map is to be tested
	 * @return true if this map contains a mapping for the specified key, false
	 *         otherwise
	 */
	@Override
	public boolean containsKey(long key) {
		int index = getIndex(key);
		Entry<V> entry = table[index];
		return entry == null ? false : entry.getKey() == key ? true : hasKey(entry, key);
	}

	/**
	 * Returns true if this map maps one or more keys to the specified value.
	 *
	 * @param value the value to check for presence in the map
	 * @return true if this map maps one or more keys to the specified value, false
	 *         otherwise
	 */
	@Override
	public boolean containsValue(V value) {
		return Arrays.asList(table).stream().anyMatch(basket -> hasValue(basket, value));
	}

	/**
	 * Returns an array containing all the keys in this map.
	 *
	 * @return an array containing all the keys in this map
	 */
	@Override
	public long[] keys() {
		List<Long> list = new ArrayList<>();
	    for (Entry<V> entry : table) {
	        while (entry != null) {
	            list.add(entry.getKey());
	            entry = entry.getNext();
	        }
	    }
	    long[] keys = new long[list.size()];
	    for (int i = 0; i < list.size(); i++) {
	        keys[i] = list.get(i);
	    }
		return keys;
	}

	/**
	 * Returns an array containing all the values in this map.
	 *
	 * @return an array containing all the values in this map
	 */
	@Override
	public V[] values() {
		List<V> list = new ArrayList<>();
		for (Entry<V> entry : table) {
			if (entry != null) {
				while (entry != null) {
					list.add(entry.getValue());
					entry = entry.getNext();
				}
			}
		}
		return (V[]) list.toArray();
	}

	/**
	 * Returns the number of key-value mappings in this map.
	 *
	 * @return the number of key-value mappings in this map
	 */
	@Override
	public long size() {
		return size;
	}

	/**
	 * Removes all of the mappings from this map. The map will be empty after this
	 * call returns.
	 */
	@Override
	public void clear() {
		size = 0;
		Arrays.fill(table, null);
	}

	private long hash(long value) {
		return Long.hashCode(value);
	}

	private int getIndex(long key) {
		long hashCode = hash(key);
		int index = Math.abs((int) (hashCode % capacity));
		return index;
	}

	private void ensureCapacity() {
		if (size / (capacity) >= DEFAULT_LOAD_FACTOR) {
			capacity *= 2;
			Entry<V>[] newTable = new Entry[capacity];
			for (Entry<V> entry : table) {
				if (entry != null) {
					forkNextValues(entry, newTable);
				}
			}
			table = newTable;
		}
	}

	private void forkNextValues(Entry<V> entry, Entry<V>[] entryTable) {
		Entry<V> currentEntry = entry;
		while (currentEntry != null) {
			int index = getIndex(currentEntry.getKey());
			if (entryTable[index] == null) {
				if (currentEntry.hasNext()) {
					forkNextValues(currentEntry.getNext(), entryTable);
				}
				if (entryTable[index] != null) {
					putToBasket(entryTable[index], currentEntry.clone());
				} else {
					entryTable[index] = currentEntry.clone();
				}
				return;
			} else {
				putToBasket(entryTable[index], currentEntry.clone());
			}
			currentEntry = currentEntry.getNext();
		}
	}

	private V putToBasket(Entry<V> entry, Entry<V> newEntry) {
		Entry<V> previousEntry = null;
		while (entry != null) {
			if (entry.getKey() == newEntry.getKey()) {
				entry.setValue(newEntry.getValue());
				return newEntry.getValue();
			}
			previousEntry = entry;
			entry = entry.getNext();
		}

		return previousEntry.setNext(newEntry);
	}

	private V assignTableIndex(long key, V value, int index) {
		table[index] = new Entry<V>(key, value);
		size++;
		return table[index].getValue();
	}

	private Entry<V> getEntry(int index, long key) {
		Entry<V> entry = table[index];
		return entry == null ? null : entry.getKey() == key ? entry : getFromBasket(entry, key);
	}

	private Entry<V> getFromBasket(Entry<V> basket, long key) {
		while (basket != null) {
			if (basket.getKey() == key) {
				return basket;
			}
			basket = basket.getNext();
		}
		return null;
	}

	private V removeEntry(Entry<V> basket, long key, int index) {
		return basket.getKey() == key ? removeBasket(index) : removeEntryInBasket(basket, key);
	}

	private V removeBasket(int index) {
		V removedValue = table[index].getValue();
		table[index] = null;
		return removedValue;
	}

	private V removeEntryInBasket(Entry<V> basket, long key) {
		Entry<V> priviousBacket = null;
		while (basket != null) {
			if (basket.getKey() == key) {
				priviousBacket.setNullNext(basket);
				size--;
				return basket.getValue();
			}
			priviousBacket = basket;
			basket = basket.getNext();
		}
		return null;
	}

	private boolean hasValue(Entry<V> basket, V value) {
		while (basket != null) {
			if (basket.getValue().equals(value)) {
				return true;
			}
			basket = basket.getNext();
		}
		return false;
	}

	private boolean hasKey(Entry<V> basket, long key) {
		while (basket != null) {
			if (basket.getKey() == key) {
				return true;
			}
			basket = basket.getNext();
		}
		return false;
	}

	private class Entry<V> {
		private int defaultDepthLimit = 5;
		private final long key;
		private V value;
		private Entry<V> left;
		private Entry<V> right;
		private int depthLeft;
		private int depthRight;
		private boolean hasNext;

		public Entry(long key, V value) {
			this.key = key;
			this.value = value;
		}

		public long getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			this.value = value;
			return this.value;
		}

		private Entry<V> getNext() {
			if (left != null) {
				return left;
			} else if (right != null) {
				return right;
			} else {
				return null;
			}
		}

		public V setNext(Entry<V> entry) {
			hasNext = true;
			if (key < entry.getKey() && getLeftDepth() <= defaultDepthLimit) {
				return setLeft(entry);
			} else if (key > entry.getKey() && getRightDepth() <= defaultDepthLimit) {
				return setRight(entry);
			} else {
				setValue(entry.getValue());
			}
			doubleDepthLimitIfNeed();
			return value;
		}

		public boolean hasNext() {
			return hasNext;
		}

		private int getLeftDepth() {
			return depthLeft;
		}

		private int getRightDepth() {
			return depthRight;
		}

		public Entry<V> clone() {
			return new Entry<>(key, value);
		}

		private V setNullNext(Entry<V> entry) {
			if (key < entry.getKey() && getLeftDepth() <= defaultDepthLimit) {
				return setLeft(null);
			} else if (key > entry.getKey() && getRightDepth() <= defaultDepthLimit) {
				return setRight(null);
			}
			return null;
		}

		private void doubleDepthLimitIfNeed() {
			if (getLeftDepth() >= defaultDepthLimit + 1) {
				defaultDepthLimit = defaultDepthLimit * 2;
			}
			if (getRightDepth() >= defaultDepthLimit) {
				defaultDepthLimit = defaultDepthLimit * 2;
			}
		}

		private V setLeft(Entry<V> left) {
			if (left == null) {
				if (!this.left.hasNext()) {
					V value = this.left.value;
					this.left = left;
					depthRight--;
					return value;
				} else {
					depthRight--;
					this.left.setNullNext(left);
				}
			}
			if (this.left == null) {
				this.left = left;
				this.depthLeft++;
			} else {
				this.depthLeft++;
				return this.left.setNext(left);
			}
			return left.value;
		}

		private V setRight(Entry<V> right) {
			if (right == null) {
				if (this.right == null) {
					V value = this.right.value;
					this.right = right;
					depthRight--;
					return value;
				} else {
					depthRight--;
					this.right.setNullNext(right);
				}
			}
			if (this.right == null) {
				this.right = right;
				this.depthRight++;
			} else {
				this.depthRight++;
				return this.right.setNext(right);
			}
			return right.value;
		}

		@Override
		public String toString() {
			return "Key: " + key + ", Value: " + value;
		}

	}
}
