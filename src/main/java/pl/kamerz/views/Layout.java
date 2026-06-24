package pl.kamerz.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;

public class Layout extends AppLayout {

    public Layout() {
        setPrimarySection(Section.NAVBAR);
        addClassName("main-layout");

        Image logo = new Image("img/logo.png", "spash logo");
        logo.addClassName("nav-logo");
        RouterLink home = new RouterLink("", Home.class);
        home.add(logo);
        home.addClassName("nav-home");

        RouterLink offsets = new RouterLink("Offsets", Offsets.class);
        offsets.addClassName("nav-link");

        RouterLink gallery = new RouterLink("Gallery", Gallery.class);
        gallery.addClassName("nav-link");

        RouterLink docs = new RouterLink("Docs", Docs.class);
        docs.addClassName("nav-link");

        RouterLink format = new RouterLink("Format", Format.class);
        format.addClassName("nav-link");

        Anchor discord = new Anchor("https://dsc.gg/spashapi", "Discord");
        discord.setTarget("_blank");
        discord.addClassName("nav-link");

        HorizontalLayout links = new HorizontalLayout(offsets, gallery, docs, format, discord);
        links.addClassName("nav-links");

        HorizontalLayout bar = new HorizontalLayout(home, links);
        bar.addClassName("nav-bar");
        bar.setWidthFull();

        addToNavbar(bar);
    }
}
