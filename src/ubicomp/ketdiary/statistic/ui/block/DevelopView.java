package ubicomp.ketdiary.statistic.ui.block;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.system.config.PreferenceControl;
import ubicomp.ketdiary.data.file.MainStorage;
import android.widget.TextView;

public class DevelopView extends StatisticPageView {

	
	private TextView brac,ts;
	private long timestamp;
	
	private double brac_val;
	
	public DevelopView() {
		super(R.layout.statistic_developer_view_acvm);
		brac = (TextView) view.findViewById(R.id.developer_brac_value);
		ts = (TextView) view.findViewById(R.id.developer_ts_value);
	}

	@Override
	public void load() {
		
		timestamp = PreferenceControl.getDebugDetectionTimestamp();

		File mainStorageDir = MainStorage.getMainStorageDirectory();
		File textFile;
        
        textFile = new File(mainStorageDir.getPath() + File.separator + timestamp + File.separator + timestamp + ".txt");
        boolean result = parseTextFile(textFile);

        if (result){
			brac.setText(String.valueOf(brac_val));
		}else{
			brac.setText("NULL");
			ts.setText("NULL");
		}
		ts.setText(String.valueOf(timestamp));
	}

	@Override
	public void onCancel() {

	}

	@Override
	public void clear() {

	}

	protected boolean parseTextFile(File textFile){
        try {
			Scanner s = new Scanner(textFile);
			int index = 0;
			List<Double> valueArray_A0 = new ArrayList<Double>();
			List<Double> valueArray_A1 = new ArrayList<Double>();
			while(s.hasNext()){
				index++;
				String word = s.next();
				if(index % 5 == 3)
					valueArray_A0.add(Double.valueOf(word));
				else if (index %5 == 4)
					valueArray_A1.add(Double.valueOf(word));
			}
			
			
			int size = valueArray_A0.size();
			int min_size =size, max_size = size;
			if (min_size >valueArray_A1.size() )
				min_size = valueArray_A1.size();
			else
				max_size = valueArray_A1.size();
				
			Double[] values_A0 = valueArray_A0.toArray(new Double[max_size]);
			Double[] values_A1 = valueArray_A1.toArray(new Double[max_size]);
			
			Value[] values = new Value[min_size];
			for (int i=0;i<values.length;++i){
				values[i]= new Value(values_A0[i],values_A1[i]);
			}
			
			Arrays.sort(values);
			brac_val = values[(values.length-1)/2].brac;
			s.close();
		} catch (FileNotFoundException e1) {
			return false;
		}
        return true;
	}
	
	
	private class Value implements Comparable<Value>{
		double brac;
		
		public Value(double A0, double A1){
			this.brac = A1 - A0;
		}

		@Override
		public int compareTo(Value val) {
			if (this.brac > val.brac)
				return 1;
			else if (this.brac < val.brac)
				return -1;
			return 0;
		}
		
	}
	
}
