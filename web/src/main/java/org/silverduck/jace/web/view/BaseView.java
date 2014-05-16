package org.silverduck.jace.web.view;

import com.vaadin.navigator.View;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.silverduck.jace.web.component.ManagementToolbar;

/**
 * @author Iiro Hietala 13.5.2014.
 */
public abstract class BaseView extends GridLayout implements View {

    private GridLayout contentLayout;

    public BaseView() {
        setSizeFull();
        setSpacing(true);
        setMargin(true);
        setContentLayout(new GridLayout());
        getContentLayout().setSizeFull();
        getContentLayout().setSpacing(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.addComponent(new ManagementToolbar());
        layout.addComponent(getContentLayout());
        layout.addComponent(new Label("Footer Area"));
        super.addComponent(layout);
        layout.setExpandRatio(getContentLayout(), 1);
    }

    public GridLayout getContentLayout() {
        return contentLayout;
    }

    public void setContentLayout(GridLayout contentLayout) {
        this.contentLayout = contentLayout;
    }
}
