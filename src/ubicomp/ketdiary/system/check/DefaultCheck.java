package ubicomp.ketdiary.system.check;

import ubicomp.ketdiary.system.config.PreferenceControl;

public class DefaultCheck {

	public static boolean check() {
		return PreferenceControl.defaultCheck();
	}

}
