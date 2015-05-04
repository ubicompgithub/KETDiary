package ubicomp.ketdiary.storytelling.ui;

import ubicomp.ketdiary.main.R;

public class StorytellingGraphics {

	private static final int[][] PAGE = {
			{ R.drawable.story_0_0, R.drawable.story_0_1, R.drawable.story_0_2, R.drawable.story_0_3,
					R.drawable.story_0_4, R.drawable.story_0_5, R.drawable.story_0_6, R.drawable.story_0_7,
					R.drawable.story_0_8, R.drawable.story_0_9, R.drawable.story_0_10, R.drawable.story_0_11,
					R.drawable.story_0_12, R.drawable.story_0_13, R.drawable.story_0_14, }, // 0
			{ R.drawable.story_1_0, R.drawable.story_1_1, R.drawable.story_1_2, R.drawable.story_1_3,
					R.drawable.story_1_4, R.drawable.story_1_5, R.drawable.story_1_6, R.drawable.story_1_7,
					R.drawable.story_1_8, R.drawable.story_1_9, R.drawable.story_1_10, R.drawable.story_1_11,
					R.drawable.story_1_12, R.drawable.story_1_13, R.drawable.story_1_14, }, // 1
			{ R.drawable.story_2_0, R.drawable.story_2_1, R.drawable.story_2_2, R.drawable.story_2_3,
					R.drawable.story_2_4, R.drawable.story_2_5, R.drawable.story_2_6, R.drawable.story_2_7,
					R.drawable.story_2_8, R.drawable.story_2_9, R.drawable.story_2_10, R.drawable.story_2_11,
					R.drawable.story_2_12, R.drawable.story_2_13, R.drawable.story_2_14, }, // 2
			{ R.drawable.story_3_0, R.drawable.story_3_1, R.drawable.story_3_2, R.drawable.story_3_3,
					R.drawable.story_3_4, R.drawable.story_3_5, R.drawable.story_3_6, R.drawable.story_3_7,
					R.drawable.story_3_8, R.drawable.story_3_9, R.drawable.story_3_10, R.drawable.story_3_11,
					R.drawable.story_3_12, R.drawable.story_3_13, R.drawable.story_3_14, }, // 3
			{ R.drawable.story_4_0, R.drawable.story_4_1, R.drawable.story_4_2, R.drawable.story_4_3,
					R.drawable.story_4_4, R.drawable.story_4_5, R.drawable.story_4_6, R.drawable.story_4_7,
					R.drawable.story_4_8, R.drawable.story_4_9, R.drawable.story_4_10, R.drawable.story_4_11,
					R.drawable.story_4_12, R.drawable.story_4_13, R.drawable.story_4_14, }, // 4
			{ R.drawable.story_5_0, R.drawable.story_5_1, R.drawable.story_5_2, R.drawable.story_5_3,
					R.drawable.story_5_4, R.drawable.story_5_5, R.drawable.story_5_6, R.drawable.story_5_7,
					R.drawable.story_5_8, R.drawable.story_5_9, R.drawable.story_5_10, R.drawable.story_5_11,
					R.drawable.story_5_12, R.drawable.story_5_13, R.drawable.story_5_14, }, // 5
			{ R.drawable.story_6_0, R.drawable.story_6_1, R.drawable.story_6_2, R.drawable.story_6_3,
					R.drawable.story_6_4, R.drawable.story_6_5, R.drawable.story_6_6, R.drawable.story_6_7,
					R.drawable.story_6_8, R.drawable.story_6_9, R.drawable.story_6_10, R.drawable.story_6_11,
					R.drawable.story_6_12, R.drawable.story_6_13, R.drawable.story_6_14, }, // 6
			{ R.drawable.story_7_0, R.drawable.story_7_1, R.drawable.story_7_2, R.drawable.story_7_3,
					R.drawable.story_7_4, R.drawable.story_7_5, R.drawable.story_7_6, R.drawable.story_7_7,
					R.drawable.story_7_8, R.drawable.story_7_9, R.drawable.story_7_10, R.drawable.story_7_11,
					R.drawable.story_7_12, R.drawable.story_7_13, R.drawable.story_7_14, }, // 7
			{ R.drawable.story_8_0, R.drawable.story_8_1, R.drawable.story_8_2, R.drawable.story_8_3,
					R.drawable.story_8_4, R.drawable.story_8_5, R.drawable.story_8_6, R.drawable.story_8_7,
					R.drawable.story_8_8, R.drawable.story_8_9, R.drawable.story_8_10, R.drawable.story_8_11,
					R.drawable.story_8_12, R.drawable.story_8_13, R.drawable.story_8_14, }, // 8
			{ R.drawable.story_9_0, R.drawable.story_9_1, R.drawable.story_9_2, R.drawable.story_9_3,
					R.drawable.story_9_4, R.drawable.story_9_5, R.drawable.story_9_6, R.drawable.story_9_7,
					R.drawable.story_9_8, R.drawable.story_9_9, R.drawable.story_9_10, R.drawable.story_9_11,
					R.drawable.story_9_12, R.drawable.story_9_13, R.drawable.story_9_14, }, // 9
			{ R.drawable.story_10_0, R.drawable.story_10_1, R.drawable.story_10_2, R.drawable.story_10_3,
					R.drawable.story_10_4, R.drawable.story_10_5, R.drawable.story_10_6, R.drawable.story_10_7,
					R.drawable.story_10_8, R.drawable.story_10_9, R.drawable.story_10_10, R.drawable.story_10_11,
					R.drawable.story_10_12, R.drawable.story_10_13, R.drawable.story_10_14, }, // 10
			{ R.drawable.story_11_0, R.drawable.story_11_1, R.drawable.story_11_2, R.drawable.story_11_3,
					R.drawable.story_11_4, R.drawable.story_11_5, R.drawable.story_11_6, R.drawable.story_11_7,
					R.drawable.story_11_8, R.drawable.story_11_9, R.drawable.story_11_10, R.drawable.story_11_11,
					R.drawable.story_11_12, R.drawable.story_11_13, R.drawable.story_11_14, }, // 11
	};

