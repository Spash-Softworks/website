package pl.kamerz.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import pl.kamerz.service.OffsetService;
import pl.kamerz.service.OffsetService.Cat;
import pl.kamerz.service.OffsetService.Offset;
import pl.kamerz.service.OffsetService.OffsetData;
import pl.kamerz.service.OffsetService.Provider;
import pl.kamerz.service.VersionService;
import pl.kamerz.service.VersionService.Version;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Offsets")
@Route(value = "offsets", layout = Layout.class)
public class Offsets extends VerticalLayout {

    private final VersionService versioning;
    private final OffsetService offsets;

    private final RadioButtonGroup<Provider> providerSelect = new RadioButtonGroup<>();
    private final ComboBox<String> versionPicker = new ComboBox<>();
    private final RadioButtonGroup<Cat> categorySelect = new RadioButtonGroup<>();
    private final Div badgeSlot = new Div();
    private final Div contentSlot = new Div();

    private Provider provider = Provider.SODA;
    private OffsetData data;
    private boolean suppress;

    public Offsets(VersionService versioning, OffsetService offsets) {
        this.versioning = versioning;
        this.offsets = offsets;

        addClassName("offsets-view");
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Roblox Offsets");
        title.addClassName("page-title");
        Paragraph subtitle = new Paragraph(
                "Welcome to spash offsets, here you can simply select a provider and a category to download or view the offsets.");
        subtitle.addClassName("page-subtitle");

        add(title, subtitle, buildControls());
        badgeSlot.addClassName("badge-slot");
        contentSlot.addClassName("content-slot");
        contentSlot.setWidthFull();
        add(badgeSlot, contentSlot);

        applyProvider(Provider.SODA);
    }

    private Div buildControls() {
        Div bar = new Div();
        bar.addClassName("offsets-controls");

        providerSelect.setLabel("Provider");
        providerSelect.setItems(Provider.values());
        providerSelect.setValue(Provider.SODA);
        providerSelect.setItemLabelGenerator(p -> p == Provider.SODA ? "Internal" : "External");
        providerSelect.addClassName("provider-select");
        providerSelect.addValueChangeListener(e -> {
            if (!suppress && e.getValue() != null) applyProvider(e.getValue());
        });

        versionPicker.setLabel("Version");
        versionPicker.setAllowCustomValue(true);
        versionPicker.setPlaceholder("live / latest");
        versionPicker.setClearButtonVisible(true);
        versionPicker.setWidth("300px");
        versionPicker.addClassName("version-picker");
        versionPicker.addCustomValueSetListener(e -> versionPicker.setValue(e.getDetail()));
        versionPicker.addValueChangeListener(e -> {
            if (!suppress) loadVersion(e.getValue());
        });

        categorySelect.setLabel("Category");
        categorySelect.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        categorySelect.addClassName("category-select");
        categorySelect.addValueChangeListener(e -> {
            if (!suppress && e.getValue() != null) renderCategory(e.getValue());
        });

        bar.add(providerSelect, versionPicker, categorySelect);
        return bar;
    }

    private void applyProvider(Provider p) {
        this.provider = p;
        List<Cat> cats = offsets.categories(p);
        String live = offsets.liveHash();

        suppress = true;
        versionPicker.setItems(offsets.versions(p));
        versionPicker.setItemLabelGenerator(v ->
                stripPrefix(v).equals(live) ? v + "  (live)" : v);
        versionPicker.clear();
        categorySelect.setItems(cats);
        categorySelect.setValue(cats.get(0));
        suppress = false;

        loadVersion(null);
    }

    private void loadVersion(String version) {
        data = offsets.get(provider, version);
        renderBadge();
        Cat c = categorySelect.getValue();
        renderCategory(c != null ? c : offsets.categories(provider).get(0));
    }

    private static String stripPrefix(String v) {
        return v != null && v.startsWith("version-") ? v.substring(8) : (v == null ? "" : v);
    }

    private void renderBadge() {
        badgeSlot.removeAll();
        badgeSlot.add(versionBadge(versioning.windows(), data));
    }

    private Div versionBadge(Version v, OffsetData d) {
        Div badge = new Div();
        badge.addClassName("version-badge");

        Span robloxLabel = new Span("Roblox");
        robloxLabel.addClassName("version-label");
        Span robloxValue = new Span(v.ok() ? v.version() : "unavailable");
        robloxValue.addClassName("version-value");
        badge.add(robloxLabel, robloxValue);

        if (d.ok()) {
            Span sep = new Span("·");
            sep.addClassName("version-label");
            Span srcLabel = new Span(provider.label);
            srcLabel.addClassName("version-label");
            Span offsetValue = new Span("version-" + d.offsetVersion());
            offsetValue.addClassNames("version-value", d.current() ? "" : "stale-version");
            badge.add(sep, srcLabel, offsetValue);
            if (d.current()) {
                badge.addClassName("ok");
            } else {
                Span warn = new Span("(not live version)");
                warn.addClassName("version-warn");
                badge.add(warn);
                badge.addClassName("stale");
            }
        } else {
            badge.addClassName("stale");
        }
        return badge;
    }

