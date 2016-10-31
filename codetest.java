import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// test code.
public class codetest {

	private static int timeDiffInSeconds(int timeBefore, int timeAfter) {
		// 秒単位時刻(初めの2桁がhour, 次の2桁がminute, 最後の2桁がsecond となる数値データ)の差を 秒 で返す．
		int secondB = timeBefore % 100;
		int secondA = timeAfter % 100;
		int minuteB = (timeBefore % 10000 - secondB) / 100;
		int minuteA = (timeAfter % 10000 - secondA) / 100;
		int hourB = (timeBefore - minuteB * 100 - secondB) / 10000;
		int hourA = (timeAfter - minuteA * 100 - secondA) / 10000;
		int diff = (hourA - hourB) * 3600 + (minuteA - minuteB) * 60 + (secondA - secondB);
		return diff;
	}

	private static void sorttest() {
		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < 10; i++) {
			list.add(Math.random());
		}
		for (int i = 0; i < 10; i++) {
			System.out.println(list.get(i));
		}
		Collections.sort(list);
		System.out.println("----------sorted-----------");
		for (int i = 0; i < 10; i++) {
			System.out.println(list.get(i));
		}
	}

	private static void arraytest() {
		int i = 0;
		int j = 0;

		Map<Integer, int[]> map = new HashMap<>();
		for (int r = 0; r < 10; r++) {
			int[] array = {i , j};
			map.put(r, array);
			i = i + r;
			System.out.println(map);
		}
	}

	public static void main(String[] args) {
		// System.out.println(timeDiffInSeconds(113001,113005));
		// sorttest();
		arraytest();
	}
}
