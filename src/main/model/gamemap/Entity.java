package main.model.gamemap;

// an interface for classes that use Behaviour
// maybe make abstract class instead?

import main.model.enums.Direction;
import main.model.enums.EntityType;
import main.model.enums.ItemType;
import main.utility.GameConstants;

//maybe no use at all?? -> instead behavior map in level
public class Entity {

    private String name;
    private Direction direction;
    private EntityType entityType;
    private ItemType item;

    public Entity(String name, Direction direction, EntityType entityType){
        this.name = name;
        this.direction = direction;
        this.entityType =entityType;
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
            if(this == GameConstants.NO_ENTITY && entity == this)return true;
            return name.equals(entity.name) && direction.equals(entity.direction) && entityType.equals(entity.entityType) && item.equals(entity.item) ;
        }
        return super.equals(obj);
    }
}
