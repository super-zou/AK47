package com.mufu.update;

import java.io.Serializable;

/**
 * 自定义版本检查的结果
 */
public class UpdateCheckResult implements Serializable {

    public boolean hasUpdate = false;

    public boolean isIgnorable = false;

    public int versionCode;

    public String versionName;

    public String updateLog;

    public String apkUrl;

    public long apkSize;
}
