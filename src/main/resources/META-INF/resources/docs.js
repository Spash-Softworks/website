(function () {
    'use strict';

    function monacoize() {
        if (!window.monaco || !window.monaco.editor) return;
        var nodes = document.querySelectorAll(
            '.docs-md pre:not([data-m]) code, .api-docs pre:not([data-m]) code'
        );
        if (!nodes.length) return;
        nodes.forEach(function (code) {
            var pre = code.parentElement;
            if (!pre || pre.hasAttribute('data-m')) return;
            // Setting data-m triggers CSS display:none immediately — no flash of ugly pre
            pre.setAttribute('data-m', '1');

            var text = code.textContent.trimEnd();
            var lines = text.split('\n').length;
            var height = Math.min(Math.max(lines * 20 + 24, 60), 520);

            var wrap = document.createElement('div');
            wrap.className = 'docs-monaco-block';
            wrap.style.height = height + 'px';
            pre.parentNode.insertBefore(wrap, pre);
            pre.remove();

            var lang = (Array.from(code.classList)
                .find(function (c) { return c.startsWith('language-'); }) || '')
                .replace('language-', '') || 'plaintext';

            var mq = window.matchMedia('(prefers-color-scheme: dark)');
            var ed = monaco.editor.create(wrap, {
                value: text,
                language: lang,
                theme: mq.matches ? 'vs-dark' : 'vs',
                readOnly: true,
                automaticLayout: true,
                minimap: { enabled: false },
                fontSize: 13,
                scrollBeyondLastLine: false,
                lineNumbers: 'on',
                renderLineHighlight: 'none',
                overviewRulerLanes: 0,
                scrollbar: { alwaysConsumeMouseWheel: false },
                padding: { top: 10, bottom: 10 },
                fontFamily: "'SF Mono', ui-monospace, 'Cascadia Code', Consolas, monospace"
            });
            mq.addEventListener('change', function () {
                ed.updateOptions({ theme: mq.matches ? 'vs-dark' : 'vs' });
            });
        });
    }

    function buildToc() {
        setTimeout(function () {
            var toc = document.getElementById('docs-toc');
            if (!toc) return;
            var all = Array.from(document.querySelectorAll('.docs-md, .api-docs'));
            var active = all.find(function (el) {
                return getComputedStyle(el).display !== 'none';
            }) || all[0];
            if (!active) { toc.innerHTML = ''; return; }
            var hs = Array.from(active.querySelectorAll('h2, h3'));
            if (!hs.length) { toc.innerHTML = ''; return; }
            var html = '<p class="toc-heading">On this page</p><ul class="toc-list">';
            hs.forEach(function (h, i) {
                var id = 'sec-' + i;
                h.id = id;
                var cls = h.tagName === 'H3' ? ' toc-h3' : '';
                html += '<li class="toc-item' + cls + '"><a href="#' + id + '"' +
                    ' onclick="event.preventDefault();document.getElementById(\'' + id + '\')' +
                    '.scrollIntoView({behavior:\'smooth\'});return false;">' +
                    h.textContent + '</a></li>';
            });
            toc.innerHTML = html + '</ul>';
        }, 120);
    }

    function update() {
        monacoize();
        buildToc();
    }

    function boot() {
        window.require.config({
            paths: { vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs' }
        });
        window.require(['vs/editor/editor.main'], update);
    }

    window.__docsUpdate = update;

    window.__docsInit = function () {
        if (window.__docsBooted) {
            update();
            return;
        }
        window.__docsBooted = true;
        if (window.monaco && window.monaco.editor) {
            update();
        } else if (window.require && window.require.config) {
            boot();
        } else {
            var s = document.createElement('script');
            s.src = 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs/loader.js';
            s.onload = boot;
            document.head.appendChild(s);
        }
    };

    // Handle case where this script loaded after onAttach's executeJs already ran
    if (window.__docsInitPending) {
        delete window.__docsInitPending;
        window.__docsInit();
    }
})();
