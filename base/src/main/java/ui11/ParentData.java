package ui11;

import org.jspecify.annotations.NonNull;

/**
 * An object that can be accessed by some of its ancestor widgets.
 * A {@code ParentData} can be supplied using the {@link #attach(ParentData, Widget)} method,
 * then retreived by:
 * <ul>
 *     <li>Specifying the type of the object (or one if its subtypes) to
 *     {@link PeerRequestor#withInterestedParentDataType(Class[])}
 *     <li>Getting the supplied parent data objects by {@link PeerRequestor.Result#parentDataList()}</li>
 * </ul>
 */
public interface ParentData {

    /**
     * Returns a widget that will supply the specified ParentData objects to the
     * {@linkplain PeerRequestor.Request peer request} in which it is contained.
     */
    static @NonNull Widget attach(@NonNull ParentData parentData, @NonNull Widget widget) {
        return new ParentDataWidget(parentData, widget);
    }
}
