import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

// Li; Hui; Endo; Kishimoto (2014) に倣って以下の量を算出する．
// reference:
// arrival frequency of market buy order (成行買い注文)
// arrival frequency of market sell order (成行売り注文)
// arrival frequency of limit buy order (指値買い注文)
// arrival frequency of limit sell order (指値売り注文)
// average pieces of one market buy order (成行買い注文の注文数)
// average pieces of one market sell order (成行き売り注文の注文数)
// average pieces of one limit buy order (指値買い注文の注文数)
// average pieces of one limit sell order (指値売り注文の注文数)
// upmovement/downmovement times of the best bid
// upmovement/downmovement times of the best ask
//
// 上記の量の計算は前場/後場で別にする．
// 使用データは，continuous session のみ(寄付直後～場の最終約定直前)
public class arrival_frequency {

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2006";
		String datadir = "\\price_or_depth_change";
		String writedir = "\\statistics_of_the_limit_order_book"; // 書き込みファイル
		String errordir = "\\NumberFormatException";
		File newdir = new File(currentdir + writedir);
		if (!newdir.exists()) {
			newdir.mkdirs();
		}
		int sep = 4; // ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[2006~2008]
		// int sep = 3; // [2009~2014]
		String rfilename;
		String rfiledate; // 読み込むファイルの日付を格納する．

		File rfilepath = new File(currentdir + datayear + datadir + errordir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); // 読み込むファイル名を取得する．

		File file = new File(currentdir + writedir + datayear + "_.csv");
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		File errorfile = new File(currentdir + writedir + errordir + datayear + "_.csv");
		PrintWriter errorpw = new PrintWriter(new BufferedWriter(new FileWriter(errorfile)));

		for (int i = 0; i < filelist.length; i++) {

			rfilename = filelist[i].getAbsolutePath();
			int pathlength = rfilename.split("\\_")[sep].length();
			rfiledate = rfilename.split("\\_")[sep].substring(pathlength - 8, pathlength); // 読み込むファイルの日付を取得

			System.out.println(rfiledate);
			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			// データ抽出に使う変数の定義．
			int freq_market_buy = 0; // arrival frequency of market buy order
			int freq_market_sell = 0; // arrival frequency of market sell order
			int freq_limit_buy = 0; // arrival frequency of limit buy order
			int freq_limit_sell = 0; // arrival frequency of limit sell order
			ArrayList<Integer> pieces_market_buy = new ArrayList<Integer>(); // average piecese of one market buy order
			ArrayList<Integer> pieces_market_sell = new ArrayList<Integer>(); // average piecese of one market sell order
			ArrayList<Integer> pieces_limit_buy = new ArrayList<Integer>(); // average piecese of one limit buy order
			ArrayList<Integer> pieces_limit_sell = new ArrayList<Integer>(); // average piecese of one limit sell order
			int up_times_bid = 0; // upmovement times of the best bid
			int down_times_bid = 0; // downmovement times of the best bid
			int up_times_ask = 0; // upmovement times of the best ask
			int down_times_ask = 0; // downmovement times of the best ask
			int bidpricetemp = 0; // 最良買い気配値一時保存
			int askpricetemp = 0; // 最良売り気配値一時保存
			int biddepthtemp = 0; // 最良買い気配数量一時保存
			int askdepthtemp = 0; // 最良売り気配数量一時保存
			int bidprice = 0; // 最良買い気配値
			int askprice = 0; // 最良売り気配値
			int biddepth = 0; // 最良買い気配にかかる数量
			int askdepth = 0; // 最良売り気配にかかる数量
			int tradeprice = 0; // 約定価格
			int tradevolume = 0; // 約定数量
			ArrayList<String> error = new ArrayList<String>();
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
			boolean isInit = true; // 最良気配に初期値を入れるための判定記号．
			boolean market_buy_order = false; // 買いの成行注文が来たらtrue.
			boolean market_sell_order = false; // 売りの成行注文が来たらtrue.

			while ((line = brtxt.readLine()) != null) {

				time = line.substring(30,34);
				if (Arrays.asList(closing).contains(time) && line.substring(34, 36).equals(" 0")) {
					continuous = false;
				}

				if (continuous && isInit) {
					// 最良気配に初期値を入れる．
					if (line.substring(49, 52).equals("  0")) {
						askprice = Integer.parseInt(line.substring(41,47));
						askdepth = Integer.parseInt(line.substring(56,66));
					}
					if (line.substring(49, 52).equals("128")){
						bidprice = Integer.parseInt(line.substring(41,47));
						biddepth = Integer.parseInt(line.substring(56,66));
						isInit = false; // 売り気配→買い気配の順にデータが並んでいるので，買い気配に初期値を入れたら初期化完了．
					}
				}

				if (continuous && !isInit) {
					// ザラバ時間のみデータ抽出．
					if (line.substring(34, 36).equals("33")) {
						if (line.substring(49, 52).equals("  0")) {

							if (Integer.parseInt(line.substring(56,66)) > askdepth) {
								// 売り気配数量が増加したら売りの指値注文として数える．
								// 増加分は指値注文数として記録する．
								freq_limit_sell++;
								pieces_limit_sell.add(Integer.parseInt(line.substring(56,66)) - askdepth);
							}
							askdepth = Integer.parseInt(line.substring(56,66));
							askprice = Integer.parseInt(line.substring(41,47));

							if (market_buy_order) {
								// 直前に買いの成行注文が入ったら，
							}

						}
						if (line.substring(49, 52).equals("128")){

							if (Integer.parseInt(line.substring(56,66)) > biddepth) {
								// 買い気配数量が増加したら買いの指値注文として数える．
								// 増加分は指値注文数として記録する．
								freq_limit_buy++;
								pieces_limit_buy.add(Integer.parseInt(line.substring(56,66)) - biddepth);
							}
							bidprice = Integer.parseInt(line.substring(41,47));
							biddepth = Integer.parseInt(line.substring(56,66));
						}
					}

					if (line.substring(34, 36).equals(" 0")) {

						tradeprice = Integer.parseInt(line.substring(41,47));
						tradevolume = Integer.parseInt(line.substring(56,66));
						if (tradeprice >= askprice) {
							// 約定価格が直前のbest ask に等しい又は高いなら，買いの成行注文として数える．
							freq_market_buy++;
							market_buy_order = true;
						} else if (tradeprice <= bidprice) {
							// 約定価格が直前のbest bid に等しい又は低いなら，売りの成行注文として数える．
							freq_market_sell++;
							market_sell_order = true;
						}
					}
				}

				if (line.substring(49, 52).equals("  1")) {
					continuous = true;
				}

			}
			brtxt.close();
			fr.close();
		}
		pw.close();
		errorpw.close();
	}
}
