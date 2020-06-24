package main.model.gamemap;

import main.model.gamemap.enums.CellContent;
import main.model.gamemap.enums.CellFlag;
import main.model.gamemap.enums.ItemType;

import java.util.ArrayList;
import java.util.List;

import static main.model.gamemap.enums.ItemType.NONE;
import static main.model.GameConstants.NO_ENTITY;

public class Cell {
    private int cellId = -1;
    private CellContent content = CellContent.EMPTY;
    private List<CellFlag> flagList = new ArrayList<>();
    private List<Integer> linkedCellIdList = new ArrayList<>();
    private Entity entity = NO_ENTITY;
    private ItemType item = NONE;

    public Cell(CellContent content){
        this.content = content;
    }

    public Cell(CellContent content, ItemType item, Entity entity, List<CellFlag> flagList, List<Integer> linkedCellIdList, int cellId) {
        if(item != NONE && entity != NO_ENTITY)throw new IllegalStateException("Cannot have both an item and an entity");
        this.item=item;
        this.entity = entity;
        this.content = content;
        for(CellFlag cellFlag :flagList){
            this.flagList.add(cellFlag);
        }
        for(Integer linkedId :linkedCellIdList){
            this.linkedCellIdList.add(linkedId);
        }
        this.cellId = cellId;
    }

    public int getCellId(){ return cellId; }
    public void setCellId(int cellId){ this.cellId = cellId; }

    public boolean hasFlag(CellFlag cellFlag) {
        return flagList.contains(cellFlag);
    }

    public CellContent getContent() {
        return content;
    }

    private void setFlagValue(CellFlag flag, Boolean flagValue) {
        if (flagValue) {
            if(!hasFlag(flag))flagList.add(flag);
        } else {
            if(hasFlag(flag))flagList.remove(flag);
        }
    }

    public Cell copy() {
        return new Cell(content,item,entity.copy(),new ArrayList<>(flagList),new ArrayList<>(linkedCellIdList),cellId);
    }

    public Entity getEntity() {
        return entity;
    }

    private void setEntity(Entity entity) {
        if(item != NONE)throw new IllegalStateException("There is already an item in this cell!");
        else this.entity = entity;
    }

    private void setItem(ItemType item) {
        if(entity != NO_ENTITY)throw new IllegalStateException("There is already an entity in this cell!");
        else if(content.isTraversable()||(hasFlag(CellFlag.OPEN)^hasFlag(CellFlag.INVERTED))) this.item = item;
    }

    private void setContent(CellContent content) {
        if(!content.isTraversable())this.item = NONE;
        this.content = content;
    }

    public void addLinkedCellId(int linkedCellId) {
        this.linkedCellIdList.add(linkedCellId);
    }
    public int getLinkedCellId(int index){
        return linkedCellIdList.get(index);
    }

    public int getLinkedCellsSize() {
        return linkedCellIdList.size();
    }

    public boolean hasLinkedCellId(int s) {
        return linkedCellIdList.contains(s);
    }

    public void removeLinkedCellId(Integer s) {
        this.linkedCellIdList.remove(s);
    }

    public ItemType getItem() {
        return item;
    }

    public boolean isFree() {
        return entity == NO_ENTITY && item == NONE;
    }

    public List<CellFlag> getFlags() {
        return new ArrayList<>(flagList);
    }

    public Cell getMutation(Entity actorEntity) {
        Cell mutatedCell = this.copy();
        mutatedCell.entity = actorEntity;
        return mutatedCell;
    }

    public Cell getMutation(CellFlag flag, boolean flagValue) {
        Cell mutatedCell = this.copy();
        if (flagValue) {
            if(!hasFlag(flag))mutatedCell.flagList.add(flag);
        } else {
            if(hasFlag(flag))mutatedCell.flagList.remove(flag);
        }
        return mutatedCell;
    }

    public Cell getMutation(ItemType item) {
        Cell mutatedCell = this.copy();
        mutatedCell.item = item;
        return mutatedCell;
    }

    public Cell getMutation(CellContent content) {
        Cell mutatedCell = this.copy();
        mutatedCell.content = content;
        if(!content.isTraversable())mutatedCell.item = NONE;
        return mutatedCell;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Cell){
            Cell cell = (Cell)obj;
            return cellId == cell.cellId && content.equals(cell.content) && flagList.equals(cell.flagList) && entity.equals(cell.entity) && item.equals(cell.item) && linkedCellIdList.equals(cell.linkedCellIdList);
        }
        return super.equals(obj);
    }
}
