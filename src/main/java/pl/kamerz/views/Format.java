package pl.kamerz.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import pl.kamerz.service.OffsetService;
import pl.kamerz.utils.Formatter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Format")
@Route(value = "format", layout = Layout.class)
public class Format extends VerticalLayout {

    private String outputCode = "";

    public Format() {
        addClassName("format-view");
        setPadding(false);
        setSpacing(false);

        H1 title = new H1("Offset Formatter");
        title.addClassName("page-title");
        Paragraph subtitle = new Paragraph(
                "Paste a raw .hpp dump or drop a file. Pick an output format and hit Format.");
        subtitle.addClassName("page-subtitle");

        Div header = new Div(title, subtitle);
        header.addClassName("docs-header");

        Select<Formatter> formatSelect = new Select<>();
        formatSelect.setItems(Formatter.values());
        formatSelect.setValue(Formatter.CSHARP);
        formatSelect.setLabel("Output format");
        formatSelect.addClassName("fmt-select");

        TextField versionField = new TextField("Version hash");
        versionField.setPlaceholder("e.g. ad5d3e2906444472");
        versionField.addClassName("fmt-field");

        TextField categoryField = new TextField("Category");
        categoryField.setValue("Classes");
        categoryField.addClassName("fmt-field");

        Button formatBtn = new Button("Format");
        formatBtn.addClassName("fmt-btn");

        HorizontalLayout controls = new HorizontalLayout(formatSelect, versionField, categoryField, formatBtn);
        controls.addClassName("fmt-controls");
        controls.setAlignItems(Alignment.END);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".hpp", ".h", ".txt");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Drop .hpp file here"));
        upload.addClassName("fmt-upload");
        upload.addSucceededListener(ev -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(buffer.getInputStream(), StandardCharsets.UTF_8))) {
                String code = r.lines().collect(Collectors.joining("\n"));
                getElement().executeJs("if(window.__fmtSetInput) window.__fmtSetInput($0)", code);
            } catch (Exception ex) {
                Notification.show("Could not read file: " + ex.getMessage());
            }
        });

        Div inputPaneLabel = new Div(new Span("Input"));
        inputPaneLabel.addClassName("fmt-pane-label");

        Div inputEditor = new Div();
        inputEditor.setId("fmt-input-editor");
        inputEditor.addClassName("fmt-editor");

        Div inputPane = new Div(inputPaneLabel, upload, inputEditor);
        inputPane.addClassName("fmt-pane");

        Button copyBtn = new Button("Copy");
        copyBtn.addClassName("fmt-action-btn");
        copyBtn.addClickListener(ev ->
                getElement().executeJs(
                        "const w=document.getElementById('fmt-output-editor');" +
                        "if(w&&w.__monacoEd) navigator.clipboard.writeText(w.__monacoEd.getValue());" +
                        "return 'ok';"));

        Anchor downloadAnchor = new Anchor();
        downloadAnchor.addClassNames("fmt-action-btn", "fmt-download");
        downloadAnchor.getElement().setProperty("innerText", "Download");
        downloadAnchor.getElement().setAttribute("download", "offsets.txt");

        Div outputActions = new Div(new Span("Output"), copyBtn, downloadAnchor);
        outputActions.addClassName("fmt-pane-label");

        Div outputEditor = new Div();
        outputEditor.setId("fmt-output-editor");
        outputEditor.addClassName("fmt-editor");

        Div outputPane = new Div(outputActions, outputEditor);
        outputPane.addClassName("fmt-pane");

        HorizontalLayout editors = new HorizontalLayout(inputPane, outputPane);
        editors.addClassName("fmt-editors");
        editors.setWidthFull();

        formatBtn.addClickListener(ev ->
                getElement().executeJs("return window.__fmtGetInput ? window.__fmtGetInput() : ''")
                        .then(String.class, code -> {
                            if (code == null || code.isBlank()) {
                                Notification.show("Paste some .hpp code first.");
                                return;
                            }
                            List<OffsetService.Offset> parsed = OffsetService.parseHpp(code);
                            if (parsed.isEmpty()) {
                                Notification.show("No offsets found in input.");
                                return;
                            }
                            String version = versionField.getValue().isBlank()
                                    ? "custom" : versionField.getValue().strip();
                            String category = categoryField.getValue().isBlank()
                                    ? "Custom" : categoryField.getValue().strip();
                            Formatter fmt = formatSelect.getValue();
                            outputCode = fmt.format(parsed, version, category);
                            String ext = fmt.extension;
                            String lang = fmt.monacoLanguage;

                            StreamResource res = new StreamResource(
                                    "offsets." + ext,
                                    () -> new ByteArrayInputStream(
                                            outputCode.getBytes(StandardCharsets.UTF_8)));
                            downloadAnchor.setHref(res);

                            getElement().executeJs(
                                    "if(window.__fmtSetOutput) window.__fmtSetOutput($0,$1)",
                                    outputCode, lang);
                        }));

        add(header, controls, editors);
    }

    @Override
    protected void onAttach(AttachEvent e) {
        super.onAttach(e);
        getElement().executeJs("""
            const boot = () => {
                window.require.config({ paths: { vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs' } });
                window.require(['vs/editor/editor.main'], init);
            };
            const init = () => {
                const mq = window.matchMedia('(prefers-color-scheme: dark)');
                const theme = () => mq.matches ? 'vs-dark' : 'vs';
                const shared = {
                    automaticLayout: true, minimap: { enabled: false }, fontSize: 13,
                    scrollBeyondLastLine: false, lineNumbers: 'on',
                    scrollbar: { alwaysConsumeMouseWheel: false },
                    padding: { top: 12, bottom: 12 },
                    fontFamily: "'SF Mono', ui-monospace, 'Cascadia Code', Consolas, monospace"
                };
                const inWrap = document.getElementById('fmt-input-editor');
                const inEd = monaco.editor.create(inWrap, {
                    ...shared, value: '', language: 'cpp', theme: theme(),
                    readOnly: false, renderLineHighlight: 'line'
                });
                inWrap.__monacoEd = inEd;

                const outWrap = document.getElementById('fmt-output-editor');
                const outEd = monaco.editor.create(outWrap, {
                    ...shared, value: '', language: 'csharp', theme: theme(),
                    readOnly: true, renderLineHighlight: 'none', overviewRulerLanes: 0
                });
                outWrap.__monacoEd = outEd;

                mq.addEventListener('change', () => {
                    inEd.updateOptions({ theme: theme() });
                    outEd.updateOptions({ theme: theme() });
                });

                window.__fmtGetInput = () => inEd.getValue();
                window.__fmtSetInput = v => inEd.setValue(v);
                window.__fmtSetOutput = (v, lang) => {
                    outEd.setValue(v);
                    monaco.editor.setModelLanguage(outEd.getModel(), lang);
                };
            };
            if (window.monaco && window.monaco.editor) { init(); }
            else if (window.require && window.require.config) { boot(); }
            else {
                const s = document.createElement('script');
                s.src = 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs/loader.js';
                s.onload = boot;
                document.head.appendChild(s);
            }
        """);
    }
}
