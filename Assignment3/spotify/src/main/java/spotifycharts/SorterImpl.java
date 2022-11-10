package spotifycharts;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SorterImpl<E> implements Sorter<E> {

    /**
     * Sorts all items by insertion sort using the provided comparator
     * for deciding relative ordening of two items.
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items
     * @param comparator
     * @return the items sorted in place
     */
    public List<E> selInsBubSort(List<E> items, Comparator<E> comparator) {
        for (int i = 0; i < items.size(); i++) {
            int j = i;
            E temp = items.get(i);
            while (j > 0 && comparator.compare(items.get(j - 1), temp) > 0) {
                items.set(j, items.get(j - 1));
                j = j - 1;
            }
            items.set(j, temp);
        }
        return items;
    }

    /**
     * Sorts all items by quick sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items
     * @param comparator
     * @return the items sorted in place
     */
    public List<E> quickSort(List<E> items, Comparator<E> comparator) {
        quickSortPart(items, comparator, 0, items.size() - 1);

        return items;
    }

    private void quickSortPart(List<E> items, Comparator<E> comparator, int from, int to) {
        if (from >= to) return; // If the list is empty or has only one item, the list is already sorted

        int partitionIndex = partition(items, comparator, from, to);

        quickSortPart(items, comparator, from, partitionIndex - 1);
        quickSortPart(items, comparator, partitionIndex + 1, to);
    }

    private int partition(List<E> items, Comparator<E> comparator, int from, int to) {
        int pivot = to;
        int i = from - 1;

        for (int j = from; j < to; j++) {
            if (comparator.compare(items.get(j), items.get(pivot)) <= 0) {
                i++;

                Collections.swap(items, i, j);
            }
        }
        Collections.swap(items, i + 1, to);

        return i + 1;
    }

    /**
     * Identifies the lead collection of numTops items according to the ordening criteria of comparator
     * and organizes and sorts this lead collection into the first numTops positions of the list
     * with use of (zero-based) heapSwim and heapSink operations.
     * The remaining items are kept in the tail of the list, in arbitrary order.
     * Items are sorted 'in place' without use of an auxiliary list or array or other positions in items
     *
     * @param numTops    the size of the lead collection of items to be found and sorted
     * @param items
     * @param comparator
     * @return the items list with its first numTops items sorted according to comparator
     * all other items >= any item in the lead collection
     */
    public List<E> topsHeapSort(int numTops, List<E> items, Comparator<E> comparator) {

        // the lead collection of numTops items will be organised into a (zero-based) heap structure
        // in the first numTops list positions using the reverseComparator for the heap condition.
        // that way the root of the heap will contain the worst item of the lead collection
        // which can be compared easily against other candidates from the remainder of the list
        Comparator<E> reverseComparator = comparator.reversed();

        // initialise the lead collection with the first numTops items in the list
        for (int heapSize = 2; heapSize <= numTops; heapSize++) {
            // repair the heap condition of items[0..heapSize-2] to include new item items[heapSize-1]
            heapSwim(items, heapSize, reverseComparator);
        }

        // insert remaining items into the lead collection as appropriate
        for (int i = numTops; i < items.size(); i++) {
            // loop-invariant: items[0..numTops-1] represents the current lead collection in a heap data structure
            //  the root of the heap is the currently trailing item in the lead collection,
            //  which will lose its membership if a better item is found from position i onwards
            E item = items.get(i);
            E worstLeadItem = items.get(0);
            if (comparator.compare(item, worstLeadItem) < 0) {
                // item < worstLeadItem, so shall be included in the lead collection
                items.set(0, item);
                // demote worstLeadItem back to the tail collection, at the orginal position of item
                items.set(i, worstLeadItem);
                // repair the heap condition of the lead collection
                heapSink(items, numTops, reverseComparator);
            }
        }

        // the first numTops positions of the list now contain the lead collection
        // the reverseComparator heap condition applies to this lead collection
        // now use heapSort to realise full ordening of this collection
        for (int i = numTops - 1; i > 0; i--) {
            // loop-invariant: items[i+1..numTops-1] contains the tail part of the sorted lead collection
            // position 0 holds the root item of a heap of size i+1 organised by reverseComparator
            // this root item is the worst item of the remaining front part of the lead collection
            E item = items.get(i);
            E worstItem = items.get(0);

            if (comparator.compare(worstItem, item) > 0) {
                // item > worstLeadItem, so shall be included in the lead collection

                Collections.swap(items, 0, i);

                heapSink(items, i, reverseComparator);
            }

        }
        return items;
    }

    /**
     * Repairs the zero-based heap condition for items[heapSize-1] on the basis of the comparator
     * all items[0..heapSize-2] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSwim(List<E> items, int heapSize, Comparator<E> comparator) {
        int childIndex = heapSize - 1;
        int parentIndex = childIndex / 2;
        E swimmer = items.get(childIndex);

        while (parentIndex >= 0 && comparator.compare(items.get(parentIndex), swimmer) > 0) {
            // swap swimmer with parent
            Collections.swap(items, childIndex, parentIndex);
            // proceed with next level towards the root
            childIndex = parentIndex;
            parentIndex = childIndex / 2;
            swimmer = items.get(childIndex);
        }
    }

    /**
     * Repairs the zero-based heap condition for its root items[0] on the basis of the comparator
     * all items[1..heapSize-1] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSink(List<E> items, int heapSize, Comparator<E> comparator) {
        int parentIndex = 0;
        int childIndex = 1;

        while (childIndex < heapSize) {
            E sinker = items.get(parentIndex);
            E child = items.get(childIndex);
            int compareResult = comparator.compare(child, items.get(childIndex + 1));

            if (childIndex + 1 < heapSize && compareResult > 0) {
                childIndex++;
                child = items.get(childIndex);
            }

            if (comparator.compare(sinker, child) < 0) break;

            Collections.swap(items, parentIndex, childIndex);
            parentIndex = childIndex;
            childIndex = 2 * parentIndex;
        }
    }
}
