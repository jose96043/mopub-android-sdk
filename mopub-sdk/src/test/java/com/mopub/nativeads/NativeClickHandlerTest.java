// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.nativeads;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mopub.common.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(SdkTestRunner.class)
public class NativeClickHandlerTest {

    private NativeClickHandler subject;
    private Activity context;

    private TextView titleView;
    private RelativeLayout relativeLayout;
    private RelativeLayout relativeLayout2;

    @Mock private View mockView;
    @Mock private ClickInterface mockClickInterface;
    @Mock private SpinningProgressView mockSpinningProgressView;

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).create().get();
        subject = new NativeClickHandler(context);

        titleView = new TextView(context);
        titleView.setId(View.generateViewId());

        relativeLayout = new RelativeLayout(context);
        relativeLayout.setId(View.generateViewId());
        relativeLayout.addView(titleView);

        relativeLayout2 = new RelativeLayout(context);
        relativeLayout2.setId(View.generateViewId());
        relativeLayout2.addView(relativeLayout);
    }

    @Test
    public void setOnClickListener_shouldSetClickListenerOnViewHierarchy() {
        subject.setOnClickListener(relativeLayout2, mockClickInterface);

        titleView.callOnClick();
        relativeLayout.callOnClick();
        relativeLayout2.callOnClick();

        verify(mockClickInterface).handleClick(titleView);
        verify(mockClickInterface).handleClick(relativeLayout);
        verify(mockClickInterface).handleClick(relativeLayout2);
    }

    @Test
    public void clearOnClickListener_shouldClearClickListenerFromViewHierarchy() throws Exception {
        subject.setOnClickListener(relativeLayout2, mockClickInterface);
        subject.clearOnClickListener(relativeLayout2);

        assertThat(titleView.hasOnClickListeners()).isFalse();
        assertThat(relativeLayout.hasOnClickListeners()).isFalse();
        assertThat(relativeLayout2.hasOnClickListeners()).isFalse();
    }

    @Test
    public void handleClick_shouldShowSpinner_shouldRemoveSpinner_WhenSucceeded() {
        Robolectric.getBackgroundThreadScheduler().pause();

        subject.openClickDestinationUrl("https://www.mopub.com", mockView, mockSpinningProgressView);

        verify(mockSpinningProgressView).addToRoot(mockView);

        Robolectric.getBackgroundThreadScheduler().unPause();
        verify(mockSpinningProgressView).removeFromRoot();
    }

    @Test
    public void handleClick_shouldShowSpinner_shouldRemoveSpinner_WhenFailed() {
        Robolectric.getBackgroundThreadScheduler().pause();

        subject.openClickDestinationUrl("", mockView, mockSpinningProgressView);

        verify(mockSpinningProgressView).addToRoot(mockView);
        Robolectric.getBackgroundThreadScheduler().unPause();
        verify(mockSpinningProgressView).removeFromRoot();
    }

    @Test
    public void handleClick_shouldShowSpinnerOnceWhileClickIsResolving() {
        Robolectric.getBackgroundThreadScheduler().pause();

        subject.openClickDestinationUrl("https://www.mopub.com", mockView, mockSpinningProgressView);
        subject.openClickDestinationUrl("https://www.mopub.com", mockView, mockSpinningProgressView);

        // only is called once
        verify(mockSpinningProgressView).addToRoot(mockView);

        Robolectric.getBackgroundThreadScheduler().unPause();
        verify(mockSpinningProgressView).removeFromRoot();
    }

    @Test
    public void handleClick_withNullClickDestinationUrl_shouldNotThrowNPE_shouldDoNothing() throws Exception {
        subject.openClickDestinationUrl(null, mockView, mockSpinningProgressView);

        verifyNoMoreInteractions(mockSpinningProgressView);
        assertThat(ShadowApplication.getInstance().peekNextStartedActivity()).isNull();
    }

    @Test
    public void handleClick_withNullView_shouldNotShowSpinner() {
        Robolectric.getBackgroundThreadScheduler().pause();

        subject.openClickDestinationUrl("https://www.mopub.com", null, mockSpinningProgressView);

        verify(mockSpinningProgressView, never()).addToRoot(mockView);
        Robolectric.getBackgroundThreadScheduler().unPause();
        verify(mockSpinningProgressView, never()).removeFromRoot();
    }
}
