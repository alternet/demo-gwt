package com.github.alternet.demo.criminal.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.sencha.gxt.widget.core.client.grid.Grid;

public abstract class GridHoverHandler<T> implements MouseOutHandler, MouseMoveHandler {

    private final Grid<T> grid;
    private T curHover = null;

    public GridHoverHandler(Grid<T> grid) {
        this.grid = grid;
        grid.addHandler(this, MouseMoveEvent.getType());
        grid.addHandler(this, MouseOutEvent.getType());
    }

    public abstract void setObjHover(T object, boolean isHovered);

    public void onMouseMove(MouseMoveEvent event) {
        T newHover = null;
        NativeEvent natev = event.getNativeEvent();
        if (Element.is(natev.getEventTarget())) {
            int hoverIdx = grid.getView().findRowIndex( Element.as(natev.getEventTarget()) );
            newHover = grid.getStore().get(hoverIdx);
        }
        if(newHover == curHover) return;
        setObjHover(curHover, false);
        curHover = newHover;
        setObjHover(curHover, true);
    }

    public void onMouseOut(MouseOutEvent event) {
        EventTarget to = event.getNativeEvent().getRelatedEventTarget();
        // This event is chatty; make sure we're actually moving off the grid
        if (to == null || (Element.is(to) && ! grid.getElement().isOrHasChild(Element.as(to)))) {
            setObjHover(curHover, false);
            curHover = null;
        }
    }

}