package main;

import org.jetbrains.annotations.Range;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Sudoku implements Externalizable {

    private static final long serialVersionUID = 1L;

    private static class Entry {
        private final byte row, column;

        private Entry(byte i, byte j) {
            row = i;
            column = j;
        }
    }

    private boolean[][] userSet = new boolean[9][9];
    private byte[][] array = new byte[9][9];
    private final List<Consumer<Sudoku>> listeners = new LinkedList<>();
    private Stack<Entry> stack = new Stack<>();

    /**
     * Creates a new Sudoku with a default value of 0 for all entries
     */
    public Sudoku() {
        forAll((i, j) -> {
            array[i][j] = 0;
            userSet[i][j] = false;
        });
    }

    @SuppressWarnings("unchecked")
    private Sudoku(byte[][] contents, boolean[][] flags, Stack<Entry> stack) {
        forAll((i, j) -> {
            array[i][j] = contents[i][j];
            userSet[i][j] = flags[i][j];
        });
        this.stack = (Stack<Entry>) stack.clone();
    }

    /**
     * Returns the entry at position {@literal (i, j)}
     *
     * @param i the desired number's row, between 0 and 8 (inclusive)
     * @param j the desired number's column, between 0 and 8 (inclusive)
     * @return the number at position {@literal (i, j)}, between 0 and 9 (inclusive)
     * @throws IllegalArgumentException if a parameter exceeds its specified range
     * @see #set(int, int, byte)
     */
    public byte get(@Range(from = 0, to = 8) int i, @Range(from = 0, to = 8) int j) {
        try {
            return array[i][j];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Sets the number at position (i, j) to the specified value
     *
     * @param row    the row, between 0 and 8 (inclusive)
     * @param column the column, between 0 and 8 (inclusive)
     * @param number the new value, between 0 and 9 (inclusive)
     * @throws IllegalArgumentException if a parameter exceeds its specified bounds
     * @see #get(int, int)
     */
    public void set(@Range(from = 0, to = 8) int row, @Range(from = 0, to = 8) int column, @Range(from = 0, to = 9) byte number) {
        if (number < 0 || number > 9)
            throw new IllegalArgumentException("The number may not be negative or greater than 9");

        try {
            final Sudoku copy = clone();
            array[row][column] = number;
            userSet[row][column] = number != 0;
            notifyListeners(copy);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Evaluates whether the specified number can be placed at the specified position, i.e. whether the row, the column
     * and the respective 3x3 box already contain the number.
     *
     * @param row    the row, between 0 and 8 (inclusive)
     * @param column the column, between 0 and 8 (inclusive)
     * @param number the number, betwwen 0 and 9 (inclusive)
     * @return whether the number can be placed at the specified position without breaking the rules, {@code true} if
     * {@code number == 0}, {@code false} if an argument exceeds the specified range
     */
    public boolean isValid(@Range(from = 0, to = 8) int row, @Range(from = 0, to = 8) int column, @Range(from = 0, to = 9) int number) {
        if (number < 0 || number > 9 || row < 0 || row >= array.length || column < 0 || column >= array[row].length)
            return false;
        if (number == 0)
            return true;

        // check row
        for (int i = 0; i < array[row].length; i++) {
            if (array[row][i] == number && i != column)
                return false;
        }

        //check column
        for (int i = 0; i < array.length; i++) {
            if (array[i][column] == number && i != row)
                return false;
        }

        //check box
        int iBox = row - row % 3, jBox = column - column % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (array[iBox + i][jBox + j] == number && iBox + i != row && jBox + j != column)
                    return false;
            }
        }

        return true;
    }

    /**
     * Evaluates whether this Sudoku adheres to the rules, i.e. whether every field is either empty or contains a number
     * between 1 and 9, and if every number is allowed to be where it is. All this method does is internally calling
     * {@link #isValid(int, int, int)} for every single one of this Sudoku's entries.
     *
     * @return whether this Sudoku complies to the rules of the game
     * @see #isValid(int, int, int)
     */
    public boolean isValid() {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (!isValid(i, j, array[i][j]))
                    return false;
            }
        }
        return true;
    }

    /**
     * Resets this Sudoku to its initial state, including all solutions that have been computed until now. More
     * precisely, calling this method will delete all generated entries in this Sudoku and empty the stack that holds
     * the information on which solutions have already been calculated.
     * <p></p>
     * For easier use with {@link #clone()}, this method returns the Sudoku it is called on.
     *
     * @return this
     */
    public Sudoku reset() {
        final Sudoku copy = clone();
        forAll((i, j) -> {
            if (!userSet[i][j])
                array[i][j] = 0;
        });
        stack.clear();
        if (!equals(copy))
            notifyListeners(copy);
        return this;
    }

    /**
     * Solves this Sudoku, and returns whether it could be solved. If the Sudoku could not be solved, it remains
     * unchanged. If this Sudoku is already solved, this method tries to compute another solution. If all possible
     * solutions have already been calculated, this method does nothing and returns {@code false}
     *
     * @return whether the algorithm was able to compute a solution for this Sudoku
     * @see #getSolved()
     * @see #isSolved()
     */
    public boolean solve() {
        if (!isValid())
            return false;

        final Sudoku copy = clone();

        byte iStart = 0, jStart = 0;
        if (!stack.isEmpty()) {
            iStart = stack.peek().row;
            jStart = stack.pop().column;
        }

        for (byte i = iStart; i < array.length; i++) {
            for (byte j = jStart; j < array[i].length; j++) {
                jStart = 0;
                if (!userSet[i][j]) {
                    if (array[i][j] < 9) {
                        do array[i][j]++; while (array[i][j] < 9 && !isValid(i, j, array[i][j]));
                        if (array[i][j] == 9 && !isValid(i, j, array[i][j])) {
                            array[i][j] = 0;
                            if (!stack.isEmpty()) {
                                i = stack.peek().row;
                                j = stack.pop().column;
                                j--;
                            } else {
                                this.array = copy.array;
                                return false;
                            }
                        } else
                            stack.push(new Entry(i, j));
                    } else {
                        array[i][j] = 0;
                        if (!stack.isEmpty()) {
                            i = stack.peek().row;
                            j = stack.pop().column;
                            j--;
                        } else {
                            this.array = copy.array;
                            return false;
                        }
                    }
                }
            }
        }

        if (isSolved()) {
            notifyListeners(copy);
            return true;
        } else {
            array = copy.array;
            return false;
        }
    }

    /**
     * This method is equivalent to {@link #isValid()}, with the exception that no field in this Sudoku is allowed to be
     * 0
     *
     * @return whether this Sudoku is solved, i.e. every field is valid and between 1 and 9 respectively
     * @see #solve()
     * @see #getSolved()
     */
    public boolean isSolved() {
        for (byte[] ints : array) {
            for (byte anInt : ints) {
                if (anInt <= 0)
                    return false;
            }
        }
        return isValid();
    }

    /**
     * Copies this Sudoku, solves the copy and returns it. If this Sudoku's {@link #solve()} method returns {@code
     * false}, {@code getSolved()} simply returns a copy of this Sudoku
     *
     * @return a solved copy of this Sudoku
     * @see #solve()
     * @see #isSolved()
     */
    public Sudoku getSolved() {
        Sudoku s = clone();
        s.solve();
        return s;
    }

    /**
     * Adds a new change listener to this Sudoku. Change listeners are executed every time this Sudoku's internal array
     * of entries changes, i.e. when either one of the methods {@link #set(int, int, byte)}, {@link #reset()}, and
     * {@link #solve()} is called, and only if the respective method actually changes the internal array; if {@link
     * #solve()} is called but returns {@code false}, for example, the listeners are not notified. In other words, the
     * listeners are notified every time the values considered by {@link #equals(Object)} change, but not necessarily
     * the ones considered by {@link #deepEquals(Object)}.
     * <p></p>
     * The argument passed to the listeners is a copy of this Sudoku before the changes. Note that every listener gets
     * passed the same instance, so it may be necessary to copy it using {@link #clone()} before making any changes.
     * Moreover, every listener can only be added once to every Sudoku. When trying to add a listener more than once,
     * nothing happens.
     *
     * @param listener the listener that should be added
     * @see #removeChangeListener(Consumer)
     * @see #getListenersUnmodifiable()
     * @see #notifyListeners(Sudoku)
     */
    public final void addChangeListener(Consumer<Sudoku> listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    /**
     * Removes a change listener. If the listener is unknown to this Sudoku, nothing happens.
     *
     * @param listener the listener that should be removed
     * @see #addChangeListener(Consumer)
     * @see #getListenersUnmodifiable()
     * @see #notifyListeners(Sudoku)
     */
    public final void removeChangeListener(Consumer<Sudoku> listener) {
        listeners.remove(listener);
    }

    /**
     * Creates and returns an unmodifiable list of all change listeners that are currently added to this Sudoku
     *
     * @return a list of all change listeners
     * @see Collections#unmodifiableList(List)
     * @see #addChangeListener(Consumer)
     * @see #removeChangeListener(Consumer)
     * @see #notifyListeners(Sudoku)
     */
    public final List<Consumer<Sudoku>> getListenersUnmodifiable() {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Notifies all change listeners that have been added to this object. For more information on when and how this
     * method should be called, see {@link #addChangeListener(Consumer)}.
     *
     * @param previous a copy of this Sudoku before the changes
     * @see #addChangeListener(Consumer)
     * @see #removeChangeListener(Consumer)
     * @see #getListenersUnmodifiable()
     */
    protected final void notifyListeners(Sudoku previous) {
        for (Consumer<Sudoku> listener : listeners) {
            listener.accept(previous);
        }
    }

    /**
     * Returns an exact copy of this Sudoku. For the returned value, {@link #deepEquals(Object)} will be {@code true}.
     * To obtain a copy that is in its initial state (i.e. without any solutions having been computed yet, and without
     * non-user set entries), one can call {@code clone().}{@link #reset()}.
     *
     * @return an exact copy of this Sudoku
     */
    @Override
    public Sudoku clone() {
        return new Sudoku(array, userSet, stack);
    }

    /**
     * Checks this Sudoku and the other object for absolute equality. In contrast to {@link #equals(Object)}, this
     * method also considers which entries have been externally set and which solutions have already been calculated.
     *
     * @param o the other object
     * @return whether this Sudoku and the other object are 100% equal
     * @see #equals(Object)
     */
    public boolean deepEquals(Object o) {
        if (this == o)
            return true;
        if (!equals(o))
            return false;

        Sudoku s = (Sudoku) o;
        return s.stack.equals(this.stack) && Arrays.deepEquals(this.userSet, s.userSet);
    }

    /**
     * Evaluates whether this Sudoku and the other object are equal. In other words, if the other object is a Sudoku,
     * this method evaluates if both Sudokus contain the same numbers at the same positions, respectively.
     * <p></p>
     * Note that this method does not take into account which values have been set by the user and which have been
     * generated / loaded from a file, and which solutions have already been computed. For this, {@link
     * #deepEquals(Object)} should be used.
     *
     * @param o the other object
     * @return whether the other object is a Sudoku and contains the same entries
     * @see #deepEquals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sudoku sudoku = (Sudoku) o;
        return Arrays.deepEquals(this.array, sudoku.array);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(array);
    }

    @Override
    public String toString() {
        return Arrays.deepToString(array);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(array);
        out.writeObject(userSet);
        out.writeBoolean(!stack.isEmpty());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        array = (byte[][]) in.readObject();
        if (array.length != 9)
            throw new IOException("array.length != 9");
        for (int i = 0; i < array.length; i++) {
            if (array[i].length != 9)
                throw new IOException("array[" + i + "].length != 9");
        }

        userSet = (boolean[][]) in.readObject();
        if (userSet.length != 9)
            throw new IOException("userSet.length != 9");
        for (int i = 0; i < userSet.length; i++) {
            if (userSet[i].length != 9)
                throw new IOException("userSet[" + i + "].length != 9");
        }

        if (in.readBoolean()) {
            forAll((i, j) -> {
                if (!userSet[i][j])
                    stack.push(new Entry(i, j));
            });
        }
    }

    private void forAll(BiConsumer<Byte, Byte> action) {
        for (byte i = 0; i < array.length; i++) {
            for (byte j = 0; j < array[i].length; j++) {
                action.accept(i, j);
            }
        }
    }
}
