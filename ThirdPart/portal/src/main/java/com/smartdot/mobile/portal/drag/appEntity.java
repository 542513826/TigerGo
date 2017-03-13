package com.smartdot.mobile.portal.drag;

/**
 * 频道实体类 Created by YoKeyword on 15/12/29.
 */
public class appEntity {

    private long id;

    private String name;

    private boolean isDraggable;
    private boolean isDelete;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDraggable() {
        return isDraggable;
    }

    public void setDraggable(Boolean isDraggable) {
        this.isDraggable = isDraggable;
    }
    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(Boolean isDelete) {
        this.isDelete = isDelete;
    }
}
