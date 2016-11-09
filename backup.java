import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class backup {

	private static void copyTransfer(String srcpath, String dstpath) throws IOException {
		/*
		 * コピー元とコピー先のパスを指定してファイルのコピーを行う．
		 * @srcpath コピー元のパス
		 * @dstpath コピー先のパス
		 * @throws IOException ファイル出入力に関する例外が発生した場合．
		 */
		FileChannel srcChannel = new FileInputStream(srcpath).getChannel();
		FileChannel dstChannel = new FileOutputStream(dstpath).getChannel();
		try {
			srcChannel.transferTo(0, srcChannel.size(), dstChannel);
		} finally {
			srcChannel.close();
			dstChannel.close();
		}
	}

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String[] datayear = { "\\2013", "\\2014", "\\2015", "\\2016" };
		String[] datadirs = { "\\raw", "\\raw_daily", "\\dailydata" };
		String writedir = "E:\\nikkei_data_backup"; // 書き込みファイル
		String rfilepath;
		String wfilepath;
		String rfiledate; // 読み込むファイルの日付を格納する．

		for (String dy : datayear) {
			for (String datadir : datadirs) {

				File rfile = new File(currentdir + dy + datadir); // 読み込むファイルのディレクトリのパス．

				if (rfile.exists()) {

					File[] filelist = rfile.listFiles(); // 読み込むファイル名を取得する．

					for (int i = 0; i < filelist.length; i++) {
						rfilepath = filelist[i].getAbsolutePath();
						rfiledate = new File(rfilepath).getName();
						System.out.println(datadir + "\\" + rfiledate);

						File wfile = new File(writedir + dy + datadir);
						if (!wfile.exists()) {
							wfile.mkdirs();
						}
						wfilepath = new File(writedir + dy + datadir + "\\" + rfiledate).getAbsolutePath();
						copyTransfer(rfilepath, wfilepath);
					}
				}
			}
		}
	}
}