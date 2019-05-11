package com.sample.moviepicture;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class Fragment extends ArFragment {
    private static final String TAG = "AugmentedImageFragment";


    // Do a runtime check for the OpenGL level available at runtime to avoid Sceneform crashing the
    // application.
    private static final double MIN_OPENGL_VERSION = 3.0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Check for Sceneform being supported on this device.  This check will be integrated into
        // Sceneform eventually.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
        }
        String openGlVersionString =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later");
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Turn off the plane discovery since we're only looking for images
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        return view;
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = super.getSessionConfiguration(session);
        if (!setupAugmentedImageDatabase(config, session)) {
            Log.e(TAG, "Could not setup augmented image database");
        }
        return config;
    }

    private boolean setupAugmentedImageDatabase(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase;

        AssetManager asset = getContext() != null ? getContext().getAssets() : null;
        if (asset == null) {
            Log.e(TAG, "Context is null, cannot initialize image database.");
            return false;
        }

        try {
            String image_folder = "image";
            String[] image_list = asset.list(image_folder);

            if (image_list == null) {
                Log.e(TAG, "image list is null");
                return false;
            }

            // Create ImageDataBase
            augmentedImageDatabase = new AugmentedImageDatabase(session);
            for (String s : image_list) {
                augmentedImageDatabase.addImage(s, Objects.requireNonNull(loadImageBitmap(asset, image_folder + "/" + s)));
            }
            config.setAugmentedImageDatabase(augmentedImageDatabase);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "asset/image folder not exists");
            return false;
        }

    }

    private Bitmap loadImageBitmap(AssetManager assetManager, String IMAGE_NAME) {
        try (InputStream is = assetManager.open(IMAGE_NAME)) {
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e);
        }
        return null;
    }
}
