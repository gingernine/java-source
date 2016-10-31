import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 売り/買い気配の価格の変動について系列相関を計算する．
public class serial_correlation {

	private static double correlation(ArrayList<Integer> series) {
		// num で価格の系列を取得する．取得した系列について，期を一つずらした系列を作り，numの系列との相関(系列相関)を計算する．

		// 計算に必要な変数の定義
		int n = series.size() - 1; // 系列のデータ数．
		double series1[] = new double[n]; // num に入っている系列を double 型に変換する．
		double series2[] = new double[n]; // time1 の系列から期を一つずらしたものを格納する．
		double mean1 = 0.0; // time1 の系列の平均値
		double mean2 = 0.0; // time2 の系列の平均値
		double var1 = 0.0; // time1 の系列の不偏分散
		double var2 = 0.0; // time2 の系列の不偏分散
		double cov = 0.0; // time1 と time2 の系列の不偏共分散

		// time2 は time1 よりも1期だけ進んでいる．
		for (int j = 0; j < n; j++) {
			series1[j] = (double) ((Integer) series.get(j)).intValue();
			series2[j] = (double) ((Integer) series.get(j + 1)).intValue();
		}

		// 平均値を計算する．
		double sum1 = 0.0;
		double sum2 = 0.0;
		for (int j = 0; j < n; j++) {
			sum1 = sum1 + series1[j];
			sum2 = sum2 + series2[j];
		}
		mean1 = sum1 / n;
		mean2 = sum2 / n;

		// 分散を計算する．(ただし n で割る必要はない)
		for (int j = 0; j < n; j++) {
			var1 = var1 + (series1[j] - mean1) * (series1[j] - mean1);
			var2 = var2 + (series2[j] - mean2) * (series2[j] - mean2);
		}

		// 共分散を計算する．(ただし n で割る必要はない)
		for (int j = 0; j < n; j++) {
			cov = cov + (series1[j] - mean1) * (series2[j] - mean2);
		}

		return cov / Math.sqrt(var1 * var2);
	}

