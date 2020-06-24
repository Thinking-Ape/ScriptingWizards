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

    public Entity(String name, Direction direction, EntityType entityType){
        this.name = name;
        this.direction = direction;
        this.entityType =entityType;
    }
    private  Entity(String name, Direction direction, EntityType entityType, ItemType item){
        this(name,direction,entityType);
        this.item = item;
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
        if(this == NO_ENTITY)return this;
        return new Entity(name, direction, entityType,item);
    }
}
