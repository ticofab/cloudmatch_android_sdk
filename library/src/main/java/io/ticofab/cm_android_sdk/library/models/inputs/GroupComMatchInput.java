package io.ticofab.cm_android_sdk.library.models.inputs;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.consts.Criteria;
import io.ticofab.cm_android_sdk.library.consts.JsonLabels;
import io.ticofab.cm_android_sdk.library.models.inputs.base.MatchInput;

public class GroupComMatchInput extends MatchInput {

    public GroupComMatchInput(final Criteria criteria) {
        super(criteria);
        // TODO Auto-generated constructor stub
    }

    public String mIdInGroup;
    public String mGroupId;

    public String toJsonStr() throws JSONException {
        final JSONObject json = super.toJson();

        if (!TextUtils.isEmpty(mGroupId)) {
            json.put(JsonLabels.GROUP_ID, mGroupId);
        }
        if (!TextUtils.isEmpty(mIdInGroup)) {
            json.put(JsonLabels.MY_ID_IN_GROUP, mGroupId);
        }

        return json.toString();
    }

}
