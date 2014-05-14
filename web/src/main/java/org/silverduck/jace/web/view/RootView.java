package org.silverduck.jace.web.view;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;

/**
 * The root view of the application.
 */
@CDIView
public class RootView extends BaseView implements View {
    public static final String VIEW = "";

    public RootView() {
        setSizeFull();
        setDefaultComponentAlignment(Alignment.TOP_CENTER);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
        contentLayout.setSpacing(true);

        contentLayout.addComponent(new Label("Nothing yet :("));
        super.getContentLayout().addComponent(contentLayout);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}
