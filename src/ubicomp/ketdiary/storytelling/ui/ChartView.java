package ubicomp.ketdiary.storytelling.ui;

import java.util.ArrayList;

import ubicomp.ketdiary.data.structure.BarInfo;
import ubicomp.ketdiary.data.structure.TimeValue;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public class ChartView extends View {

	private Paint paint_pass = new Paint();
	private Paint paint_fail = new Paint();

	private Paint paint_highlight = new Paint();
	private Paint text_paint_small = new Paint();
	private Paint text_paint_button = new Paint();
	private Paint focus_paint_len = new Paint();
	private Paint line_paint = new Paint();
	private Paint axis_paint = new Paint();

	private Paint emotion_paint = new Paint();
	private Paint craving_paint = new Paint();
	private Paint brac_paint = new Paint();

	private Paint emotion_paint_bg = new Paint();
	private Paint craving_paint_bg = new Paint();
	private Paint brac_paint_bg = new Paint();

	private int RADIUS;
	private int RADIUS_SQUARE;
	private int BUTTON_RADIUS;
	private int BUTTON_RADIUS_SQUARE;
	private int BUTTON_GAPS;

	private int curX = -1, curY = -1;

	private ArrayList<Point> circle_centers = new ArrayList<Point>();
	private ArrayList<Point> selected_centers = new ArrayList<Point>();
	private ArrayList<Point> button_centers = new ArrayList<Point>();

	private int bar_width, bar_gap, circle_radius, bar_bottom, bar_left;

	private static Bitmap chartCircleBmp, chartDataBmp, chartAddBmp;

	private ArrayList<TimeValue> selected_dates = new ArrayList<TimeValue>();;
	private ArrayList<Integer> selected_idx = new ArrayList<Integer>();;
	private ArrayList<BarInfo> bars;
	private ArrayList<Boolean> hasAudio;

	private int chartType = 0;
	private int pageWeek = 0;

	private int pageHeight = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_height);
	private int smallTextSize = App.getContext().getResources().getDimensionPixelSize(R.dimen.small_text_size);
	private int line_size = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_line_paint);
	private int bar_size = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_bar_size);
	private int bar_margin = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_bar_margin);
	private float top_margin = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_top_panel_height);
	private int button_left = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_button_start_left);
	private int button_top = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_button_start_top);
	private int bar_scroll_size = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_bar_scroll_size);
	
	private boolean onButton = false;
	private int buttonNum = -1;
	private HorizontalScrollView scrollView;
	private ChartCaller caller;
	
	private int dataLabelX, dataLabelY;
	private int addLabelX, addLabelY;
	
	private static final int axis_color = App.getContext().getResources().getColor(R.color.chart_axis);
	private static final int green = App.getContext().getResources().getColor(R.color.green);
	private static final int orange = App.getContext().getResources().getColor(R.color.lite_orange);
	private static final int white = App.getContext().getResources().getColor(R.color.white);
	private static final int xaxis_color = App.getContext().getResources().getColor(R.color.chart_x_axis);
	
	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		if (chartCircleBmp == null || chartCircleBmp.isRecycled())
			chartCircleBmp = BitmapFactory.decodeResource(getResources(), R.drawable.chart_button);
		if (chartDataBmp == null || chartDataBmp.isRecycled())
			chartDataBmp = BitmapFactory.decodeResource(getResources(), R.drawable.chart_show);
		if (chartAddBmp == null || chartAddBmp.isRecycled())
			chartAddBmp = BitmapFactory.decodeResource(getResources(), R.drawable.chart_add);
		
		dataLabelX = chartDataBmp.getWidth() / 2;
		dataLabelY = chartDataBmp.getHeight() / 2;
		addLabelX = chartAddBmp.getWidth() / 2;
		addLabelY = chartAddBmp.getHeight() / 2;
		
		paint_pass.setColor(green);
		paint_fail.setColor(orange);

		paint_highlight.setColor(white);
		paint_highlight.setAlpha(50);

		text_paint_button.setColor(white);
		text_paint_button.setTextSize(smallTextSize);
		text_paint_button.setTextAlign(Align.CENTER);

		text_paint_small.setColor(xaxis_color);
		text_paint_small.setTextAlign(Align.CENTER);
		text_paint_small.setTextSize(smallTextSize);

		focus_paint_len.setColor(white);
		focus_paint_len.setAlpha(70);

		axis_paint.setColor(axis_color);
		axis_paint.setStrokeWidth(line_size);

		line_paint.setColor(white);
		line_paint.setStrokeWidth(line_size/2);

		emotion_paint.setColor(green);
		emotion_paint.setStrokeWidth(line_size);
		emotion_paint.setAlpha(180);
		craving_paint.setColor(orange);
		craving_paint.setStrokeWidth(line_size);
		craving_paint.setAlpha(180);
		brac_paint.setColor(white);
		brac_paint.setStrokeWidth(line_size);
		brac_paint.setAlpha(180);

		emotion_paint_bg.setColor(green);
		emotion_paint_bg.setAlpha(90);
		craving_paint_bg.setColor(orange);
		craving_paint_bg.setAlpha(90);
		brac_paint_bg.setColor(white);
		brac_paint_bg.setAlpha(90);

		bar_width = bar_size;
		bar_gap = bar_width/3;
		circle_radius = bar_width / 3;
		bar_bottom = bar_margin;
		bar_left = bar_margin;

		RADIUS = bar_width * 9 / 5;
		RADIUS_SQUARE = RADIUS * RADIUS;
		BUTTON_RADIUS = chartCircleBmp.getWidth() / 2;
		BUTTON_RADIUS_SQUARE = BUTTON_RADIUS * BUTTON_RADIUS;
		BUTTON_GAPS = BUTTON_RADIUS * 9 / 2;

		
	}

	public void setting(ArrayList<BarInfo> bars, int pageWeek, ArrayList<Boolean> hasAudio,
			HorizontalScrollView scrollView, ChartCaller caller){
		this.caller = caller;

		this.chartType = 0;
		this.bars = bars;
		this.pageWeek = pageWeek;
		this.hasAudio = hasAudio;
		this.scrollView = scrollView;
	}
	
	public void setTouchToday(){
		int idx = circle_centers.size()-1;
		if  (idx<0)
			return;
		curX = circle_centers.get(idx).x+RADIUS/2;
		curY = circle_centers.get(idx).y;
		invalidate();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!isEnabled())
			return true;

		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			onButton = false;
			buttonNum = -1;
			for (int i = 0; i < button_centers.size(); ++i) {
				Point c = button_centers.get(i);
				int distance_square = (c.x - x) * (c.x - x) + (c.y - y) * (c.y - y);
				if (distance_square < BUTTON_RADIUS_SQUARE * 2.25F) {
					onButton = true;
					buttonNum = i;
					break;
				}
			}
			if (!onButton) {
				float ty = event.getY();
				if (ty >= top_margin) {
					curX = (int) event.getX();
					curY = (int) event.getY();
				}
				ClickLog.Log(ClickLogId.STORYTELLING_CHART);
			}
			caller.closeRecorder();
		} else if (action == MotionEvent.ACTION_UP && onButton && buttonNum >= 0 && buttonNum < selected_dates.size()) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			Point c = button_centers.get(buttonNum);
			int distance_square = (c.x - x) * (c.x - x) + (c.y - y) * (c.y - y);
			if (distance_square < BUTTON_RADIUS_SQUARE * 2.25F) {
				TimeValue tv = selected_dates.get(buttonNum);
				caller.openRecordBox(tv, selected_idx.get(buttonNum));
				ClickLog.Log(ClickLogId.STORYTELLING_CHART_BUTTON+tv.toClickValue());
			}
			onButton = false;
			buttonNum = -1;
		}
		invalidate();
		return true;
	}
	
	public void showToday(){
		int size = bars.size();
		if (size > 0){
			TimeValue tv = bars.get(size-1).getTv();
			caller.openRecordBox(tv, size-1);
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		circle_centers.clear();
		selected_centers.clear();
		selected_idx.clear();
		button_centers.clear();
		selected_dates.clear();

		if (chartType < 3) {
			drawBarChart(canvas);
			drawButtons(canvas);
		} else
			drawLineChart(canvas);
	}

	private void drawLineChart(Canvas canvas) {
		int max_height = (pageHeight - bar_bottom) * 4 / 10;
		int left = bar_left;
		int small_radius = circle_radius / 2;

		int _bottom = pageHeight - bar_bottom;

		if (bars.size() == 0)
			return;

		Point prev_e_center = null;
		Point prev_d_center = null;
		Point prev_b_center = null;

		for (int i = 0; i < bars.size(); ++i) {
			BarInfo bar = bars.get(i);

			float e_height, d_height, b_height;
			e_height = bar.getEmotion() / 5 * max_height;
			d_height = bar.getCraving() / 10 * max_height;
			b_height = bar.getBrac() / 0.5F * max_height;
			if (b_height > max_height)
				b_height = max_height;

			int e_top = _bottom - (int) e_height;
			int d_top = _bottom - (int) d_height;
			int b_top = _bottom - (int) b_height;
			if (!bar.isHasData())
				e_top = d_top = b_top = _bottom;

			// Draw X axis Label
			if (i % 7 == 0) {
				String str = bar.getTv().toSimpleDateString();
				canvas.drawLine(left, _bottom, left, _bottom - max_height, axis_paint);
				canvas.drawText(str, left + small_radius, _bottom + bar_width + small_radius, text_paint_small);
			}
			// Draw bars & annotation_circles
			Point e_center = new Point(left + small_radius, e_top - bar_gap);
			Point d_center = new Point(left + small_radius, d_top - bar_gap);
			Point b_center = new Point(left + small_radius, b_top - bar_gap);

			if (prev_e_center != null && prev_d_center != null && prev_b_center != null) {

				Path path_e = new Path();
				path_e.moveTo(prev_e_center.x, _bottom);
				path_e.lineTo(prev_e_center.x, prev_e_center.y);
				path_e.lineTo(e_center.x, e_center.y);
				path_e.lineTo(e_center.x, _bottom);

				Path path_d = new Path();
				path_d.moveTo(prev_d_center.x, _bottom);
				path_d.lineTo(prev_d_center.x, prev_d_center.y);
				path_d.lineTo(d_center.x, d_center.y);
				path_d.lineTo(d_center.x, _bottom);
				path_d.lineTo(prev_d_center.x, _bottom);

				Path path_b = new Path();
				path_b.moveTo(prev_d_center.x, _bottom);
				path_b.lineTo(prev_b_center.x, prev_b_center.y);
				path_b.lineTo(b_center.x, b_center.y);
				path_b.lineTo(b_center.x, _bottom);
				path_b.lineTo(prev_b_center.x, _bottom);

				canvas.drawPath(path_e, emotion_paint_bg);
				canvas.drawPath(path_d, craving_paint_bg);
				canvas.drawPath(path_b, brac_paint_bg);

				canvas.drawLine(prev_e_center.x, prev_e_center.y, e_center.x, e_center.y, emotion_paint);
				canvas.drawLine(prev_d_center.x, prev_d_center.y, d_center.x, d_center.y, craving_paint);
				canvas.drawLine(prev_b_center.x, prev_b_center.y, b_center.x, b_center.y, brac_paint);

			} else {
				canvas.drawLine(e_center.x, _bottom, e_center.x, e_center.y, emotion_paint);
				canvas.drawLine(d_center.x, _bottom, d_center.x, d_center.y, craving_paint);
				canvas.drawLine(b_center.x, _bottom, b_center.x, b_center.y, brac_paint);
			}
			prev_e_center = e_center;
			prev_d_center = d_center;
			prev_b_center = b_center;

			// draw highlights
			if (bar.getWeek() == pageWeek)
				canvas.drawRect(left, _bottom - max_height - bar_width - circle_radius, left + bar_width + bar_gap,
						_bottom, paint_highlight);

			if (i == bars.size() - 1) {
				canvas.drawLine(e_center.x, _bottom, e_center.x, e_center.y, emotion_paint);
				canvas.drawLine(d_center.x, _bottom, d_center.x, d_center.y, craving_paint);
				canvas.drawLine(b_center.x, _bottom, b_center.x, b_center.y, brac_paint);
			}
			left += (bar_width + bar_gap);
		}

		if (curX > 0 && curY > 0)
			canvas.drawCircle(curX, curY, RADIUS, focus_paint_len);
	}

	private void drawBarChart(Canvas canvas) {
		int max_height = (pageHeight - bar_bottom) * 4 / 10;
		int left = bar_left;

		if (bars.size() == 0)
			return;

		int bar_half = bar_width / 2;
		for (int i = 0; i < bars.size(); ++i) {

			float height = 0;
			BarInfo bar = bars.get(i);

			if (chartType == 0)
				height = bar.getEmotion() / 5 * max_height;
			else if (chartType == 1)
				height = bar.getCraving() / 10 * max_height;
			else if (chartType == 2) {
				height = bar.getBrac() / 0.5F * max_height;
				if (height > max_height)
					height = max_height;
			}

			// Draw bars & annotation_circles & highlights
			int right = left + bar_width;
			int _bottom = pageHeight - bar_bottom;
			int _top = _bottom - (int) height;

			// Draw bars & annotation_circles
			Point center = new Point(left + bar_half, _top - bar_gap - circle_radius);

			boolean hasAudioData = hasAudio.get(i);
			
			if (!hasAudioData)
				canvas.drawBitmap(chartAddBmp, center.x-addLabelX, center.y - addLabelY, null);
			else
				canvas.drawBitmap(chartDataBmp, center.x - dataLabelX, center.y - dataLabelY, null);

			if (!bar.isHasData())
				;
			else if (bar.isDrink())
				canvas.drawRect(left, _top, right, _bottom, paint_fail);
			else
				canvas.drawRect(left, _top, right, _bottom, paint_pass);

			circle_centers.add(center);

			// draw highlights
			if (bar.getWeek() == pageWeek)
				canvas.drawRect(left, _bottom - max_height - bar_width - circle_radius, right + bar_gap, _bottom,
						paint_highlight);

			// Draw X axis Label
			if (i % 7 == 0) {
				String str = bar.getTv().toSimpleDateString();
				canvas.drawText(str, left + circle_radius/2, _bottom + bar_width + circle_radius/2, text_paint_small);
			}
			left += (bar_width + bar_gap);
		}
	}

	private void drawButtons(Canvas canvas) {
		// Draw buttons
		if (curX > 0 && curY > 0) {

			// Draw focus area
			canvas.drawCircle(curX, curY, RADIUS, focus_paint_len);

			for (int i = 0; i < circle_centers.size(); ++i) {
				Point c = circle_centers.get(i);
				int distance_square = (curX - c.x) * (curX - c.x) + (curY - c.y) * (curY - c.y);
				if (distance_square < RADIUS_SQUARE) {
					TimeValue tv = bars.get(i).getTv();
					selected_centers.add(c);
					selected_dates.add(tv);
					selected_idx.add(i);
				}
			}

			int b_center_x = button_left + scrollView.getScrollX();
			int b_center_y = button_top;
			int b_center_x_bak = b_center_x;
			// Draw lines
			for (int i = 0; i < selected_centers.size(); ++i) {
				Point from = selected_centers.get(i);
				Point to = new Point(b_center_x, b_center_y);
				button_centers.add(to);
				canvas.drawLine(from.x, from.y, to.x, to.y, line_paint);
				b_center_x += BUTTON_GAPS;
			}
			// Draw buttons
			b_center_x = b_center_x_bak;
			for (int i = 0; i < selected_centers.size(); ++i) {
				Point to = new Point(b_center_x, b_center_y);

				canvas.drawBitmap(chartCircleBmp, to.x - BUTTON_RADIUS, to.y - BUTTON_RADIUS, null);
				TimeValue tv = selected_dates.get(i);
				String str = tv.toSimpleDateString();
				canvas.drawText(str, to.x, to.y + BUTTON_RADIUS / 3, text_paint_button);
				b_center_x += BUTTON_GAPS;
			}
		}
	}

	public void setChartType(int chartType) {
		this.chartType = chartType;
		invalidate();
	}

	public void setPageWeek(int pageWeek) {
		this.pageWeek = pageWeek;
		invalidate();
	}

	public int getChartWidth() {
		return bar_left * 3 / 2 + (bar_width + bar_gap) * bars.size();
	}

	public int getScrollValue(int page_week) {
		return bar_left + bar_scroll_size * 7 * (page_week - 1);
	}
}