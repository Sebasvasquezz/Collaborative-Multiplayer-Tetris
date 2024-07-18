package Tetris.Tetris.model;

public class Cell {
    private String value;
    private String status;

    public Cell() {
        this.value = "0";
        this.status = "clear";
    }

    public Cell(String value, String status) {
        this.value = value;
        this.status = status;
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

    @Override
    public String toString() {
        return "Cell{" +
                "value='" + value + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

