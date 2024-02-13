package com.app.plutope.browser.custom;

import android.webkit.WebBackForwardList;

public interface AddressBarListener {
    boolean onLoad(String urlText);

    void onClear();

    WebBackForwardList loadNext();

    WebBackForwardList loadPrevious();

    WebBackForwardList onHomePagePressed();
}
