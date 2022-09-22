package models;

public class Train {
    private final String origin;
    private final String destination;
    private final Locomotive engine;
    private Wagon firstWagon;

    /* Representation invariants:
        firstWagon == null || firstWagon.previousWagon == null
        engine != null
     */

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
    }

    /* three helper methods that are useful in other methods */
    public boolean hasWagons() {
        return firstWagon != null;
    }

    public boolean isPassengerTrain() {
        return this.firstWagon instanceof PassengerWagon;
    }

    public boolean isFreightTrain() {
        return this.firstWagon instanceof FreightWagon;
    }

    public Locomotive getEngine() {
        return engine;
    }

    public Wagon getFirstWagon() {
        return firstWagon;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    /**
     * Replaces the current sequence of wagons (if any) in the train
     * by the given new sequence of wagons (if any)
     * (sustaining all representation invariants)
     *
     * @param wagon the first wagon of a sequence of wagons to be attached
     *              (can be null)
     */
    public void setFirstWagon(Wagon wagon) {
        this.firstWagon = wagon;
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        if (hasWagons()) {
            int totalWagons = 1;
            Wagon currentWagon = firstWagon;

            while (currentWagon.hasNextWagon()) {
                currentWagon = currentWagon.getNextWagon();
                totalWagons++;
            }
            return totalWagons;
        } else {
            return 0;
        }
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        Wagon lastWagon = getFirstWagon();
        if (hasWagons()) {
            while (lastWagon.hasNextWagon()) {
                lastWagon = lastWagon.getNextWagon();
            }
        }
        return lastWagon;
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        if (isFreightTrain()) {
            return 0;
        } else {
            if (hasWagons()) {
                Wagon currentWagon = firstWagon;
                int numerOfSeats = ((PassengerWagon) currentWagon).getNumberOfSeats();
                while (currentWagon.hasNextWagon()) {
                    currentWagon = currentWagon.getNextWagon();
                    numerOfSeats += ((PassengerWagon) currentWagon).getNumberOfSeats();
                }
                return numerOfSeats;
            } else {
                return 0;
            }
        }
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        if (isPassengerTrain()) {
            return 0;
        } else {
            if (hasWagons()) {
                Wagon currentWagon = firstWagon;
                int maxWeight = ((FreightWagon) currentWagon).getMaxWeight();
                while (currentWagon.hasNextWagon()) {
                    currentWagon = currentWagon.getNextWagon();
                    maxWeight += ((FreightWagon) currentWagon).getMaxWeight();
                }
                return maxWeight;
            } else {
                return 0;
            }
        }
    }

    /**
     * Finds the wagon at the given position (starting at 1 for the first wagon of the train)
     *
     * @param position
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {

        if (!hasWagons() || position <= 0 || position > getNumberOfWagons())
            return null;

        Wagon currentWagon = firstWagon;

        for (int i = 1; i < position; i++) {
            currentWagon = currentWagon.getNextWagon();
        }

        return currentWagon;
    }

    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {

        Wagon currentWagon = firstWagon;

        for (int i = 0; i < getNumberOfWagons(); i++) {
            if (currentWagon.getId() == wagonId) {
                return currentWagon;
            }
            currentWagon = currentWagon.getNextWagon();
        }
        return null;
    }

    /**
     * Determines if the given sequence of wagons can be attached to this train
     * Verifies if the type of wagons match the type of train (Passenger or Freight)
     * Verifies that the capacity of the engine is sufficient to also pull the additional wagons
     * Verifies that the wagon is not part of the train already
     * Ignores the predecessors before the head wagon, if any
     *
     * @param wagon the head wagon of a sequence of wagons to consider for attachment
     * @return whether type and capacity of this train can accommodate attachment of the sequence
     */
    public boolean canAttach(Wagon wagon) {

        int totalNumberOfWagons = wagon.getSequenceLength() + getNumberOfWagons();

        if (isFreightTrain() && wagon instanceof PassengerWagon) {
            return false;
        }
        if (isPassengerTrain() && wagon instanceof FreightWagon) {
            return false;
        }
        return findWagonById(wagon.getId()) == null && totalNumberOfWagons <= this.engine.getMaxWagons();
    }

    /**
     * Tries to attach the given sequence of wagons to the rear of the train
     * No change is made if the attachment cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if attachment is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be attached
     * @return whether the attachment could be completed successfully
     */
    public boolean attachToRear(Wagon wagon) {
        if (canAttach(wagon)) {
            wagon.detachFront();
            if (this.hasWagons()) {
                Wagon rearWagon = this.getLastWagonAttached();
                rearWagon.attachTail(wagon);
            } else {
                this.firstWagon = wagon;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tries to insert the given sequence of wagons at the front of the train
     * (the front is at position one, before the current first wagon, if any)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if insertion is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtFront(Wagon wagon) {
        if (canAttach(wagon)) {
            wagon.detachFront();
            Wagon currentFirstWagon = this.firstWagon;
            if (hasWagons()) {
                wagon.getLastWagonAttached().attachTail(currentFirstWagon);
            }
            this.firstWagon = wagon;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tries to insert the given sequence of wagons at/before the given position in the train.
     * (The current wagon at given position including all its successors shall then be reattached
     * after the last wagon of the given sequence.)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity
     * or the given position is not valid for insertion into this train)
     * if insertion is possible, the head wagon of the sequence is first detached from its predecessors, if any
     *
     * @param position the position where the head wagon and its successors shall be inserted
     *                 1 <= position <= numWagons + 1
     *                 (i.e. insertion immediately after the last wagon is also possible)
     * @param wagon    the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtPosition(int position, Wagon wagon) {
        if (canAttach(wagon)) {
            wagon.detachFront();
            if (hasWagons() && position != 1) {
                if (position < this.getNumberOfWagons()) {
                    Wagon positionedWagon = findWagonAtPosition(position);
                    positionedWagon.detachFront();
                    if (positionedWagon != this.firstWagon) {
                        int previousPosition = position - 1;
                        wagon.attachTail(positionedWagon);
                        findWagonAtPosition(previousPosition).attachTail(wagon);
                    }
                }
                attachToRear(wagon);
            }
            insertAtFront(wagon);
            return true;
        }
        return false;
    }

    /**
     * Tries to remove one Wagon with the given wagonId from this train
     * and attach it at the rear of the given toTrain
     * No change is made if the removal or attachment cannot be made
     * (when the wagon cannot be found, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param wagonId the id of the wagon to be removed
     * @param toTrain the train to which the wagon shall be attached
     *                toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean moveOneWagon(int wagonId, Train toTrain) {
        // TODO

        return false;
    }

    /**
     * Tries to split this train before the wagon at given position and move the complete sequence
     * of wagons from the given position to the rear of toTrain.
     * No change is made if the split or re-attachment cannot be made
     * (when the position is not valid for this train, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param position 1 <= position <= numWagons
     * @param toTrain  the train to which the split sequence shall be attached
     *                 toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean splitAtPosition(int position, Train toTrain) {
        // TODO

        return false;
    }

    /**
     * Reverses the sequence of wagons in this train (if any)
     * i.e. the last wagon becomes the first wagon
     * the previous wagon of the last wagon becomes the second wagon
     * etc.
     * (No change if the train has no wagons or only one wagon)
     */
    public void reverse() {
        // TODO

    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 1; i < getNumberOfWagons(); i++) {
            s.append(findWagonAtPosition(i));
        }
        return engine.toString() + s + " with " + getNumberOfWagons() + " wagons from " + getOrigin() + " to " + getDestination();
    }
}
