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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Map;

@PageTitle("Documentation")
@Route(value = "docs", layout = Layout.class)
public class Docs extends VerticalLayout implements BeforeEnterObserver {

    private static final Map<String, Integer> TAB_INDEX = Map.of(
            "velocity", 1,
            "madium",   2,
            "ronix",    3,
            "xeno",     4,
            "pluto",    5,
            "solara",   6
    );

    private final TabSheet tabs = new TabSheet();

    public Docs() {
        addClassName("docs-view");
        setPadding(false);
        setSpacing(false);

        H1 title = new H1("Documentation");
        title.addClassName("page-title");
        Paragraph subtitle = new Paragraph(
                ".NET WPF and WinForms API reference for SpashAPI integrations.");
        subtitle.addClassName("page-subtitle");

        Div header = new Div(title, subtitle);
        header.addClassName("docs-header");

        tabs.addClassName("docs-tabs");
        tabs.setWidthFull();
        tabs.add("Offsets API", buildApiDocs());
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

        tabs.addSelectedChangeListener(ev -> getElement().executeJs(
                "window.__docsUpdate && window.__docsUpdate();"));

        add(header, body);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getLocation().getQueryParameters()
                .getParameters()
                .getOrDefault("tab", java.util.List.of())
                .stream().findFirst()
                .map(String::toLowerCase)
                .map(TAB_INDEX::get)
                .ifPresent(tabs::setSelectedIndex);
    }

    @Override
    protected void onAttach(AttachEvent e) {
        super.onAttach(e);
        e.getUI().getPage().addJavaScript("docs.js");
        getElement().executeJs(
                "if (window.__docsInit) window.__docsInit(); else window.__docsInitPending = true;");
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

    private static Div buildApiDocs() {
        Div root = new Div();
        root.addClassNames("api-docs", "docs-md");
        root.getElement().setProperty("innerHTML", """
            <h2>Offsets REST API</h2>
            <p style="margin-bottom:24px">Fetch Roblox offsets over HTTP. No auth required, cached server-side. Base URL: <code>/api</code></p>

            <div class="api-ep">
              <div class="ep-hdr">
                <span class="ep-badge ep-get">GET</span>
                <span class="ep-path">/api/offsets/versions</span>
              </div>
              <p class="ep-desc">Returns available version hashes for a provider.</p>
              <div class="ep-label">Parameters</div>
              <table>
                <thead><tr><th>Name</th><th>Default</th><th>Description</th></tr></thead>
                <tbody>
                  <tr><td>provider</td><td>soda</td><td>soda or theo</td></tr>
                </tbody>
              </table>
              <div class="ep-label"><span class="ep-status">200</span> Response</div>
              <pre><code class="language-json">["ad5d3e2906444472", "a182ba0d4c6f483b", "460909c4fe904aae"]</code></pre>
            </div>

            <div class="api-ep">
              <div class="ep-hdr">
                <span class="ep-badge ep-get">GET</span>
                <span class="ep-path">/api/offsets/categories</span>
              </div>
              <p class="ep-desc">Returns the available categories for a provider. Use the label values as the category param on the main endpoint.</p>
              <div class="ep-label">Parameters</div>
              <table>
                <thead><tr><th>Name</th><th>Default</th><th>Description</th></tr></thead>
                <tbody>
                  <tr><td>provider</td><td>soda</td><td>soda or theo</td></tr>
                </tbody>
              </table>
              <div class="ep-label"><span class="ep-status">200</span> Soda</div>
              <pre><code class="language-json">[
  { "label": "Classes",  "file": "offsets.hpp" },
  { "label": "Internal", "file": "int.hpp"     },
  { "label": "CFG",      "file": "CFG.hpp"     },
  { "label": "FFlags",   "file": "fflags.hpp"  }
]</code></pre>
              <div class="ep-label"><span class="ep-status">200</span> Theo</div>
              <pre><code class="language-json">[
  { "label": "External", "file": "offsets.hpp" },
  { "label": "Structs",  "file": "struct.hpp"  },
  { "label": "FFlags",   "file": "fflags.hpp"  }
]</code></pre>
            </div>

            <div class="api-ep">
              <div class="ep-hdr">
                <span class="ep-badge ep-get">GET</span>
                <span class="ep-path">/api/offsets</span>
              </div>
              <p class="ep-desc">Main endpoint. Returns JSON by default, or formatted source code when format is set.</p>
              <div class="ep-label">Parameters</div>
              <table>
                <thead><tr><th>Name</th><th>Default</th><th>Description</th></tr></thead>
                <tbody>
                  <tr><td>provider</td><td>soda</td><td>soda or theo</td></tr>
                  <tr><td>version</td><td>latest</td><td>version hash</td></tr>
                  <tr><td>category</td><td>all</td><td>category label or filename, omit for all</td></tr>
                  <tr><td>format</td><td></td><td>file extension for source output: cs, py, rs, hpp, go, lua, js, ts, kt, swift, asm... omit for JSON</td></tr>
                </tbody>
              </table>
              <div class="ep-label"><span class="ep-status">200</span> All categories</div>
              <pre><code class="language-json">{
  "version": "ad5d3e2906444472",
  "current": true,
  "categories": {
    "Classes":  [{ "namespace": "Player", "name": "Health", "value": "0x168" }],
    "Internal": [],
    "CFG":      [],
    "FFlags":   []
  }
}</code></pre>
              <div class="ep-label"><span class="ep-status">200</span> category=Classes</div>
              <pre><code class="language-json">[
  { "namespace": "Player",     "name": "Health", "value": "0x168" },
  { "namespace": "RenderView", "name": "Fov",    "value": "0x2B4" }
]</code></pre>
              <div class="ep-label"><span class="ep-status">200</span> category=Classes &amp; format=cs</div>
              <pre><code class="language-csharp">/*             ╱|、
 *            (˚ˎ 。 7     kamerz is gay (version-ad5d3e2906444472)
 *             |、˜ふ      time 04:20, 19 Jun 2026, *meow*!
 *             じしˍ, )ノ   dsc.gg/spashapi
 */

public static class Offsets
{
    public static class Player
    {
        public const ulong Health = 0x168;
    }
}</code></pre>
            </div>

            <div class="api-ep">
              <div class="ep-hdr">
                <span class="ep-badge ep-get">GET</span>
                <span class="ep-path">/api/offsets/raw</span>
              </div>
              <p class="ep-desc">Returns the original .hpp dump as plain text. category is required.</p>
              <div class="ep-label">Parameters</div>
              <table>
                <thead><tr><th>Name</th><th>Default</th><th>Description</th></tr></thead>
                <tbody>
                  <tr><td>provider</td><td>soda</td><td>soda or theo</td></tr>
                  <tr><td>version</td><td>latest</td><td>version hash</td></tr>
                  <tr><td>category</td><td></td><td>required, accepts label or filename</td></tr>
                </tbody>
              </table>
            </div>
            """);
        return root;
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
