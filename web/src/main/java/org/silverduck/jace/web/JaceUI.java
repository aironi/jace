package org.silverduck.jace.web;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Constants;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
@SuppressWarnings("serial")
@Push
@CDIUI
public class JaceUI extends UI {

    // Have to set asyncSupported here, otherwise asnyc won't work with Vaadin
    @WebServlet(asyncSupported = true, urlPatterns = { "/", "/*", "/VAADIN/*" }, initParams = {
            @WebInitParam(name = VaadinSession.UI_PARAMETER, value = "org.silverduck.jace.web.JaceUI"),
            @WebInitParam(name = Constants.SERVLET_PARAMETER_UI_PROVIDER, value = "com.vaadin.cdi.CDIUIProvider"),
            @WebInitParam(name = Constants.SERVLET_PARAMETER_PRODUCTION_MODE, value = "false"),
            @WebInitParam(name = Constants.SERVLET_PARAMETER_PUSH_MODE, value = "automatic") })
    public static class JaceUIApplicationServlet extends VaadinServlet {
    }

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

}
