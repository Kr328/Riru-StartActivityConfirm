package android.app;

import android.content.Intent;

public class ResultInfo {
    public final String mResultWho;
    public final int mRequestCode;
    public final int mResultCode;
    public final Intent mData;

    public ResultInfo(String resultWho, int requestCode, int resultCode,
                      Intent data) {
        mResultWho = resultWho;
        mRequestCode = requestCode;
        mResultCode = resultCode;
        mData = data;
    }
}