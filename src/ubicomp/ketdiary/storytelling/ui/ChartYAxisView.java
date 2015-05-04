package ubicomp.ketdiary.storytelling.ui;

import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.Typefaces;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class ChartYAxisView extends View {

	private Paint text_paint_small = new Paint();
	private String high;

	private int bar_width = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_bar_size);
	private int bar_bottom = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_bar_margin);
	private int chartHeight = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_height);
	private int textSize = App.getContext().getResources().getDimensionPixelSize(R.dimen.sn_text_size);

	private int chart_type = 0;

	private int yaxis_color = App.getContext().getResources().getColor(R.color.chart_y_axis);

	public ChartYAxisView(Context context, AttributeSet attrs) {
		super(context, attrs);
		text_paint_small.setColor(yaxis_color);
		text_paint_small.setTextAlign(Align.CENTER);
		text_paint_small.setTextSize(textSize);
		text_paint_small.setTypeface(Typefaces.getDigitTypefaceBold());
		high = getResources().getString(R.string.high);
		bar_width = App.getContext().getResources().getDimensionPixelSize(R.dimen.chart_bar_size);
	}

	public void setChartType(int type) {
		chart_type = type;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int max_height = (chartHeight - bar_bottom) * 4 / 10;
		int _bottom = chartHeight - bar_bottom;

		// Draw Y axis label
		canvas.drawText("0", 3 * bar_width / 2, _bottom, text_paint_small);
		String maxLabel;
		if (chart_type == 0)
			maxLabel = "5";
		else if (chart_type == 1)
			maxLabel = "10";
		else if (chart_type == 2)
			maxLabel = "0.5";
		else
			maxLabel = high;
		canvas.drawText(maxLabel, 3 * bar_width / 2, _bottom - max_height, text_paint_small);
	}

}
