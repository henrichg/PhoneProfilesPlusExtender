package sk.henrichg.phoneprofilesplusextender;

import android.graphics.Rect;
import android.view.View;

/**
 * Created by chrismcmeeking on 2/27/17.
 */

@SuppressWarnings({"unused"})
public class A11yNodeInfoMatcher {

    private String mContentDescription;
    private String mText;
    private Class<? extends View> mClass;

    private Rect mContainedIn = null;
    private Rect mPositionEqual = null;

    private String mViewIdResourceName = "";

    public A11yNodeInfoMatcher() {}

    public A11yNodeInfoMatcher setContentDescription(final String contentDescription) {
        mContentDescription = contentDescription;
        return this;
    }

    public A11yNodeInfoMatcher setClass(final Class<? extends View> clazz) {
        mClass = clazz;
        return this;
    }

    public A11yNodeInfoMatcher setPositionContainedIn(final Rect rect) {
        mContainedIn = rect;
        return this;
    }

    public A11yNodeInfoMatcher setPositionEqualTo(final Rect rect) {
        mPositionEqual = rect;
        return this;
    }

    public A11yNodeInfoMatcher setText(final String text) {
        mText = text;
        return this;
    }

    public A11yNodeInfoMatcher setViewIdResourceName(final String viewIdResourceName) {
        mViewIdResourceName = viewIdResourceName;
        return this;
    }

    public boolean match(A11yNodeInfo nodeInfo) {

        Rect position = nodeInfo.getBoundsInScreen();

        if (mContainedIn != null) {
            if (position.top < mContainedIn.top) return false;
            if (position.left < mContainedIn.left) return false;
            if (position.right > mContainedIn.right) return false;
            if (position.bottom > mContainedIn.bottom) return false;
        }

        if (mPositionEqual != null) {
            if (position.top != mPositionEqual.top) return false;
            if (position.bottom != mPositionEqual.bottom) return false;
            if (position.left != mPositionEqual.left) return false;
            if (position.right != mPositionEqual.right) return false;
        }

        if (mContentDescription != null &&
                (nodeInfo.getContentDescription() == null
                        || !mContentDescription.contentEquals(nodeInfo.getContentDescription()))) return false;

        if (mText != null && (nodeInfo.getText() == null || !mText.contentEquals(nodeInfo.getText()))) return false;

        if (mClass != null && !mClass.getName().contentEquals(nodeInfo.getClassName())) return false;

        if (!nodeInfo.getViewIdResourceName().contains(mViewIdResourceName)) return false;

        return true;
    }
}