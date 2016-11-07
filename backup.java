import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class backup {

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String[] datayear = { "\\2006", "\\2007", "\\2008", "\\2009", "\\2010", "\\2011", "\\2012",
				"\\2013", "\\2014", "\\2015", "\\2016" };
		String[] datadirs = { "\\raw", "\\raw_daily", "\\dailydata" };
		String writedir = "E:\\nikkei_data_backup"; // 書き込みファイル
		String rfilename;
		String rfiledate; // 読み込むファイルの日付を格納する．

		for (String dy : datayear) {

			for (String datadir : datadirs) {

				File rfilepath = new File(currentdir + dy + datadir); // 読み込むファイルのディレクトリのパス．
				if (rfilepath.exists()) {
					File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．
					for (int i = 0; i < filelist.length; i++) {
						rfilename = filelist[i].getAbsolutePath();
						rfiledate = new File(rfilename).getName();

						System.out.println(datadir + "\\" + rfiledate);

						FileReader fr = new FileReader(filelist[i]);
						BufferedReader brtxt = new BufferedReader(fr);
						String line = "";

						File file = new File(writedir + dy + datadir);
						if (!file.exists()) {
							file.mkdirs();
						}
						file = new File(writedir + dy + datadir + "\\" + rfiledate);
						PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
						while ((line = brtxt.readLine()) != null) {
							pw.println(line);
						}
						pw.close();
						fr.close();
						brtxt.close();
					}
				}
			}
		}
	}
}