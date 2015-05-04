package ubicomp.ketdiary.storytelling.facebook;

import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.storytelling.ui.StorytellingGraphics;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;

public class BitmapGenerator {

	private static String[] QUOTE_STR;

	public static Bitmap generateBitmap(int week, int score) {
		int res_id = StorytellingGraphics.getPage(score, week);

		QUOTE_STR = App.getContext().getResources().getStringArray(R.array.quote_message);
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 2;
		Bitmap bmp_t = BitmapFactory.decodeResource(App.getContext().getResources(), res_id, opts);
		
		Bitmap bmp = bmp_t.copy(Bitmap.Config.ARGB_8888, true);
		bmp_t.recycle();

		Canvas canvas = new Canvas(bmp);

		int image_x = bmp.getWidth();
		int image_y = bmp.getHeight();
		int textSize = image_x * 21 / 480;

		// Draw quote
		int text_color = App.getContext().getResources().getColor(R.color.page_gray);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(text_color);
		paint.setTextSize(textSize);
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setTypeface(Typefaces.getWordTypeface());

		int black = App.getContext().getResources().getColor(R.color.black);
		Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		titlePaint.setColor(black);
		titlePaint.setAlpha(35);
		titlePaint.setTextSize(textSize * 3);
		titlePaint.setTypeface(Typefaces.getWordTypefaceBold());
		titlePaint.setTextAlign(Align.CENTER);

		int top_margin = image_y * 428 / 480;
		int left_margin = image_x * 45 / 480;

		String[] strs = QUOTE_STR[week%12].split("\n");
		for (int i = 0; i < strs.length; ++i) {
			canvas.drawText(strs[i], left_margin, top_margin, paint);
			top_margin += textSize;
		}

		canvas.drawText(App.getContext().getString(R.string.app_name) + " \u00a9", image_x >> 1, image_y >> 1,
				titlePaint);

		return bmp;
	}
}
