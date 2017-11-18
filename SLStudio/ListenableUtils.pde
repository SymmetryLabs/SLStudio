public static class ListenableList<E> implements Iterable<E> {

  public final List<E> list = new ArrayList<E>();

  private final List<ListListener<E>> listeners = new ArrayList<ListListener<E>>();

  public int size() {
    return list.size();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public boolean contains(Object o) {
    return list.contains(o);
  }

  public E get(int index) {
    return list.get(index);
  }

  public void add(int index, E element) {
    list.add(index, element);
    for (ListListener<E> listener : listeners) {
      listener.itemAdded(index, element);
    }
  }

  public boolean add(E element) {
    list.add(element);
    for (ListListener<E> listener : listeners) {
      listener.itemAdded(list.size() - 1, element);
    }
    return true;
  }

  public E remove(int index) {
    E element = list.remove(index);
    for (ListListener<E> listener : listeners) {
      listener.itemRemoved(index, element);
    }
    return element;
  }

  public boolean remove(Object o) {
    int index = list.indexOf(o);
    if (index != -1) {
      remove(index);
      return true;
    }
    return false;
  }

  public void clear() {
    while (!isEmpty()) {
      remove(0);
    }
  }

  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  public final ListenableList<E> addListener(ListListener<E> listener) {
    listeners.add(listener);
    return this;
  }

  public final ListenableList<E> addListenerWithInit(ListListener<E> listener) {
    listeners.add(listener);
    int index = 0;
    for (E element : list) {
      listener.itemAdded(index++, element);
    }
    return this;
  }

  public final ListenableList<E> removeListener(ListListener<E> listener) {
    listeners.remove(listener);
    return this;
  }

  public Iterator<E> iterator() {
    return list.iterator();
  }

}

public static interface ListListener<E> {
  public void itemAdded(int index, E element);
  public void itemRemoved(int index, E element);
}

public static abstract class AbstractListListener<E> implements ListListener<E> {
  public void itemAdded(int index, E element) {}
  public void itemRemoved(int index, E element) {}
}

public static class ListenableInt {

  private int value;

  private final List<IntListener> listeners = new ArrayList<IntListener>();

  public ListenableInt() {
    this(0);
  }

  public ListenableInt(int value) {
    this.value = value;
  }

  public int get() {
    return value;
  }

  public void set(int value) {
    this.value = value;
    for (IntListener listener : listeners) {
      listener.onChange(value);
    }
  }

  public void increment() {
    set(get() + 1);
  }

  public void decrement() {
    set(get() - 1);
  }

  public String toString() {
    return Integer.toString(value);
  }

  public final ListenableInt addListener(IntListener listener) {
    listeners.add(listener);
    return this;
  }

  public final ListenableInt addListenerWithInit(IntListener listener) {
    listeners.add(listener);
    listener.onChange(value);
    return this;
  }

  public final ListenableInt removeListener(IntListener listener) {
    listeners.remove(listener);
    return this;
  }

}

public static interface IntListener {
  public void onChange(int value);
}
