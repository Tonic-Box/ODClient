package osrs.dev.util;

import lombok.Getter;

import java.util.LinkedList;

public class CircularList<E> {
    @Getter
    private final LinkedList<E> list = new LinkedList<E>();
    private final int capacity;

    public CircularList(int capacity) {
        this.capacity = capacity;
    }

    public void add(E element) {
        if (list.size() == capacity) {
            list.removeFirst();
        }
        list.addLast(element);
    }

    public E get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }
}