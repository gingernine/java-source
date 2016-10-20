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

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2014";
		String datadir = "\\raw"; // 2014
		// String datadir = "\\raw_daily"; //2006,2007
		int sep = 3; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[2014]
		// int sep = 4; //ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[2006,2007]
		String writedir = "\\rawcsv\\daily_seperated\\";

		File rfilepath = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		// int startrow = 0; //読み込むはじめの行．[2006,2007]
		int startrow = 1; // 読み込むはじめの行．[2014]
		for (int i = startrow; i < filelist.length; i++) {

			System.out.println(filelist[i]);

			// 取り出すデータに関する変数の定義
			String wline; // ファイルに書き込む行を作る．
			// String record1; //レコード種別1x
			int date; // 日付
			// String exchange; //取引所コード
			// String security; //証券種別
			// String code1; //銘柄コード(a,b)
			// String code2; //銘柄コード(限月)
			// String code3; //銘柄コード(c,d)
			String time; // 時刻
			String record2; // レコード種別2
			String second; // 秒
			String price;// 株価
			String kind; // 約定データは約定種別を表し，気配種別は最良気配を表す．
			String volume;// 売買高 一枚単位
			String bidtemp = "";// 最良買気配値の初期値 兼 一時保存
			String asktemp = "";// 最良売気配値の初期値 兼 一時保存
			String biddepth = ""; // 最良買気配数量の一時保存
			String askdepth = ""; // 最良売気配数量の一時保存
			// String tradeprice = ""; //約定データの取引価格の一時保存
			// String tradevolume = ""; //約定データの取引数量(枚)の一時保存
			int time_second; // 時刻＋秒数を数値化したもの
			// boolean just_before_trade = false; //約定データが最良気配値変化の直前のものであるか．

			// 扱うファイル名の取得と書き出すファイルの指定
			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			String[] filename = filelist[i].getAbsolutePath().split("\\_");
			int length = filename[sep].length();
			String fileID = filename[sep].substring(length - 8, length);
			System.out.println(fileID);

			File file = new File(currentdir + datayear + writedir + fileID + "_" + filename[sep + 1] + "_.csv");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			while ((line = brtxt.readLine()) != null) {

				// record1 = line.substring(0,1);
				date = Integer.parseInt(line.substring(4, 12));
				// exchange = line.substring(13,15);
				// security = line.substring(15,17);
				// code1 = line.substring(21,23);
				// code2 = line.substring(23,26);
				// code3 = line.substring(26,30);
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
						wline = fileID + "," + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + second
								+ ",Trade," + price + "," + volume + ",,,,," + kind + ",";
						pw.println(wline);
					} else if (kind.equals("128")) {
						wline = fileID + "," + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + second
								+ ",Quote,,," + price + "," + volume + ",,,,";
						pw.println(wline);
						// bidtemp = price;
						// bidnum = number; //最良値が変化していない場合は数量のみの変化を記録する．
					} else if (kind.equals("  0")) {
						wline = fileID + "," + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + second
								+ ",Quote,,," + ",," + price + "," + volume + ",,";
						pw.println(wline);
						// asktemp = price;
						// asknum = number; //最良値が変化していない場合は数量のみの変化を記録する．
					}
				}
			}
			brtxt.close();
			fr.close();
			pw.close();
		}
	}
}
