package org.silverduck.jace.web;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@Theme("mytheme")
@SuppressWarnings("serial")
@CDIUI
public class JaceUI extends UI {

    /**
     * Helper method to navigate between Views.
     * 
     * @param view
     *            View name defined for a CDIView
     */
    public static void navigateTo(String view, Object... parameters) {

        StringBuilder sb = new StringBuilder();
        sb.append("!").append(view);

        if (parameters != null && parameters.length > 0 && parameters[0] != null) {
            sb.append("/");

            for (int i = 0; i < parameters.length; i++) {
                String parameter = parameters[i].toString();
                sb.append(parameter);
                if (i < parameters.length - 1) {
                    sb.append("/");
                }
            }
        }
        Page.getCurrent().setUriFragment(sb.toString());

    }

    @Inject
    private CDIViewProvider viewProvider;

    @Override
    protected void init(VaadinRequest request) {
        initializeLayout(request);
    }

    private void initializeLayout(VaadinRequest request) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSizeFull();
        Navigator navigator = new Navigator(this, horizontalLayout);
        navigator.addProvider(viewProvider);
        setContent(horizontalLayout);
    }

    @PostConstruct
    public void initJPAContainer() {
        // applicantsContainer = new JPAContainer<Applicant>(Applicant.class);
        // applicantsContainer.setEntityProvider(applicantsRepository);
    }

}
