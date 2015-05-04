package ubicomp.ketdiary.system.check;

import java.util.Calendar;

import ubicomp.ketdiary.system.config.PreferenceControl;

public class LockCheck {
	public static boolean check() {
		if (PreferenceControl.isLocked()) {
			Calendar c_1 = PreferenceControl.getLockDate();
			Calendar c = Calendar.getInstance();
			if (c_1.before(c))
				return true;
		}
		return false;
	}

}
