package io.ticofab.cm_android_sdk.library.models.inputs;


import io.ticofab.cm_android_sdk.library.consts.GesturePurpose;

public class GesturePurposeInfo {
    public GesturePurpose mGesturePurpose;
    public String mGroupId;
    public int mMyIdInGroup;

    public GesturePurposeInfo(final GesturePurpose purpose) {
        mGesturePurpose = purpose;
    }
}
