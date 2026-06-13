package pl.kamerz.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;

/**
 * Read-only Monaco editor loaded from CDN; theme tracks OS color scheme and updates live.
 */
public class Monaco extends Div {

    private final String language;
    private final String content;

    public Monaco(String language, String content) {
        this.language = language == null ? "plaintext" : language;
        this.content = content == null ? "" : content;
        addClassName("monaco-viewer");
        setSizeFull();
    }

    @Override
    protected void onAttach(AttachEvent e) {
        super.onAttach(e);
        getElement().executeJs(
                "const el = this;" +
                        "const lang = $0; const code = $1;" +
                        "const mq = window.matchMedia('(prefers-color-scheme: dark)');" +
                        "const theme = () => mq.matches ? 'vs-dark' : 'vs';" +
                        "const create = () => {" +
                        "  if (el.__monaco) el.__monaco.dispose();" +
                        "  el.__monaco = window.monaco.editor.create(el, {" +
                        "    value: code, language: lang, theme: theme()," +
                        "    readOnly: true, automaticLayout: true," +
                        "    minimap: { enabled: false }," +
                        "    fontSize: 13, scrollBeyondLastLine: false, smoothScrolling: true," +
                        "    fontFamily: \"'SF Mono', ui-monospace, 'Cascadia Code', Consolas, monospace\"" +
                        "  });" +
                        "  mq.addEventListener('change', () => {" +
                        "    if (el.__monaco) el.__monaco.updateOptions({ theme: theme() });" +
                        "  });" +
                        "};" +
                        "const boot = () => {" +
                        "  window.require.config({ paths: { vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs' } });" +
                        "  window.require(['vs/editor/editor.main'], create);" +
                        "};" +
                        "if (window.monaco && window.monaco.editor) { create(); }" +
                        "else if (window.require && window.require.config) { boot(); }" +
                        "else {" +
                        "  const s = document.createElement('script');" +
                        "  s.src = 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs/loader.js';" +
                        "  s.onload = boot; document.head.appendChild(s);" +
                        "}",
                language, content);
    }
}
