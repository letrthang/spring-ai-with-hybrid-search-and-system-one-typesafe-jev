package com.example.ragdemo.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.splitlayout.SplitLayout;

/**
 * A grid on the left, a detail card on the right, and a <b>draggable splitter</b> between them — drag it to make the
 * grid wider or narrower. With no detail the grid takes the whole width and the splitter is hidden. On a phone the
 * card goes below the grid instead of beside it.
 */
public class SplitMasterDetail extends SplitLayout {

    /** Where the splitter sits when a detail opens, in % of the width (the grid gets this share). */
    private static final double DEFAULT_POSITION = 62;
    private static final int PHONE_WIDTH = 700;

    private double position = DEFAULT_POSITION;

    public SplitMasterDetail(Component master) {
        addToPrimary(master);
        addClassNames("rd-split", "rd-split-closed");
        setSplitterPosition(100);
        // remember where the user dragged it, so the next detail opens with the same split
        addSplitterDragEndListener(e -> {
            Double now = getSplitterPosition();
            if (now != null && now > 10 && now < 95) position = now;
        });
    }

    /** Shows {@code detail} next to the grid, or closes the detail area when it is null. */
    public void setDetail(Component detail) {
        Component old = getSecondaryComponent();
        if (old != null) remove(old);
        if (detail == null) {
            addClassName("rd-split-closed");
            setSplitterPosition(100);
            return;
        }
        addToSecondary(detail);
        removeClassName("rd-split-closed");
        setSplitterPosition(getOrientation() == Orientation.VERTICAL ? 50 : position);
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        event.getUI().getPage().executeJs("return window.innerWidth").then(Integer.class,
                width -> setOrientation(width < PHONE_WIDTH ? Orientation.VERTICAL : Orientation.HORIZONTAL));
    }
}
