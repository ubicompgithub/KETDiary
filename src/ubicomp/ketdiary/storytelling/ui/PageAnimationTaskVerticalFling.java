package ubicomp.ketdiary.storytelling.ui;

import ubicomp.ketdiary.main.MainActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.AsyncTask;

public class PageAnimationTaskVerticalFling extends AsyncTask<Void, Void, Void> {

	private PageWidgetVertical pageWidget;
	private PointF from,to;
	private float width_gap;
	private float height_gap;
	private static final int gaps = 20;
	private static final int clip_time = 400;
	private static final int sleep_time = clip_time/gaps;
	private int[] bgs;
	private PageAnimationCaller caller;
	
	private int startImageIdx;
	
	private Bitmap cur=null,next=null,tmp=null;
	private int type=-1; 
	private int width,height;
	
	public PageAnimationTaskVerticalFling(PageWidgetVertical pageWidget, PointF from, PointF to, int[] bgs,PageAnimationCaller caller,int startImageIdx, int type){
		this.pageWidget = pageWidget;
		this.from = from;
		this.to = to;
		this.startImageIdx = startImageIdx;
		
		this.caller = caller;
		width_gap = (to.x - from.x)/(float)gaps;
		height_gap = (to.y - from.y)/(float)gaps;
		this.bgs = bgs;
		this.type = type;
		width = caller.getPageWidth();
		height =  caller.getPageHeight();
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		
		cur=null;next=null;tmp=null;
		
		int curC = startImageIdx;
		
		if  (type == 1){// cur to next ()
			cur = pageWidget.getCurPageBmp();
			tmp = BitmapFactory.decodeResource(pageWidget.getResources(), bgs[curC+1]);
			next = Bitmap.createScaledBitmap(tmp, width, height, true);
			tmp.recycle();
			
			pageWidget.setBitmaps(cur, next);
			pageWidget.setTouchPosition(from);
			
			PointF touch = new PointF(from.x,from.y);
			
			for (int i=0;i<gaps;++i){
				touch.x += width_gap;
				touch.y += height_gap;
				try {
					Thread.sleep(sleep_time);
				} catch (InterruptedException e) {}
				pageWidget.setTouchPosition(touch);
			}
			
			caller.resetPage(+1);
			
			
		}else{ //next to cur (DOWN)
			next=pageWidget.getCurPageBmp();
			tmp = BitmapFactory.decodeResource(pageWidget.getResources(), bgs[curC-1]);
			cur = Bitmap.createScaledBitmap(tmp, width, height, true);
			tmp.recycle();
			
			pageWidget.setBitmaps(cur, next);
			pageWidget.setTouchPosition(to);
			
			PointF touch = new PointF(to.x,to.y);
			
			for (int i=0;i<gaps;++i){
				touch.x -= width_gap;
				touch.y -= height_gap;
				try {
					Thread.sleep(sleep_time);
				} catch (InterruptedException e) {}
				pageWidget.setTouchPosition(touch);
			}
			
			caller.resetPage(-1);
		}
		
		return null;
	}
	@Override
	 protected void onPostExecute(Void result) {
		caller.endFlingAnimation();
		caller.invalidatePage();
    }
	@Override
	protected void onCancelled(){
		MainActivity.getMainActivity().enableTabAndClick(true);
		if (cur!=null && !cur.isRecycled()){
			cur.recycle();
			cur = null;
		}
		if (next!=null && !next.isRecycled()){
			next.recycle();
			next = null;
		}
		if (tmp!=null && !tmp.isRecycled()){
			tmp.recycle();
			tmp = null;
		}
	}
	
}
