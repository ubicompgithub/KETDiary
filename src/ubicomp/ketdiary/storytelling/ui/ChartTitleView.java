package ubicomp.ketdiary.storytelling.ui;

import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ChartTitleView extends View {

	private Paint text_paint_large = new Paint();
	private Paint text_paint_large_2 = new Paint();
	private String[] title_str = new String[4];
	
	private boolean chartTouchable = true;
	
	private int title_0 = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_title_0);
	private int title_1 = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_title_1);
	private int title_2 = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_title_2);
	private int title_3 = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_title_3);
	
	private int titleTop = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_title_top);
	
	private ChartCaller caller;
	
	private int chart_type = 0;
	
	private static final int text_color = App.getContext().getResources().getColor(R.color.chart_text);
	private static final int orange = App.getContext().getResources().getColor(R.color.lite_orange);
	
	public ChartTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		text_paint_large.setColor(text_color);
		int textSize = App.getContext().getResources().getDimensionPixelSize(R.dimen.normal_text_size);
		text_paint_large.setTextSize(textSize);
		text_paint_large.setTextAlign(Align.LEFT);
		text_paint_large.setTypeface(Typefaces.getWordTypefaceBold());
		text_paint_large_2.setColor(orange);
		text_paint_large_2.setTextSize(textSize);
		text_paint_large_2.setTextAlign(Align.LEFT);
		text_paint_large_2.setTypeface(Typefaces.getWordTypefaceBold());
		title_str[0] = getResources().getString(R.string.emotion);
		title_str[1] = getResources().getString(R.string.craving);
		title_str[2] = getResources().getString(R.string.brac_result);
		title_str[3] = getResources().getString(R.string.total_result);
	}
	
	public void setting(ChartCaller caller){
		this.caller = caller;
	}
	
	public void setTouchable(boolean touchable){
		chartTouchable = touchable;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!chartTouchable)
			return true;
		int x = (int) event.getX();

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (x < title_1) {
				chart_type = 0;
				ClickLog.Log(ClickLogId.STORYTELLING_CHART_TYPE0);
			} else if (x < title_2) {
				chart_type = 1;
				ClickLog.Log(ClickLogId.STORYTELLING_CHART_TYPE1);
			} else if (x < title_3) {
				chart_type = 2;
				ClickLog.Log(ClickLogId.STORYTELLING_CHART_TYPE2);
			} else {
				chart_type = 3;
				ClickLog.Log(ClickLogId.STORYTELLING_CHART_TYPE3);
			}
			caller.setChartType(chart_type);
		}
		return true;
	}

	public void setChartType(int type){
		chart_type = type;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawText(title_str[0], title_0, titleTop, text_paint_large);
		canvas.drawText(title_str[1], title_1, titleTop, text_paint_large);
		canvas.drawText(title_str[2], title_2, titleTop, text_paint_large);
		canvas.drawText(title_str[3], title_3, titleTop, text_paint_large);
		switch (chart_type) {
		case 0:
			canvas.drawText(title_str[0], title_0, titleTop, text_paint_large_2);
			break;
		case 1:
			canvas.drawText(title_str[1], title_1, titleTop, text_paint_large_2);
			break;
		case 2:
			canvas.drawText(title_str[2], title_2, titleTop, text_paint_large_2);
			break;
		case 3:
			canvas.drawText(title_str[3], title_3, titleTop, text_paint_large_2);
			break;
		}
	}

}
