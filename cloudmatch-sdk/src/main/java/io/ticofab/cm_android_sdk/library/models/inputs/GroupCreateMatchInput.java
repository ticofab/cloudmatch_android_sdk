package io.ticofab.cm_android_sdk.library.models.inputs;

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.consts.Criteria;
import io.ticofab.cm_android_sdk.library.models.inputs.base.MatchInput;

public class GroupCreateMatchInput extends MatchInput {
    private static final String MATCH_INPUT_LATITUDE = "latitude";
    private static final String MATCH_INPUT_LONGITUDE = "longitude";

    public GroupCreateMatchInput(final Criteria criteria) {
        super(criteria);
        // TODO Auto-generated constructor stub
    }

    public Double mLatitude;
    public Double mLongitude;

    public String toJsonStr() throws JSONException {
        final JSONObject json = super.toJson();

        if (mLatitude != null) {
            json.put(MATCH_INPUT_LATITUDE, mLatitude);
        }
        if (mLongitude != null) {
            json.put(MATCH_INPUT_LONGITUDE, mLongitude);
        }

        return json.toString();
    }
}
