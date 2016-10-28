import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

// クロージングの時間が来てから最後の約定が在るかを確認する．
// 使用データは，continuous session のみ(寄付直後～場の最終約定直前)
public class closing {

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2011";
		String datadir = "\\rawcsv_2\\daily";
		String rfilename;
		String rfiledate; // 読み込むファイルの日付を格納する．

		File rfilepath = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		for (int i = 0; i < filelist.length; i++) {

			rfilename = filelist[i].getAbsolutePath();
			int pathlength = rfilename.split("\\_")[4].length();
			rfiledate = rfilename.split("\\_")[4].substring(pathlength - 8, pathlength); // 読み込むファイルの日付を取得

			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			// データ抽出に使う変数の定義．
			String time = ""; // 時刻
			String[] closing = new String[2];
			if (Integer.parseInt(rfiledate) < 20110214 && !(i == 0 || i == filelist.length - 1)) {
				closing[0] = "1100";
				closing[1] = "1510";
			} else if (Integer.parseInt(rfiledate) < 20090130 && (i == 0 || i == filelist.length - 1)) {
				closing[0] = "1110";
			} else {
				closing[0] = "1510";
			}
			boolean continuous = false; // ザラバを判定する．場中はtrue.
			int count = 0; // クロージングの時間になってから複数の約定があったかどうかを判定する．

			while ((line = brtxt.readLine()) != null) {

				time = line.split(",", -1)[1].split(":")[0] + line.split(",", -1)[1].split(":")[1];
				if (line.split(",", -1)[9].equals("  1")) {
					continuous = true;
				}
				if (Arrays.asList(closing).contains(time) && line.split(",", -1)[2].equals("Trade")) {
					System.out.println(line);
					count++;
					continuous = false;
				}
			}
			if (continuous || count > 2) {
				System.out.println("-------------------------------------------------------");
				System.out.println(rfiledate + " : error");
				System.out.println("-------------------------------------------------------");
			}
			brtxt.close();
			fr.close();
		}
	}
}
