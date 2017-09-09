package slapshotapp.game.support;

import android.graphics.drawable.Drawable;

public class Cell {
    private final int CELL_NOT_OWNED = -1;
    private int _Row, _Column;
    private Drawable _MyImage, _MyEmptyImage;
    private int _MyOwner;

    /*
     * Constructor
     *
     * @param row The row number for the cell
     * @param col The column number for the cell
     * @param emptyImage The empty image for the cell
     */
    public Cell(int row, int col, Drawable emptyImage) {
        _Row = row;
        _Column = col;
        _MyEmptyImage = _MyImage = emptyImage;
        _MyOwner = CELL_NOT_OWNED;
    }

    /*
     * Returns the cells non-empty image
     *
     * @return The cell non-empty drawable
     */
    public Drawable GetImage() {
        return _MyImage;
    }

    /*
     * Clears the cell contents, resets image to
     * empty image.
     */
    public void ClearCell() {
        _MyImage = _MyEmptyImage;
        _MyOwner = CELL_NOT_OWNED;
    }

    public int GetCellOwner() {
        return _MyOwner;
    }

    /*
     * Determines if the cell owner is from the given player.
     *
     * @param playerID ID to check if cell contains
     *
     * @return true if cell contains playerID, false otherwise
     */
    public boolean CellContains(int playerID) {
        return (_MyOwner == playerID);
    }

    /*
     * Checks to see if the cell is empty by
     * checking if there is an owner of the cell.
     *
     * @return true if the cell is empty, false otherwise
     */
    public boolean IsCellEmpty() {
        boolean cellEmpty = false;

        if (_MyOwner == CELL_NOT_OWNED) {
            cellEmpty = true;
        }

        return cellEmpty;
    }

    /*
     * Sets the cell state to a given image.
     *
     * @param image The drawable image
     * @param owner The owner ID of the cell
     *
     * @param true if successful false if the cell already has an owner
     */
    public boolean SetCellState(Drawable image, int ownerID) {
        boolean result = false;

        //only allow the cell state to be set if it is empty
        if (IsCellEmpty()) {
            result = true;
            _MyImage = image;
            _MyOwner = ownerID;
        }

        return result;
    }

    /*
     * Returns the cell row number.
     *
     * @return The cell row number
     */
    public int GetCellRow() {
        return _Row;
    }

    /*
     * Returns the cell column number.
     *
     * @return The cell column number
     */
    public int GetCellColumn() {
        return _Column;
    }
}
