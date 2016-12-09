import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/*
 * rawcsv_2フォルダのdailyデータから，ザラバにおいて
 * (1)最良買い気配値が最良売り気配値より大きい(>の関係)場合のデータ
 * (2)最良買い気配値が最良売り気配値と等しい(=の関係)場合のデータ
 * (3)最良気配値が0となる箇所
 * (4)最良気配が2ティック以上離れている箇所
 * を抽出する．
 */
public class error_detector {

	public static String lastTrade(File file, int closing) throws IOException {

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String l = "";
		String lasttrade = "";
		int time = 0;
		while ((l = br.readLine()) != null) {
			time = Integer.parseInt(l.split(",", -1)[1].split(":")[0] + l.split(",", -1)[1].split(":")[1]);
			if (l.split(",", -1)[2].equals("Trade") && time <= closing + 1) {
				lasttrade = l;
			}
		}
		fr.close();
		br.close();
		return lasttrade;
	}

	private static void removeEmptyFiles(String dirpath) throws IOException {
		/*
		 * ディレクトリ内の空ファイルを削除する．
		 */
		File file = new File(dirpath); // 読み込むファイルのディレクトリのパス．
		File[] filelist = file.listFiles(); // 読み込むファイル名を取得する．
		for (int i = 0; i < filelist.length; i++) {
			if (filelist[i].length() == 0) {
				filelist[i].delete();
			}
		}
	}

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String[] datayears = { "\\2006", "\\2007", "\\2008", "\\2009", "\\2010", "\\2011", "\\2012", "\\2013", "\\2014",
				"\\2015", "\\2016" };
		String datadir = "\\rawcsv_2\\daily";
		String writedir_more = "\\bid_more_than_ask_in_rawcsv_2"; // 最良買い気配値が最良売り気配値より大きい(>の関係)場合のデータ
		String writedir_equal = "\\bid_equal_to_ask_in_rawcsv_2"; // 最良買い気配値が最良売り気配値と等しい(=の関係)場合のデータ
		String writedir_na = "\\bid_or_ask_is_na_in_rawcsv_2"; // Quoteのデータの内，欠損箇所を記録する．
		String writedir_twotick = "\\bid_ask_spread_two_tick"; // 最良気配が2ティック以上離れている箇所を記録する．
		// String datadir = "\\price_or_depth_change\\daily";
		String rfilepath;
		String rfiledate; // 読み込むファイルの日付を格納する．

