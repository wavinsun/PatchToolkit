package cn.mutils.app.patch;

import android.content.Intent;
import android.os.ResultReceiver;

/**
 * Created by wenhua.ywh on 2016/12/21.
 */
public class SoHotfixExtra {

    private static final String EXTRA_FILE_PATH = "file";
    private static final String EXTRA_URL = "url";
    private static final String EXTRA_SIGN = "sign";
    private static final String EXTRA_VERSION = "version";
    private static final String EXTRA_RECEIVER = "receiver";

    private String mFilePath;
    private String mUrl;
    private String mSign;
    private int mVersion;
    private ResultReceiver mReceiver;

    public SoHotfixExtra() {

    }

    public SoHotfixExtra(Intent intent) {
        mFilePath = intent.getStringExtra(EXTRA_FILE_PATH);
        mUrl = intent.getStringExtra(EXTRA_URL);
        mSign = intent.getStringExtra(EXTRA_SIGN);
        mVersion = intent.getIntExtra(EXTRA_VERSION, 0);
        mReceiver = intent.getParcelableExtra(EXTRA_RECEIVER);
    }

    public void putTo(Intent intent) {
        intent.putExtra(EXTRA_FILE_PATH, mFilePath);
        intent.putExtra(EXTRA_URL, mUrl);
        intent.putExtra(EXTRA_SIGN, mSign);
        intent.putExtra(EXTRA_VERSION, mVersion);
        intent.putExtra(EXTRA_RECEIVER, mReceiver);
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        this.mFilePath = filePath;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getSign() {
        return mSign;
    }

    public void setSign(String mSign) {
        this.mSign = mSign;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int mVersion) {
        this.mVersion = mVersion;
    }

    public ResultReceiver getReceiver() {
        return mReceiver;
    }

    public void setReceiver(ResultReceiver mReceiver) {
        this.mReceiver = mReceiver;
    }
}
