package main.test;

import main.model.gamemap.Cell;
import main.model.gamemap.enums.CellContent;

public abstract class TestConstants {

    public static final Cell[][] TEST_MOVE_MAP = new Cell[][]{{new Cell(CellContent.WALL),new Cell(CellContent.WALL),new Cell(CellContent.WALL),new Cell(CellContent.WALL)},{new Cell(CellContent.WALL),new Cell(CellContent.PATH),new Cell(CellContent.SPAWN),new Cell(CellContent.WALL)},{new Cell(CellContent.WALL),new Cell(CellContent.WALL),new Cell(CellContent.WALL),new Cell(CellContent.WALL)}};
    public static final String SUCCESS_STRING = "Test successful!\n========================================";
}
