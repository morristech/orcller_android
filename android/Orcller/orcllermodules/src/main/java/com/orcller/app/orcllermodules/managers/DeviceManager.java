package com.orcller.app.orcllermodules.managers;

/**
 * Created by pisces on 11/10/15.
 */
public class DeviceManager {
    public static final String SYSTEM_NAME = "Android OS";
    private static DeviceManager uniqueInstance;
    private String deviceToken;

    // ================================================================================================
    //  Public
    // ================================================================================================

    /**
     * @constructor
     **/
    public DeviceManager() {
    }

    public static DeviceManager getDefault() {
        if(uniqueInstance == null) {
            synchronized(DeviceManager.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new DeviceManager();
                }
            }
        }
        return uniqueInstance;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void registerDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;

        if (deviceToken != null)
            AuthenticationCenter.getDefault().updateDevice();
    }
}