	private static int time_diff_in_seconds(int timeBefore, int timeAfter) {
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

	private static Map<Integer, List<Integer>> time_span_parse(Map<Integer, Integer> timeseries) {
		// 板の変化を変化時間感覚毎に分類する． 前に 上がった / 下がったとき，何秒後に 上がる / 下がる かを把握する．

		int[] timespan = { 1, 2, 5, 15, 30, 60, 120, 300 }; //時間間隔(秒)
		Map<Integer, List<Integer>> span_amount = new HashMap<Integer, List<Integer>>();
		List<Integer> timelist = new ArrayList<Integer>(timeseries.keySet());
		Collections.sort(timelist); // 時間に沿ってキーをソートする．
		int timetemp = 0; // 一つ前の変化時刻を保存．
		int timediff = 0; // 変化時間間隔を取得．
		for (int t : timelist) {
			timediff = time_diff_in_seconds(timetemp, t);
			for (int ts : timespan) {
				if (timediff <= ts) {
					if (span_amount.containsKey(ts)) {
						span_amount.put(ts, new ArrayList<Integer>());
					}
					span_amount.get(ts).add(timeseries.get(timetemp));
					span_amount.get(ts).add(timeseries.get(t));
					break;
				}
			}
			timetemp = t;
		}
		return span_amount;
	}

	private static void filewriter1(File file1, File file2, ArrayList<Integer> bidseries,
			ArrayList<Integer> askseries) {
		// 価格変化量の系列と，「次の価格変化量」の系列をファイルに書き出す．ついでに，散布図で見るために乱数項をつけた系列のファイルも作成する．

		try {
			PrintWriter pw1 = new PrintWriter(new BufferedWriter(new FileWriter(file1)));
			PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(file2)));
			pw1.println("bid_t,bid_t+1,ask_t,ask_t+1,");
			pw2.println("bid_t,bid_t+1,ask_t,ask_t+1,");
			int maxrow = 0;
			int minrow = 0;
			if (bidseries.size() < askseries.size()) {
				maxrow = askseries.size() - 1;
				minrow = bidseries.size() - 1;
			} else {
				maxrow = bidseries.size() - 1;
				minrow = askseries.size() - 1;
			}
			for (int r = 0; r < maxrow; r++) {
				if (r < minrow) {
					pw1.println(bidseries.get(r) + "," + bidseries.get(r + 1) + "," + askseries.get(r) + ","
							+ askseries.get(r + 1) + ",");
					pw2.println((((Integer) bidseries.get(r)).intValue() + 5 * (2 * Math.random() - 1)) + ","
							+ (((Integer) bidseries.get(r + 1)).intValue() + 5 * (2 * Math.random() - 1)) + ","
							+ (((Integer) askseries.get(r)).intValue() + 5 * (2 * Math.random() - 1)) + ","
							+ (((Integer) askseries.get(r + 1)).intValue() + 5 * (2 * Math.random() - 1)) + ",");
				} else {
					if (bidseries.size() < askseries.size()) {
						pw1.println(",," + askseries.get(r) + "," + askseries.get(r + 1) + ",");
						pw2.println(",," + (((Integer) askseries.get(r)).intValue() + 5 * (2 * Math.random() - 1)) + ","
								+ (((Integer) askseries.get(r + 1)).intValue() + 5 * (2 * Math.random() - 1)) + ",");
					} else {
						pw1.println(bidseries.get(r) + "," + bidseries.get(r + 1) + ",,,");
						pw2.println((((Integer) bidseries.get(r)).intValue() + 5 * (2 * Math.random() - 1)) + ","
								+ (((Integer) bidseries.get(r + 1)).intValue() + 5 * (2 * Math.random() - 1)) + ",,,");
					}
				}
			}
			pw1.close();
			pw2.close();
		} catch (IOException ignored) {
		}
	}

	private static void filewriter2(File file, Map<Integer, Integer> bidseries, Map<Integer, Integer> askseries) {
		// 最後に変動量の時系列グラフを書くためのファイルを作成する．
		// 時間は9時ちょうどから15時10分までで統一．

		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println("time,bid_change_amount,ask_change_amount");
			int hour = 90000;
			int minute = 60;
			String stringtime = ""; // 時間を":"で連結したもの．
			String bid_change_amount = "";
			String ask_change_amount = "";
			for (int h = 0; h < 7; h++) {
				hour = hour + h * 10000;
				if (h == 6) {
					minute = 10; // 15時台は10分までに制限
				}
				for (int m = 0; m < minute; m++) {
					for (int s = 0; m < 60; m++) {
						bid_change_amount = "";
						ask_change_amount = "";
						stringtime = String.valueOf(hour + m * 100 + s).substring(0, 2) + ":"
								+ String.valueOf(hour + m * 100 + s).substring(3, 4) + ":"
								+ String.valueOf(hour + m * 100 + s).substring(4, 6);
						if (bidseries.containsKey((hour + m * 100 + s))) {
							bid_change_amount = String.valueOf(bidseries.get((hour + m * 100 + s)));
						}
						if (askseries.containsKey((hour + m * 100 + s))) {
							ask_change_amount = String.valueOf(askseries.get((hour + m * 100 + s)));
						}
						pw.println(stringtime + "," + bid_change_amount + "," + ask_change_amount + ",");
					}
				}
			}
			pw.close();
		} catch (IOException ignored) {
		}
	}

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2006";
		String datadir = "\\rawcsv_2\\daily";
		String writedir = "\\correlation";
		// String datadir = "\\price_or_depth_change\\daily";
		int sep = 4; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[rawcsv_2]
		// int sep = 6;
		// // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[price_or_depth_change]
		String rfilename;
		String rfiledate; // 読み込むファイルの日付を格納する．

		File rfilepath = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		// 書き出すファイルと，しまうフォルダを用意する．
		File file1 = new File(currentdir + datayear + writedir + "\\daily\\");
		if (!file1.exists()) {
			file1.mkdirs();
		}
		File file2 = new File(currentdir + datayear + writedir + "\\scattered\\");
		if (!file2.exists()) {
			file2.mkdirs();
		}
		File file3 = new File(currentdir + datayear + writedir + "\\correlation.csv");
		PrintWriter pw3 = new PrintWriter(new BufferedWriter(new FileWriter(file3)));
		pw3.println("date,bid,ask,");
		File file4 = new File(currentdir + datayear + writedir + "\\time_span\\");
		if (!file4.exists()) {
			file4.mkdirs();
		}

		for (int i = 0; i < filelist.length; i++) {

			rfilename = filelist[i].getAbsolutePath();
			int pathlength = rfilename.split("\\_")[sep].length();
			rfiledate = rfilename.split("\\_")[sep].substring(pathlength - 8, pathlength); // 読み込むファイルの日付を取得

			System.out.println(rfiledate);
			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			// データ抽出に使う変数の定義．
			int bidprice = 0;
			int askprice = 0;
			int bidtemp = 0; // 買い気配値を一時保存
			int asktemp = 0; // 売り気配値を一時保存
			ArrayList<Integer> bidseries = new ArrayList<Integer>(); // 最良買い気配値
			ArrayList<Integer> askseries = new ArrayList<Integer>(); // 最良売り気配値
			Map<Integer, Integer> bidtime = new HashMap<Integer, Integer>(); // <変化時刻, 変化量>
			Map<Integer, Integer> asktime = new HashMap<Integer, Integer>(); // <変化時刻, 変化量>

			String time = ""; // 時刻
			int seconds = 0; // 秒単位時刻
			boolean hasSeconds = true;
			if (Integer.parseInt(rfiledate) < 20060227) {
				hasSeconds = false; // 2006年2月26日以前は秒のデータがない．
			}
			String[] closing = new String[2];
			if (Integer.parseInt(rfiledate) < 20110214) {
				if (Integer.parseInt(rfiledate) < 20090130 && (i == 0 || i == filelist.length - 1)) {
					closing[0] = "1110";
				} else {
					closing[0] = "1100";
					closing[1] = "1510";
				}
			} else {
				closing[0] = "1510";
			}
			boolean continuous = false; // ザラバを判定する．場中はtrue.
			boolean morning = true; // 前場と後場で分かれているならtrue.ファイル分割のため．
			String ampm = ""; // 前場と後場で別れている場合はファイル名を morning/afternoon で分ける．

			while ((line = brtxt.readLine()) != null) {

				time = line.split(",", -1)[1].split(":")[0] + line.split(",", -1)[1].split(":")[1];
				if (hasSeconds) {
					seconds = Integer.parseInt(time + line.split(",", -1)[1].split(":")[2]);
				} else {
					seconds = Integer.parseInt(time + "00");
				}

				if (line.split(",", -1)[9].equals("  1")) {
					continuous = true;
				}

				if (Arrays.asList(closing).contains(time) && line.split(",", -1)[2].equals("Trade")) {
					// for loop でここに入る場合は，場が終わったときである．(系列相関の計算とファイルへの書き出しは前場後場で分けている．)

					if ((Integer.parseInt(rfiledate) < 20090130 && (i == 0 || i == filelist.length - 1))
							|| Integer.parseInt(rfiledate) >= 20110214) {
						ampm = "";
					} else if (morning) {
						ampm = "_morning";
						morning = false;
					} else {
						ampm = "_afternoon";
					}
					pw3.println(rfiledate + "," + correlation(bidseries) + "," + correlation(askseries) + ",");
					System.out.println("bid: " + correlation(bidseries) + ", ask: " + correlation(askseries));
					file1 = new File(currentdir + datayear + writedir + "\\daily\\" + rfiledate + ampm +"_.csv");
					file2 = new File(currentdir + datayear + writedir + "\\scattered\\" + rfiledate
									+ ampm + "_scattered_.csv");
					filewriter1(file1, file2, bidseries, askseries);
					bidseries = new ArrayList<Integer>(); // initialize
					askseries = new ArrayList<Integer>(); // initialize
					bidtemp = 0; // initialize
					asktemp = 0; // initialize
					continuous = false;
				}

				if (continuous && line.split(",", -1)[2].equals("Quote")) {

					bidprice = Integer.parseInt(line.split(",", -1)[5]);
					askprice = Integer.parseInt(line.split(",", -1)[7]);

					if (bidprice != bidtemp && bidprice != 0) {
						if (bidtemp != 0) {
							bidseries.add(bidprice - bidtemp);
							bidtime.put(seconds, bidprice - bidtemp);
						}
						bidtemp = bidprice;
					}
					if (askprice != asktemp && askprice != 0) {
						if (asktemp != 0) {
							askseries.add(askprice - asktemp);
							asktime.put(seconds, askprice - asktemp);
						}
						asktemp = askprice;
					}
				}
			}
			brtxt.close();
			fr.close();
		}
		pw3.close();
	}
}