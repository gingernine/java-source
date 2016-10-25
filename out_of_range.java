import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

//約定値が直前の気配値の範囲(最良売り気配値　と　最良買い気配値　の間)の外にあるものを抽出する．
public class out_of_range {

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2007";
		String datadir = "\\rawcsv_2\\daily";
		String writedir = "\\out_of_range_from_rawcsv_2"; // エラーを発見したときのみファイルを作成
		File newdir = new File(currentdir + datayear + writedir + "\\daily");
		// String datadir = "\\price_or_depth_change\\daily";
		int sep = 4; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[rawcsv_2]
		// int sep = 6;
		// //ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[price_or_depth_change]
		String rfilename;
		String rfiledate; // 読み込むファイルの日付を格納する．

		File rfilepath = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		for (int i = 0; i < filelist.length; i++) {

			rfilename = filelist[i].getAbsolutePath();
			int pathlength = rfilename.split("\\_")[sep].length();
			rfiledate = rfilename.split("\\_")[sep].substring(pathlength - 8, pathlength); // 読み込むファイルの日付を取得

			System.out.println(rfiledate);
			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			File file = new File("C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\dummy\\dummy.txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file))); // エラー回避のためダミーファイルを用意する．

			// データ抽出に使う変数の定義．
			String quoteline = ""; // 気配値の行を保存する．
			int bidprice = 0; // 最良買い気配値
			int askprice = 0; // 最良売り気配値
			int tradeprice = 0; // 約定価格
			boolean writing = false; // in writing: 1, not in writing: 0.

			while ((line = brtxt.readLine()) != null) {

				if (line.split(",", -1)[2].equals("Quote")) {

					quoteline = line;

					try {
						bidprice = Integer.parseInt(line.split(",", -1)[5]);
						askprice = Integer.parseInt(line.split(",", -1)[7]);
					} catch (NumberFormatException e) {
						System.out.println(e);
						file = new File(currentdir + datayear + writedir + "\\daily\\" + rfiledate + "_.csv");
						if (!writing) {
							pw.close();
							if (!newdir.exists()) {
								newdir.mkdirs();
							}
							pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
							writing = true;
						}
						pw.println(line);
					}
				}
				if (line.split(",", -1)[2].equals("Trade") && !line.split(",", -1)[9].equals("  1")) {

					try {
						tradeprice = Integer.parseInt(line.split(",", -1)[3]);
					} catch (NumberFormatException e) {
						System.out.println(e);
						file = new File(currentdir + datayear + writedir + "\\daily\\" + rfiledate + "_.csv");
						if (!writing) {
							pw.close();
							if (!newdir.exists()) {
								newdir.mkdirs();
							}
							pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
							writing = true;
						}
						pw.println(line);
					}

					if (bidprice > tradeprice || askprice < tradeprice) {
						file = new File(currentdir + datayear + writedir + "\\daily\\" + rfiledate + "_.csv");
						if (!writing) {
							pw.close();
							if (!newdir.exists()) {
								newdir.mkdirs();
							}
							pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
							writing = true;
						}
						pw.println(quoteline);
						pw.println(line);
					}
				}
			}
			brtxt.close();
			fr.close();
			pw.close();
		}
	}
}
