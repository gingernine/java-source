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

	private static double correlation(List<Integer> series, boolean span) {

		// 計算に必要な変数の定義
		int n = series.size(); // 系列のデータ数．
		double series1[] = new double[n]; // series に入っている系列を double 型に変換する．
		double series2[] = new double[n]; // series1 の系列から期を一つずらしたものを格納する．
		double mean1 = 0.0; // series1 の系列の平均値
		double mean2 = 0.0; // series2 の系列の平均値
		double var1 = 0.0; // series1 の系列の不偏分散
		double var2 = 0.0; // series2 の系列の不偏分散
		double cov = 0.0; // series1 と series2 の系列の不偏共分散

		// time2 は time1 よりも1期だけ進んでいる．
		int q = 0;
		int add = 1;
		int ext = 1;
		if (span) {
			// 時間間隔別のデータは2行1ペアで書き出す．これは time_span_parse 関数の描き方に因る．
			add = 2;
			ext = 2;
		}
		while(q < n - ext) {
			series1[q] = (double) ((Integer) series.get(q)).intValue();
			series2[q] = (double) ((Integer) series.get(q + 1)).intValue();
			q = q + add;
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

	private static Map<Integer, List<Integer>> time_span_parse(Map<Integer, int[]> timeseries, int[] timespan) {
		// 板の変化を変化時間感覚毎に分類する． 前に 上がった / 下がったとき，何秒後に 上がる / 下がる かを把握する．

		Map<Integer, List<Integer>> span_amount = new HashMap<Integer, List<Integer>>();
		List<Integer> serialnum = new ArrayList<Integer>(timeseries.keySet());
		Collections.sort(serialnum); // 時間に沿ってキーをソートする．
		int readtime = 0; // 読み込む時刻．
		int timetemp = 0; // 一つ前の変化時刻を保存．
		int timediff = 0; // 変化時間間隔を取得．
		for (int sn : serialnum) {
			readtime = timeseries.get(sn)[0];
			timediff = time_diff_in_seconds(timetemp, readtime);
			for (int ts : timespan) {
				if (timediff <= ts) {
					if (!span_amount.containsKey(ts)) {
						span_amount.put(ts, new ArrayList<Integer>());
					}
					span_amount.get(ts).add(timeseries.get(sn - 1)[1]); // snは0から始まるが初回のループで上のif文を通過することはないだろう．
					span_amount.get(ts).add(timeseries.get(sn)[1]);
					break;
				}
			}
			timetemp = readtime;
		}
		return span_amount;
	}

	private static void filewriter(File file1, File file2, List<Integer> bidseries,
			List<Integer> askseries, boolean span) {
		// 価格変化量の系列と，「次の価格変化量」の系列をファイルに書き出す．ついでに，散布図で見るために乱数項をつけた系列のファイルも作成する．

		try {
			PrintWriter pw1 = new PrintWriter(new BufferedWriter(new FileWriter(file1)));
			PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(file2)));
			pw1.println("bid_t,bid_t+1,ask_t,ask_t+1,");
			pw2.println("bid_t,bid_t+1,ask_t,ask_t+1,");
			int maxrow = 0;
			int minrow = 0;
			if (bidseries.size() < askseries.size()) {
				maxrow = askseries.size();
				minrow = bidseries.size();
			} else {
				maxrow = bidseries.size();
				minrow = askseries.size();
			}
			int r = 0;
			int add = 1;
			int ext = 1;
			if (span) {
				// 時間間隔別のデータは2行1ペアで書き出す．これは time_span_parse 関数の描き方に因る．
				add = 2;
				ext = 2;
			}
			while (r < maxrow - ext) {
				if (r < minrow - ext) {
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
				r = r + add;
			}
			pw1.close();
			pw2.close();
		} catch (IOException ignored) {
		}
	}

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2007";
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
		File file3 = new File(currentdir + datayear + writedir + "\\correlation\\");
		if (!file3.exists()) {
			file3.mkdirs();
		}
		file3 = new File(currentdir + datayear + writedir + "\\correlation\\correlation.csv");
		PrintWriter pw3 = new PrintWriter(new BufferedWriter(new FileWriter(file3)));
		pw3.println("date,bid,ask,");
		file1 = new File(currentdir + datayear + writedir + "\\time_span\\daily\\");
		if (!file1.exists()) {
			file1.mkdirs();
		}
		file2 = new File(currentdir + datayear + writedir + "\\time_span\\scattered\\");
		if (!file2.exists()) {
			file2.mkdirs();
		}

		int[] timespan = { 1, 2, 5, 15, 30, 60, 120, 300 }; //時間間隔(秒)
		PrintWriter[] printwriters = new PrintWriter[timespan.length];
		for (int t = 0; t < timespan.length; t++) {
			File f = new File(currentdir + datayear + writedir + "\\correlation\\correlation_" + timespan[t] + "_.csv");
			printwriters[t] = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			printwriters[t].println("date,bid,ask,");
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
			List<Integer> bidseries = new ArrayList<Integer>(); // 最良買い気配値
			List<Integer> askseries = new ArrayList<Integer>(); // 最良売り気配値
			Map<Integer, int[]> bid_timestamp = new HashMap<Integer, int[]>(); // <連番, {変化時刻, 変化量}>
			Map<Integer, int[]> ask_timestamp = new HashMap<Integer, int[]>(); // <連番, {変化時刻, 変化量}>
			Map<Integer, List<Integer>> bid_span_amount = new HashMap<Integer, List<Integer>>(); // <変化間隔, 対応するデータのリスト>
			Map<Integer, List<Integer>> ask_span_amount = new HashMap<Integer, List<Integer>>(); // <変化間隔, 対応するデータのリスト>

			int bidserial = 0; // 連想配列のキーとなる連番．
			int askserial = 0; // 連想配列のキーとなる連番．

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
					pw3.println(rfiledate + "," + correlation(bidseries, false) + "," + correlation(askseries, false) + ",");
					System.out.println("bid: " + correlation(bidseries, false) + ", ask: " + correlation(askseries, false));
					file1 = new File(currentdir + datayear + writedir + "\\daily\\" + rfiledate + ampm +"_.csv");
					file2 = new File(currentdir + datayear + writedir + "\\scattered\\" + rfiledate
									+ ampm + "_scattered_.csv");
					filewriter(file1, file2, bidseries, askseries, false);

					bid_span_amount = time_span_parse(bid_timestamp, timespan);
					ask_span_amount = time_span_parse(ask_timestamp, timespan);

					for (int t = 0; t < timespan.length; t++) {
						int ts = timespan[t];
						file1 = new File(currentdir + datayear + writedir + "\\time_span\\daily\\" + rfiledate
								+ ampm + "_" + ts +"_.csv");
						file2 = new File(currentdir + datayear + writedir + "\\time_span\\scattered\\" + rfiledate
								+ "_" + ts + "_scattered_.csv");
						if (bid_span_amount.containsKey(ts) && ask_span_amount.containsKey(ts)) {
							printwriters[t].println(rfiledate + "," + correlation(bid_span_amount.get(ts), true) + ","
									+ correlation(ask_span_amount.get(ts), true) + ",");
							filewriter(file1, file2, bid_span_amount.get(ts), ask_span_amount.get(ts), true);
						} else {
							if (bid_span_amount.containsKey(ts) && !ask_span_amount.containsKey(ts)) {
								printwriters[t].println(rfiledate + "," + correlation(bid_span_amount.get(ts), true) + ","
										+ correlation(new ArrayList<Integer>(), true) + ",");
								filewriter(file1, file2, bid_span_amount.get(ts), new ArrayList<Integer>(), true);
							} else if (!bid_span_amount.containsKey(ts) && ask_span_amount.containsKey(ts)) {
								printwriters[t].println(rfiledate + "," + correlation(new ArrayList<Integer>(), true) + ","
										+ correlation(ask_span_amount.get(ts), true) + ",");
								filewriter(file1, file2, new ArrayList<Integer>(), ask_span_amount.get(ts), true);
							} else {
								printwriters[t].println(rfiledate + "," + correlation(new ArrayList<Integer>(), true) + ","
										+ correlation(new ArrayList<Integer>(), true) + ",");
								filewriter(file1, file2, new ArrayList<Integer>(), new ArrayList<Integer>(), true);
							}
						}
					}
					bidseries = new ArrayList<Integer>(); // initialize
					askseries = new ArrayList<Integer>(); // initialize
					bid_timestamp = new HashMap<Integer, int[]>(); // initialize
					ask_timestamp = new HashMap<Integer, int[]>(); // initialize
					bidtemp = 0; // initialize
					asktemp = 0; // initialize
					bidserial = 0; // initialize
					askserial = 0; // initialize
					continuous = false;
				}

				if (continuous && line.split(",", -1)[2].equals("Quote")) {

					bidprice = Integer.parseInt(line.split(",", -1)[5]);
					askprice = Integer.parseInt(line.split(",", -1)[7]);

					if (bidprice != bidtemp && bidprice != 0) {
						if (bidtemp != 0) {
							bidseries.add(bidprice - bidtemp);
							int[] time_amount = {seconds, bidprice - bidtemp};
							bid_timestamp.put(bidserial, time_amount);
							bidserial++;
						}
						bidtemp = bidprice;
					}
					if (askprice != asktemp && askprice != 0) {
						if (asktemp != 0) {
							askseries.add(askprice - asktemp);
							int[] time_amount = {seconds, askprice - asktemp};
							ask_timestamp.put(askserial, time_amount);
							askserial++;
						}
						asktemp = askprice;
					}
				}
			}
			brtxt.close();
			fr.close();
		}
		pw3.close();
		for (int t = 0; t < timespan.length; t++) {
			printwriters[t].close();
		}
	}
}