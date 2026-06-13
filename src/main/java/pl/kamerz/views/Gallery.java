package pl.kamerz.views;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@PageTitle("Gallery")
@Route(value = "gallery", layout = Layout.class)
public class Gallery extends VerticalLayout {

    public record Project(String name, String description, String tag, String link) {
    }

    public Gallery() {
        addClassName("gallery-view");
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Gallery");
        title.addClassName("page-title");
        Paragraph subtitle = new Paragraph("Premade projects, ready to use.");
        subtitle.addClassName("page-subtitle");
        add(title, subtitle);

        String discord = "https://dsc.gg/spashapi";

        add(section("APIs", "Supported executors grab them in the Discord.", List.of(
                new Project("Velocity", "Velocity executor API support.", "API", discord),
                new Project("Madium", "Madium executor API support.", "API", discord),
                new Project("Ronix", "Ronix executor API support.", "API", discord),
                new Project("Xeno", "Xeno executor API support.", "API", discord),
                new Project("Pluto", "Pluto executor API support.", "Soon", null),
                new Project("Solara", "Solara executor API support.", "Soon", null))));

        add(soonSection("Injectors", "Injection tooling."));

        add(section("Modules", "Drop-in modules, hosted on GitHub.", List.of(
                new Project("Modules", "Open-source modules repository.", "GitHub",
                        "https://github.com/Spash-Softworks/module"))));
    }

    private Div soonSection(String heading, String desc) {
        Div section = new Div();
        section.addClassName("gallery-section");

        H2 h = new H2(heading);
        h.addClassName("section-title");
        Paragraph d = new Paragraph(desc);
        d.addClassName("section-desc");

        Div soon = new Div();
        soon.addClassName("soon-placeholder");
        soon.add(new Span("Soon"));

        section.add(h, d, soon);
        return section;
    }

    private Div section(String heading, String desc, List<Project> projects) {
        Div section = new Div();
        section.addClassName("gallery-section");

        H2 h = new H2(heading);
        h.addClassName("section-title");
        Paragraph d = new Paragraph(desc);
        d.addClassName("section-desc");
        section.add(h, d);

        Div grid = new Div();
        grid.addClassName("card-grid");
        for (Project p : projects) grid.add(card(p));
        section.add(grid);
        return section;
    }

    private Div card(Project p) {
        Div card = new Div();
        card.addClassName("project-card");

        Span tag = new Span(p.tag());
        tag.addClassName("card-tag");
        H3 name = new H3(p.name());
        name.addClassName("card-name");
        Paragraph desc = new Paragraph(p.description());
        desc.addClassName("card-desc");

        card.add(tag, name, desc);

        if (p.link() != null) {
            Anchor link = new Anchor(p.link(), "View");
            link.setTarget("_blank");
            link.addClassName("card-link");
            card.add(link);
        } else {
            Span soon = new Span("Soon");
            soon.addClassName("card-soon");
            card.add(soon);
            card.addClassName("is-soon");
        }
        return card;
    }
}
