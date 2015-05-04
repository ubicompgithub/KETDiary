package ubicomp.ketdiary.storytelling.ui;

import java.util.Random;

import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.structure.StorytellingRead;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.EnablePage;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.main.ui.toast.CustomToast;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.config.PreferenceControl;
import ubicomp.ketdiary.system.uploader.DataUploader;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("InlinedApi")
public class QuoteMsgBox {

	private EnablePage enablePage;
	private LayoutInflater inflater;
	private FrameLayout boxLayout = null;

	private RelativeLayout mainLayout;
	private TextView title, help, end, cancel;
	private Typeface wordTypefaceBold;
	private int page;
	private DatabaseControl db;
	private String[] learningArray = App.getContext().getResources().getStringArray(R.array.quote_learning);
	private String[] learningArrayExtend = App.getContext().getResources()
			.getStringArray(R.array.quote_learning_extend);

	public QuoteMsgBox(EnablePage enablePage, RelativeLayout mainLayout) {
		this.enablePage = enablePage;
		this.inflater = (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mainLayout = mainLayout;
		db = new DatabaseControl();
		setting();
	}

	private void setting() {

		wordTypefaceBold = Typefaces.getWordTypefaceBold();

		boxLayout = (FrameLayout) inflater.inflate(R.layout.dialog_storytelling_quote, null);
		boxLayout.setVisibility(View.INVISIBLE);

		title = (TextView) boxLayout.findViewById(R.id.quote_title);
		help = (TextView) boxLayout.findViewById(R.id.quote_text);
		end = (TextView) boxLayout.findViewById(R.id.quote_enter);
		cancel = (TextView) boxLayout.findViewById(R.id.quote_cancel);

		mainLayout.addView(boxLayout);

		RelativeLayout.LayoutParams boxParam = (RelativeLayout.LayoutParams) boxLayout.getLayoutParams();
		boxParam.width = boxParam.height = LayoutParams.MATCH_PARENT;

		help.setTypeface(wordTypefaceBold);
		end.setTypeface(wordTypefaceBold);
		title.setTypeface(wordTypefaceBold);
		cancel.setTypeface(wordTypefaceBold);

		cancel.setOnClickListener(new CancelListener());
		end.setOnClickListener(new EndListener());
	}

	public void clear() {
		if (boxLayout != null)
			mainLayout.removeView(boxLayout);
	}

	public void openBox(int page) {
		enablePage.enablePage(false);
		boxLayout.setVisibility(View.VISIBLE);
		this.page = page;
		
		Random rand = new Random();
		int showType = rand.nextInt(3);
		if (showType < 1){
			help.setText(learningArray[page%12]);
		}else{
			int idx = rand.nextInt(learningArrayExtend.length);
			help.setText(learningArrayExtend[idx]);
		}
		return;
	}

	private class CancelListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			closeBox();
			ClickLog.Log(ClickLogId.STORYTELLING_READ_CANCEL);
		}

	}

	private class EndListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			closeBox();
			ClickLog.Log(ClickLogId.STORYTELLING_READ_OK);
			int addScore = db.insertStorytellingRead(new StorytellingRead(System.currentTimeMillis(), false, page, 0));
			if (PreferenceControl.checkCouponChange())
				PreferenceControl.setCouponChange(true);
			CustomToast.generateToast(R.string.bonus, addScore);
			DataUploader.upload();
		}

	}

	public void closeBox() {
		enablePage.enablePage(true);
		boxLayout.setVisibility(View.INVISIBLE);
	}
}
