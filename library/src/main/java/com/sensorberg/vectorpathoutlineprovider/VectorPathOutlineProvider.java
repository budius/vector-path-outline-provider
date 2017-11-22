package com.sensorberg.vectorpathoutlineprovider;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

public class VectorPathOutlineProvider {
	public static void apply(View view, String path, float width, float height) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return;
		}
		apply_V21(view, path, width, height);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static void apply_V21(View view, String path, float width, float height) {
		view.setOutlineProvider(new InternalProvider(path, width, height));
	}
}