package main.model.gamemap;

import main.model.enums.*;
import main.utility.GameConstants;
import main.utility.Point;
import main.utility.SimpleSet;

import java.util.*;

import static main.utility.GameConstants.NO_ENTITY;

public class GameMap {

//    private PropertyChangeSupport changeSupport;
    private Cell[][] cellArray2D;
    private Map<String,Point> entityCellMap;
    private Set<Point> changedCellPoints;

    public GameMap(Cell[][] cellArray2D){
        if(cellArray2D.length == 0) throw new IllegalArgumentException("Cannot have a boundX of 0!");
     this.cellArray2D = cloneArray(cellArray2D);
//     this.changeSupport = new PropertyChangeSupport(this);
//     changeSupport.addPropertyChangeListener(pCL);
     entityCellMap = new HashMap<>();
     changedCellPoints = new SimpleSet<>();
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

    public GameMap copy(){
        GameMap cloneMap = new GameMap(cellArray2D);
        cloneMap.changedCellPoints = new SimpleSet<>(changedCellPoints);
//         cloneMap.entityCellMap = new HashMap<>(this.entityCellMap);
         return cloneMap;
    }

    public int getBoundX(){
        return cellArray2D.length;
    }
    public int getBoundY(){
        return cellArray2D[0].length;
    }

    public CellContent getContentAtXY(int x, int y){
        if(x >= getBoundX() || y >= getBoundY() || x < 0 || y < 0)throw  new IllegalArgumentException("Illegal input: x = " + x+", y = " +y+". Must be within 0 and " + (getBoundX()-1) +" and within 0 and " + (getBoundY()-1) +"!");
        return cellArray2D[x][y].getContent();
    }

    public Cell findCellWithId(int linkedCellId) {
        for(Cell[] cellRow : cellArray2D) for (Cell cell : cellRow){
            if(cell.getCellId()==linkedCellId)return cell;
        }
        return null;
    }

    public void print() {
        for (Cell[] cellRow : cellArray2D) {
            for (Cell cell : cellRow) {
                if(cell.getEntity()==NO_ENTITY)System.out.print(cell.getContent().name() + ", ");
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
    public CellContent getTargetContent(Point point) {
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
                if(getContentAtXY(x,y) == CellContent.SPAWN)return new Point(x,y);
            }
        }
       return new Point(-1, -1);
    }

    public List<Point> getEnemySpawnList() {
        List<Point> output = new ArrayList<>();
        for(int x = 0; x < getBoundX(); x++){
            for(int y = 0; y < getBoundY(); y++){
                if(getContentAtXY(x,y) == CellContent.ENEMY_SPAWN)output.add(new Point(x,y));
            }
        }
        return output;
    }

    public Point getEntityPosition(String name) {
       return entityCellMap.getOrDefault(name, null);
    }

    public void setItem(int x, int y, ItemType item) {
        setItem(new Point(x, y), item);
    }

    public boolean cellHasLinkedCellId(int selectedColumn, int selectedRow, Integer integer) {
        return cellArray2D[selectedColumn][selectedRow].hasLinkedCellId(integer);
    }

    public int getCellID(int x, int y) {
        return cellArray2D[x][y].getCellId();
    }

    public void addLinkedCellId(int x, int y, int integer) {

        GameMap oldMap = this.copy();
        Cell oldCell = cellArray2D[x][y].copy();
        cellArray2D[x][y].addLinkedCellId(integer);
        Cell newCell = cellArray2D[x][y].copy();
        Point p = new Point(x, y);
    }

    public void setCellId(int x, int y, int id) {
        GameMap oldMap = this.copy();
//        changeSupport.firePropertyChange("cellId", cellArray2D[x][y].getCellId(),id);
        Cell oldCell = cellArray2D[x][y].copy();
        cellArray2D[x][y].setCellId(id);
        Cell newCell = cellArray2D[x][y].copy();
        Point p = new Point(x, y);
    }

    public void removeCellLinkedId(int x, int y, Integer s) {
        GameMap oldMap = this.copy();
        Cell oldCell = cellArray2D[x][y].copy();
        cellArray2D[x][y].removeLinkedCellId(s);
        Cell newCell = cellArray2D[x][y].copy();
        Point p = new Point(x, y);
    }

    public void setFlag(int x, int y, CFlag flag, boolean t1) {
        setFlag(new Point(x, y), flag, t1);
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
        Entity entity = cell.getEntity();
        if(entity==NO_ENTITY){
            if(cell.getItem()==ItemType.KEY)
                cell = cell.getMutation(CFlag.KEY_DESTROYED,true);

            else if(cell.getItem()!=ItemType.NONE)
                cell = cell.getMutation(CFlag.ITEM_DESTROYED,true);
            cell = cell.getMutation(ItemType.NONE);
            cellArray2D[x][y] = cell;
            return;
        }
        if(GameConstants.DEBUG)System.out.println(cell.getEntity().getEntityType().getDisplayName() +" "+ cell.getEntity().getName()+" died!");
//        ecMapKill(cell.getEntity().getName());
        if(entity.getEntityType()== EntityType.KNIGHT)
            cell = cell.getMutation(CFlag.KNIGHT_DEATH,true);
        else if(entity.getEntityType()==EntityType.SKELETON)
            cell = cell.getMutation(CFlag.SKELETON_DEATH,true);
        cellArray2D[x][y] = cell;
//        changeSupport.firePropertyChange(cell.getEntity().getEntityType().getDisplayName()+"Death",null,new Point(x,y));
        removeEntity(x,y);
//        if(cellArray2D[x][y].hasFlag(CFlag.KNIGHT_DEATH)){
        setItem(x,y,entity.getItem());
        entityCellMap.remove(entity.getName());
//        }
    }

    public void removeEntity(int x, int y) {

        Entity e = cellArray2D[x][y].getEntity();
        if (e == NO_ENTITY) return;
        setEntity(x,y,NO_ENTITY);
    }

    public void removeEntity(Point p){
        removeEntity(p.getX(), p.getY());
    }

    public void setEntity(int x, int y, Entity entity) {
        setEntity(new Point(x, y), entity);
    }

    public Entity getEntity(Point ecMapGet) {//TODO: sloppy Point vs x,y??
        if(ecMapGet== null)return NO_ENTITY;
        return cellArray2D[ecMapGet.getX()][ecMapGet.getY()].getEntity();
    }

    public Point getTargetPoint(String actorName) {
        Point p = getEntityPosition(actorName);
        Entity entity = cellArray2D[p.getX()][p.getY()].getEntity();
        if(entity == NO_ENTITY) return new Point(-1, -1);
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

    public CellContent getContentAtXY(Point targetPos) {
        return getContentAtXY(targetPos.getX(), targetPos.getY());
    }

    public void setMultipleItems(List<Point> targetPosList, ItemType item) {
        GameMap oldMap = this.copy();
        for(Point p : targetPosList){
            cellArray2D[p.getX()][p.getY()] = cellArray2D[p.getX()][p.getY()].getMutation(item);
        }
    }
    public void setItem(Point targetPos, ItemType item) {
        GameMap oldMap = this.copy();
        if(item == null)throw new IllegalArgumentException("Item cannot be null!");
        if(cellArray2D[targetPos.getX()][targetPos.getY()].getItem()==item)return;
        cellArray2D[targetPos.getX()][targetPos.getY()] = cellArray2D[targetPos.getX()][targetPos.getY()].getMutation(item);
        changedCellPoints.add(targetPos);
    }

    public void setContent(Point targetPos, CellContent content) {
        GameMap oldMap = this.copy();
        if(cellArray2D[targetPos.getX()][targetPos.getY()].getContent()==content)return;
        cellArray2D[targetPos.getX()][targetPos.getY()] = cellArray2D[targetPos.getX()][targetPos.getY()].getMutation(content);
        changedCellPoints.add(targetPos);
    }
    public void setMultipleContents(List<Point> targetPosList, CellContent content) {
        GameMap oldMap = this.copy();
        for(Point p : targetPosList){
            cellArray2D[p.getX()][p.getY()] = cellArray2D[p.getX()][p.getY()].getMutation(content);
        }
//        Cell oldCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
//
//        Cell newCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
    }

    public boolean cellHasFlag(Point targetPoint, CFlag open) {
        return cellHasFlag(targetPoint.getX(), targetPoint.getY(), open);
    }

    public boolean isCellFree(Point targetPoint) {
        return cellArray2D[targetPoint.getX()][targetPoint.getY()].isFree();
    }

    public void setEntity(Point targetPos, Entity actorEntity) {
        GameMap oldMap = this.copy();
        if(actorEntity == null)throw new IllegalArgumentException("Entity shall not be null! Use NO_ENTITY instead");
        if(cellArray2D[targetPos.getX()][targetPos.getY()].getEntity()==actorEntity)return;
        Cell oldCell = cellArray2D[targetPos.getX()][targetPos.getY()].copy();
        cellArray2D[targetPos.getX()][targetPos.getY()] = oldCell.getMutation(actorEntity);
        if(entityCellMap.containsKey(actorEntity.getName()))entityCellMap.replace(actorEntity.getName(),targetPos);
        else entityCellMap.put(actorEntity.getName(), targetPos);
        changedCellPoints.add(targetPos);
    }

    public void setFlag(Point targetPos, CFlag flag, boolean b) {
        GameMap oldMap = this.copy();
        Cell oldCell = cellArray2D[targetPos.getX()][targetPos.getY()];
        if(oldCell.hasFlag(flag) == b)return;
        cellArray2D[targetPos.getX()][targetPos.getY()] = oldCell.getMutation(flag, b);
        changedCellPoints.add(targetPos);
    }

    public int getCellID(Point spawnPoint) {
        return getCellID(spawnPoint.getX(), spawnPoint.getY());
    }


    /*@Override
    public void propertyChange(PropertyChangeEvent evt) {
        Pair<Point,Cell> pair = (Pair<Point, Cell>) evt.getNewValue();
        cellArray2D[pair.getKey().getX()][pair.getKey().getY()] = pair.getValue();
    }*/

//    public void initLevelChangeListener(PropertyChangeListener listener) {
//        this.changeSupport.addPropertyChangeListener(listener);
//    }

    public ItemType getItem(Point targetPoint) {
        return cellArray2D[targetPoint.getX()][targetPoint.getY()].getItem();
    }

    public ItemType getItem(int x, int y) {
        return getItem(new Point(x, y));
    }

    public void setContent(int x, int y, CellContent path) {
        setContent(new Point(x, y), path);
    }

    public void spawn(Point spawnPoint, Entity entity) {
        ItemType item = getItem(spawnPoint);
        setItem(spawnPoint,  ItemType.NONE);
        entity.setItem(item);
        setEntity(spawnPoint, entity);
    }

    public List<String> getEntityNames() {
        return new ArrayList<>(entityCellMap.keySet());
    }

//    public gamemap getSwappedEntityMap(String name, String beaconName) {
//        //TODO: set Entity -> map
//        gamemap output = this.copy();
//        Point p1 = output.entityCellMap.get(name);
//        Point p2 = output.entityCellMap.get(beaconName);
//        output.entityCellMap.replace(beaconName, p1);
//        output.entityCellMap.replace(name, p2);
//        Entity replacer = output.getEntity(p1);
//        Entity beacon = output.getEntity(p2);
//        output.setEntity(p2, replacer);
//        output.setEntity(p1, beacon);
//        return output;
//    }

    public Entity getEntity(String name) {
        return getEntity(entityCellMap.getOrDefault(name, null));
    }

    public int getAmountOfEntities(EntityType entityType) {
        return (int)entityCellMap.keySet().stream().filter(entity -> getEntity(entity).getEntityType() == entityType).count();
    }

    public void clearFlags(int x, int y) {
        for(CFlag flag : CFlag.values())
            setFlag(x, y, flag, false);
    }

    public boolean isGateWrongDirection(Point actorPoint, Point targetPoint) {
        Entity actorEntity = getEntity(actorPoint);
        if(getContentAtXY(targetPoint)== CellContent.GATE){
            if((actorEntity.getDirection()== Direction.NORTH||actorEntity.getDirection()==Direction.SOUTH)&&!cellHasFlag(targetPoint, CFlag.TURNED))return true;
            if((actorEntity.getDirection()==Direction.WEST||actorEntity.getDirection()==Direction.EAST)&&cellHasFlag(targetPoint, CFlag.TURNED))return true;
        }
        if(getContentAtXY(actorPoint)== CellContent.GATE){
            if((actorEntity.getDirection()==Direction.NORTH||actorEntity.getDirection()==Direction.SOUTH)&&!cellHasFlag(actorPoint, CFlag.TURNED))return true;
            if((actorEntity.getDirection()==Direction.WEST||actorEntity.getDirection()==Direction.EAST)&&cellHasFlag(actorPoint, CFlag.TURNED))return true;
        }
        return false;
    }

    public boolean gateIsOpen(Point targetPos) {
        return cellHasFlag(targetPos, CFlag.OPEN)^cellHasFlag(targetPos, CFlag.INVERTED);
    }

//    public void removeAllListeners() {
//        changeSupport = new PropertyChangeSupport(this);
//    }

    public void changeEntityDirection(Point actorPoint, String direction) {
        GameMap oldMap = this.copy();
        Entity entity = getEntity(actorPoint);
        switch (entity.getDirection()){
            case NORTH:
                if(direction.equals("LEFT"))
                    entity.setDirection(Direction.WEST);
                else if(direction.equals("RIGHT"))
                    entity.setDirection(Direction.EAST);
                else if(direction.equals("AROUND"))
                    entity.setDirection(Direction.SOUTH);
                break;
            case SOUTH:
                if(direction.equals("LEFT"))
                    entity.setDirection(Direction.EAST);
                else if(direction.equals("RIGHT"))
                    entity.setDirection(Direction.WEST);
                else if(direction.equals("AROUND"))
                    entity.setDirection(Direction.NORTH);
                break;
            case EAST:
                if(direction.equals("LEFT"))
                    entity.setDirection(Direction.NORTH);
                else if(direction.equals("RIGHT"))
                    entity.setDirection(Direction.SOUTH);
                else if(direction.equals("AROUND"))
                    entity.setDirection(Direction.WEST);
                break;
            case WEST:
                if(direction.equals("LEFT"))
                    entity.setDirection(Direction.SOUTH);
                else if(direction.equals("RIGHT"))
                    entity.setDirection(Direction.NORTH);
                else if(direction.equals("AROUND"))
                    entity.setDirection(Direction.EAST);
                break;
        }
        changedCellPoints.add(actorPoint);
    }

    public void setEntityItem(Point actorPoint, ItemType item) {
        GameMap oldMap = this.copy();
        Entity entity = getEntity(actorPoint);
        entity.setItem(item);
        changedCellPoints.add(actorPoint);
    }

    public Set<Point> getAndResetChangedPointList(){
        Set<Point> output = new SimpleSet<>(changedCellPoints);
        changedCellPoints = new SimpleSet<>();
        return output;
    }

    public void changeHeight(int newHeight) {
        Cell[][] newMapArray = new Cell[this.getBoundX()][newHeight];
        for(int y = 0; y < newHeight; y++ ){
            for(int x = 0; x < this.getBoundX(); x++){

                if(y < this.getBoundY()){
                    if(y == newHeight-1 && this.getContentAtXY(x,y) != CellContent.EMPTY)newMapArray[x][y]=  new Cell(CellContent.WALL);
                    else newMapArray[x][y]=this.getCellAtXYClone(x,y);
                }
                else newMapArray[x][y] = new Cell(CellContent.WALL);
            }
        }
        this.cellArray2D = newMapArray;
    }
    public void changeWidth(int newWidth) {
        Cell[][] newMapArray = new Cell[newWidth][this.getBoundY()];
        for(int y = 0; y < this.getBoundY(); y++ ){
            for(int x = 0; x < newWidth; x++){
                if(x < this.getBoundX()){
                    if(x == newWidth-1 && this.getContentAtXY(x,y) != CellContent.EMPTY)newMapArray[x][y]=  new Cell(CellContent.WALL);
                    else newMapArray[x][y] = this.getCellAtXYClone(x,y);
                }
                else newMapArray[x][y] = new Cell(CellContent.WALL);
            }
        }
        this.cellArray2D = newMapArray;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GameMap){
            GameMap map = (GameMap)obj;
            boolean equal = true;
            if(map.getBoundX() != getBoundX() || map.getBoundY() != getBoundY())return false;
            for(int i = 0; i < cellArray2D.length;i++)
                for(int j = 0; j < cellArray2D[0].length;j++){
                    equal = equal && cellArray2D[i][j].equals(map.cellArray2D[i][j]);
                }
            return equal;
        }
        return super.equals(obj);
    }
}
