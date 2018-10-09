package ru.nordavind.ecgdonglelib.util;

import java.util.ArrayList;

/**
 * used to recycle objects see {@link IRecycleable} for details
 */

public abstract class ObjectRecycler<T> {
    protected final Object sync = new Object();
    private final int size;
    private final ArrayList<T> values;

    public ObjectRecycler(int size) {
        this.size = size;
        values = new ArrayList<>(size);
    }

    public void clear() {
        synchronized (sync) {
            values.clear();
        }
    }

    public T obtain(boolean createNew) {
        synchronized (sync) {
            if (values.size() > 0) {
                return values.remove(values.size() - 1);
            }
        }
        return createNew ? createNew() : null;
    }

    public void recycle(T reply) {
        synchronized (sync) {
            if (values.size() < size) {
                values.add(reply);
            }
        }
    }

    protected abstract T createNew();
}
