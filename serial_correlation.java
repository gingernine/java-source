import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

// 売り/買い気配の価格の変動について系列相関を計算する．
public class serial_correlation {

	private static double correlation(ArrayList series) {
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

	private static void writefile(File file1, File file2, ArrayList bidseries, ArrayList askseries) {

		try {
			PrintWriter pw1 = new PrintWriter(new BufferedWriter(new FileWriter(file1)));
			PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(file2)));
			pw1.println("bid_t,bid_t+1,ask_t,ask_t+1,");
			pw2.println("bid_t,bid_t+1,ask_t,ask_t+1,");
			for (int r = 0; r < Math.max(bidseries.size(), askseries.size()) - 1; r++) {
				try {
					pw1.println(bidseries.get(r) + "," + bidseries.get(r + 1) + "," + askseries.get(r) + ","
							+ askseries.get(r + 1) + ",");
					pw2.println((((Integer) bidseries.get(r)).intValue() + 5 * (2 * Math.random() - 1)) + ","
							+ (((Integer) bidseries.get(r + 1)).intValue() + 5 * (2 * Math.random() - 1)) + ","
							+ (((Integer) askseries.get(r)).intValue() + 5 * (2 * Math.random() - 1)) + ","
							+ (((Integer) askseries.get(r + 1)).intValue() + 5 * (2 * Math.random() - 1)) + ",");
				} catch (IndexOutOfBoundsException e) {
					if (bidseries.size() < askseries.size()) {
						pw1.println(",,," + askseries.get(r) + "," + askseries.get(r + 1) + ",");
						pw2.println(",,," + (((Integer) askseries.get(r)).intValue() + 5 * (2 * Math.random() - 1))
								+ "," + (((Integer) askseries.get(r + 1)).intValue() + 5 * (2 * Math.random() - 1))
								+ ",");
					} else {
						pw1.println(bidseries.get(r) + "," + bidseries.get(r + 1) + ",,,");
						pw2.println((((Integer) bidseries.get(r)).intValue() + 5 * (2 * Math.random() - 1)) + ","
								+ (((Integer) bidseries.get(r + 1)).intValue() + 5 * (2 * Math.random() - 1)) + ",,,");
					}
				}
			}
			pw1.close();
			pw2.close();
		} catch (IOException e) {
			;
		}
	}

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2006";
		String datadir = "\\rawcsv_2\\daily";
		String writedir = "\\correlation";
		// エラーを発見したときのみファイルを作成
		// File newdir = new File(currentdir + datayear + writedir + "\\daily");
		// String datadir = "\\price_or_depth_change\\daily";
		int sep = 4; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[rawcsv_2]
		// int sep = 6;
		// // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[price_or_depth_change]
		String rfilename;
		String rfiledate; // 読み込むファイルの日付を格納する．

		File rfilepath = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		// 書き出すファイルをしまうフォルダを用意する．
		File file1 = new File(currentdir + datayear + writedir + "\\daily\\");
		if (!file1.exists()) {
			file1.mkdirs();
		}
		File file2 = new File(currentdir + datayear + writedir + "\\scattered\\");
		if (!file2.exists()) {
			file2.mkdirs();
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

			String time = ""; // 時刻
			String[] closing = new String[2];
			if (Integer.parseInt(rfiledate) < 20110214 && !(i == 0 || i == filelist.length - 1)) {
				closing[0] = "1100";
				closing[1] = "1510";
			} else if (Integer.parseInt(rfiledate) < 20090130 && (i == 0 || i == filelist.length - 1)) {
				closing[0] = "1110";
			} else {
				closing[0] = "1510";
			}
			boolean continuous = false; // ザラバを判定する．場中はtrue.
			boolean morning = true; // 前場と後場で分かれているならtrue.ファイル分割のため．

			while ((line = brtxt.readLine()) != null) {

				time = line.split(",", -1)[1].split(":")[0] + line.split(",", -1)[1].split(":")[1];
				if (line.split(",", -1)[9].equals("  1")) {
					continuous = true;
				}
				if (Arrays.asList(closing).contains(time) && line.split(",", -1)[2].equals("Trade")) {
					if (Integer.parseInt(rfiledate) < 20110214 && !(i == 0 || i == filelist.length - 1)) {
						if (morning) {
							System.out.println("bid: " + correlation(bidseries) + ", ask: " + correlation(askseries));
							file1 = new File(
									currentdir + datayear + writedir + "\\daily\\" + rfiledate + "_morning_.csv");
							file2 = new File(currentdir + datayear + writedir + "\\scattered\\" + rfiledate
									+ "_morning_scattered_.csv");
							writefile(file1, file2, bidseries, askseries);
							bidseries = new ArrayList<Integer>(); // initialize
							askseries = new ArrayList<Integer>(); // initialize
							morning = false;
						} else {
							System.out.println("bid: " + correlation(bidseries) + ", ask: " + correlation(askseries));
							file1 = new File(
									currentdir + datayear + writedir + "\\daily\\" + rfiledate + "_afternoon_.csv");
							file2 = new File(currentdir + datayear + writedir + "\\scattered\\" + rfiledate
									+ "_afternoon_scattered_.csv");
							writefile(file1, file2, bidseries, askseries);
						}
					} else {
						System.out.println("bid: " + correlation(bidseries) + ", ask: " + correlation(askseries));
						file1 = new File(currentdir + datayear + writedir + "\\daily\\" + rfiledate + "_.csv");
						file2 = new File(
								currentdir + datayear + writedir + "\\scattered\\" + rfiledate + "_scattered_.csv");
						writefile(file1, file2, bidseries, askseries);
					}
					continuous = false;
				}

				if (continuous && line.split(",", -1)[2].equals("Quote")) {

					bidprice = Integer.parseInt(line.split(",", -1)[5]);
					askprice = Integer.parseInt(line.split(",", -1)[7]);

					if (bidprice != bidtemp && bidprice != 0) {
						if (bidtemp != 0) {
							bidseries.add(bidprice - bidtemp);
						}
						bidtemp = bidprice;
					}
					if (askprice != asktemp && askprice != 0) {
						if (asktemp != 0) {
							askseries.add(askprice - asktemp);
						}
						asktemp = askprice;
					}
				}
			}
			brtxt.close();
			fr.close();
		}
	}
}