		for (String datayear : datayears) {
			File newdir = new File(currentdir + datayear + writedir_more + "\\daily");
			if (!newdir.exists())
				newdir.mkdirs();
			newdir = new File(currentdir + datayear + writedir_equal + "\\daily");
			if (!newdir.exists())
				newdir.mkdirs();
			newdir = new File(currentdir + datayear + writedir_na + "\\daily");
			if (!newdir.exists())
				newdir.mkdirs();
			newdir = new File(currentdir + datayear + writedir_twotick + "\\daily");
			if (!newdir.exists())
				newdir.mkdirs();

			File rfile = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
			File[] filelist = rfile.listFiles(); // 読み込むファイル名を取得する．

			serial_correlation sc = new serial_correlation(); // serial_correlationクラスのインスタンス，時間感覚の計算に使う．

			for (int i = 0; i < filelist.length; i++) {

				rfilepath = filelist[i].getAbsolutePath();
				rfiledate = new File(rfilepath).getName().substring(0, 8); // 読み込むファイルの日付を取得

				System.out.println(rfiledate);
				FileReader fr = new FileReader(filelist[i]);
				BufferedReader brtxt = new BufferedReader(fr);
				String line = "";

				// エラー回避のためダミーファイルを用意する
				File file1 = new File(currentdir + datayear + writedir_more + "\\daily\\" + rfiledate + "_.csv");
				PrintWriter pw_more = new PrintWriter(new BufferedWriter(new FileWriter(file1)));
				File file2 = new File(currentdir + datayear + writedir_equal + "\\daily\\" + rfiledate + "_.csv");
				PrintWriter pw_equal = new PrintWriter(new BufferedWriter(new FileWriter(file2)));
				File file3 = new File(currentdir + datayear + writedir_na + "\\daily\\" + rfiledate + "_.csv");
				PrintWriter pw_na = new PrintWriter(new BufferedWriter(new FileWriter(file3)));
				File file4 = new File(currentdir + datayear + writedir_twotick + "\\daily\\" + rfiledate + "_.csv");
				PrintWriter pw_twotick = new PrintWriter(new BufferedWriter(new FileWriter(file4)));

				// データ抽出に使う変数の定義．
				int bidprice = 0; // 最良買い気配値
				int askprice = 0; // 最良売り気配値
				String time = ""; // String型の時間.
				int inttime = 0; // int型の時間.
				int continuoustime = 0; // ザラバ時間を計測する．
				int finishtime = 0; // ザラバの終了時間
				String begin_two_tick_string = "";
				int begin_two_tick = 0; // 最良気配値が2ティック以上離れたところの時間を記録する．
				String[] closing = new String[2];
				if (Integer.parseInt(rfiledate) < 20110214) {
					if (Integer.parseInt(rfiledate) < 20090130 && (i == 0 || i == filelist.length - 1)) {
						closing[0] = lastTrade(filelist[i], 1110);
					} else {
						closing[0] = lastTrade(filelist[i], 1100);
						closing[1] = lastTrade(filelist[i], 1510);
					}
				} else {
					closing[0] = lastTrade(filelist[i], 1510);
				}
				boolean continuous = false; // ザラバを判定する．場中はtrue.
				boolean TickIsTwo = false; // 最良気配値が2ティック以上離れている時間はtrue.
				int t = 0; // closing配列の番号を指定．ザラバ時間の計算に使う．

				while ((line = brtxt.readLine()) != null) {

					time = line.split(",", -1)[1];

					try {
						inttime = Integer.parseInt(time.split(":")[0]
								+ time.split(":")[1]
								+ time.split(":")[2]);
					} catch (Exception e) {
						inttime = Integer.parseInt(time.split(":")[0]
								+ time.split(":")[1] + "00");
					}

					if (line.split(",", -1)[9].equals("  1")) {
						try {
							finishtime = Integer.parseInt(closing[t].split(",", -1)[1].split(":")[0]
									+ closing[t].split(",", -1)[1].split(":")[1]
									+ closing[t].split(",", -1)[1].split(":")[2]);
						} catch (Exception e) {
							finishtime = Integer.parseInt(closing[t].split(",", -1)[1].split(":")[0]
									+ closing[t].split(",", -1)[1].split(":")[1] + "00");
						}
						continuoustime = sc.time_diff_in_seconds(inttime, finishtime);
						continuous = true;
					}
					if (Arrays.asList(closing).contains(line)) {
						t = 1;
						continuous = false;
						TickIsTwo = false;
					}

					if (continuous) {
						// ザラバ時間のみデータ抽出．

						if (line.split(",", -1)[2].equals("Quote")) {

							bidprice = Integer.parseInt(line.split(",", -1)[5]);
							askprice = Integer.parseInt(line.split(",", -1)[7]);

							if (TickIsTwo && askprice - bidprice == 10) {
								// 2ティック以上離れていたのが1ティックになったら時間計測終了してファイルに書き込む．
								pw_twotick.println(begin_two_tick_string + "," + time + ","
										+ sc.time_diff_in_seconds(begin_two_tick, inttime)
										+ "," + continuoustime);
								TickIsTwo = false;
							}

							if (bidprice*askprice == 0) {
								// 価格が0または厚みが0の箇所を記録する．
								pw_na.println(line);
							}
							if (askprice - bidprice > 10 && bidprice*askprice != 0) {
								// 最良気配値が2ティック以上離れている箇所を記録する．
								if (!TickIsTwo) {
									begin_two_tick_string = time;
									begin_two_tick = inttime;
									TickIsTwo = true;
								}
							}
							if (bidprice > askprice) {
								// 最良買い気配値 > 最良売り気配値の場合に記録する
								pw_more.println(line);
							} else if (bidprice == askprice) {
								// 最良買い気配値 == 最良売り気配値の場合に記録する．
								pw_equal.println(line);
							}
						}
					}
				}
				brtxt.close();
				fr.close();
				pw_more.close();
				pw_equal.close();
				pw_na.close();
				pw_twotick.close();
			}
			removeEmptyFiles(currentdir + datayear + writedir_more + "\\daily");
			removeEmptyFiles(currentdir + datayear + writedir_equal + "\\daily");
			removeEmptyFiles(currentdir + datayear + writedir_na + "\\daily");
			removeEmptyFiles(currentdir + datayear + writedir_twotick + "\\daily");
		}
	}
}
