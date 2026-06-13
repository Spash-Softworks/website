package pl.kamerz.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Documentation")
@Route(value = "docs", layout = Layout.class)
public class Docs extends VerticalLayout {

    public Docs() {
        addClassName("docs-view");
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Documentation");
        title.addClassName("page-title");
        Paragraph subtitle = new Paragraph(
                "Our reference guide for dotnet wpf and winforms integrations.");
        subtitle.addClassName("page-subtitle");

        TabSheet tabs = new TabSheet();
        tabs.addClassName("docs-tabs");
        tabs.setWidthFull();
        tabs.add("Velocity", md(VELOCITY));
        tabs.add("Madium", md(MADIUM));
        tabs.add("Ronix", md(RONIX));
        tabs.add("Xeno", md(XENO));
        tabs.add("Pluto", soon("Pluto"));
        tabs.add("Solara", soon("Solara"));
        tabs.add("VoidClient", soon("VoidClient"));
        tabs.add("Yubx", soon("Yubx"));
        tabs.add("Spash Internal", soon("Internal"));

        Div toc = new Div();
        toc.addClassName("docs-toc");
        toc.setId("docs-toc");

        HorizontalLayout body = new HorizontalLayout(tabs, toc);
        body.addClassName("docs-body");
        body.setWidthFull();
        body.setAlignItems(FlexComponent.Alignment.START);

        tabs.addSelectedChangeListener(ev ->
                getElement().executeJs("if (window.__buildToc) window.__buildToc()"));

        add(title, subtitle, body);
    }

    @Override
    protected void onAttach(AttachEvent e) {
        super.onAttach(e);
        getElement().executeJs(
                "window.__buildToc = () => setTimeout(() => {" +
                        "  const toc = document.getElementById('docs-toc');" +
                        "  if (!toc) return;" +
                        "  const all = Array.from(document.querySelectorAll('.docs-md'));" +
                        "  const active = all.find(el => getComputedStyle(el).display !== 'none') || all[0];" +
                        "  if (!active) { toc.innerHTML = ''; return; }" +
                        "  const hs = Array.from(active.querySelectorAll('h2, h3'));" +
                        "  if (!hs.length) { toc.innerHTML = ''; return; }" +
                        "  let html = '<p class=\"toc-heading\">On this page</p><ul class=\"toc-list\">';" +
                        "  hs.forEach((h, i) => {" +
                        "    const id = 'sec-' + i;" +
                        "    h.id = id;" +
                        "    const cls = h.tagName === 'H3' ? ' toc-h3' : '';" +
                        "    html += `<li class=\"toc-item${cls}\"><a href=\"#${id}\"` +" +
                        "      ` onclick=\"event.preventDefault();document.getElementById('${id}')` +" +
                        "      `.scrollIntoView({behavior:'smooth'});return false;\">${h.textContent}</a></li>`;" +
                        "  });" +
                        "  toc.innerHTML = html + '</ul>';" +
                        "}, 120);" +
                        "const hl = () => {" +
                        "  document.querySelectorAll('.docs-md pre code:not(.hljs)').forEach(el => {" +
                        "    if (window.hljs) hljs.highlightElement(el);" +
                        "  });" +
                        "};" +
                        "const boot = () => {" +
                        "  const cs = document.createElement('script');" +
                        "  cs.src = 'https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/languages/csharp.min.js';" +
                        "  cs.onload = () => { hl(); window.__buildToc(); };" +
                        "  document.head.appendChild(cs);" +
                        "  const obs = new MutationObserver(hl);" +
                        "  obs.observe(document.body, { childList: true, subtree: true });" +
                        "  setTimeout(() => obs.disconnect(), 10000);" +
                        "};" +
                        "if (window.hljs) { boot(); }" +
                        "else {" +
                        "  const s = document.createElement('script');" +
                        "  s.src = 'https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js';" +
                        "  s.onload = boot; document.head.appendChild(s);" +
                        "}" +
                        "window.__buildToc();"
        );
    }

    private static Markdown md(String content) {
        Markdown m = new Markdown(content);
        m.addClassName("docs-md");
        return m;
    }

    private static Markdown soon(String name) {
        return md("## SpashAPI" + name +
                "\n\nSorry, this is still in development, Thanks for understanding tho :3");
    }

