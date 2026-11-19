package br.dic.estudo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

// Se houver texto na busca, o botão "voltar" limpa a pesquisa; senão, fecha o app.
private const val CLEAR_SEARCH_JS =
    "(function(){var q=document.getElementById('q');" +
        "if(q&&q.value){q.value='';q.dispatchEvent(new Event('input'));" +
        "window.scrollTo(0,0);return 'cleared';}return 'exit';})()"

class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView

    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        web = WebView(this)
        setContentView(web)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowContentAccess = false
            builtInZoomControls = false
            displayZoomControls = false
        }

        // O site já tem tema claro/escuro próprio: não deixar o WebView escurecer por conta própria.
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(web.settings, false)
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(web.settings, WebSettingsCompat.FORCE_DARK_AUTO)
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(
                web.settings,
                WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING
            )
        }

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return false
                if (url.scheme == "file") return false
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, url))
                } catch (_: Exception) {
                }
                return true
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                web.evaluateJavascript(CLEAR_SEARCH_JS) { result ->
                    if (result != "\"cleared\"") finish()
                }
            }
        })

        web.loadUrl("file:///android_asset/index.html")
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }
}
