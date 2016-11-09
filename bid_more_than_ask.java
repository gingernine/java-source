import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

//前回まで：(nikkei needs)2010年7月16日以後のフォーマットでの日経平均先物の約定データ，最良気配値のデータ(限月調整済み)を抽出するプログラム
//		上で抽出したデータを用いて，ロイター社のコンマ秒データの形式に近いものを作成する．
//
//今回:rawcsvフォルダのdailyデータから，ザラバにおいて最良買い気配値が最良売り気配値より大きい(>の関係)場合のデータを抽出する．
public class bid_more_than_ask {

	private static String lastTrade(File file, int closing) throws IOException {

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

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String[] datayears = { "\\2006", "\\2007", "\\2008", "\\2009", "\\2010", "\\2011", "\\2012", "\\2013", "\\2014",
				"\\2015", "\\2016" };
		String datadir = "\\rawcsv_2\\daily";
		String writedir_more = "\\bid_more_than_ask_in_rawcsv_2"; // エラーを発見したときのみファイルを作成
		String writedir_equal = "\\bid_equal_to_ask_in_rawcsv_2"; // エラーを発見したときのみファイルを作成
		String writedir_na = "\\bid_or_ask_is_na_in_rawcsv_2"; // Quoteのデータの内，欠損箇所を記録する．
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

			File rfile = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
			File[] filelist = rfile.listFiles(); // 読み込むファイル名を取得する．

			for (int i = 0; i < filelist.length; i++) {

				rfilepath = filelist[i].getAbsolutePath();
				rfiledate = new File(rfilepath).getName().substring(0, 8); // 読み込むファイルの日付を取得

				System.out.println(rfiledate);
				FileReader fr = new FileReader(filelist[i]);
				BufferedReader brtxt = new BufferedReader(fr);
				String line = "";

				// エラー回避のためダミーファイルを用意する
				File file = new File("C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\dummy\\dummy.txt");
				PrintWriter pw_more = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				PrintWriter pw_equal = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				PrintWriter pw_na = new PrintWriter(new BufferedWriter(new FileWriter(file)));

				// データ抽出に使う変数の定義．
				int bidprice = 0; // 最良買い気配値
				int askprice = 0; // 最良売り気配値
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
				boolean writing_more = false; // in writing: 1, not in writing:
												// 0.
				boolean writing_equal = false; // in writing: 1, not in writing:
												// 0.

				while ((line = brtxt.readLine()) != null) {

					if (line.split(",", -1)[9].equals("  1")) {
						continuous = true;
					}
					if (Arrays.asList(closing).contains(line)) {
						continuous = false;
					}

					if (continuous) {
						// ザラバ時間のみデータ抽出．

						if (line.split(",", -1)[2].equals("Quote")) {

							bidprice = Integer.parseInt(line.split(",", -1)[5]);
							askprice = Integer.parseInt(line.split(",", -1)[7]);

							if (bidprice > askprice) {
								// 最良買い気配値 > 最良売り気配値の場合に記録する．
								file = new File(
										currentdir + datayear + writedir_more + "\\daily\\" + rfiledate + "_.csv");
								if (!writing_more) {
									pw_more.close();
									pw_more = new PrintWriter(new BufferedWriter(new FileWriter(file)));
									writing_more = true;
								}
								pw_more.println(line);
							} else if (bidprice == askprice) {
								// 最良買い気配値 == 最良売り気配値の場合に記録する．
								file = new File(
										currentdir + datayear + writedir_equal + "\\daily\\" + rfiledate + "_.csv");
								if (!writing_equal) {
									pw_equal.close();
									pw_equal = new PrintWriter(new BufferedWriter(new FileWriter(file)));
									writing_equal = true;
								}
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
			}
		}
	}
}
