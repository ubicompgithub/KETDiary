package ubicomp.ketdiary.statistic.ui.block;


import java.util.Calendar;

import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.ImageView.ScaleType;
import android.widget.TableLayout;
import android.widget.TextView;
import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.structure.Detection;
import ubicomp.ketdiary.main.ui.Typefaces;

public class StatisticMonthViewSDK10 extends StatisticPageView {

	private DatabaseControl db;
	private TextView[] time_labels;
	private Drawable[] circleDrawables;
	private ImageView[] circles;

	private LinearLayout timeLayout;
	private TableLayout blockLayout;
	private LinearLayout labelLayout;
	
	private TextView[] labels;
	private ImageView[] labelImgs;
	
	private TextView month0, month1, month2, month3;
	
	private TextView title;
	
	private static final int nBlocks = 3;
	private static final int nDays = 28;
	
	private static final int[] blockHint = {R.string.morning_short,R.string.noon_short,R.string.night_short};
	private static final int[] labelHint = {R.string.test_pass,R.string.test_fail,R.string.test_none}; 
	
	private Typeface digitTypefaceBold;
	private Typeface wordTypefaceBold;
	
	private static final int text_color = App.getContext().getResources().getColor(R.color.text_gray);
	
	public StatisticMonthViewSDK10() {
		super(R.layout.statistic_month_view_sdk10);
		db = new DatabaseControl();
		digitTypefaceBold = Typefaces.getDigitTypefaceBold();
		wordTypefaceBold = Typefaces.getWordTypefaceBold();
		timeLayout = (LinearLayout) view.findViewById(R.id.statistic_month_timeblock_label_layout);
		blockLayout = (TableLayout) view.findViewById(R.id.statistic_month_block_layout);
		
		title= (TextView) view.findViewById(R.id.statistic_month_title);
		title.setTypeface(wordTypefaceBold);
		
		month0 = (TextView) view.findViewById(R.id.statistic_month_0);;
		month0.setTypeface(digitTypefaceBold);
		month1 = (TextView) view.findViewById(R.id.statistic_month_1);;
		month1.setTypeface(digitTypefaceBold);
		month2 = (TextView) view.findViewById(R.id.statistic_month_2);;
		month2.setTypeface(digitTypefaceBold);
		month3 = (TextView) view.findViewById(R.id.statistic_month_3);;
		month3.setTypeface(digitTypefaceBold);
		
		labelLayout = (LinearLayout) view.findViewById(R.id.statistic_month_label_layout);
		
		circleDrawables = new Drawable[3];
		circleDrawables[0] = context.getResources().getDrawable(R.drawable.statistic_month_none);
		circleDrawables[1] = context.getResources().getDrawable(R.drawable.statistic_month_fail);
		circleDrawables[2] = context.getResources().getDrawable(R.drawable.statistic_month_pass);
	}


	@Override
	public void clear() {
	}
	
	@Override
	public void load() {
		
		time_labels = new TextView[nBlocks];
		
		int textSize = (int) App.getContext().getResources().getDimensionPixelSize(R.dimen.normal_text_size);
		for (int i=0;i<nBlocks;++i){
			time_labels[i] = new TextView(context);
			time_labels[i].setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
			time_labels[i].setTextColor(text_color);
			time_labels[i].setText(blockHint[i]);
			time_labels[i].setTypeface(wordTypefaceBold);
			time_labels[i].setGravity(Gravity.CENTER);
			timeLayout.addView(time_labels[i]);
		}

		circles = new ImageView[nBlocks*nDays];
		
		for (int i=0;i<nBlocks*nDays;++i){
			circles[i] = new ImageView(context);
			circles[i].setScaleType(ScaleType.CENTER);
		}
		
		for (int i=0;i<nBlocks;++i){
			TableRow tr = new TableRow(context);
			blockLayout.addView(tr);
			for (int j=0;j<nDays;++j){
				tr.addView(circles[i*nDays+j]);
			}
		}
		
		labels = new TextView[3];
		labelImgs = new ImageView[3];
		for (int i=0;i<3;++i){
			labelImgs[i] = new ImageView(context);
			labelImgs[i].setScaleType(ScaleType.CENTER);
			labelLayout.addView(labelImgs[i]);
			labels[i] = new TextView(context);
			labels[i].setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
			labels[i].setTextColor(text_color);
			labels[i].setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
			labels[i].setText(labelHint[i]);
			labels[i].setTypeface(wordTypefaceBold);
			labelLayout.addView(labels[i]);
		}

		int c_width =  context.getResources().getDimensionPixelSize(R.dimen.week_circle_width);
		int c_height =  context.getResources().getDimensionPixelSize(R.dimen.week_circle_height);
		
		for (int i=0;i<nBlocks;++i){
			LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) time_labels[i].getLayoutParams();
			param.width = c_width;
			param.height = c_height;
		}
		
		int b_width =  context.getResources().getDimensionPixelSize(R.dimen.month_line_width);
		int b_gap =  context.getResources().getDimensionPixelSize(R.dimen.month_line_gap);
		for (int i=0;i<nBlocks*nDays;++i){
			TableRow.LayoutParams cParam = (TableRow.LayoutParams) circles[i].getLayoutParams();
			cParam.width = b_width;
			cParam.height = c_height;
			if (i%7==6)
				cParam.rightMargin = b_gap;
		}
		
		for (int i=0;i<3;++i){
			LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) labels[i].getLayoutParams();
			param.width = c_width*3/2;
			param.height = c_height;
			param = (LinearLayout.LayoutParams) labelImgs[i].getLayoutParams();
			param.width = c_width*3/4;
			param.height = c_height;
		}
		
		Float[] bracs = db.getMultiDaysPrimeBrac(nDays);
		for (int i=0;i<bracs.length;++i){
			int idx = (i%nBlocks)*nDays + i/nBlocks;
			if (bracs[i] == null){
				circles[idx].setImageDrawable(circleDrawables[0]);
			}
			else if (bracs[i] < Detection.BRAC_THRESHOLD)
				circles[idx].setImageDrawable(circleDrawables[2]);
			else
				circles[idx].setImageDrawable(circleDrawables[1]);
			
		}
		
		Calendar cal = Calendar.getInstance();
		
		int month, date;
		String month_label = "";

		cal.add(Calendar.DAY_OF_MONTH, -6);
		month = cal.get(Calendar.MONTH)+1;
		date = cal.get(Calendar.DATE);
		month_label = month+"/"+date;
		month3.setText(month_label);
		cal.add(Calendar.DAY_OF_MONTH, -7);
		month = cal.get(Calendar.MONTH)+1;
		date = cal.get(Calendar.DATE);
		month_label = month+"/"+date;
		month2.setText(month_label);
		cal.add(Calendar.DAY_OF_MONTH, -7);
		month = cal.get(Calendar.MONTH)+1;
		date = cal.get(Calendar.DATE);
		month_label = month+"/"+date;
		month1.setText(month_label);
		cal.add(Calendar.DAY_OF_MONTH, -7);
		month = cal.get(Calendar.MONTH)+1;
		date = cal.get(Calendar.DATE);
		month_label = month+"/"+date;
		month0.setText(month_label);
		
		labelImgs[0].setImageDrawable(circleDrawables[2]);
		labelImgs[1].setImageDrawable(circleDrawables[1]);
		labelImgs[2].setImageDrawable(circleDrawables[0]);
		
	}


	@Override
	public void onCancel() {
		clear();
	}
	
	
}
