package com.github.alternet.demo.criminal.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.github.alternet.demo.criminal.client.model.Aptitude;
import com.github.alternet.demo.criminal.client.model.Gangster;
import com.github.alternet.demo.criminal.client.model.Holdup;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.AbstractEventCell;
import com.sencha.gxt.cell.core.client.ButtonCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.core.client.util.Format;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.GridDropTarget;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeShowContextMenuEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowContextMenuEvent.BeforeShowContextMenuHandler;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent.CellClickHandler;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent.CellDoubleClickHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class Conspiracy implements EntryPoint, IsWidget {

    public void onModuleLoad() {
        RootPanel.get().add(this);
    }

    Grid<Holdup> holdUpGrid;
    Holdup holdupHover; // the target line where to drop items
    Gangster gangsterHover; // an item to drag in the holdup grid

    Label tip = new Label("Select items from the left list to add in the right table.");

    public Widget asWidget() {
        // define a frame with 2 grids
        FramedPanel panel = new FramedPanel();
        panel.setHeadingText("Criminal conspiracy");
        panel.setPixelSize(800, 400);

        ListStore<Gangster> gangsters = getGangsters();
        Grid<Gangster> gangstersGrid = getGangstersGrid(gangsters);
        ListStore<Holdup> holdups = getHoldups();
        holdUpGrid = getHoldupsGrid(holdups);

        VerticalLayoutContainer vContainer = new VerticalLayoutContainer();
        HorizontalLayoutContainer hContainer = new HorizontalLayoutContainer();
        hContainer.add(gangstersGrid, new HorizontalLayoutData(.3, 1, new Margins(5)));
        hContainer.add(holdUpGrid, new HorizontalLayoutData(.7, 1, new Margins(5, 5, 5, 0)));
        vContainer.add(hContainer,new VerticalLayoutData(1, 1));
        vContainer.add(tip,new VerticalLayoutData(-1, -1));
        panel.add(vContainer);

        // track mouse hover on the target grid
        new GridHoverHandler<Holdup>(holdUpGrid) {
            @Override
            public void setObjHover(Holdup object, boolean isHovered) {
                if (isHovered) {
                    // drop target
                    holdupHover = object;
                } else {
                    holdupHover = null;
                }
            }
        };
        GridDragSource<Gangster> gangstersDragSource = new GridDragSource<Gangster>(gangstersGrid) {
            @Override
            protected void onDragDrop(DndDropEvent event) {} // disable MOVE operation
        };
        gangstersDragSource.setGroup("Criminal conspiracy");
        GridDragSource<Holdup> holdupDragSource = new GridDragSource<Holdup>(holdUpGrid) {
            @Override
            protected void onDragDrop(DndDropEvent event) {} // disable MOVE operation
            @Override
            protected void onDragStart(DndDragStartEvent event) {
                Element r = grid.getView().findRow(event.getDragStartEvent().getStartElement()).cast();
                if (r == null) {
                    event.setCancelled(true);
                    return;
                }
                int size = 0;
                if (gangsterHover == null) {
                    super.onDragStart(event); // hold the line
                    Object o = event.getData();
                    if (o != null) {
                        List<Gangster> gangsters = new ArrayList<Gangster>();
                        @SuppressWarnings("unchecked")
                        List<Holdup> holdups = (List<Holdup>) o;
                        for (Holdup h: holdups) {
                            for (Gangster g: h.getGangsters()) {
                                if (! gangsters.contains(g)) {
                                    gangsters.add(g);
                                }
                            }
                        }
                        size = gangsters.size();
                        if (size > 0) {
                            event.setData(gangsters);

                        } else {
                            event.setData(null);
                            event.setCancelled(true);
                        }
                    }
                } else {
                    size = 1;
                    event.setData(gangsterHover);
                }
                if (! event.isCancelled()) {
                    if (getStatusText() == null) {
                        event.getStatusProxy().update(getMessages().itemsSelected(size));
                    } else {
                        event.getStatusProxy().update(Format.substitute(getStatusText(), size));
                    }
                }
            }
        };
        holdupDragSource.setGroup("Criminal conspiracy");

        GridDropTarget<Holdup> target = new GridDropTarget<Holdup>(holdUpGrid) {
            @Override
            protected void onDragDrop(DndDropEvent event) {
                if (holdupHover != null) {
                    Object o = event.getData();
                    List<Gangster> gangsters = holdupHover.getGangsters();
                    if (o instanceof List) {
                        @SuppressWarnings({ "unchecked" })
                        List<Gangster> g2 = (List<Gangster>) o;
                        for (Gangster g: g2) {
                            if (! gangsters.contains(g)) {
                                gangsters.add(g);
                            }
                        }
                    } else if (o instanceof Gangster) {
                        Gangster g = (Gangster) o;
                        if (! gangsters.contains(g)) {
                            gangsters.add(g);
                        }
                    }
                    holdUpGrid.getView().refresh(false);
                 }
            }
        };
        target.setGroup("Criminal conspiracy");
        target.setAllowSelfAsSource(true);

        return panel;
    }

    // ============= Gangster =============

    private Grid<Gangster> getGangstersGrid(ListStore<Gangster> gangsters) {
        final Grid<Gangster> gangstersGrid = new Grid<Gangster>(gangsters, createGangsterColumnList());
        gangstersGrid.setBorders(true);
        gangstersGrid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
            public void onCellClick(CellDoubleClickEvent event) {
                List<Holdup> holdups = holdUpGrid.getSelectionModel().getSelectedItems();
                if (holdups.size() == 0) {
                    tip.setText("One or several lines must be selected in the right table");
                } else {
                    boolean refresh = false;
                    for (Holdup holdup: holdups) {
                        for (Gangster gangster: gangstersGrid.getSelectionModel().getSelectedItems()) {
                            if (! holdup.getGangsters().contains(gangster)) {
                                holdup.getGangsters().add(gangster);
                                refresh = true;
                            }
                        }
                    }
                    if (refresh) {
                        holdUpGrid.getView().refresh(false);
                    }
                }
            }
        });
        gangstersGrid.getView().setForceFit(true);
        gangstersGrid.addCellClickHandler(new CellClickHandler() {
            public void onCellClick(CellClickEvent event) {
                tip.setText("Drag the selection or DBL-CLIC to add a single item in the select lines if any. CTRL-CLIC (or CMD-CLIC) to select more items to drag.");
            }
        });
        return gangstersGrid;
    }

    interface GangsterProperties extends PropertyAccess<Gangster> {
        @Path("id")
        ModelKeyProvider<Gangster> id();
        @Path("name")
        ValueProvider<Gangster, String> name();
        @Path("aptitude")
        ValueProvider<Gangster, Aptitude> aptitude();

        static GangsterProperties INSTANCE = GWT.create(GangsterProperties.class);
    }
    public interface AptitudeTemplates extends SafeHtmlTemplates {
        @Template("<div style=\"{0} display: inline-block; width: 20px ;\">&nbsp;</div>")
        SafeHtml render(SafeStyles bgStyle);

        static AptitudeTemplates INSTANCE = GWT.create(AptitudeTemplates.class);
    }
    private SafeStyles getBackgroundStyle(Aptitude aptitude) {
        return new SafeStylesBuilder()
            .trustedNameAndValue("background", "linear-gradient(to left, " + aptitude.gradient() + ")")
            .toSafeStyles();
    }
    private ColumnModel<Gangster> createGangsterColumnList() {
        ColumnConfig<Gangster, String> nameCol = new ColumnConfig<Gangster, String>(GangsterProperties.INSTANCE.name());
        nameCol.setHeader(SafeHtmlUtils.fromString("Gangster"));
        ColumnConfig<Gangster, Aptitude> aptitudeCol = new ColumnConfig<Gangster, Aptitude>(GangsterProperties.INSTANCE.aptitude());
        aptitudeCol.setHeader(SafeHtmlUtils.fromString("Apt."));
        aptitudeCol.setWidth(40);
        aptitudeCol.setCell(new AbstractCell<Aptitude>() {
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    Aptitude value, SafeHtmlBuilder sb) {
                sb.append(AptitudeTemplates.INSTANCE.render(getBackgroundStyle(value)));
            }
        });
        List<ColumnConfig<Gangster, ?>> list = new ArrayList<ColumnConfig<Gangster, ?>>();
        list.add(nameCol);
        list.add(aptitudeCol);
        return new ColumnModel<Gangster>(list);
    }

    // ============= Hold Up =============

    private Grid<Holdup> getHoldupsGrid(ListStore<Holdup> holdups) {
        final Grid<Holdup> holdUpGrid = new Grid<Holdup>(holdups, createHoldupColumnList());
        holdUpGrid.setBorders(true);
        holdUpGrid.getView().setForceFit(true);
        holdUpGrid.addCellClickHandler(new CellClickHandler() {
            public void onCellClick(CellClickEvent event) {
                tip.setText("Buttons are draggable. Several lines can be dragged too. A context menu allow to remove the selection");
            }
        });

        Menu menu = new Menu();
        MenuItem item = new MenuItem("Remove all", new SelectionHandler<MenuItem>() {
            public void onSelection(SelectionEvent<MenuItem> event) {
                boolean refresh = false;
                for (Holdup holdup: holdUpGrid.getSelectionModel().getSelectedItems()) {
                    if (holdup.getGangsters().size() > 0) {
                        holdup.getGangsters().clear();
                        refresh = true;
                    }
                }
                if (refresh) {
                    holdUpGrid.getView().refresh(false);
                }
            }
        });
        menu.add(item);
        holdUpGrid.setContextMenu(menu);
        holdUpGrid.addBeforeShowContextMenuHandler(new BeforeShowContextMenuHandler() {
            public void onBeforeShowContextMenu(BeforeShowContextMenuEvent event) {
                for (Holdup holdup: holdUpGrid.getSelectionModel().getSelectedItems()) {
                    if (holdup.getGangsters().size() > 0) {
                        return;
                    }
                }
                event.setCancelled(true);
            }
        });
        return holdUpGrid;
    }

    interface HoldupProperties extends PropertyAccess<Holdup> {
        @Path("id")
        ModelKeyProvider<Holdup> id();
        @Path("target")
        ValueProvider<Holdup, String> target();
        @Path("gangsters")
        ValueProvider<Holdup, List<Gangster>> gangsters();

        static HoldupProperties INSTANCE = GWT.create(HoldupProperties.class);
    }

    public interface GangstersTemplates extends SafeHtmlTemplates {
        // data-index on the button to hold drag starts
        // data-close-index on the img to hold clicks for delete
        @Template("<div data-index=\"{3}\">{0} <span style=\"{2} border: 1; display: inline-block; width: 20px ;\">&nbsp;</span>"
                + " {1}$ <img src=\"cross.png\" data-close-index=\"{3}\"></div>")
        SafeHtml render(String gangster, int price, SafeStyles bgStyle, int index);

        @Template("<div style=\"white-space: normal;\">{0}</div>")
        SafeHtml renderAll(SafeHtml content);

        static GangstersTemplates INSTANCE = GWT.create(GangstersTemplates.class);
    }
    private ColumnModel<Holdup> createHoldupColumnList() {
        ColumnConfig<Holdup, String> targetCol = new ColumnConfig<Holdup, String>(HoldupProperties.INSTANCE.target(), 25);
        targetCol.setHeader(SafeHtmlUtils.fromString("Target"));
        ColumnConfig<Holdup, List<Gangster>> gangstersCol = new ColumnConfig<Holdup, List<Gangster>>(HoldupProperties.INSTANCE.gangsters());
        gangstersCol.setHeader(SafeHtmlUtils.fromString("Gangsters"));
        gangstersCol.setMenuDisabled(true);
        gangstersCol.setComparator(new Comparator<List<Gangster>>() {
            public int compare(List<Gangster> o1, List<Gangster> o2) {
                return o1.size() - o2.size();
            }
        });
        SafeStyles btnPaddingStyle = SafeStylesUtils.fromTrustedString("padding: 1px 3px 0;");
        gangstersCol.setColumnTextClassName(CommonStyles.get().inlineBlock());
        gangstersCol.setColumnTextStyle(btnPaddingStyle);
        gangstersCol.setCell(new AbstractEventCell<List<Gangster>>(BrowserEvents.CLICK, BrowserEvents.MOUSEDOWN, BrowserEvents.MOUSEUP) {
            ButtonCell<Gangster> button = new ButtonCell<Gangster>();
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    List<Gangster> value, SafeHtmlBuilder sb) {
                if (value == null) {
                    return;
                }
                int i = 0;
                SafeHtmlBuilder sb2 = new SafeHtmlBuilder();
                for (Gangster gangster: value) {
                    int price = gangster.getName().length();
                    sb2.append(SafeHtmlUtils.fromSafeConstant("<div style=\"display: inline-block; padding: 1px 3px 0;\">"));
                    button.setHTML(GangstersTemplates.INSTANCE.render(gangster.getName(), price, getBackgroundStyle(gangster.getAptitude()), i++));
                    button.render(context, gangster, sb2);
                    sb2.append(SafeHtmlUtils.fromSafeConstant("</div>"));
                }
                sb.append(GangstersTemplates.INSTANCE.renderAll(sb2.toSafeHtml()));
            }
            @Override
            public void onBrowserEvent(
                    com.google.gwt.cell.client.Cell.Context context,
                    com.google.gwt.dom.client.Element parent,
                    List<Gangster> value, NativeEvent event,
                    ValueUpdater<List<Gangster>> valueUpdater) {
                // Handle the click event.
                if (BrowserEvents.CLICK.equals(event.getType())) {
                    // Ignore clicks that occur outside of the outermost element.
                    EventTarget eventTarget = event.getEventTarget();
                    String index = Element.as(eventTarget).getAttribute("data-close-index");
                    if (index != null && index.length() > 0) {
                        value.remove(Integer.parseInt(index));
                        valueUpdater.update(value);
                    }
                }
                if (BrowserEvents.MOUSEDOWN.equals(event.getType())) {
                    gangsterHover = null;
                    // Ignore clicks that occur outside of the outermost element.
                    EventTarget eventTarget = event.getEventTarget();
                    String index = Element.as(eventTarget).getAttribute("data-close-index");
                    if (index != null && index.length() > 0) {
                        value.remove(Integer.parseInt(index));
                        valueUpdater.update(value);
                    } else {
                        for (Element buttonElem = Element.as(event.getEventTarget()); buttonElem != null; buttonElem = buttonElem.getParentElement()) {
                            index = buttonElem.getAttribute("data-index");
                            if (index != null && index.length() > 0) {
                                gangsterHover = value.get(Integer.parseInt(index));
                            }
                        }
                    }
                } else if (BrowserEvents.MOUSEUP.equals(event.getType())) {
                    gangsterHover = null;
                }
                super.onBrowserEvent(context, parent, value, event, valueUpdater);
            }
        });
        List<ColumnConfig<Holdup, ?>> list = new ArrayList<ColumnConfig<Holdup, ?>>();
        list.add(targetCol);
        list.add(gangstersCol);
        return new ColumnModel<Holdup>(list);
    }

    // ============= data =============

    protected ListStore<Gangster> getGangsters() {
        ListStore<Gangster> gangsters = new ListStore<Gangster>(GangsterProperties.INSTANCE.id());
        gangsters.add(new Gangster(Aptitude.WORST, "Averell"));
        gangsters.add(new Gangster(Aptitude.BAD, "William"));
        gangsters.add(new Gangster(Aptitude.BETTER, "Jack"));
        gangsters.add(new Gangster(Aptitude.BEST, "Joe"));
        gangsters.add(new Gangster(Aptitude.AVERAGE, "Rantanplan"));
        gangsters.add(new Gangster(Aptitude.BEST, "Bonnie"));
        gangsters.add(new Gangster(Aptitude.BAD, "Clyde"));
        return gangsters;
    }

    protected ListStore<Holdup> getHoldups() {
        ListStore<Holdup> holdups = new ListStore<Holdup>(new ModelKeyProvider<Holdup>() {
            public String getKey(Holdup holdup) {
                return String.valueOf(holdup.getId());
            }
        });
        holdups.add(new Holdup("Banque"));
        holdups.add(new Holdup("Train"));
        holdups.add(new Holdup("Bijouterie"));
        holdups.add(new Holdup("Buraliste"));
        holdups.add(new Holdup("Petite vieille"));
        return holdups;
    }

}
