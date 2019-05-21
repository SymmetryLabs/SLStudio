package com.symmetrylabs.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;


public class IterationUtils {
    public static class StopIteration extends RuntimeException {}

    /**
     * Iterate through a collection, aborting iteration if the collection is modified.
     *
     * There are many places in SLStudio where we iterate over a collection in
     * the GUI thread that could be mutated by the engine thread. Because this
     * happens on every frame, we don't mind if we get it wrong for a single
     * frame, as long as on the next frame we draw everything correctly. So
     * instead of synchronizing on all accesses to the collection, we just iterate over
     * it with this function, which allows us to continue until the next frame,
     * when we will get a consistent view of the collection.
     *
     * @return true if the iteration finished normally (i.e., we went through the entire iterable or a StopIteration exception was raised)
     */
    public static <T> boolean forEachIgnoreModification(Iterable<T> collection, Consumer<? super T> action) {
        try {
            for (T val : collection) {
                action.accept(val);
            }
            return true;
        } catch (ConcurrentModificationException e) {
            return false;
        } catch (StopIteration e) {
            return true;
        }
    }

    /**
     * Reduce a collection, aborting iteration if the collection is modified.
     *
     * Reduction is an operation that takes a binary operator and applies that
     * operator in turn to each element of the collection and an accumulated value,
     * then stores the result of that operation in the accumulator. For example,
     * reducing the list [1, 2, 3] with operator + and initial value 0 would in
     * effect run:
     *
     *    ((0 + 1) + 2) + 3
     *
     * There are many places in SLStudio where we iterate over a collection in
     * the GUI thread that could be mutated by the engine thread. Because this
     * happens on every frame, we don't mind if we get it wrong for a single
     * frame, as long as on the next frame we draw everything correctly. So
     * instead of synchronizing on all accesses to the collection, we just iterate over
     * it with this function, which allows us to continue until the next frame,
     * when we will get a consistent view of the collection.
     *
     * @param collection the collection to iterate over
     * @param accum the initial value of the reduction
     * @param action the function that will be applied to the accumulator and each successive value
     */
    public static <E, T> T reduceIgnoreModification(Iterable<E> collection, T accum, BiFunction<T, E, T> action) {
        try {
            for (E val : collection) {
                accum = action.apply(accum, val);
            }
        } catch (StopIteration e) {
        } catch (ConcurrentModificationException e) {
        }
        return accum;
    }
}
