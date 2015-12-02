package com.orcller.app.orcller.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.orcller.app.orcller.BuildConfig;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 12/3/15.
 */
public class AWSManager {
    public static final String S3_BUCKET_NAME = "s3orcller";
    private static AmazonS3Client s3Client;
    private static TransferUtility transferUtility;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static AmazonS3Client getS3Client() {
        if(s3Client == null) {
            synchronized(AmazonS3Client.class) {
                if(s3Client == null) {
                    try {
                        Context context = Application.applicationContext();
                        ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                        Bundle bundle = ai.metaData;
                        String awsAccessKey = bundle.getString("AWSAccessKey");
                        String awsSecretKey = bundle.getString("AWSSecretKey");
                        s3Client = new AmazonS3Client(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
                    } catch (PackageManager.NameNotFoundException e) {
                        if (BuildConfig.DEBUG)
                            Log.d(e.getMessage());
                    }
                }
            }
        }
        return s3Client;
    }

    public static TransferUtility getTransferUtility() {
        if(transferUtility == null) {
            synchronized(TransferUtility.class) {
                if(transferUtility == null) {
                    transferUtility = new TransferUtility(getS3Client(), Application.applicationContext());
                }
            }
        }
        return transferUtility;
    }
}
