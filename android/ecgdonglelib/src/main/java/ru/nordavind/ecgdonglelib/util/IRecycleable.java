package ru.nordavind.ecgdonglelib.util;


/**
 * Creating and dropping a lot of objects will trigger Garbage Collector.
 * We can recycle objects when they are no longer needed
 * Objects are emitted with counter == 0.
 * retain() will increase counter, release() will decrease counter;
 * when counter becomes 0 or less in release() it will be recycled.
 * recycled object is sent to object recycler and will be used instead of creating new objects
 */
public interface IRecycleable {
    /**
     * increases counter; Objects will not be recycled while counter > 0
     */
    void retain();

    /**
     * decreases counter; Object will be recycled if counter becomes 0 or less
     */
    void release();
}
