package com.sensorberg.vectorpathoutlineprovider;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * M = move
 * L = line
 * C = curve
 * Z = close
 * <p>
 * Currently we're only supporting M, L and Z.
 * Improvements:
 * - support the rest of all
 * - move all the parsing to the constructor and use a Matrix scaling on the path
 * TODO: wanna open source this? improve the parsing and add the `C`
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP) class InternalProvider extends ViewOutlineProvider {

	/**
	 * Test data:
	 * M180.45,6.34l-140.89,0l-27.37,30.78l-12.19,158.32l47.91,81.54l124.19,0l47.91,-81.54l-12.19,-158.32z
	 * M36.67,0l146.66,0l24.44 36.38L220 195.04 172.1 277H47.9L0 195.04 12.19 36.43z
	 * M 180.45 5.08 L 39.55 5.08 L 12.2 36.52 L 0 195.07 L 47.9 277 L 172.1 277 L 220 195.07 L 207.81 36.52 Z
	 * <p>
	 * ,M36.67,0l146.66,0l24.44,36.38L220,195.04,172.1,277H47.9L0,195.04,12.19,36.43z
	 * h = 277
	 * w = 220
	 */

	private final float width;
	private final float height;
	private final PointF[] points;
	private final Path path = new Path();

	private float lastScaleX = Float.NaN;
	private float lastScaleY = Float.NaN;

	InternalProvider(String path, float width, float height) {
		this.width = width;
		this.height = height;
/*		String[] segments = path.split("L");
		points = new PointF[segments.length];
		for (int i = 0; i < points.length; i++) {
			String segment = segments[i].replaceAll("M", "").replaceAll("Z", "");
			String[] pair = segment.split(" ");
			points[i] = new PointF(Float.parseFloat(pair[0]), Float.parseFloat(pair[1]));
		}*/

		// This matcher is matching all VectorDrawable commands,
		// but currently we're only parsing a very small subset of them
		Pattern pattern = Pattern.compile("[mhlvMLHV][-0-9., ]*");
		Matcher matcher = pattern.matcher(path);
		List<PointF> points = new ArrayList<>();
		while (matcher.find()) {
			String command = matcher.group();
			String[] pair = command.substring(1).trim().split("[, ]");
			if (pair.length >= 2) {
				points.add(new PointF(Float.parseFloat(pair[0].trim()), Float.parseFloat(pair[1].trim())));
			}
		}
		this.points = points.toArray(new PointF[points.size()]);
	}

	@Override public void getOutline(View view, Outline outline) {
		float scaleX = view.getWidth() / width;
		float scaleY = view.getHeight() / height;
		if (scaleX == 0 || scaleY == 0) {
			return;
		}

		if (scaleX != lastScaleX || scaleY != lastScaleY) {
			lastScaleX = scaleX;
			lastScaleY = scaleY;
			path.reset();
			for (int i = 0; i < points.length; i++) {
				PointF point = points[i];
				if (i == 0) {
					path.moveTo(point.x * scaleX, point.y * scaleY);
				} else {
					path.lineTo(point.x * scaleX, point.y * scaleY);
					//path.rLineTo(point.x * scaleX, point.y * scaleY);
				}
			}
			path.close();
		}

		outline.setConvexPath(path);
	}
}