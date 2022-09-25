package models;

public abstract class Wagon {
    protected int id;               // some unique ID of a Wagon
    private Wagon nextWagon;        // another wagon that is appended at the tail of this wagon
    // a.k.a. the successor of this wagon in a sequence
    // set to null if no successor is connected
    private Wagon previousWagon;    // another wagon that is prepended at the front of this wagon
    // a.k.a. the predecessor of this wagon in a sequence
    // set to null if no predecessor is connected

    // representation invariant propositions:
    // tail-connection-invariant:   wagon.nextWagon == null or wagon == wagon.nextWagon.previousWagon
    // front-connection-invariant:  wagon.previousWagon == null or wagon = wagon.previousWagon.nextWagon

    public Wagon(int wagonId) {
        this.id = wagonId;
    }

    public int getId() {
        return id;
    }

    public Wagon getNextWagon() {
        return nextWagon;
    }

    public Wagon getPreviousWagon() {
        return previousWagon;
    }

    /**
     * @return whether this wagon has a wagon appended at the tail
     */
    public boolean hasNextWagon() {
        return this.nextWagon != null;
    }

    /**
     * @return whether this wagon has a wagon prepended at the front
     */
    public boolean hasPreviousWagon() {
        return this.previousWagon != null;
    }

    /**
     * Returns the last wagon attached to it,
     * if there are no wagons attached to it then this wagon is the last wagon.
     *
     * @return the last wagon
     */
    public Wagon getLastWagonAttached() {

        Wagon currentWagon = this;

        while (currentWagon.hasNextWagon()) {
            currentWagon = currentWagon.getNextWagon();
        }
        return currentWagon;
    }

    /**
     * @return the length of the sequence of wagons towards the end of its tail
     * including this wagon itself.
     */
    public int getSequenceLength() {

        Wagon currentWagon = this;
        int length = 1; // initial length of the sequence

        while (currentWagon.hasNextWagon()) {
            currentWagon = currentWagon.getNextWagon();
            length++;
        }

        return length;
    }

    /**
     * Attaches the tail wagon and its connected successors behind this wagon,
     * if and only if this wagon has no wagon attached at its tail
     * and if the tail wagon has no wagon attached in front of it.
     *
     * @param tail the wagon to attach behind this wagon.
     * @throws IllegalStateException if this wagon already has a wagon appended to it.
     * @throws IllegalStateException if tail is already attached to a wagon in front of it.
     *                               The exception should include a message that reports the conflicting connection,
     *                               e.g.: "%s is already pulling %s"
     *                               or:   "%s has already been attached to %s"
     */
    public void attachTail(Wagon tail) {

        if (hasNextWagon())
            throw new IllegalStateException(this.nextWagon + " has already been attached to " + this);

        if (tail.hasPreviousWagon())
            throw new IllegalStateException(tail + " is already pulling " + tail.previousWagon);

        this.nextWagon = tail;
        tail.previousWagon = this;
    }

    /**
     * Detaches the tail from this wagon and returns the first wagon of this tail.
     *
     * @return the first wagon of the tail that has been detached
     * or <code>null</code> if it had no wagons attached to its tail.
     */
    public Wagon detachTail() {

        Wagon firstWagonOfTail = this.nextWagon;

        if (firstWagonOfTail != null) {
            this.nextWagon = null; //detach tail
            firstWagonOfTail.previousWagon = null;

            return firstWagonOfTail;
        }
        return null;
    }

    /**
     * Detaches this wagon from the wagon in front of it.
     * No action if this wagon has no previous wagon attached.
     *
     * @return the former previousWagon that has been detached from,
     * or <code>null</code> if it had no previousWagon.
     */
    public Wagon detachFront() {
        if (hasPreviousWagon()) {
            Wagon previousWagon = this.previousWagon;
            previousWagon.nextWagon = null;
            this.previousWagon = null;

            return previousWagon;
        }
        return null;
    }

    /**
     * Replaces the tail of the front wagon by this wagon and its connected successors
     * Before such reconfiguration can be made,
     * the method first disconnects this wagon from its predecessor,
     * and the <code>front</code> wagon from its current tail.
     *
     * @param front the wagon to which this wagon must be attached to.
     */
    public void reAttachTo(Wagon front) {
        this.detachFront(); // Disconnect wagon from its predecessor
        front.detachTail(); // Disconnect front wagon from its current tail
        front.attachTail(this); // Attaches the tail of this wagon and its successors to the front wagon
    }

    /**
     * Removes this wagon from the sequence that it is part of,
     * and reconnects its tail to the wagon in front of it, if any.
     */
    public void removeFromSequence() {
        if (hasPreviousWagon() && hasNextWagon()) {
            Wagon previousWagon = this.previousWagon;
            this.detachFront();
            Wagon nextWagon = this.nextWagon;
            this.detachTail();
            previousWagon.attachTail(nextWagon);
        } else if (hasPreviousWagon()) {
            this.detachFront();
        } else if (hasNextWagon()) {
            this.detachTail();
        }
    }


    /**
     * Reverses the order in the sequence of wagons from this Wagon until its final successor.
     * The reversed sequence is attached again to the wagon in front of this Wagon, if any.
     * No action if this Wagon has no succeeding next wagon attached.
     *
     * @return the new start Wagon of the reversed sequence (with is the former last Wagon of the original sequence)
     */
    public Wagon reverseSequence() {
        Wagon frontWagon = this;
        Wagon detachedHead = null;

        // Checks if the wagon had a previous wagon if so it will be set and detached from the wagons which are going to reverse
        if (hasPreviousWagon()) {
            detachedHead = previousWagon;
            detachedHead.detachTail();
        }

        // Set the reversed wagon, so it can be set as the front wagon later on
        Wagon reversedWagon = frontWagon.getLastWagonAttached();

        // Set the current wagon
        Wagon currentWagon = frontWagon;

        // Keep looping while the current wagon has a next wagon
        while (hasNextWagon()) {
            // set the reversed wagon from the sequence and the wagon who will become detached during the process
            Wagon reversedSequence = currentWagon.getLastWagonAttached();
            Wagon detachedWagon = currentWagon.previousWagon;

            // Detach the front of both the reversed sequence wagon and current wagon
            reversedSequence.detachFront();
            currentWagon.detachFront();

            // Checks if the detached wagon is present
            if (detachedWagon != null) {
                // Detaches and attaches the tail of the detached wagon to the reversed sequence wagon
                detachedWagon.detachTail();
                detachedWagon.attachTail(reversedSequence);
            }
            // Attach the tail of the reserved sequence wagon to the current wagon
            reversedSequence.attachTail(currentWagon);
        }

        // Make the reversed wagon the new front
        frontWagon = reversedWagon;

        // Checks if the detachedHead is present
        if (detachedHead != null) {
            // Reconnects the reversed wagons back to the detached wagons
            detachedHead.attachTail(frontWagon);
        }
        // Return the front wagon
        return frontWagon;
    }

    @Override
    public String toString() {
        return "[Wagon-" + id + "]";
    }
}