    private void renderCategory(Cat cat) {
        contentSlot.removeAll();

        if (!data.ok()) {
            Paragraph err = new Paragraph("Sorry we couldn't fetch the offsets: " + data.error() + " TwT");
            err.addClassName("page-subtitle");
            contentSlot.add(err);
            return;
        }

        List<Offset> rows = data.entries(cat.file());
        contentSlot.add(actionBar(cat, rows));

        if (rows.isEmpty()) {
            Paragraph note = new Paragraph(
                    "Sorry, we cannot render the tab system with structs, please click view or download instead for " + cat.file() + ".");
            note.addClassName("page-subtitle");
            contentSlot.add(note);
            return;
        }

        TextField search = new TextField();
        search.setPlaceholder("Search");
        search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        search.setClearButtonVisible(true);
        search.addClassName("offset-search");
        search.setWidthFull();

        Grid<Offset> grid = buildGrid(rows);
        @SuppressWarnings("unchecked")
        ListDataProvider<Offset> dp = (ListDataProvider<Offset>) grid.getDataProvider();
        search.addValueChangeListener(e -> {
            String q = e.getValue().strip().toLowerCase();
            dp.setFilter(o -> q.isEmpty()
                    || o.name().toLowerCase().contains(q)
                    || o.namespace().toLowerCase().contains(q));
        });

        Span count = new Span(rows.size() + " entries");
        count.addClassName("offset-count");

        contentSlot.add(search, count, grid);
    }

    private HorizontalLayout actionBar(Cat cat, List<Offset> rows) {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName("offset-actions");
        bar.setAlignItems(FlexComponent.Alignment.CENTER);

        String raw = data.source(cat.file());

        Button copyAll = new Button("Copy all", new Icon(VaadinIcon.COPY));
        copyAll.addThemeVariants(ButtonVariant.LUMO_SMALL);
        copyAll.setEnabled(!rows.isEmpty());
        copyAll.addClickListener(e -> copyToClipboard(
                rows.stream()
                        .map(o -> o.namespace() + "::" + o.name() + " = " + o.value())
                        .collect(Collectors.joining("\n")),
                rows.size() + " offsets copied"));

        Button view = new Button("View source", new Icon(VaadinIcon.CODE));
        view.addThemeVariants(ButtonVariant.LUMO_SMALL);
        view.setEnabled(!raw.isBlank());
        view.addClickListener(e -> openSourceDialog(cat, raw));

        Anchor download = new Anchor(rawResource(cat, raw), "");
        download.getElement().setAttribute("download", true);
        Button dlBtn = new Button("Download", new Icon(VaadinIcon.DOWNLOAD));
        dlBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        download.add(dlBtn);
        download.addClassName("offset-download");

        bar.add(copyAll, view, download);
        return bar;
    }

    private StreamResource rawResource(Cat cat, String raw) {
        String fileName = cat.file().replace(".hpp", "") + "-" + data.offsetVersion() + ".hpp";
        return new StreamResource(fileName,
                () -> new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));
    }

    private void openSourceDialog(Cat cat, String raw) {
        Dialog dialog = new Dialog();
        dialog.addClassName("source-dialog");
        dialog.setHeaderTitle(cat.file() + " · version-" + data.offsetVersion());
        dialog.setWidth("min(900px, 92vw)");
        dialog.setHeight("min(720px, 85vh)");
        dialog.setResizable(true);

        dialog.add(new Monaco("cpp", raw));

        Button copy = new Button("Copy", new Icon(VaadinIcon.COPY));
        copy.addClickListener(e -> copyToClipboard(raw, "Source copied"));
        Button close = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(copy, close);
        dialog.open();
    }

    private Grid<Offset> buildGrid(List<Offset> rows) {
        Grid<Offset> grid = new Grid<>(Offset.class, false);
        grid.addClassName("offsets-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COMPACT);

        grid.addColumn(Offset::namespace).setHeader("Class / Namespace")
                .setAutoWidth(true).setFlexGrow(0).setSortable(true).setResizable(true);
        grid.addColumn(Offset::name).setHeader("Name")
                .setFlexGrow(1).setSortable(true).setResizable(true);
        grid.addComponentColumn(o -> {
                    Span val = new Span(o.value());
                    val.addClassName("mono-cell");
                    return val;
                }).setHeader("Value").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Offset::value);

        grid.addComponentColumn(o -> {
            Button copy = new Button(new Icon(VaadinIcon.COPY));
            copy.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_ICON);
            copy.getElement().setAttribute("title", "Copy");
            copy.addClickListener(e -> copyToClipboard(
                    o.namespace() + "::" + o.name() + " = " + o.value(),
                    "Copied " + o.name()));
            return copy;
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);

        grid.setDataProvider(new ListDataProvider<>(rows));
        grid.addClassName("bounded-grid");
        return grid;
    }

    private void copyToClipboard(String text, String msg) {
        getElement().executeJs("navigator.clipboard.writeText($0)", text)
                .then(ok -> Notification.show(msg, 1800, Notification.Position.BOTTOM_END));
    }
}
