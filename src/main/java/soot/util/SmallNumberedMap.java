package soot.util;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002 Ondrej Lhotak
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * A java.util.Map with Numberable objects as the keys.
 *
 * @author Ondrej Lhotak
 */
public final class SmallNumberedMap<K extends Numberable, V> implements INumberedMap<K, V> {

  private K[] array = newArray(Numberable.class, 8);
  private V[] values = newArray(Object.class, 8);
  private int size = 0;

  public SmallNumberedMap() {
  }

  @SuppressWarnings("unchecked")
  private static <T> T[] newArray(Class<? super T> componentType, int length) {
    return (T[]) Array.newInstance(componentType, length);
  }

  @Override
  public synchronized boolean put(K key, V value) {
    int pos = findPosition(key);
    if (array[pos] == key) {
      if (values[pos] == value) {
        return false;
      }
      values[pos] = value;
      return true;
    }
    size++;
    if (size * 3 > array.length * 2) {
      doubleSize();
      pos = findPosition(key);
    }
    array[pos] = key;
    values[pos] = value;
    return true;
  }

  @Override
  public synchronized V get(K key) {
    return values[findPosition(key)];
  }

  @Override
  public synchronized void remove(K key) {
    int pos = findPosition(key);
    if (array[pos] == key) {
      array[pos] = null;
      values[pos] = null;
      size--;
    }
  }

  /**
   * Returns the number of non-null values in this map.
   */
  public int nonNullSize() {
    int ret = 0;
    for (V element : values) {
      if (element != null) {
        ret++;
      }
    }
    return ret;
  }

  @Override
  public Iterator<K> keyIterator() {
    return new SmallNumberedMapIterator<K>(array);
  }

  /**
   * Returns an iterator over the non-null values.
   */
  public Iterator<V> iterator() {
    return new SmallNumberedMapIterator<V>(values);
  }

  private class SmallNumberedMapIterator<C> implements Iterator<C> {
    private final C[] data;
    private int cur;

    SmallNumberedMapIterator(C[] data) {
      this.data = data;
      this.cur = 0;
      seekNext();
    }

    protected final void seekNext() {
      V[] temp = SmallNumberedMap.this.values;
      try {
        while (temp[cur] == null) {
          cur++;
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        cur = -1;
      }
    }

    @Override
    public final void remove() {
      SmallNumberedMap.this.array[cur - 1] = null;
      SmallNumberedMap.this.values[cur - 1] = null;
    }

    @Override
    public final boolean hasNext() {
      return cur != -1;
    }

    @Override
    public final C next() {
      C ret = data[cur];
      cur++;
      seekNext();
      return ret;
    }
  }

  private int findPosition(K o) {
    int number = o.getNumber();
    if (number == 0) {
      throw new RuntimeException("unnumbered");
    }
    number = number & (array.length - 1);
    while (true) {
      K key = array[number];
      if (key == o || key == null) {
        return number;
      }
      number = (number + 1) & (array.length - 1);
    }
  }

  private void doubleSize() {
    K[] oldArray = array;
    V[] oldValues = values;
    final int oldLength = oldArray.length;
    final int newLength = oldLength * 2;
    array = newArray(Numberable.class, newLength);
    values = newArray(Object.class, newLength);
    for (int i = 0; i < oldLength; i++) {
      K element = oldArray[i];
      if (element != null) {
        int pos = findPosition(element);
        array[pos] = element;
        values[pos] = oldValues[i];
      }
    }
  }
}
