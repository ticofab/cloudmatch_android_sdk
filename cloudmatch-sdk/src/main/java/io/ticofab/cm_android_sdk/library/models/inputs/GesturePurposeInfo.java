package io.ticofab.cm_android_sdk.library.models.inputs;


import io.ticofab.cm_android_sdk.library.consts.GesturePurpose;

public class GesturePurposeInfo {
    public final String mGroupId;
    public final int mMyIdInGroup;
    public final GesturePurpose mGesturePurpose;

    private GesturePurposeInfo(Builder builder) {
        mGroupId = builder.mGroupId;
        mMyIdInGroup = builder.mMyIdInGroup;
        mGesturePurpose = builder.mGesturePurpose;
    }

    public static class Builder {
        private String mGroupId;
        private int mMyIdInGroup;
        private GesturePurpose mGesturePurpose = GesturePurpose.group_creation;

        public Builder setGroupId(String groupId) {
            mGroupId = groupId;
            return this;
        }

        public Builder setGesturePurpose(GesturePurpose gesturePurpose) {
            mGesturePurpose = gesturePurpose;
            return this;
        }

        public Builder setMyIdInGroup(int myIdInGroup) {
            mMyIdInGroup = myIdInGroup;
            return this;
        }

        public GesturePurposeInfo build() {
            return new GesturePurposeInfo(this);
        }
    }
}
