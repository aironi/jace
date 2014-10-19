package org.silverduck.jace.web.view;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.silverduck.jace.web.component.ManagementToolbar;

import javax.annotation.PostConstruct;

/**
 * @author Iiro Hietala 13.5.2014.
 */
public abstract class BaseView extends GridLayout implements View {

    private GridLayout contentLayout;

    protected abstract Layout getContentLayout();

    @PostConstruct
    private void initLayout() {
        setSizeFull();
        getContentLayout().setSizeFull();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.addComponent(new ManagementToolbar());
        mainLayout.addComponent(getContentLayout());
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setSpacing(true);

        footerLayout.addComponent(new Label(
                "If you think this software is useful and wish to encourage further development, please feel free to  "));
        footerLayout
                .addComponent(new Label(
                        "<form action=\"https://www.paypal.com/cgi-bin/webscr\" method=\"post\" target=\"_top\">\n"
                                + "<input type=\"hidden\" name=\"cmd\" value=\"_s-xclick\">\n"
                                + "<input type=\"hidden\" name=\"encrypted\" value=\"-----BEGIN PKCS7-----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-----END PKCS7-----\n"
                                + "\">\n"
                                + "<input type=\"image\" src=\"https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif\" border=\"0\" name=\"submit\" alt=\"PayPal - The safer, easier way to pay online!\">\n"
                                + "<img alt=\"\" border=\"0\" src=\"https://www.paypalobjects.com/en_US/i/scr/pixel.gif\" width=\"1\" height=\"1\">\n"
                                + "</form>\n", ContentMode.HTML));

        //mainLayout.addComponent(footerLayout);
        super.addComponent(mainLayout);
        mainLayout.setExpandRatio(getContentLayout(), 1);
    }

}