    private static final String VELOCITY = """
            ## SpashAPIVelocity

            .NET wrapper for the Velocity Executor.
            Get it on the Discord: [dsc.gg/spashapi](https://dsc.gg/spashapi)

            ### API

            | Method | Description |
            |--------|-------------|
            | SpashAPIVelocity.API.IsAttached() | Returns whether the API is attached to a Roblox instance |
            | SpashAPIVelocity.API.IsRobloxOpen() | Returns whether a Roblox process is running |
            | SpashAPIVelocity.API.KillRoblox("pid") | Terminates the Roblox process with the given PID |
            | SpashAPIVelocity.API.KillAllRobloxInstances() | Terminates every running Roblox process |
            | SpashAPIVelocity.API.AttachAPI() | Attaches to the active Roblox instance |
            | SpashAPIVelocity.API.ExecuteScript(Luau.Text, "") | Executes a Luau script in the attached instance |
            | SpashAPIVelocity.API.ForceUpdate() | Forces Velocity to check for and apply updates |
            | SpashAPIVelocity.API.AutoAttachAPI(true) | Enables or disables automatic attachment on Roblox launch |

            ### Custom

            | Method | Description |
            |--------|-------------|
            | SpashAPIVelocity.Custom.UserAgent("Spash/1.0.0.0") | Overrides the user-agent string sent by Velocity |
            | SpashAPIVelocity.Custom.IdentifyExecutor("Spash", "1.0.0.0", true) | Sets executor name, version, and visibility |
            | SpashAPIVelocity.Custom.HideOverly(true) | Shows or hides the Velocity overlay |

            ### Example

            ```csharp
            // paste this in Window_Loaded or Form_Load or whatever :3
            SpashAPIVelocity.Custom.IdentifyExecutor("Spash", "1.0.0.0", true);
            SpashAPIVelocity.Custom.UserAgent("Spash/1.0.0.0");

            // paste this in the inject button :3
            private void Inject_Click(object sender, RoutedEventArgs e)
            {
                SpashAPIVelocity.API.AttachAPI();
            }

            // paste this in the execute button :3
            private void Execute_Click(object sender, RoutedEventArgs e)
            {
                if (!SpashAPIVelocity.API.IsAttached()) return;
                SpashAPIVelocity.API.ExecuteScript(ScriptBox.Text, "");
            }
            ```
            """;

    private static final String MADIUM = """
            ## SpashAPIMadium

            .NET wrapper for the Madium Executor.
            Get it on the Discord: [dsc.gg/spashapi](https://dsc.gg/spashapi)

            ### API

            | Method | Description |
            |--------|-------------|
            | SpashAPIMadium.API.IsAttached() | Returns whether the API is attached to a Roblox instance |
            | SpashAPIMadium.API.IsRobloxOpen() | Returns whether a Roblox process is running |
            | SpashAPIMadium.API.KillRoblox("pid") | Terminates the Roblox process with the given PID |
            | SpashAPIMadium.API.KillInstance() | Terminates the currently attached Roblox instance |
            | SpashAPIMadium.API.Attach() | Attaches to Roblox |
            | SpashAPIMadium.API.AttachAPI() | Attaches to the active Roblox instance |
            | SpashAPIMadium.API.ExecuteScript(Luau.Text) | Executes a Luau script in the attached instance |
            | SpashAPIMadium.API.ForceUpdate() | Forces Madium to check for and apply updates |
            | SpashAPIMadium.API.UpdateAPI() | Updates the SpashAPI Madium wrapper |
            | SpashAPIMadium.API.AutoAttach(true/false) | Enables or disables automatic attachment on Roblox launch |
            | SpashAPIMadium.API.RakNet(pid, enabled) | Toggles RakNet for a specific Roblox PID |
            | SpashAPIMadium.API.SetRakNet(pid, enabled) | Sets RakNet state for a specific Roblox PID |
            | SpashAPIMadium.API.RestoreIdentity() | Restores the executor's default identity |
            | SpashAPIMadium.API.HideOverlay(true) | Shows or hides the Madium overlay |

            ### Custom

            | Method | Description |
            |--------|-------------|
            | SpashAPIMadium.API.Custom.SetUserAgent("Spash/1.0.0.0") | Overrides the user-agent string sent by Madium |
            | SpashAPIMadium.API.Custom.SetIdentity("Spash", "1.0.0.0", true) | Sets executor name, version, and visibility |

            ### Example

            ```csharp
            // paste this in Window_Loaded or Form_Load or whatever :3
            SpashAPIMadium.API.Custom.SetIdentity("Spash", "1.0.0.0", true);
            SpashAPIMadium.API.Custom.SetUserAgent("Spash/1.0.0.0");
            SpashAPIMadium.API.AutoAttach(false);

            // paste this in the inject button :3
            private void Inject_Click(object sender, RoutedEventArgs e)
            {
                SpashAPIMadium.API.AttachAPI();
            }

            // paste this in the execute button :3
            private void Execute_Click(object sender, RoutedEventArgs e)
            {
                if (!SpashAPIMadium.API.IsAttached()) return;
                SpashAPIMadium.API.ExecuteScript(ScriptBox.Text);
            }
            ```
            """;

