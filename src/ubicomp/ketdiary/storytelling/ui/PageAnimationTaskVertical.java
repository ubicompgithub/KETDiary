package ubicomp.ketdiary.storytelling.ui;

import ubicomp.ketdiary.main.MainActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.AsyncTask;

public class PageAnimationTaskVertical extends AsyncTask<Void, Void, Void> {

	private PageWidgetVertical pageWidget;
	private PointF from;
	private float width_gap;
	private float height_gap;
	private static final int gaps = 20;
	private static final int start_offset = 900;
	private static final int clip_time = 400;
	private static final int sleep_time = clip_time / gaps;
	private PageAnimationCaller caller;

	private Bitmap cur = null, next = null;

	public PageAnimationTaskVertical(PageWidgetVertical pageWidget, PointF from, PointF to, int[] bgs,
			PageAnimationCaller caller, int startImageIdx) {
		this.pageWidget = pageWidget;
		this.from = from;
		this.caller = caller;
		width_gap = (to.x - from.x) / (float) gaps;
		height_gap = (to.y - from.y) / (float) gaps;
		int width = caller.getPageWidth();
		int height = caller.getPageHeight();
		Bitmap tmp;
		System.gc();
		tmp = BitmapFactory.decodeResource(pageWidget.getResources(), bgs[startImageIdx]);
		cur = Bitmap.createScaledBitmap(tmp, width, height, true);
		tmp.recycle();
		tmp = BitmapFactory.decodeResource(pageWidget.getResources(), bgs[startImageIdx + 1]);
		next = Bitmap.createScaledBitmap(tmp, width, height, true);
		tmp.recycle();
		pageWidget.setBitmaps(cur, next);
		pageWidget.setTouchPosition(from);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			Thread.sleep(sleep_time+start_offset);
		} catch (InterruptedException e) {
		}
		PointF touch = new PointF(from.x, from.y);
		for (int i = 0; i < gaps; ++i) {
			touch.x += width_gap;
			touch.y += height_gap;
			try {
				Thread.sleep(sleep_time);
			} catch (InterruptedException e) {
			}
			pageWidget.setTouchPosition(touch);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		clean();
		caller.resetPage(0);
		caller.invalidatePage();
		caller.endOnViewCreateAnimation();
	}

	@Override
	protected void onCancelled() {
		clean();
		MainActivity.getMainActivity().enableTabAndClick(true);
	}

	private void clean() {
		if (cur != null && !cur.isRecycled()) {
			cur.recycle();
			cur = null;
		}
		if (next != null && !next.isRecycled()) {
			next.recycle();
			next = null;
		}
		System.gc();
	}

}
