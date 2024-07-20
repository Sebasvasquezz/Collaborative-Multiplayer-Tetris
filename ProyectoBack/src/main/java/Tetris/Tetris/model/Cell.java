package Tetris.Tetris.model;

public class Cell {
    private String value;
    private String status;
    private String color;
    public Object getStatus;

    public Cell() {
        this.value = "0";
        this.status = "clear";
        this.color = "0, 0, 0";
    }

    public Cell(String value, String status, String color) {
        this.value = value;
        this.status = status;
        this.color = color;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "value='" + value + '\'' +
                ", status='" + status + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