    private static final String RONIX = """
            ## SpashAPIRonix

            .NET wrapper for the Ronix Executor.
            Get it on the Discord: [dsc.gg/spashapi](https://dsc.gg/spashapi)

            ### API

            | Method | Description |
            |--------|-------------|
            | SpashAPIRonix.API.IsAttached() | Returns whether the API is attached to a Roblox instance |
            | SpashAPIRonix.API.IsRobloxOpen() | Returns whether a Roblox process is running |
            | SpashAPIRonix.API.KillRoblox("pid") | Terminates the Roblox process with the given PID |
            | SpashAPIRonix.API.KillAllRobloxInstances() | Terminates every running Roblox process |
            | SpashAPIRonix.API.AttachAPI() | Attaches to the active Roblox instance |
            | SpashAPIRonix.API.ExecuteScript(Luau.Text, "") | Executes a Luau script in the attached instance |
            | SpashAPIRonix.API.ForceUpdate() | Forces Ronix to check for and apply updates |
            | SpashAPIRonix.API.AutoAttachAPI(true) | Enables or disables automatic attachment on Roblox launch |
            | SpashAPIRonix.API.RakNet(true) | Enables or disables RakNet |

            ### Custom

            | Method | Description |
            |--------|-------------|
            | SpashAPIRonix.Custom.UserAgent("Spash/1.0.0.0") | Overrides the user-agent string sent by Ronix |
            | SpashAPIRonix.Custom.IdentifyExecutor("Spash", "1.0.0.0", true) | Sets executor name, version, and visibility |

            ### Example

            ```csharp
            // paste this in Window_Loaded or Form_Load or whatever :3
            SpashAPIRonix.Custom.IdentifyExecutor("Spash", "1.0.0.0", true);
            SpashAPIRonix.Custom.UserAgent("Spash/1.0.0.0");
            SpashAPIRonix.API.RakNet(true);

            // paste this in the inject button :3
            private void Inject_Click(object sender, RoutedEventArgs e)
            {
                SpashAPIRonix.API.AttachAPI();
            }

            // paste this in the execute button :3
            private void Execute_Click(object sender, RoutedEventArgs e)
            {
                if (!SpashAPIRonix.API.IsAttached()) return;
                SpashAPIRonix.API.ExecuteScript(ScriptBox.Text, "");
            }
            ```
            """;

    private static final String XENO = """
            ## SpashAPIXeno

            .NET wrapper for the Xeno Executor.
            Get it on the Discord: [dsc.gg/spashapi](https://dsc.gg/spashapi)

            ### API

            | Method | Description |
            |--------|-------------|
            | SpashAPIXeno.API.IsAttached() | Returns whether the API is attached to a Roblox instance |
            | SpashAPIXeno.API.IsRobloxOpen() | Returns whether a Roblox process is running |
            | SpashAPIXeno.API.KillRoblox("pid") | Terminates the Roblox process with the given PID |
            | SpashAPIXeno.API.KillInstance() | Terminates the currently attached Roblox instance |
            | SpashAPIXeno.API.Attach() | Attaches to Roblox |
            | SpashAPIXeno.API.AttachAPI() | Attaches to the active Roblox instance |
            | SpashAPIXeno.API.ExecuteScript(Luau.Text) | Executes a Luau script in the attached instance |
            | SpashAPIXeno.API.AutoAttach(true/false) | Enables or disables automatic attachment on Roblox launch |

            ### Example

            ```csharp
            // paste this in Window_Loaded or Form_Load or whatever :3
            SpashAPIXeno.API.AutoAttach(false);

            // paste this in the inject button :3
            private void Inject_Click(object sender, RoutedEventArgs e)
            {
                SpashAPIXeno.API.AttachAPI();
            }

            // paste this in the execute button :3
            private void Execute_Click(object sender, RoutedEventArgs e)
            {
                if (!SpashAPIXeno.API.IsAttached()) return;
                SpashAPIXeno.API.ExecuteScript(ScriptBox.Text);
            }
            ```
            """;
}
