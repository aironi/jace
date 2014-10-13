package org.silverduck.jace.web;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.cdi.internal.VaadinCDIServlet;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Constants;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import javax.inject.Inject;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

@Theme("valo")
@SuppressWarnings("serial")
@Push(value = PushMode.MANUAL, transport = Transport.LONG_POLLING) // Due to (a lack in CDI spec?) no WebSockets may be used. See: http://stackoverflow.com/questions/24747434/atmosphere-managedservice-injection-fails-with-websockets
@CDIUI
public class JaceUI extends UI {


    // Have to set asyncSupported here, otherwise async won't work with Vaadin
    @WebServlet(asyncSupported = true, urlPatterns = { "/", "/*", "/VAADIN/*" }, initParams = {
            @WebInitParam(name = VaadinSession.UI_PARAMETER, value = "org.silverduck.jace.web.JaceUI"),
            @WebInitParam(name = Constants.SERVLET_PARAMETER_UI_PROVIDER, value = "com.vaadin.cdi.CDIUIProvider"),
            @WebInitParam(name = Constants.SERVLET_PARAMETER_PRODUCTION_MODE, value = "false"),
            @WebInitParam(name = Constants.SERVLET_PARAMETER_PUSH_MODE, value = "manual") })
    public static class JaceUIApplicationServlet extends VaadinCDIServlet {
    }

    @Inject
    private CDIViewProvider viewProvider;


    /**
     * Helper method to navigate between Views.
     * 
     * @param view
     *            View name defined for a CDIView
     */
    public static void navigateTo(String view, Object... parameters) {
        final StringBuilder sb = new StringBuilder();
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

    @Override
    protected void init(VaadinRequest request) {
        initializeLayout(request);
    }

    private void initializeLayout(VaadinRequest request) {
        VerticalLayout navigatorLayout = new VerticalLayout();
        navigatorLayout.setSizeFull();
        Navigator navigator = new Navigator(this, navigatorLayout);
        navigator.addProvider(viewProvider);
        setContent(navigatorLayout);
    }

}
