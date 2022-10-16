package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class OrderedArrayList<E> extends ArrayList<E> implements OrderedList<E> {

    protected Comparator<? super E> ordening;   // the comparator that has been used with the latest sort
    protected int nSorted;                      // the number of sorted items in the first section of the list
    // representation-invariant
    // all items at index positions 0 <= index < nSorted have been ordered by the given ordening comparator
    // other items at index position nSorted <= index < size() can be in any order amongst themselves
    // and also relative to the sorted section

    public OrderedArrayList() {
        this(null);
    }

    public OrderedArrayList(Comparator<? super E> ordening) {
        super();
        this.ordening = ordening;
        this.nSorted = 0;
    }

    public Comparator<? super E> getOrdening() {
        return this.ordening;
    }

    @Override
    public void clear() {
        super.clear();
        this.nSorted = 0;
    }

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        this.ordening = c;
        this.nSorted = this.size();
    }

    @Override
    public void add(int index, E element) {
        if (index <= nSorted) this.nSorted = index;
        super.add(index, element);
    }

    @Override
    public E remove(int index) {
        if (index <= nSorted) this.nSorted--;
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        this.nSorted--;
        return super.remove(o);
    }

    @Override
    public void sort() {
        if (this.nSorted < this.size()) {
            this.sort(this.ordening);
        }
    }

    @Override
    public int indexOf(Object item) {
        // efficient search can be done only if you have provided an ordening for the list
        if (this.getOrdening() != null) {
            return indexOfByIterativeBinarySearch((E) item);
        } else {
            return super.indexOf(item);
        }
    }

    @Override
    public int indexOfByBinarySearch(E searchItem) {
        if (searchItem != null) {
            // some arbitrary choice to use the iterative or the recursive version
            return indexOfByRecursiveBinarySearch(searchItem);
        } else {
            return -1;
        }
    }

    /**
     * finds the position of the searchItem by an iterative binary search algorithm in the
     * sorted section of the arrayList, using the this.ordening comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.ordening comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for ordening items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.ordening
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {

        int start = 0;
        int end = nSorted -1;

        if (start == this.size()) return -1;

        while (from <= to) { // Keep looping while the start index is equal or smaller than the last index of the sorted list
            int midIndex = (from + to) / 2; // make the middle index by splitting the sorted list in half
            int compareResult = this.ordening.compare(searchItem, this.get(midIndex)); // compare the search item with the existing item in the middle index

            // The compare result checks if the item is found at the midIndex, before the midIndex or after the midIndex
            if (compareResult == 0) {
                return midIndex;
            } else if (compareResult > 0) {
                from = midIndex + 1; // the new start index will be after the midIndex, cutting the list in half
            } else if (compareResult < 0) {
                to = midIndex - 1; // the new end index will be before the midIndex, cutting the list in half
            }
        }

        // If the search item isn't found in the sorted part of the list the unsorted part will be looped through
        for (int i = nSorted; i < this.size(); i++) {
            if (this.ordening.compare(searchItem,this.get(i)) == 0) {
                return i;
            }
        }

        return -1; // item wasn't found
    }

    /**
     * finds the position of the searchItem by a recursive binary search algorithm in the
     * sorted section of the arrayList, using the this.ordening comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.ordening comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for ordening items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.ordening
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem) {

        int from = 0; // Start position
        int to = nSorted - 1; // The number of sorted items in the first section of the list

        return indexOfByRecursiveBinarySearch(searchItem, from, to); // immediately return the same method where the from and to can be altered
    }

    public int indexOfByRecursiveBinarySearch(E searchItem,int from,int to){

        if (from <= to) {
            int midIndex = (from + to) / 2; // take the index of the middle of the list
            if (this.ordening.compare(searchItem,this.get(midIndex)) > 0) {
                from = midIndex + 1;
                return indexOfByRecursiveBinarySearch(searchItem,from,to);
            } else if (this.ordening.compare(searchItem,this.get(midIndex)) < 0) {
                to = midIndex - 1;
                return indexOfByRecursiveBinarySearch(searchItem,from,to);
            } else {
                return midIndex; // If it is found at the same index midIndex will be returned
            }
        } else {
            // If no match was found, a linear search has to be done in the unsorted section of the list
            for (int i = nSorted; i < this.size(); i++) {
                if (this.ordening.compare(searchItem,this.get(i)) == 0) {
                    return i;
                }
            }
        }
        return -1; // return -1 if the item wasn't found
    }


    /**
     * finds a match of newItem in the list and applies the merger operator with the newItem to that match
     * i.e. the found match is replaced by the outcome of the merge between the match and the newItem
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param newItem
     * @param merger  a function that takes two items and returns an item that contains the merged content of
     *                the two items according to some merging rule.
     *                e.g. a merger could add the value of attribute X of the second item
     *                to attribute X of the first item and then return the first item
     * @return whether a new item was added to the list or not
     */
    @Override
    public boolean merge(E newItem, BinaryOperator<E> merger) {
        if (newItem == null) return false; // if there is no new item it will return false
        int matchedItemIndex = this.indexOfByRecursiveBinarySearch(newItem); // search for the index of the new item

        // Matched item index returns -1 if the item doesn't exist yet, so it will be added as a new item to the list
        if (matchedItemIndex < 0) {
            this.add(newItem);
            return true;
        } else {
            E matchedItem = this.get(matchedItemIndex); // retrieve the item from the array on the matched item index
            E mergedItem = merger.apply(newItem, matchedItem); // merge existing item with the new item
            this.set(matchedItemIndex, mergedItem); // replace the item in the array with the new merged item
            return false;
        }
    }

    /**
     * calculates the total sum of contributions of all items in the list
     *
     * @param mapper a function that calculates the contribution of a single item
     * @return the total sum of all contributions
     */
    @Override
    public double aggregate(Function<E, Double> mapper) {
        double sum = 0.0;

        for (E e : this) {
            sum += mapper.apply(e);
        }

        return sum;
    }
}
