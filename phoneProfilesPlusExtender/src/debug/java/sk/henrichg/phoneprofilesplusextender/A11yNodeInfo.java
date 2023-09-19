package sk.henrichg.phoneprofilesplusextender;

import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chrismcmeeking on 2/25/17.
 */

@SuppressWarnings("unused")
public class A11yNodeInfo implements Iterable<A11yNodeInfo>, Comparator<A11yNodeInfo> {

    public static A11yNodeInfo wrap(AccessibilityNodeInfo node) {
        if (node == null) return null;

        return new A11yNodeInfo(node);
    }

    public static A11yNodeInfo wrap(AccessibilityNodeInfoCompat node) {
        if (node == null) return null;
        return new A11yNodeInfo(node);
    }

    private static final ArrayList<Class<? extends View>> ACTIVE_CLASSES;

    static {
        ACTIVE_CLASSES = new ArrayList<>();
        ACTIVE_CLASSES.add(Button.class);
        ACTIVE_CLASSES.add(Switch.class);
        ACTIVE_CLASSES.add(CheckBox.class);
        ACTIVE_CLASSES.add(EditText.class);
    }

    public enum Actions {

        ACCESSIBILITY_FOCUS(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS),
        CLEAR_ACCESSIBILITY_FOCUS(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS),
        CLEAR_FOCUS(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS),
        CLEAR_SELECTION(AccessibilityNodeInfo.ACTION_CLEAR_SELECTION),
        CLICK(AccessibilityNodeInfo.ACTION_CLICK),
        COLLAPSE(AccessibilityNodeInfo.ACTION_COLLAPSE),
        COPY(AccessibilityNodeInfo.ACTION_COPY),
        CUT(AccessibilityNodeInfo.ACTION_CUT),
        LONG_CLICK(AccessibilityNodeInfo.ACTION_LONG_CLICK),
        PASTE(AccessibilityNodeInfo.ACTION_PASTE),
        PREVIOUS_AT_MOVEMENT_GRANULARITY(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY),
        PREVIOUS_HTML_ELEMENT(AccessibilityNodeInfo.ACTION_PREVIOUS_HTML_ELEMENT);

        private final int mAndroidValue;

        Actions(int androidValue) {
            mAndroidValue = androidValue;
        }

        int getAndroidValue() {
            return mAndroidValue;
        }
    }

    private final AccessibilityNodeInfoCompat mNodeInfo;

    //A special constructor for testing.
    protected A11yNodeInfo() {
        mNodeInfo = null;
    }

    protected A11yNodeInfo(AccessibilityNodeInfo nodeInfo) {
        //this(new AccessibilityNodeInfoCompat(nodeInfo));
        this(AccessibilityNodeInfoCompat.wrap(nodeInfo));
    }

    protected A11yNodeInfo(AccessibilityNodeInfoCompat nodeInfoCompat) {
        if (nodeInfoCompat == null) throw new RuntimeException("Wrapping a null node doesn't make sense");
        mNodeInfo = nodeInfoCompat;
    }

    @Override public int compare(A11yNodeInfo lhs, A11yNodeInfo rhs) {

        int result;

        result = lhs.getSpeakableText().compareTo(rhs.getSpeakableText());

        if (result != 0) return result;

        Rect lhsRect = lhs.getBoundsInScreen();
        Rect rhsRect = rhs.getBoundsInScreen();


        //noinspection ConstantConditions
        if (result != 0) return result;

        if (lhsRect.top < rhsRect.top) return -1;
        else if (lhsRect.top > rhsRect.top) return 1;

        if (lhsRect.left < rhsRect.left) return -1;
        else if (lhsRect.left > rhsRect.left) return 1;

        if (lhsRect.right < rhsRect.right) return -1;
        else if (lhsRect.right > rhsRect.right) return 1;

        if (lhsRect.bottom < rhsRect.bottom) return -1;
        else if (lhsRect.bottom > rhsRect.bottom) return 1;

        //noinspection ConstantConditions
        if (result != 0) return result;

        return 0;
    }

