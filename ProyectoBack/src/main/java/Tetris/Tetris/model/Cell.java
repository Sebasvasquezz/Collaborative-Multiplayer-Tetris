package Tetris.Tetris.model;


/**
 * Represents a cell in the Tetris game grid.
 */
public class Cell {
    private String value;
    private String status;
    private String color;

     /**
     * Default constructor for Cell.
     * Initializes the cell with default values.
     */
    public Cell() {
        this.value = "0";
        this.status = "clear";
        this.color = "0, 0, 0";
    }

    /**
     * Constructor for Cell with specified values.
     * 
     * @param value the value of the cell
     * @param status the status of the cell
     * @param color the color of the cell in RGB format
     */
    public Cell(String value, String status, String color) {
        this.value = value;
        this.status = status;
        this.color = color;
    }

     /**
     * Gets the value of the cell.
     * 
     * @return the value of the cell
     */
    public String getValue() {
        return value;
    }

    
    /**
     * Sets the value of the cell.
     * 
     * @param value the new value of the cell
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the status of the cell.
     * 
     * @return the status of the cell
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the cell.
     * 
     * @param status the new status of the cell
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the color of the cell.
     * 
     * @return the color of the cell in RGB format
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the color of the cell.
     * 
     * @param color the new color of the cell in RGB format
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Returns a string representation of the cell.
     * 
     * @return a string representation of the cell
     */
    @Override
    public String toString() {
        return "Cell{" +
                "value='" + value + '\'' +
                ", status='" + status + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}