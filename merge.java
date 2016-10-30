import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// raw 及び raw_daily フォルダには一日のデータが複数ファイルに分割されているので各日のファイルに統合する．
public class merge {

	private static ArrayList<String> filesort(Map<Integer, String> paths) {
		// 日ごとにファイル名を個別番号でソートする．

		int size = paths.size();
		ArrayList<String> sortedlist = new ArrayList<String>();
		for (int r = 0; r < size; r++){
			sortedlist.add(paths.get(r + 1));
		}
		return sortedlist;
	}

	private static void filewriter(PrintWriter pw, ArrayList<String> sortedpaths) {
		for (String filepath : sortedpaths) {
			try {
				FileReader fr = new FileReader(filepath);
				BufferedReader brtxt = new BufferedReader(fr);
				String line = "";
				while ((line = brtxt.readLine()) != null) {
					pw.println(line);
				}
				brtxt.close();
				fr.close();
			} catch (Exception ignored) {
			}
		}
	}

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2016";
		String datadir = "\\raw"; // 読み込むファイルが在るディレクトリ名 [ 2009 ~ 2016 ]
		// String datadir = "\\raw_daily"; // 読み込むファイルが在るディレクトリ名  [ 2006 ~ 2008 ]
		String writedir = "\\dailydata\\"; // ファイルを書き込むディレクトリ名
		int sep = 3; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う． [ raw ]
		// int sep = 4; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[ raw_daily ]
		File makedir = new File(currentdir + datayear + writedir);
		if(!makedir.exists()){
			// 書き込む先のディレクトリが存在しなければ作成する．
			makedir.mkdir();
		}
		String rfilename;
		String wfiledate = ""; // 書き込むファイルの日付を設定する．
		String rfiledate; // 読み込むファイルの日付を格納する．
		int fileid = 0;
		Map<Integer, String> unsorted = new HashMap<Integer, String>();
		boolean isInit = true;

		File rfilepath = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		File file = new File("C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\dummy\\dummy.txt");
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

		for (int i = 0; i < filelist.length; i++) {

			rfilename = filelist[i].getAbsolutePath();
			int pathlength = rfilename.split("\\_")[sep].length();
			rfiledate = rfilename.split("\\_")[sep].substring(pathlength - 8, pathlength); // 読み込むファイルの日付を取得
			fileid = Integer.parseInt(rfilename.split("\\_")[sep + 1]);

			if (!rfiledate.equals(wfiledate)) {

				if (isInit) {
					pw.close();
					isInit = false;
				} else {
					filewriter(pw, filesort(unsorted));
					pw.close();
					unsorted = new HashMap<Integer, String>();
				}
				wfiledate = rfiledate;
				file = new File(currentdir + datayear + writedir + wfiledate + "_.txt");
				pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			}
			unsorted.put(fileid, filelist[i].getAbsolutePath());
			System.out.println(wfiledate + "_" + fileid);
		}
		filewriter(pw, filesort(unsorted));
		pw.close();
	}
}