package ubicomp.ketdiary.storytelling.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

@SuppressLint("ViewConstructor")
public class PageWidgetVertical extends View {

	private int width = 1, height = 1, cornerX = 1, cornerY = 1;
	private Path path0, path1;

	private Bitmap curPageBmp;
	private Bitmap nextPageBmp;

	private PointF touch = new PointF();
	private PointF BezierStart1 = new PointF();
	private PointF BezierControl1 = new PointF();
	private PointF Beziervertex1 = new PointF();
	private PointF BezierEnd1 = new PointF();
	private PointF BezierStart2 = new PointF();
	private PointF BezierControl2 = new PointF();
	private PointF Beziervertex2 = new PointF();
	private PointF BezierEnd2 = new PointF();

	private float middleX, middleY, degrees;
	private ColorMatrixColorFilter colorMatrixFilter;
	private Matrix matrix;
	private float[] matrixArray = { 0, 0, 0, 0, 0, 0, 0, 0, 1 };

	private float maxLength = 1;
	private int[] backShadowColors;
	private int[] frontShadowColors;
	private GradientDrawable backShadowDrawableLR;
	private GradientDrawable backShadowDrawableRL;
	private GradientDrawable folderShadowDrawableLR;
	private GradientDrawable folderShadowDrawableRL;
	private GradientDrawable frontShadowDrawableHBT;
	private GradientDrawable frontShadowDrawableHTB;
	private GradientDrawable frontShadowDrawableVLR;
	private GradientDrawable frontShadowDrawableVRL;

	private Bitmap _bitmap;
	private Canvas _canvas;
	private Paint _bitmapPaint;
	private Paint _paint;

	public PageWidgetVertical(Context context) {
		super(context);
	}

	public void setting(int _width, int _height) {
		width = _width;
		height = _height;
		maxLength = (float) Math.hypot(_width, _height);
		path0 = new Path();
		path1 = new Path();
		createDrawable();

		_bitmap = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888);
		_canvas = new Canvas(_bitmap);
		_bitmapPaint = new Paint(Paint.DITHER_FLAG);
		_paint = new Paint();
		_paint.setStyle(Paint.Style.FILL);

