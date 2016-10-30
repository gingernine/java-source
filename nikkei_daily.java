import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

//raw,csv,rawcsvの3フォルダには一日のデータが複数ファイルに分割されているので各日のファイルに統合する．
public class nikkei_daily {

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2016";
		// String datadir = "\\rawcsv\\daily_seperated"; //読み込むファイルが在るディレクトリ名
		// String writedir = "\\rawcsv\\daily\\"; //ファイルを書き込むディレクトリ名
		// String datadir = "\\rawcsv_2\\daily_seperated"; //読み込むファイルが在るディレクトリ名
		// String writedir = "\\rawcsv_2\\daily\\"; //ファイルを書き込むディレクトリ名
		// String datadir = "\\price_change\\daily_seperated"; // 読み込むファイルが在るディレクトリ名
		// String writedir = "\\price_change\\daily\\"; //ファイルを書き込むディレクトリ名
		String datadir = "\\price_or_depth_change\\daily_seperated"; // 読み込むファイルが在るディレクトリ名
		String writedir = "\\price_or_depth_change\\daily\\"; // ファイルを書き込むディレクトリ名
		// int sep = 4; //ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[ rawcsv ]
		// int sep = 5; //ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[ rawcsv_2, price_change ]
		int sep = 7; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[ price_or_depth_change ]
		// File makedir = new File(currentdir + datayear + writedir);
		// if(!makedir.exists()){
		//// 書き込む先のディレクトリが存在しなければ作成する．
		// makedir.mkdir();
		// }
		String rfilename;
		String wfiledate = ""; // 書き込むファイルの日付を設定する．
		String rfiledate; // 読み込むファイルの日付を格納する．

		File rfilepath = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		File file = new File("C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\dummy\\dummy.txt");
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

		for (int i = 0; i < filelist.length; i++) {

			rfilename = filelist[i].getAbsolutePath();
			int pathlength = rfilename.split("\\_")[sep].length();
			rfiledate = rfilename.split("\\_")[sep].substring(pathlength - 8, pathlength); // 読み込むファイルの日付を取得

			if (!rfiledate.equals(wfiledate)) {
				pw.close();
				wfiledate = rfiledate;
				file = new File(currentdir + datayear + writedir + wfiledate + "_.csv");
				pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			}
			System.out.println(wfiledate);

			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			while ((line = brtxt.readLine()) != null) {
				pw.println(line);
			}
			brtxt.close();
			fr.close();
		}
		pw.close();
	}
}