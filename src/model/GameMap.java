package model;

import javafx.util.Pair;
import model.enums.CContent;
import model.enums.CFlag;
import model.enums.ItemType;
import model.util.Point;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMap /*implements PropertyChangeListener*/ {

    private PropertyChangeSupport changeSupport;
    private int boundX;
    private int boundY;
    private Cell[][] cellArray2D;
    private Map<String,Point> entityCellMap;

    public GameMap(Cell[][] cellArray2D){
        if(cellArray2D.length == 0) throw new IllegalArgumentException("Cannot have a boundX of 0!");
     this.cellArray2D = cloneArray(cellArray2D);
     this.boundX = cellArray2D.length;
     this.boundY = cellArray2D[0].length;
     this.changeSupport = new PropertyChangeSupport(this);
     entityCellMap = new HashMap<>();
    }

    private Cell[][] cloneArray(Cell[][] originalState) {
        Cell[][] output=new Cell[originalState.length][originalState[0].length];
        for(int column = 0; column < originalState.length; column++){
            for(int row = 0; row < originalState[0].length;row++){
                output[column][row] = originalState[column][row].copy();
            }
        }
        return output;
    }

    public GameMap clone(){
        return new GameMap(cellArray2D);
    }

    public int getBoundX(){
        return boundX;
    }
    public int getBoundY(){
        return boundY;
    }

    public CContent getContentAtXY(int x, int y){
        if(x >= boundX || y >= boundY || x < 0 || y < 0)throw  new IllegalArgumentException("Illegal input: x = " + x+", y = " +y+". Must be within 0 and " + (boundX-1) +" and within 0 and " + (boundY-1) +"!");
        return cellArray2D[x][y].getContent();
    }

    public Cell findCellWithId(int linkedCellId) {
        for(Cell[] cellRow : cellArray2D) for (Cell cell : cellRow){
            if(cell.getCellId()==linkedCellId)return cell;
        }
        return null;
    }

    public Point find(Cell actorCell) {
        for(int x = 0; x < boundX; x++){
            for(int y = 0; y < boundY; y++){
                if(actorCell == cellArray2D[x][y])return new Point(x,y);
            }
        }
        return new Point(-1,-1);
    }

    public void setCell(int x, int y, Cell cell) {

        Cell oldCell =cellArray2D[x][y].copy();
        cellArray2D[x][y]=cell;
        changeSupport.firePropertyChange("cell", new Pair<>(new Point(x,y),oldCell.copy()),new Pair<>(new Point(x,y),cell.copy()));
    }
    public void print() {
        for (Cell[] cellRow : cellArray2D) {
            for (Cell cell : cellRow) {
                if(cell.getEntity()==null)System.out.print(cell.getContent().name() + ", ");
                else System.out.print(cell.getEntity().getName()+", ");
            }
            System.out.println();
        }
    }
    public boolean testIfIdIsUnique(int id) {
        if(id ==-1)return true;
        for(Cell[] cellRow : cellArray2D){
            for(Cell cell : cellRow){
                if(id == cell.getCellId())return false;
            }
        }
        return true;
    }
    public CContent getTargetContent(Point point) {
        Entity entity = cellArray2D[point.getX()][point.getY()].getEntity();
        switch (entity.getDirection()){
            case NORTH:
                return getContentAtXY(point.getX(),point.getY()-1);
            case SOUTH:
                return getContentAtXY(point.getX(),point.getY()+1);
            case EAST:
                return getContentAtXY(point.getX()+1,point.getY());
            case WEST:
                return getContentAtXY(point.getX()-1,point.getY());
        }
        return getContentAtXY(new Point(-1, -1));
    }
    public Point findSpawn() {
        for(int x = 0; x < getBoundX(); x++){
            for(int y = 0; y < getBoundY(); y++){
                if(getContentAtXY(x,y) == CContent.SPAWN)return new Point(x,y);
            }
        }
       return new Point(-1, -1);
    }

    public List<Point> getEnemySpawnList() {
        List<Point> output = new ArrayList<>();
        for(int x = 0; x < getBoundX(); x++){
            for(int y = 0; y < getBoundY(); y++){
                if(getContentAtXY(x,y) == CContent.ENEMY_SPAWN)output.add(new Point(x,y));
            }
        }
        return output;
    }

    public boolean eCMapContains(String name) {
        return  entityCellMap.containsKey(name);
    }

    public void eCMapPut(String name, Point point) {
        entityCellMap.put(name,point);
    }

    public void ecMapReplace(String name, Point point) {
        entityCellMap.replace(name,point);
    }

    private void ecMapKill(String name) {
        entityCellMap.remove(name);
    }

    public Point ecMapGet(String name) {
       return entityCellMap.getOrDefault(name, null);
    }

    public void setItem(int x, int y, ItemType item) {
//        changeSupport.firePropertyChange("item", cellArray2D[selectedColumn][selectedRow].getItem(), item);
//        cellArray2D[selectedColumn][selectedRow].setItem(item);
        setItem(new Point(x, y), item);
    }

    public boolean cellHasLinkedCellId(int selectedColumn, int selectedRow, Integer integer) {
        return cellArray2D[selectedColumn][selectedRow].hasLinkedCellId(integer);
    }

    public int getCellID(int x, int y) {
        return cellArray2D[x][y].getCellId();
    }

    public void addLinkedCellId(int x, int y, int integer) {
//        List<Integer> oldList = new ArrayList<>();
//        List<Integer> newList = new ArrayList<>();
//        for(int i = 0; i < cellArray2D[x][y].getLinkedCellsSize();i++){
//            oldList.add(cellArray2D[x][y].getLinkedCellId(i));
//            newList.add(cellArray2D[x][y].getLinkedCellId(i));
//        }
//        newList.add(integer);
//        changeSupport.firePropertyChange("linkedCellId", oldList,newList);
        Cell oldCell = cellArray2D[x][y].copy();
        cellArray2D[x][y].addLinkedCellId(integer);
        Cell newCell = cellArray2D[x][y].copy();
        Point p = new Point(x, y);
        changeSupport.firePropertyChange("linkedCellId", new Pair<>(p,oldCell),new Pair<>(p,newCell));
    }

    public void setCellId(int x, int y, int id) {
//        changeSupport.firePropertyChange("cellId", cellArray2D[x][y].getCellId(),id);
        Cell oldCell = cellArray2D[x][y].copy();
        cellArray2D[x][y].setCellId(id);
        Cell newCell = cellArray2D[x][y].copy();
        Point p = new Point(x, y);
        changeSupport.firePropertyChange("cellId", new Pair<>(p,oldCell),new Pair<>(p,newCell));
    }

    public void removeCellLinkedId(int x, int y, Integer s) {
//        List<Integer> oldList = new ArrayList<>();
//        List<Integer> newList = new ArrayList<>();
//        for(int i = 0; i < cellArray2D[x][y].getLinkedCellsSize();i++){
//            oldList.add(cellArray2D[x][y].getLinkedCellId(i));
//            if(cellArray2D[x][y].getLinkedCellId(i)!= s)newList.add(cellArray2D[x][y].getLinkedCellId(i));
//        }
        Cell oldCell = cellArray2D[x][y].copy();
        cellArray2D[x][y].removeLinkedCellId(s);
        Cell newCell = cellArray2D[x][y].copy();
        Point p = new Point(x, y);
        changeSupport.firePropertyChange("linkedCellId", new Pair<>(p,oldCell),new Pair<>(p,newCell));
    }

    public void setFlag(int x, int y, CFlag flag, boolean t1) {
        setFlag(new Point(x, y), flag, t1);
//        changeSupport.firePropertyChange("flag", new Pair<>(flag,cellArray2D[x][y].hasFlag(flag)), new Pair<>(flag,t1));
    }

    public boolean cellHasFlag(int x, int y, CFlag flag) {
        return cellArray2D[x][y].hasFlag(flag);
    }

    public int getLinkedCellListSize(int x, int y) {
        return cellArray2D[x][y].getLinkedCellsSize();
    }

    public int getLinkedCellId(int x,int y,int l) {
        return cellArray2D[x][y].getLinkedCellId(l);
    }

    public Cell getCellAtXYClone(int x, int y) {
        return cellArray2D[x][y].copy();
    }

    public void kill(Point p){
        kill(p.getX(),p.getY());
    }
    public void kill(int x, int y) {
        Cell cell =cellArray2D[x][y];
        if(cell.getEntity()==null){
            cell.setItem(null);
        return;
        }
        System.out.println(cell.getEntity().getEntityType() +" "+ cell.getEntity().getName()+" died!");
        ecMapKill(cell.getEntity().getName());
        ItemType item = cell.getEntity().getItem();
        setEntity(x,y,null);
        setItem(x,y,item);
    }

    public void setEntity(int x, int y, Entity entity) {
        setEntity(new Point(x, y), entity);
    }

    public Entity getEntity(Point ecMapGet) {//TODO: sloppy Point vs x,y??
        return cellArray2D[ecMapGet.getX()][ecMapGet.getY()].getEntity();
    }

    public Point getTargetPoint(String actorName) {
        Point p = ecMapGet(actorName);
        Entity entity = cellArray2D[p.getX()][p.getY()].getEntity();
        switch (entity.getDirection()){
            case NORTH:
                return new Point(p.getX(), p.getY()-1);
            case SOUTH:
                return new Point(p.getX(), p.getY()+1);
            case EAST:
                return new Point(p.getX()+1, p.getY());
            case WEST:
                return new Point(p.getX()-1, p.getY());
        }
        return new Point(-1, -1);
    }

    public CContent getContentAtXY(Point targetPos) {
        return getContentAtXY(targetPos.getX(), targetPos.getY());
    }

    public void setItem(Point targetPos, ItemType item) {
        Cell oldCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
        cellArray2D[targetPos.getX()][targetPos.getY()].setItem(item);
        Cell newCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
        changeSupport.firePropertyChange("item",new Pair<>(targetPos,oldCell),new Pair<>(targetPos,newCell));
    }

    public void setContent(Point targetPos, CContent path) {
        Cell oldCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
        cellArray2D[targetPos.getX()][targetPos.getY()].setContent(path);
        Cell newCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
        changeSupport.firePropertyChange("content",new Pair<>(targetPos,oldCell),new Pair<>(targetPos,newCell));
    }

    public boolean cellHasFlag(Point targetPoint, CFlag open) {
        return cellHasFlag(targetPoint.getX(), targetPoint.getY(), open);
    }

    public boolean isCellFree(Point targetPoint) {
        return cellArray2D[targetPoint.getX()][targetPoint.getY()].isFree();
    }

    public void setEntity(Point targetPos, Entity actorEntity) {

        Cell oldCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
        cellArray2D[targetPos.getX()][targetPos.getY()].setEntity(actorEntity);
        Cell newCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
        changeSupport.firePropertyChange("entity",new Pair<>(targetPos,oldCell),new Pair<>(targetPos,newCell));
    }

    public void setFlag(Point targetPos, CFlag flag, boolean b) {
        Cell oldCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
        cellArray2D[targetPos.getX()][targetPos.getY()].setFlagValue(flag, b);
        Cell newCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
        changeSupport.firePropertyChange("flag",new Pair<>(targetPos,oldCell),new Pair<>(targetPos,newCell));
    }

    public int getCellID(Point spawnPoint) {
        return getCellID(spawnPoint.getX(), spawnPoint.getY());
    }


    /*@Override
    public void propertyChange(PropertyChangeEvent evt) {
        Pair<Point,Cell> pair = (Pair<Point, Cell>) evt.getNewValue();
        cellArray2D[pair.getKey().getX()][pair.getKey().getY()] = pair.getValue();
    }*/

    public void addChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);
    }

    public ItemType getItem(Point targetPoint) {
        return cellArray2D[targetPoint.getX()][targetPoint.getY()].getItem();
    }

    public ItemType getItem(int x, int y) {
        return getItem(new Point(x, y));
    }

    public void setContent(int x, int y, CContent path) {
        setContent(new Point(x, y), path);
    }

    public void spawn(Point spawnPoint, Entity entity) {
        ItemType item = getItem(spawnPoint);
        setItem(spawnPoint,  null);
        entity.setItem(item);
        setEntity(spawnPoint, entity);
    }
}
