import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

//前回まで：(nikkei needs)2010年7月16日以後のフォーマットでの日経平均先物の約定データ，最良気配値のデータ(限月調整済み)を抽出するプログラム
//		上で抽出したデータを用いて，ロイター社のコンマ秒データの形式に近いものを作成する．
//
//今回:rawcsvフォルダのdailyデータから最良買い気配値が最良売り気配値よりも大きい(>の関係)場合のデータを抽出し，その個数も把握する．
public class error_volume {

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String[] datayears = { "\\2006", "\\2007", "\\2008", "\\2009", "\\2010", "\\2011", "\\2012", "\\2013", "\\2014",
				"\\2015", "\\2016" };
		String[] datadirs = { "\\bid_more_than_ask_in_rawcsv_2", "\\bid_equal_to_ask_in_rawcsv_2" };
		String[] tasks = { "\\date_error", "\\time_error" };
		String rfilepath;
		String rfiledate; // 読み込むファイルの日付を格納する．

		for (String datayear : datayears) {
			for (String datadir : datadirs) {

				File rfile = new File(currentdir + datayear + datadir + "\\daily"); // 読み込むファイルのディレクトリのパス．
				File[] filelist = rfile.listFiles(); // 読み込むファイル名を取得する．

				for (String task : tasks) {
					File newdir = new File(currentdir + datayear + datadir + task);
					if (!newdir.exists() && rfile.exists()) {
						newdir.mkdirs();
					}

					File file = new File(currentdir + datayear + datadir + task + task + ".csv");
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

					if (task.equals("\\date_error")) {

						int count = 0; // errorデータ数を勘定する．
						pw.println("date,error,");

						for (int i = 0; i < filelist.length; i++) {

							rfilepath = filelist[i].getAbsolutePath();
							rfiledate = new File(rfilepath).getName().substring(0, 8); // 読み込むファイルの日付を取得

							System.out.println(task + "\\" + rfiledate);
							FileReader fr = new FileReader(filelist[i]);
							BufferedReader brtxt = new BufferedReader(fr);

							while (brtxt.readLine() != null) {
								count++;
							}
							brtxt.close();
							fr.close();
							pw.println(rfiledate.substring(0, 4) + "/" + rfiledate.substring(4, 6) + "/"
									+ rfiledate.substring(6, 8) + "," + count + ",");
							count = 0;
						}
					}
					if (task.equals("\\time_error")) {
						TreeMap<String, Integer> time_count = new TreeMap<String, Integer>(); // 辞書型で保存する:<分単位時刻,エラー数>
						String line;
						String time;
						pw.println("time,error,");
						for (int i = 0; i < filelist.length; i++) {

							FileReader fr = new FileReader(filelist[i]);
							BufferedReader brtxt = new BufferedReader(fr);

							while ((line = brtxt.readLine()) != null) {
								time = line.split(",", -1)[1].split(":")[0] + ":"
										+ line.split(",", -1)[1].split(":")[1];
								if (time_count.containsKey(time)) {
									// キーに登録されている時刻についてはエラー数のみ勘定．．
									time_count.compute(time, (key, old) -> old + 1);
								} else {
									// キーに登録されていない時刻はキーとして新規に登録する
									time_count.put(time, 1);
								}
							}
							brtxt.close();
							fr.close();
						}

						for (String key : time_count.keySet()) {
							pw.println(key + "," + time_count.get(key) + ",");
						}
					}
					pw.close();
				}
			}
		}
	}
}
