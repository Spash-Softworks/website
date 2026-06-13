package pl.kamerz.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@PageTitle("Home")
@Route("")
public class Home extends VerticalLayout {

    public Home() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        addClassName("homepage");

        Image logo = new Image("img/logo.png", "spash logo");
        logo.addClassName("branding");

        Div logoWrap = new Div(logo);
        logoWrap.addClassName("logo-wrap");

        Span tagline = new Span("Software, refined.");
        tagline.addClassName("tagline");

        RouterLink offsetsLink = new RouterLink(Offsets.class);
        offsetsLink.addClassName("btn-primary");
        offsetsLink.add(new Html("<i class=\"fa-solid fa-download\"></i>"), new Span("Offsets"));

        RouterLink galleryBtn = new RouterLink(Gallery.class);
        galleryBtn.addClassName("btn-icon");
        galleryBtn.add(new Html("<i class=\"fa-solid fa-table-cells\"></i>"));
        galleryBtn.getElement().setAttribute("title", "Gallery");

        Anchor docsBtn = new Anchor("/docs", "");
        docsBtn.addClassName("btn-icon");
        docsBtn.add(new Html("<i class=\"fa-solid fa-book\"></i>"));
        docsBtn.getElement().setAttribute("title", "Documentation");

        Anchor discordBtn = new Anchor("https://dsc.gg/spashapi", "");
        discordBtn.setTarget("_blank");
        discordBtn.addClassName("btn-icon");
        discordBtn.add(new Html("<i class=\"fa-brands fa-discord\"></i>"));
        discordBtn.getElement().setAttribute("title", "Discord");

        Div iconGroup = new Div(galleryBtn, docsBtn, discordBtn);
        iconGroup.addClassName("icon-group");

        HorizontalLayout buttonRow = new HorizontalLayout(offsetsLink, iconGroup);
        buttonRow.addClassName("button-row");

        Span footerText = new Span("(c) 2026 - spash softworks");
        footerText.addClassName("footer-text");

        add(logoWrap, tagline, buttonRow, footerText);
    }
}