    public List<AccessibilityNodeInfoCompat.AccessibilityActionCompat> getActionList() {
        return mNodeInfo.getActionList();
    }

    public int getActions() {
        //noinspection deprecation
        return mNodeInfo.getActions();
    }

    /**
     * Callbacks for iterating over the A11yNodeInfo hierarchy.
     */
    public interface OnVisitListener {

        /**
         * Called for every node during hierarchy traversals.
         * @param nodeInfo The node that work will be done.
         * @return Return true to stop traversing, false to continue.
         */
        boolean onVisit(A11yNodeInfo nodeInfo);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean isActiveElement() {
        for (Class<? extends View> clazz : ACTIVE_CLASSES) {
            if (this.getClassName().equalsIgnoreCase(clazz.getName())) return true;
        }

        if (getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) return true;
        if (getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CONTEXT_CLICK)) return true;

        if (getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_LONG_CLICK)) return true;
        if (getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SELECT)) return true;

        final int actions = getActions();

        return (actions & AccessibilityNodeInfo.ACTION_CLICK) != 0 ||
                (actions & AccessibilityNodeInfo.ACTION_LONG_CLICK) != 0 ||
                (actions & AccessibilityNodeInfo.ACTION_SELECT) != 0;
    }

    public boolean performAction(Actions action) {
        return mNodeInfo.performAction(action.getAndroidValue());
    }

    public AccessibilityNodeInfoCompat getAccessibilityNodeInfoCompat() {
        return mNodeInfo;
    }

    public Rect getBoundsInScreen() {
        Rect result = new Rect();
        mNodeInfo.getBoundsInScreen(result);
        return result;
    }

    public A11yNodeInfo getChild(final int i) {

        if (i >= mNodeInfo.getChildCount()) throw new IndexOutOfBoundsException();

        return new A11yNodeInfo(mNodeInfo.getChild(i));
    }

    public int getChildCount() {
        return mNodeInfo.getChildCount();
    }

    public String getClassName() {
        return mNodeInfo.getClassName().toString();
    }

    public CharSequence getContentDescription() {
        return mNodeInfo.getContentDescription();
    }

    /**
     * I don't often use CharSequence's, and prefer strings.  Note: null strings will return as empty strings!
     * @return The content description as a NotNull String.
     */
    public String getContentDescriptionAsString() {
        if (mNodeInfo.getContentDescription() == null) return "";

        return mNodeInfo.getContentDescription().toString();
    }

    /**
     * Gets the depth of the child in the node info hierarchy.
     * @return The depth of the node.
     */
    public int getDepthInTree() {

        int result = 0;

        A11yNodeInfo parentNode = getParent();

        while (parentNode != null) {
            parentNode = parentNode.getParent();
            result++;
        }

        return result;
    }

    public A11yNodeInfo getLabeledBy() {
        return A11yNodeInfo.wrap(mNodeInfo.getLabeledBy());
    }

    public A11yNodeInfo getParent() {
        if (mNodeInfo.getParent() == null) return null;

        return new A11yNodeInfo(mNodeInfo.getParent());
    }

    /**
     * Attempts to calculate the string that will be read off by TalkBack for a given
     * accessibility node.  Eventually including role, trait, and value information.
     * If null, returns an empty string instead.
     *
     * @return The string representing the spoken text.
     */
    public String getSpeakableText() {
        if (getContentDescription() != null) return getContentDescriptionAsString();
        if (getText() != null) return getTextAsString();
        return "";
    }

    public CharSequence getText() {
        return mNodeInfo.getText();
    }

    /**
     * Don't like CharSequences, and random null string checks.  This will get the Text
     * as a NotNull String.
     * @return The text as a NotNull String.
     */
    public String getTextAsString() {
        if (getText() != null) return getText().toString();
        else return "";
    }

    public String getViewIdResourceName() {
        if (mNodeInfo.getViewIdResourceName() == null) return "";
        return mNodeInfo.getViewIdResourceName();
    }

    /**
     * Implementing the iterable interface to more easily navigate the node info children.
     * @return An iterator over the children of this A11yNodeInfo.
     */
    @SuppressWarnings("NullableProblems")
    @Override public Iterator<A11yNodeInfo> iterator() {
        return new Iterator<A11yNodeInfo>() {
            private int mNextIndex = 0;

            @Override
            public boolean hasNext() {
                //ChildCount isn't always accurate.  Nodes may get recycled depending on the vent.
                //So we check the child count AND that the child isn't null.
                return mNextIndex < getChildCount() && (mNodeInfo == null || mNodeInfo.getChild(mNextIndex) != null);
            }

            @Override
            public A11yNodeInfo next() {
                return getChild(mNextIndex++);
            }

            @Override
            public void remove() {

            }
        };
    }

    /**
     * Get the entire node hierarchy as a string.
     * @return The node hierarchy.
     */
    public String toViewHierarchy() {
        final StringBuilder result = new StringBuilder();

        result.append("--------------- Accessibility Node Hierarchy ---------------").append(StringConstants.CHAR_NEW_LINE);

        visitNodes(nodeInfo -> {

            int size = nodeInfo.getDepthInTree();
            for (int i = 0; i < size; i++) {
                result.append('-');
            }

            result.append(nodeInfo/*.toString()*/);
            result.append(StringConstants.CHAR_NEW_LINE);

            return false;
        });

        result.append("--------------- Accessibility Node Hierarchy ---------------");

        return result.toString();
    }

    /**
     * Get the first {@link A11yNodeInfo node} that matches the given {@link A11yNodeInfoMatcher matcher}
     * @param matcher The matcher with props to match.
     * @return The first node that matches.
     */
    public A11yNodeInfo getFirstNodeThatMatches(final A11yNodeInfoMatcher matcher) {
        return visitNodes(matcher::match);
    }

    public boolean isClassType(Class<?> clazz) {
        return (clazz.getName().equalsIgnoreCase(getClassName()));
    }

    public boolean isScrollable() {
        return mNodeInfo.isScrollable();
    }


    public boolean isVisibleToUser() {
        return mNodeInfo.isVisibleToUser();
    }

    public boolean isInVisibleScrollableField() {
        A11yNodeInfo tempNode = wrap(mNodeInfo);
        A11yNodeInfo scrollableView = null;

        while(tempNode.getParent() != null) {
            if(tempNode.isScrollable() && !tempNode.isClassType(ViewPager.class)) {
                scrollableView = tempNode;
            }
            tempNode = tempNode.getParent();
        }

        return scrollableView != null && scrollableView.isVisibleToUser();
    }


    @SuppressWarnings("NullableProblems")
    @Override public String toString() {
        if (mNodeInfo == null) throw new RuntimeException("This shouldn't be null");
        return mNodeInfo.toString();
    }

    /**
     * Loop over children in the node hierarchy, until one of them returns true.  Return the
     * first element where "onVisit" returns true.  This can be used to create a very
     * simple "find first" type of method.  Though most of the time, you likely want
     * to travel all, in which case, just return "false" from your onVisit method, and
     * you will visit every node.
     * @param onVisitListener {@link A11yNodeInfo.OnVisitListener#onVisit(A11yNodeInfo) onVisit}
     * will be added for every node, until {@link A11yNodeInfo.OnVisitListener#onVisit(A11yNodeInfo) onVisit}
     * returns true.
     * @return The first node for which {@link A11yNodeInfo.OnVisitListener#onVisit(A11yNodeInfo) onVisit}  returns true.
     */
    public A11yNodeInfo visitNodes(OnVisitListener onVisitListener) {

        if (onVisitListener.onVisit(this)) return this;

        for (A11yNodeInfo child : this) {
            A11yNodeInfo result = child.visitNodes(onVisitListener);
            if (result != null) return result;
        }

        return null;
    }
}