package models;

public class Train {
    private final String origin;
    private final String destination;
    private final Locomotive engine;
    private Wagon firstWagon;

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
    }

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
        // If train has wagons
        if (hasWagons()) {

            // Initialize total wagons with 1
            int totalWagons = 1;

            // Set first wagon as current wagon
            Wagon currentWagon = firstWagon;

            // While the current wagon has a next wagon
            while (currentWagon.hasNextWagon()) {
                // Set the next wagon as the current wagon
                currentWagon = currentWagon.getNextWagon();
                // Increase the total with 1
                totalWagons++;
            }
            return totalWagons;
        }
        return 0;
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        // Set the first wagon as the last wagon
        Wagon lastWagon = getFirstWagon();

        // If train has wagons
        if (hasWagons()) {
            // While the last wagon has a next wagon
            while (lastWagon.hasNextWagon()) {
                // Set the next wagon as the new last wagon
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
        // If the train is a passenger train and the train has wagons
        if (hasWagons() && isPassengerTrain()) {

            // Set the first wagon as the current wagon
            Wagon currentWagon = firstWagon;

            // Initialize the number of seats with the number of seats from the current wagon
            int totalNumberOfSeats = ((PassengerWagon) currentWagon).getNumberOfSeats();

            // While the current wagon has a next wagon
            while (currentWagon.hasNextWagon()) {
                // Set the next wagon as the current wagon
                currentWagon = currentWagon.getNextWagon();
                // Increase number of seats with the number of the new current wagon
                totalNumberOfSeats += ((PassengerWagon) currentWagon).getNumberOfSeats();
            }
            return totalNumberOfSeats;
        }
        return 0; // Return 0 if train has nog wagons or if it's a freight train
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        // If the train is a freight train and has wagons
        if (hasWagons() && isFreightTrain()) {

            // Set the first wagon as the current wagon
            Wagon currentWagon = firstWagon;

            // Initialize the total max weight with the max weight of the current wagon
            int totalMaxWeight = ((FreightWagon) currentWagon).getMaxWeight();

            // While the current wagon has a next wagon
            while (currentWagon.hasNextWagon()) {
                // Set the next wagon as the current wagon
                currentWagon = currentWagon.getNextWagon();
                // Increase total max weight with the total max weight of the current wagon
                totalMaxWeight += ((FreightWagon) currentWagon).getMaxWeight();
            }
            return totalMaxWeight;
        }
        return 0; // Return 0 if train has no wagons or if it's a passenger train
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

        // Make current wagon variable of the first wagon
        Wagon currentWagon = firstWagon;

        // Loop through all the wagons
        for (int i = 0; i < getNumberOfWagons(); i++) {
            // If the id of the current wagon matches the id of the wagon in the loop
            if (currentWagon.getId() == wagonId) {
                return currentWagon;  //return the wagon found with the given id
            }
            // Take the next wagon if the wagons didn't match
            currentWagon = currentWagon.getNextWagon();
        }
        return null; // If no wagon was found with the given id
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

        // If the wagon is null, it's invalid
        if (wagon == null) {
            return false;
        }

        // Verifies if the type of wagon matches its type of train
        if ((isFreightTrain() && wagon instanceof PassengerWagon) || (isPassengerTrain() && wagon instanceof FreightWagon)) {
            return false;
        }

        // The total number of wagons that the train has combined with the number of wagons behind the head wagon
        int totalNumberOfWagons = wagon.getSequenceLength() + getNumberOfWagons();

        // returns whether there is an already existing wagon with the same id,
        // and if the total amount of wagons is less than the max allowed wagons of the engine
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

        // If attachment is possible
        if (canAttach(wagon)) {

            // Detach predecessors of wagon
            wagon.detachFront();

            // If the train has wagons
            if (hasWagons()) {
                // Take the last wagon of the train (rear)
                Wagon rearWagon = getLastWagonAttached();
                // Attach the wagon to the rear
                rearWagon.attachTail(wagon);
            } else {
                // If the train doesn't have wagons, set the wagon as the first wagon of the train
                this.firstWagon = wagon;
            }
            return true;
        }
        return false;
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

        // If attachment is possible
        if (canAttach(wagon)) {

            // Detach predecessors of wagon
            wagon.detachFront();

            // If train has wagons
            if (hasWagons()) {
                // Take the current first wagon of the train
                Wagon currentFirstWagon = this.firstWagon;
                // Attach the current first wagon at the end of the wagon sequence to be inserted
                wagon.getLastWagonAttached().attachTail(currentFirstWagon);
            }
            // Insert the wagon sequence at the front of the train
            this.firstWagon = wagon;
            return true;
        }
        return false;
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
        final int firstWagonPosition = 1;

        // If position is invalid return false
        if (position < firstWagonPosition || position > getNumberOfWagons() + 1) {
            return false;
        }

        // If the train has no wagons or the position is the first position, insert wagon at front
        if (!hasWagons() || position == firstWagonPosition) {
            return insertAtFront(wagon);
        }

        // If the position is behind the last wagon, attach wagon to rear
        if (position == getNumberOfWagons() + 1) {
            return attachToRear(wagon);
        }

        if (canAttach(wagon)) {

            // Detach predecessors of wagon
            wagon.detachFront();

            // Find wagon at given position
            Wagon positionedWagon = findWagonAtPosition(position);

            // Detach its predecessors
            positionedWagon.detachFront();

            // Set the previous position
            int previousPosition = position - 1;

            // The wagon who was at the position will be attached at the end of the wagons sequence
            wagon.getLastWagonAttached().attachTail(positionedWagon);

            // The new wagon sequence will be attached to the previous position
            findWagonAtPosition(previousPosition).attachTail(wagon);

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
        // Checks if the wagon found by ID can be attached to the train and if the train has wagons
        if (toTrain.canAttach(findWagonById(wagonId)) && hasWagons()) {

            // Set the wagon who will be removed
            Wagon wagonToRemoved = findWagonById(wagonId);

            // If the wagon who will be removed is the first wagon
            // ,the trains new first wagon will be the wagon that came after the previous first wagon
            if (wagonToRemoved == this.firstWagon) {
                this.firstWagon = wagonToRemoved.getNextWagon();
            }

            // remove the wagon from the sequence and attach it to the rear of the new train
            wagonToRemoved.removeFromSequence();
            toTrain.attachToRear(wagonToRemoved);
            return true;
        }
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
        final int firstWagonPosition = 1;

        // Returns false when the train has no wagons or if the position isn't valid
        if (!hasWagons() || position < firstWagonPosition || position > getNumberOfWagons()) {
            return false;
        }

        // Set the positioned wagon to the wagon found on the given position
        Wagon positionedWagon = findWagonAtPosition(position);

        // Checks if the train can attach the positioned wagon and its sequence
        if (toTrain.canAttach(positionedWagon)) {

            // Detach the wagon sequence and attaches it to the new train's rear
            positionedWagon.detachFront();
            toTrain.attachToRear(positionedWagon);

            // Checks if the given position was the position of the first wagon, if so the train loses its first wagon
            if (position == firstWagonPosition) {
                this.firstWagon = null;
            }
            return true;
        }
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
        // If the train has more than one wagon
        if (getNumberOfWagons() > 1) {
            // The last wagon of the current sequence
            Wagon lastWagon = getLastWagonAttached();
            // Reverse the sequence
            this.firstWagon.reverseSequence();
            // The last wagon becomes the first wagon
            firstWagon = lastWagon;
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 1; i <= getNumberOfWagons(); i++) {
            s.append(findWagonAtPosition(i));
        }
        return engine.toString() + s + " with " + getNumberOfWagons() + " wagons from " + getOrigin() + " to " + getDestination();
    }
}