	private static final int MAX_PAGE = 11;
	private static final int MAX_SCORE = 42;

	/*
	 * pos= 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
	 */

	private static final int[][] UPDATE_POS = { { 0, 8, 11, 8, 11, 4, 0, 7, 8, 7, 11, 11, 7, 4, 2 },// 0
			{ 0, 1, 4, 4, 4, 8, 8, 11, 11, 7, 7, 7, 4, 2, 7 },// 1
			{ 0, 8, 11, 8, 11, 11, 4, 11, 7, 4, 4, 1, 8, 4, 7 },// 2
			{ 0, 8, 11, 8, 11, 7, 7, 7, 4, 4, 8, 8, 4, 7, 7 },// 3
			{ 0, 8, 8, 7, 7, 2, 2, 11, 11, 7, 8, 4, 11, 4, 11 },// 4
			{ 0, 11, 11, 11, 7, 7, 7, 8, 4, 4, 7, 4, 7, 1, 1 },// 5
			{ 0, 4, 4, 1, 8, 7, 11, 2, 7, 11, 8, 11, 8, 7, 4 },// 6
			{ 0, 4, 11, 4, 1, 11, 6, 6, 6, 4, 8, 8, 4, 7, 7 },// 7
			{ 0, 7, 11, 8, 8, 11, 4, 7, 7, 7, 8, 4, 8, 11, 7 },// 8
			{ 0, 11, 7, 7, 11, 11, 11, 11, 11, 11, 4, 4, 8, 7, 11 },// 9
			{ 0, 4, 4, 8, 7, 11, 8, 7, 7, 1, 1, 2, 7, 4, 2 },// 10
			{ 0, 8, 8, 8, 8, 11, 11, 7, 7, 11, 11, 4, 8, 7, 3 },// 11
	};

	public static int getArrowPos(int idx, int week) {
		if (week > MAX_PAGE)
			week = week%(MAX_PAGE+1);
		return UPDATE_POS[week][idx];
	}

	public static int getPage(int score, int week) {
		if (week < 0)
			week = 0;
		else if (week > MAX_PAGE)
			week = week%(MAX_PAGE+1);
		if (score < 0)
			score = 0;
		else if (score > MAX_SCORE)
			score = MAX_SCORE;
		int idx = score * 14 / MAX_SCORE;
		return PAGE[week][idx];
	}

	public static int getPageByIdx(int idx, int week) {
		if (week < 0)
			week = 0;
		else if (week > MAX_PAGE)
			week = week%(MAX_PAGE+1);
		if (idx < 0)
			idx = 0;
		else if (idx > 14)
			idx = 14;
		return PAGE[week][idx];
	}

	public static int getPageIdx(int score, int week) {
		if (week < 0)
			week = 0;
		else if (week > MAX_PAGE)
			week = week%(MAX_PAGE+1);
		if (score < 0)
			score = 0;
		else if (score > MAX_SCORE)
			score = MAX_SCORE;
		int idx = score * 14 / MAX_SCORE;
		return idx;
	}

	public static int[] getAnimationBgs(Integer[] scores) {
		if (scores == null)
			return null;
		int[] pages = new int[scores.length];
		for (int i = 0; i < pages.length; ++i)
			pages[i] = getPage(scores[i], i);
		return pages;
	}

}
