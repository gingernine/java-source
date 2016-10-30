import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// raw 及び raw_daily フォルダには一日のデータが複数ファイルに分割されているので各日のファイルに統合する．
public class validator {

	private static void time_validator(ArrayList<String> data) {
		// 時系列の順番が正しいかどうかを判定する．

		int n = data.size();
		int time = 0; // 読み込む行の時刻を入れる.
		int tmp = 0; // 比較用時刻.
		boolean isInit = true;

		for (int j = 0; j < n; j++) {
			time = Integer.parseInt(data.get(j).substring(30, 34));
			if (isInit) {
				System.out.println(time);
				tmp = time;
				isInit = false;
			} else if (time < tmp) {
				System.out.println("----------------------------------------error");
				break;
			}
			tmp = time;
		}
		System.out.println(time);
	}

	private static int merge_validator(ArrayList<String> data1, ArrayList<String> data2, int nrow) {
		// ファイル統合に伴うエラー(僕のスクリプト nikkei_daily.java のミス)がないか確認する．

		int p = nrow; //
		int n = data1.size();
		String line1 = "";
		String line2 = "";

		if (p + n > data2.size()) {
			System.out.println("size contradiction error");
		}
		for (int j = 0; j < n; j++) {
			line1 = data1.get(j);
			line2 = data2.get(p + j);
			if (!line1.equals(line2)){
				System.out.println("line contradiction error");
				System.out.println(line1);
				System.out.println(line2);
			}
		}
		return n;
	}

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2016";
		String datadir1 = "\\raw"; // 読み込むファイルが在るディレクトリ名 [ 2009 ~ 2016 ]
		// String datadir1 = "\\raw_daily"; // 読み込むファイルが在るディレクトリ名  [ 2006 ~ 2008 ]
		String datadir2 = "\\dailydata"; // 読み込むファイルが在るディレクトリ名
		int sep = 3; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う． [ raw ]
		// int sep = 4; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[ raw_daily ]
		String rfilename;
		String rfiledate1; // 読み込むファイルの日付を格納する．[ dailydata ]
		String rfiledate2; // 読み込むファイルの日付を格納する． [ rawdata ]

		File rfilepath1 = new File(currentdir + datayear + datadir1); // 読み込むファイルのディレクトリのパス．
		File[] rfilelist1 = rfilepath1.listFiles(); // 読み込むファイル名を取得する．
		File rfilepath2 = new File(currentdir + datayear + datadir2); // 読み込むファイルのディレクトリのパス．
		File[] rfilelist2 = rfilepath2.listFiles(); // 読み込むファイル名を取得する．

		for (int i = 0; i < rfilelist2.length; i++) {

			ArrayList<String> dailydata = new ArrayList<String>();
			ArrayList<String> rawdata = new ArrayList<String>();

			rfilename = rfilelist2[i].getAbsolutePath();
			int pathlength = rfilename.split("\\_")[3].length();
			rfiledate2 = rfilename.split("\\_")[3].substring(pathlength - 8, pathlength); // 読み込むファイルの日付を取得
			System.out.println(rfiledate2);
			if (Integer.parseInt(rfiledate2) <= 20160212) continue;

			FileReader fr = new FileReader(rfilelist2[i]);
			BufferedReader brtxt = new BufferedReader(fr);

			String line = "";
			while((line = brtxt.readLine()) != null) {
				dailydata.add(line);
			}
			brtxt.close();
			fr.close();

			time_validator(dailydata);

			int nrow = 0; // 読み込む行数
			boolean datematch = false;
			for (int r = 0; r < rfilelist1.length; r++) {
				rfilename = rfilelist1[r].getAbsolutePath();
				pathlength = rfilename.split("\\_")[sep].length();
				rfiledate1 = rfilename.split("\\_")[sep].substring(pathlength - 8, pathlength); // 読み込むファイルの日付を取得
				rawdata = new ArrayList<String>();

				if (rfiledate1.equals(rfiledate2)) {
					datematch = true;
					fr = new FileReader(rfilelist1[r]);
					brtxt = new BufferedReader(fr);
					while((line = brtxt.readLine()) != null) {
						rawdata.add(line);
					}
					brtxt.close();
					fr.close();
					nrow = merge_validator(rawdata, dailydata, nrow);
				}
				if (datematch && !rfiledate1.equals(rfiledate2)) break;
			}
		}
	}
}