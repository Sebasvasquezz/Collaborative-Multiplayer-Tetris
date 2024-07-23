package Tetris.Tetris.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CellTest {

    @Test
    public void testDefaultConstructor() {
        Cell cell = new Cell();
        assertEquals("0", cell.getValue());
        assertEquals("clear", cell.getStatus());
        assertEquals("0, 0, 0", cell.getColor());
    }

    @Test
    public void testParameterizedConstructor() {
        Cell cell = new Cell("1", "merged", "255, 255, 255");
        assertEquals("1", cell.getValue());
        assertEquals("merged", cell.getStatus());
        assertEquals("255, 255, 255", cell.getColor());
    }

    @Test
    public void testGetValue() {
        Cell cell = new Cell("1", "merged", "255, 255, 255");
        assertEquals("1", cell.getValue());
    }

    @Test
    public void testSetValue() {
        Cell cell = new Cell();
        cell.setValue("1");
        assertEquals("1", cell.getValue());
    }

    @Test
    public void testGetStatus() {
        Cell cell = new Cell("1", "merged", "255, 255, 255");
        assertEquals("merged", cell.getStatus());
    }

    @Test
    public void testSetStatus() {
        Cell cell = new Cell();
        cell.setStatus("merged");
        assertEquals("merged", cell.getStatus());
    }

    @Test
    public void testGetColor() {
        Cell cell = new Cell("1", "merged", "255, 255, 255");
        assertEquals("255, 255, 255", cell.getColor());
    }

    @Test
    public void testSetColor() {
        Cell cell = new Cell();
        cell.setColor("255, 255, 255");
        assertEquals("255, 255, 255", cell.getColor());
    }

    @Test
    public void testToString() {
        Cell cell = new Cell("1", "merged", "255, 255, 255");
        String expected = "Cell{value='1', status='merged', color='255, 255, 255'}";
        assertEquals(expected, cell.toString());
    }
}
