package main.model.gamemap;

import main.model.enums.CellContent;
import main.model.enums.CFlag;
import main.model.enums.ItemType;

import java.util.ArrayList;
import java.util.List;

import static main.model.enums.ItemType.NONE;
import static main.utility.GameConstants.NO_ENTITY;

public class Cell {
    private int cellId = -1;
    private CellContent content = CellContent.EMPTY;
    private List<CFlag> flagList = new ArrayList<>();
    private List<Integer> linkedCellIdList = new ArrayList<>();
    private Entity entity = NO_ENTITY;
    private ItemType item = NONE;

//    public Cell(CellContent content, CFlag... flags){
//        this.content = content;
//        for(CFlag cFlag :flags){
//            flagList.add(cFlag);
//        }
//    }
    public Cell(CellContent content, Entity entity, CFlag... flags){
        //this.cellId = cellId;
        this.entity =entity;
        this.content = content;
        for(CFlag cFlag :flags){
            flagList.add(cFlag);
        }
    }
//    public Cell(CellContent content, Entity entity,List<CFlag> flags){
//        this.entity = entity;
//        this.content = content;
//        for(CFlag cFlag :flags){
//            flagList.add(cFlag);
//        }
//
//    }
    public Cell(CellContent content){
        //this.cellId = cellId;
        this.content = content;
    }

    public Cell(CellContent content, ItemType item, Entity entity, List<CFlag> flagList, List<Integer> linkedCellIdList, int cellId) {
        if(item != NONE && entity != NO_ENTITY)throw new IllegalStateException("Cannot hava an item and an entity");
        this.item=item;
        this.entity = entity;
        this.content = content;
        for(CFlag cFlag :flagList){
            this.flagList.add(cFlag);
        }
        for(Integer linkedId :linkedCellIdList){
            this.linkedCellIdList.add(linkedId);
        }
        this.cellId = cellId;
    }

    public int getCellId(){ return cellId; }
    public void setCellId(int cellId){ this.cellId = cellId; }

    public boolean hasFlag(CFlag cFlag) {
        return flagList.contains(cFlag);
    }

    public CellContent getContent() {
        return content;
    }

    private void setFlagValue(CFlag flag, Boolean flagValue) {
        if (flagValue) {
            if(!hasFlag(flag))flagList.add(flag);
        } else {
            if(hasFlag(flag))flagList.remove(flag);
        }
    }

    public Cell copy() {
        return new Cell(content,item,entity,new ArrayList<>(flagList),linkedCellIdList,cellId);
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
        else if(content.isTraversable()||(hasFlag(CFlag.OPEN)^hasFlag(CFlag.INVERTED))) this.item = item;
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
        return item == NONE && entity == NO_ENTITY;
    }

    public List<CFlag> getFlags() {
        return new ArrayList<>(flagList);
    }

    public Cell getMutation(Entity actorEntity) {
        Cell mutatedCell = this.copy();
        mutatedCell.entity = actorEntity;
        return mutatedCell;
    }

    public Cell getMutation(CFlag flag, boolean flagValue) {
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
