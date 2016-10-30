import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

//前回：(nikkei needs)2010年7月16日以後のフォーマットでの日経平均先物の約定データ，最良気配値のデータ(限月調整済み)を抽出するプログラム
//今回：上で抽出したデータを用いて，ロイター社のコンマ秒データの形式に近いものを作成する．
//約定データは約定種別も記録する．
public class raw_to_csv {

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2016";
		String datadir = "\\raw"; // [ 2009 ~ 2016 ]
		// String datadir = "\\raw_daily"; // [ 2006 ~ 2008 ]
		int sep = 3; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[ 2009 ~ 2016 ]
		// int sep = 4; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[ 2006 ~ 2008 ]
		String writedir = "\\rawcsv\\daily_seperated\\";

		File rfilepath = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		for (int i = 0; i < filelist.length; i++) {

			System.out.println(filelist[i]);

			// 取り出すデータに関する変数の定義
			String wline; // ファイルに書き込む行を作る．
			int date; // 日付
			String time; // 時刻
			String record2; // レコード種別2
			String second; // 秒
			String price;// 株価
			String kind; // 約定データは約定種別を表し，気配種別は最良気配を表す．
			String volume;// 売買高 一枚単位
			int time_second; // 時刻＋秒数を数値化したもの．

			// 扱うファイル名の取得と書き出すファイルの指定
			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			String[] filename = filelist[i].getAbsolutePath().split("\\_");
			int length = filename[sep].length();
			String rfiledate = filename[sep].substring(length - 8, length);
			System.out.println(rfiledate);

			File file = new File(currentdir + datayear + writedir + rfiledate + "_" + filename[sep + 1] + "_.csv");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			while ((line = brtxt.readLine()) != null) {

				date = Integer.parseInt(line.substring(4, 12));
				time = line.substring(30, 34);
				record2 = line.substring(34, 36);
				second = line.substring(36, 38);
				price = line.substring(41, 47);
				kind = line.substring(49, 52);
				volume = line.substring(56, 66);

				if (date < 20060227) {
					// 2006年2月26日までは秒のデータがない．
					time_second = Integer.parseInt(time + "00");
				} else {
					time_second = Integer.parseInt(time + second);
				}

				// 9:00:00以降はデータを収録する．
				// 収録するデータ行はcsv形式で作成する.
				if (time_second >= 90000 && time_second <= 151500) {
					if (record2.equals(" 0")) {
						wline = rfiledate + "," + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + second
								+ ",Trade," + price + "," + volume + ",,,,," + kind + ",";
						pw.println(wline);
					} else if (kind.equals("128")) {
						wline = rfiledate + "," + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + second
								+ ",Quote,,," + price + "," + volume + ",,,,";
						pw.println(wline);
					} else if (kind.equals("  0")) {
						wline = rfiledate + "," + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + second
								+ ",Quote,,," + ",," + price + "," + volume + ",,";
						pw.println(wline);
					}
				}
			}
			brtxt.close();
			fr.close();
			pw.close();
		}
	}
}
