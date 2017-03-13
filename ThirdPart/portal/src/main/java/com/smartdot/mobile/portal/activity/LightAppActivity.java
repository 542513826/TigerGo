package com.smartdot.mobile.portal.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.utils.CommonUtil;


public class LightAppActivity extends BaseActivity implements OnClickListener {
	private TextView tvTitle; // 应用筛选显示标题
	private TextView tvBack; // “返回”操作
	private TextView tvClose; // “关闭”操作
	private TextView refresh_tv; // “刷新”操作
	private ProgressBar progressBar;// webapp_progress_bar
	private WebView webView;
	private String title;
	private boolean firstLoad = true;
	String myUrl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myapp_webapp);

		tvTitle = (TextView) findViewById(R.id.webapp_tv_title);
		webView = (WebView) findViewById(R.id.webapp_webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.requestFocus();

		// 清理cache 和历史记录的方法
		// webView.clearCache(true);
		// webView.clearHistory();

		tvBack = (TextView) findViewById(R.id.webapp_tv_back);
		tvBack.setOnClickListener(this);

		refresh_tv = (TextView) findViewById(R.id.refresh_tv);
		refresh_tv.setOnClickListener(this);

		tvClose = (TextView) findViewById(R.id.webapp_tv_close);
		tvClose.setOnClickListener(this);

		progressBar = (ProgressBar) findViewById(R.id.webapp_progress_bar);

		Intent intent = getIntent();
		title = intent.getStringExtra("title");
		tvTitle.setText(title);
	    myUrl = intent.getStringExtra("url");
		if (!myUrl.startsWith("http://")) {
			myUrl = "http://" + myUrl;
		}
		webView.loadUrl(myUrl);
		initWebview();
	}

	private void initWebview() {
		webView.setWebChromeClient(new WebChromeClient() {
			// 网页加载进度更新
			@Override
			public void onProgressChanged(WebView view, int progress) {
				if (firstLoad) {
					progressBar.setVisibility(progressBar.VISIBLE);
					progressBar.setProgress(progress);
					if (progress == 100) {
						progressBar.setVisibility(progressBar.GONE);
					}

				}
				super.onProgressChanged(view, progress);
			}

		});

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// 设置点击网页里面的链接还是在当前的webview里跳转，而不是跳到浏览器里边。
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				webView.getSettings().setBlockNetworkImage(true);

				super.onPageStarted(view, url, favicon);
			}

			// 网页加载结束
			@Override
			public void onPageFinished(WebView view, String url) {
				if (firstLoad) {
					firstLoad = false;
					progressBar.setVisibility(progressBar.GONE);
				}
				webView.getSettings().setBlockNetworkImage(false);

				super.onPageFinished(view, url);
			}

			// @Override
			// public void onReceivedSslError(WebView view,
			// SslErrorHandler handler, android.net.http.SslError error) {
			// //设置webview处理https请求
			// handler.proceed();
			// }

			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				// 加载页面报错时的处理
				CommonUtil.showToast(LightAppActivity.this, "加载页面出错！" + description);
				// Toast.makeText(MainActivity.this,
				// "Oh no! " + description, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	// public boolean onKeyDown(int keyCoder, KeyEvent event) {
	//
	// if (webView.canGoBack() && keyCoder == KeyEvent.KEYCODE_BACK) {
	// webView.goBack(); // 返回webView的上一页面
	// return true;
	// }
	// return false;
	// }

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == R.id.webapp_tv_back){
			if (webView.canGoBack()) {
				webView.goBack();
			} else {
				finish();
			}
		}else if(arg0.getId() == R.id.webapp_tv_close){
			finish();
		}else if(arg0.getId() == R.id.refresh_tv){
			Intent intent= new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_url = Uri.parse(myUrl);
			intent.setData(content_url);
			startActivity(intent);
		}
	}
}

/*
 * 
 * 如何创建WebView:
 * 
 * 1、权限：AndroidManifest.xml中必须使用权限："android.permission.INTERNET",否则会出Web page
 * not available错误。
 * 
 * 2、创建WebView实例：WebView webView = new WebView(this);
 * 
 * 3、WebView基本设置： webview.getSettings().setJavaScriptEnabled(true);
 * //设置支持Javascript webView.getSettings().setBuiltInZoomControls(true); //页面缩放按钮
 * webView.requestFocus(); //触摸焦点起作用.如果不设置，则在点击网页文本输入框时，不能弹出软键盘及不响应其他的一些事件。
 * webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); //取消滚动条
 * 
 * 4、设置WevView要显示的网页： 互联网用：webView.loadUrl("http://www.google.com");
 * 本地文件用：webView.loadUrl("file:///android_asset/XX.html"); 本地文件存放在：assets文件中
 * 
 * 5、如果希望点击链接由自己处理，而不是新开Android的系统browser中响应该链接。 给WebView一个事件监听对象（WebViewClient)
 * 并重写其中的一些方法 shouldOverrideUrlLoading：对网页中超链接按钮的响应。
 * 当按下某个连接时WebViewClient会调用这个方法，并传递参数：按下的url onLoadResource onPageStart
 * onPageFinish onReceiveError onReceivedHttpAuthRequest
 * 
 * 6、如果用webview点链接跳转多页后，如果不做任何处理，点击系统“Back”键，整个浏览器会调用finish()而结束自身，
 * 如果希望浏览的网页回退而不是退出浏览器，需要在当前Activity中处理并消费掉该Back事件。 覆盖Activity类的onKeyDown(int
 * keyCoder,KeyEvent event)方法。 public boolean onKeyDown(int keyCoder,KeyEvent
 * event) { if ( webView.canGoBack() && keyCoder == KeyEvent.KEYCODE_BACK ) {
 * webView.goBack(); //返回webView的上一页面 return true; } return false; }
 */
