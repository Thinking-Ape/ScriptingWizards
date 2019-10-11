package model;

import model.enums.CContent;
import model.enums.CFlag;
import model.enums.ItemType;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    private int cellId = -1;
    private CContent content = CContent.EMPTY;
    private List<CFlag> flagList = new ArrayList<>();
    private List<Integer> linkedCellIdList = new ArrayList<>();
    private Entity entity = null;
    private ItemType item = null;

//    public Cell(CContent content, CFlag... flags){
//        this.content = content;
//        for(CFlag cFlag :flags){
//            flagList.add(cFlag);
//        }
//    }
    public Cell(CContent content, Entity entity, CFlag... flags){
        //this.cellId = cellId;
        this.entity =entity;
        this.content = content;
        for(CFlag cFlag :flags){
            flagList.add(cFlag);
        }
    }
//    public Cell(CContent content, Entity entity,List<CFlag> flags){
//        this.entity = entity;
//        this.content = content;
//        for(CFlag cFlag :flags){
//            flagList.add(cFlag);
//        }
//
//    }
    public Cell(CContent content){
        //this.cellId = cellId;
        this.content = content;
    }

    public Cell(CContent content, ItemType item,Entity entity, List<CFlag> flagList, List<Integer> linkedCellIdList, int cellId) {
        if(item != null && entity != null)throw new IllegalStateException("Cannot hava an item and an entity");
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

    public CContent getContent() {
        return content;
    }

    void setFlagValue(CFlag flag, Boolean flagValue) {
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

    public void setEntity(Entity entity) {
        if(item != null)throw new IllegalStateException("There is already an item in this cell!");
        else this.entity = entity;
    }

    public void setItem(ItemType item) {
        if(entity != null)throw new IllegalStateException("There is already an entity in this cell!");
        else if(content.isTraversable()) this.item = item;
    }

    public void setContent(CContent content) {
        if(!content.isTraversable())this.item =null;
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
        return item == null && entity == null;
    }
}