		ColorMatrix cm = new ColorMatrix();
		float array[] = { 0.55f, 0, 0, 0, 80.0f, 0, 0.55f, 0, 0, 80.0f, 0, 0, 0.55f, 0, 80.0f, 0, 0, 0, 0.2f, 0 };
		cm.set(array);
		colorMatrixFilter = new ColorMatrixColorFilter(cm);
		matrix = new Matrix();
	}

	public void setTouchPosition(PointF p) {
		touch.x = p.x;
		touch.y = p.y;
		cornerX = width;
		cornerY = height;
		this.postInvalidate();
	}

	public PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
		PointF CrossP = new PointF();
		float a1 = (P2.y - P1.y) / (P2.x - P1.x);
		float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

		float a2 = (P4.y - P3.y) / (P4.x - P3.x);
		float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
		CrossP.x = (b2 - b1) / (a1 - a2);
		CrossP.y = a1 * CrossP.x + b1;
		return CrossP;
	}

	private void calcPoints() {
		middleX = (touch.x + cornerX) / 2;
		middleY = (touch.y + cornerY) / 2;

		BezierControl1.x = middleX - (cornerY - middleY) * (cornerY - middleY) / (cornerX - middleX);
		BezierControl1.y = cornerY;
		BezierControl2.x = cornerX;
		BezierControl2.y = middleY - (cornerX - middleX) * (cornerX - middleX) / (cornerY - middleY);

		BezierStart1.x = BezierControl1.x - (cornerX - BezierControl1.x) / 2;
		BezierStart1.y = cornerY;

		BezierStart2.x = cornerX;
		BezierStart2.y = BezierControl2.y - (cornerY - BezierControl2.y) / 2;

		BezierEnd1 = getCross(touch, BezierControl1, BezierStart1, BezierStart2);
		BezierEnd2 = getCross(touch, BezierControl2, BezierStart1, BezierStart2);

		Beziervertex1.x = (BezierStart1.x + 2 * BezierControl1.x + BezierEnd1.x) / 4;
		Beziervertex1.y = (2 * BezierControl1.y + BezierStart1.y + BezierEnd1.y) / 4;
		Beziervertex2.x = (BezierStart2.x + 2 * BezierControl2.x + BezierEnd2.x) / 4;
		Beziervertex2.y = (2 * BezierControl2.y + BezierStart2.y + BezierEnd2.y) / 4;
	}

	private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap, Path path) {
		path0.reset();
		path0.moveTo(BezierStart1.x, BezierStart1.y);
		path0.quadTo(BezierControl1.x, BezierControl1.y, BezierEnd1.x, BezierEnd1.y);
		path0.lineTo(touch.x, touch.y);
		path0.lineTo(BezierEnd2.x, BezierEnd2.y);
		path0.quadTo(BezierControl2.x, BezierControl2.y, BezierStart2.x, BezierStart2.y);
		path0.lineTo(cornerX, cornerY);
		path0.close();

		canvas.save();
		canvas.clipPath(path, Region.Op.XOR);
		if (bitmap != null && !bitmap.isRecycled())
			canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}

	public void drawCurrentPageShadow(Canvas canvas) {
		double degree;

		degree = Math.PI / 4 - Math.atan2(touch.y - BezierControl1.y, touch.x - BezierControl1.x);

		double d1 = (float) 25 * 1.414 * Math.cos(degree);
		double d2 = (float) 25 * 1.414 * Math.sin(degree);
		float x = (float) (touch.x + d1);
		float y;

		y = (float) (touch.y - d2);

		path1.reset();
		path1.moveTo(x, y);
		path1.lineTo(touch.x, touch.y);
		path1.lineTo(BezierControl1.x, BezierControl1.y);
		path1.lineTo(BezierStart1.x, BezierStart1.y);
		path1.close();
		float rotateDegrees;
		canvas.save();
		canvas.clipPath(path0, Region.Op.XOR);
		canvas.clipPath(path1, Region.Op.INTERSECT);
		int leftx;
		int rightx;
		GradientDrawable currentPageShadow;

		leftx = (int) (BezierControl1.x - 25);
		rightx = (int) BezierControl1.x + 1;
		currentPageShadow = frontShadowDrawableVRL;

		rotateDegrees = (float) Math.toDegrees(Math.atan2(touch.x - BezierControl1.x, BezierControl1.y - touch.y));
		canvas.rotate(rotateDegrees, BezierControl1.x, BezierControl1.y);
		canvas.restore();

		path1.reset();
		path1.moveTo(x, y);
		path1.lineTo(touch.x, touch.y);
		path1.lineTo(BezierControl2.x, BezierControl2.y);
		path1.lineTo(BezierStart2.x, BezierStart2.y);
		path1.close();
		canvas.save();
		canvas.clipPath(path0, Region.Op.XOR);
		canvas.clipPath(path1, Region.Op.INTERSECT);

		leftx = (int) (BezierControl2.y - 25);
		rightx = (int) (BezierControl2.y + 1);
		currentPageShadow = frontShadowDrawableHBT;

		rotateDegrees = (float) Math.toDegrees(Math.atan2(BezierControl2.y - touch.y, BezierControl2.x - touch.x));
		canvas.rotate(rotateDegrees, BezierControl2.x, BezierControl2.y);
		float temp;
		if (BezierControl2.y < 0)
			temp = BezierControl2.y - height;
		else
			temp = BezierControl2.y;

		int hmg = (int) Math.hypot(BezierControl2.x, temp);
		if (hmg > maxLength)
			currentPageShadow.setBounds((int) (BezierControl2.x - 25) - hmg, leftx,
					(int) (BezierControl2.x + maxLength) - hmg, rightx);
		else
			currentPageShadow.setBounds((int) (BezierControl2.x - maxLength), leftx, (int) (BezierControl2.x), rightx);
		currentPageShadow.draw(canvas);
		canvas.restore();
	}

	private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap) {
		path1.reset();
		path1.moveTo(BezierStart1.x, BezierStart1.y);
		path1.lineTo(Beziervertex1.x, Beziervertex1.y);
		path1.lineTo(Beziervertex2.x, Beziervertex2.y);
		path1.lineTo(BezierStart2.x, BezierStart2.y);
		path1.lineTo(cornerX, cornerY);
		path1.close();

		degrees = (float) Math.toDegrees(Math.atan2(BezierControl1.x - cornerX, BezierControl2.y - cornerY));

		canvas.save();
		canvas.clipPath(path0);
		canvas.clipPath(path1, Region.Op.INTERSECT);
		if (bitmap != null && !bitmap.isRecycled())
			canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.rotate(degrees, BezierStart1.x, BezierStart1.y);
		canvas.restore();
	}

	public void setBitmaps(Bitmap curPage, Bitmap nextPage) {
		curPageBmp = curPage;
		nextPageBmp = nextPage;
	}

	private void createDrawable() {
		int[] color = { 0x333333, 0xb0333333 };
		folderShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, color);
		folderShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		folderShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
		folderShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		backShadowColors = new int[] { 0xff111111, 0x111111 };
		backShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, backShadowColors);
		backShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		backShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, backShadowColors);
		backShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		frontShadowColors = new int[] { 0x80111111, 0x111111 };
		frontShadowDrawableVLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, frontShadowColors);
		frontShadowDrawableVLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		frontShadowDrawableVRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, frontShadowColors);
		frontShadowDrawableVRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		frontShadowDrawableHTB = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, frontShadowColors);
		frontShadowDrawableHTB.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		frontShadowDrawableHBT = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, frontShadowColors);
		frontShadowDrawableHBT.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	}

	private void drawCurrentBackArea(Canvas canvas) {
		path1.reset();
		path1.moveTo(Beziervertex2.x, Beziervertex2.y);
		path1.lineTo(Beziervertex1.x, Beziervertex1.y);
		path1.lineTo(BezierEnd1.x, BezierEnd1.y);
		path1.lineTo(touch.x, touch.y);
		path1.lineTo(BezierEnd2.x, BezierEnd2.y);
		path1.close();

		canvas.save();
		canvas.clipPath(path0);
		canvas.clipPath(path1, Region.Op.INTERSECT);

		_paint.setColorFilter(colorMatrixFilter);

		float dis = (float) Math.hypot(cornerX - BezierControl1.x, BezierControl2.y - cornerY);
		float f8 = (cornerX - BezierControl1.x) / dis;
		float f9 = (BezierControl2.y - cornerY) / dis;
		matrixArray[0] = 1 - 2 * f9 * f9;
		matrixArray[1] = 2 * f8 * f9;
		matrixArray[3] = matrixArray[1];
		matrixArray[4] = 1 - 2 * f8 * f8;
		matrix.reset();
		matrix.setValues(matrixArray);
		matrix.preTranslate(-BezierControl1.x, -BezierControl1.y);
		matrix.postTranslate(BezierControl1.x, BezierControl1.y);

		canvas.drawColor(0xFFF5F4F0);

		canvas.rotate(degrees, BezierStart1.x, BezierStart1.y);
		canvas.restore();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0xFFAAAAAA);
		calcPoints();

		drawCurrentPageArea(_canvas, curPageBmp, path0);
		drawCurrentBackArea(_canvas);
		drawNextPageAreaAndShadow(_canvas, nextPageBmp);
		drawCurrentPageShadow(_canvas);

		if (_bitmap != null && !_bitmap.isRecycled())
			canvas.drawBitmap(_bitmap, 0, 0, _bitmapPaint);
	}

	public void clear() {
		_canvas = null;
		_bitmapPaint.clearShadowLayer();
		_paint.clearShadowLayer();
		_bitmap.recycle();
	}

	public Bitmap getCurPageBmp() {
		return curPageBmp;
	}

	public Bitmap getNextPageBmp() {
		return nextPageBmp;
	}

}
