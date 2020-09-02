package main.model.gamemap;

import main.model.gamemap.enums.Direction;
import main.model.gamemap.enums.EntityType;
import main.model.gamemap.enums.ItemType;
import static main.model.gamemap.enums.ItemType.NONE;
import static main.model.GameConstants.NO_ENTITY;

public class Entity {

    private String name;
    private Direction direction;
    private EntityType entityType;
    private ItemType item = NONE;
    private boolean isSpecialized = false;
    private Entity possessor = NO_ENTITY;
    private boolean isPossessing;

    //    public Entity(String name, Direction direction, EntityType entityType){
//        this( name,  direction,  entityType,false);
//    }
    public Entity(String name, Direction direction, EntityType entityType,boolean isSpecialized){
        this.name = name;
        this.direction = direction;
        this.entityType =entityType;
        this.isSpecialized = isSpecialized;
//        if(name.equals("skelly")) System.out.println("jetzat");
    }
    private  Entity(String name, Direction direction, EntityType entityType, ItemType item,boolean isSpecialized){
        this(name,direction,entityType,isSpecialized);
        this.item = item;
    }

    private Entity(String name, Direction direction, EntityType entityType, ItemType item, boolean isSpecialized, Entity possessor, boolean isPossessing) {
        this(name,direction,entityType,item,isSpecialized);
        this.possessor = possessor;
        this.isPossessing = isPossessing;
    }

    public Direction getDirection(){
        return direction;
    }
    void setDirection(Direction direction){
        this.direction = direction;
    }
    public String getName(){return name;}

    public EntityType getEntityType() {
        return entityType;
    }

    void setItem(ItemType item) {
        this.item = item;
    }

    public ItemType getItem() {
        return item;
    }

    public void deleteIdentity() {
        name = "";
    }

    public void becomePossessedBy(Entity possessor){
        possessor.setPossessing(true);
        this.possessor = possessor;
    }
    public Entity dispossess(){
        possessor.setPossessing(false);
        Entity tempPos = possessor.copy();
        possessor = NO_ENTITY;
        tempPos.setDirection(this.getDirection());
        return tempPos;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Entity){
            Entity entity = (Entity)obj;
            if(this == NO_ENTITY && entity == this)return true;
            return name.equals(entity.name) && direction.equals(entity.direction) && entityType.equals(entity.entityType) && item.equals(entity.item) ;
        }
        return super.equals(obj);
    }

    public Entity copy(){
//        if(this == NO_ENTITY)
            return this;
//        return new Entity(name, direction, entityType,item,isSpecialized,possessor,isPossessing);
    }

    public boolean isSpecialized() {
        return isSpecialized;
    }

    private void setPossessing(boolean possessing){
//        if(entityType != EntityType.SKELETON || !isSpecialized)isPossessing = false;
//        else
            this.isPossessing = possessing;
    }

    public boolean isPossessed() {
        if(this == NO_ENTITY)return false;
        return !isPossessedBy("");
    }
    public boolean isPossessedBy(String name) {
        if(this == NO_ENTITY)return false;
        if(isPossessing){
            return false;
        }
        return possessor.getName().equals(name);
    }

    public boolean isPossessing() {
        return isPossessing;
    }

    public Entity dispossess(Direction dir) {
        Entity output = dispossess();
        output.setDirection(dir);
        return output;
    }
}
