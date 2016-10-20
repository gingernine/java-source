import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

//(nikkei needs)2010年7月16日以後のフォーマットでの日経平均先物の約定データ，最良気配値のデータ(限月調整済み)を抽出するプログラム
public class extract_nikkei {

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2011";
		String datadir = "\\nikkei_needs_data";

		File rfilepath = new File("G:\\日経平均先物" + datayear); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		for (int i = 0; i < filelist.length; i++) {
			if (filelist[i].isDirectory()) {
				continue;
			}

			// 取り出すデータに関する変数の定義
			String record1; // レコード種別1x
			int day; // 日付(月日のみ)
			String exchange; // 取引所コード
			String security; // 証券種別
			String code1; // 銘柄コード(a,b)
			String code2; // 銘柄コード(限月)
			String code3; // 銘柄コード(c,d)
			// String time; //時刻
			String record2; // レコード種別2
			// String price;//株価
			String quote; // 気配種別
			// String depth;//売買高 一枚単位
			// String bidtemp = "";//最良買気配値を一時保存する
			// String asktemp = "";//最良売気配値を一時保存する
			String fileID = ""; // 同一日のファイルを区別する．

			// 扱うファイル名の取得と書き出すファイルの指定
			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			String[] filename = filelist[i].getAbsolutePath().split("\\.");
			System.out.println(filename[1]);

			if (filename.length == 3) {
				fileID = filename[2];
			} else {
				fileID = "1";
			}
			File file = new File(currentdir + datayear + "\\raw\\" + filename[1] + "_" + fileID + "_.txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			// データが取られた年を知る
			int year = Integer.parseInt(filename[1].substring(0, 4));
			String yearcode = String.valueOf((year - 1985) % 10);// 10年サイクルで年を表す．1989年が4．
			String nextyearcode = String.valueOf((year + 1 - 1985) % 10);// 10年サイクルで年を表す．1989年が4．

			// 期近限月のデータを取り出す．
			String maturity[] = { "03", "06", "09", "12", "03" };// 限月
			int maturitydate[] = { 101, 301, 601, 901, 1201, 1300 };// 限月には期近限月のデータを除外する．
			int month = Integer.parseInt(filename[1].substring(4, 6));
			int term = 0; // データの日付と期近限月を合わせる．

			if (1 <= month && month <= 2) {
				term = 0;
			} else if (3 <= month && month <= 5) {
				term = 1;
			} else if (6 <= month && month <= 8) {
				term = 2;
			} else if (9 <= month && month <= 11) {
				term = 3;
			} else if (month == 12) {
				term = 4;
				// 12月のデータは翌年の3月が期近限月になる．
				yearcode = nextyearcode;
			}
			System.out.println(yearcode);

			while ((line = brtxt.readLine()) != null) { // データを一行ずつロードする

				record1 = line.substring(0, 1);
				day = Integer.parseInt(line.substring(8, 12));
				exchange = line.substring(13, 15);
				security = line.substring(15, 17);
				code1 = line.substring(21, 23);
				code2 = line.substring(23, 26);
				code3 = line.substring(26, 30);
				// time = line.substring(30,34);
				record2 = line.substring(34, 36);
				// price = line.substring(41,47);
				quote = line.substring(49, 52);
				// depth = line.substring(56,66);

				// 先物である条件を満たすデータを選別
				if ((record1.equals("3") && exchange.equals("21") && security.equals("20") && code1.equals("16")
						&& code3.equals("0018"))) {
					// 次に期近限月のデータを抽出する
					if (code2.equals(yearcode + maturity[term]) && day < maturitydate[term + 1]
							&& day >= maturitydate[term]) {
						// 最後に約定データ，最良気配値を収録する．
						if (record2.equals(" 0") || quote.equals("128") || quote.equals("  0")) {
							pw.println(line);
						}
					}
				}
			}
			brtxt.close();
			fr.close();
			pw.close();
		}
	}
}
