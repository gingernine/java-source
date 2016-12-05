import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/*
 Li; Hui; Endo; Kishimoto (2014) に倣って以下の量を算出する．
 reference: "A Quontitative Model For Intraday Stock Price Changes Based On Order Flows"
 J Syst Sci Complex (2014) 27: 208-224.

 以下，今回見るべき量．
 arrival frequency of market buy order (成行買い注文)
 arrival frequency of market sell order (成行売り注文)
 arrival frequency of limit buy order (指値買い注文)
 arrival frequency of limit sell order (指値売り注文)
 average pieces of one market buy order (成行買い注文の注文数)
 average pieces of one market sell order (成行き売り注文の注文数)
 average pieces of one limit buy order (指値買い注文の注文数)
 average pieces of one limit sell order (指値売り注文の注文数)
 upmovement/downmovement times of the best bid (最良買い気配値が上/下に動いた回数)
 upmovement/downmovement times of the best ask (最良売り気配値が上/下に動いた回数)

 ついでに調べるものを以下の3つとする．
 (1) 到着時間間隔(指値注文時間間隔)
 (2) サービス時間間隔(成行注文時間間隔)
 (3) 厚みが0になるまでの時間間隔

 上記の量の計算は前場/後場で別にする．
 使用データは，ザラバ(continuous session) のみ．(寄付直後～場の最終約定直前)
 */

public class arrival_frequency {

	private static void count(List<Integer> list) {
		// 回数を数えるリストで系列を作成するために，リストの最後尾に更新した回数を追加する．
		int lastval = getlast(list);
		list.add((lastval + 1));
	}

	private static int getlast(List<Integer> list) {
		// リストの最後の要素を取り出す．
		int lastval = 0;
		try {
			lastval = ((Integer) list.get(list.size() - 1)).intValue();
		} catch (ArrayIndexOutOfBoundsException ignored) {
		}
		return lastval;
	}

	private static double mean(List<Integer> list) {
		// リスト内要素の平均値を計算する．
		int n = list.size();
		int sum = 0;
		for (int j = 0; j < n; j++) {
			sum = sum + ((Integer) list.get(j)).intValue();
		}
		return (double) sum / n;
	}

	private static double variance(List<Integer> list) {
		// リスト内要素の不偏分散を計算する．
		int n = list.size();
		double sum = 0;
		double average = mean(list);
		for (int j = 0; j < n; j++) {
			sum = sum + (((Integer) list.get(j)).intValue() - average)*(((Integer) list.get(j)).intValue() - average);
		}
		return sum / (n-1);
	}

	private static void mkdirs(String dirpath) throws IOException {
		File dir = new File(dirpath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	private static void filewriter(List<Integer> list, String dirpath, String filedate, boolean morning ) throws IOException {
		int n = list.size();
		String ampm = "\\morning\\";
		if (!morning) {
			ampm = "\\afternoon\\";
		}
		if (Integer.parseInt(filedate) >= 20110214) {
			ampm = "";
		}
		mkdirs(dirpath + ampm);
		File file = new File(dirpath + ampm + filedate + "_.csv");
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		for (int j = 0; j < n; j++) {
			pw.println(list.get(j));
		}
		pw.close();
	}

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2009";
		String datadir = "\\price_or_depth_change\\daily";
		String writedir = "\\statistics_of_the_limit_order_book"; // 書き込みファイル
		mkdirs(currentdir + writedir);
		String rfilepath;
		String rfiledate; // 読み込むファイルの日付を格納する．

		File rfiledir = new File(currentdir + datayear + datadir); // 読み込むファイルのディレクトリのパス．
		File[] filelist = rfiledir.listFiles(); // 読み込むファイル名を取得する．

		String[] subpaths = { "\\yearly" + datayear, "\\initial_depth" + datayear + "\\after_up",
				"\\initial_depth" + datayear + "\\after_down" };
		PrintWriter pw[] = new PrintWriter[subpaths.length];
		for (int q = 0; q < subpaths.length; q++) {
			File file = new File(currentdir + writedir + subpaths[q] + "_.csv");
			pw[q] = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		}
		pw[0].println("date, Arrival Frequency of Market Buy Orders,Arrival Frequency of Market sell Orders,"
				+ "Arrival Frequency of limit Buy Orders,Arrival Frequency of limit sell Orders,"
				+ "Averege Pieces of One Market Buy Order,Averege Pieces of One Market sell Order,"
				+ "Averege Pieces of One limit Buy Order,Averege Pieces of One limit sell Order,"
				+ "Unbiased Variance of Pieces of One Market Buy Order,Unbiased Variance of Pieces of One Market sell Order,"
				+ "Unbiased Variance of Pieces of One limit Buy Order,Unbiased Variance of Pieces of One limit sell Order,"
				+ "Upmovement Times Of the Best Bid,Downmovement Times Of the Best Bid,"
				+ "Upmovement Times Of the Best Ask,Downmovement Times Of the Best Ask,");
		pw[1].println("date,time,Quote/Trade,,,bid price,bid depth,ask price,ask depth,na,na");
		pw[2].println("date,time,Quote/Trade,,,bid price,bid depth,ask price,ask depth,na,na");

		serial_correlation sc = new serial_correlation(); // serial_correlationクラスのインスタンス，時間感覚の計算に使う．

		for (int i = 0; i < filelist.length; i++) {

			rfilepath = filelist[i].getAbsolutePath();
			rfiledate = new File(rfilepath).getName().substring(0, 8); // 読み込むファイルの日付を取得

			System.out.println(rfiledate);
			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			// データ抽出に使う変数の定義．
			List<Integer> freq_market_buy      = new ArrayList<Integer>(); // arrival frequency of market buy order
			List<Integer> freq_market_sell     = new ArrayList<Integer>(); // arrival frequency of market sell order
			List<Integer> freq_limit_buy       = new ArrayList<Integer>(); // arrival frequency of limit buy order
			List<Integer> freq_limit_sell      = new ArrayList<Integer>(); // arrival frequency of limit sell order
			List<Integer> pieces_market_buy    = new ArrayList<Integer>(); // average piecese of one market buy order
			List<Integer> pieces_market_sell   = new ArrayList<Integer>(); // average piecese of one market sell order
			List<Integer> pieces_limit_buy     = new ArrayList<Integer>(); // average piecese of one limit buy order
			List<Integer> pieces_limit_sell    = new ArrayList<Integer>(); // average piecese of one limit sell order
			List<Integer> up_times_bid         = new ArrayList<Integer>(); // upmovement times of the best bid
			List<Integer> down_times_bid       = new ArrayList<Integer>(); // downmovement times of the best bid
			List<Integer> up_times_ask         = new ArrayList<Integer>(); // upmovement times of the best ask
			List<Integer> down_times_ask       = new ArrayList<Integer>(); // downmovement times of the best ask
			List<Integer> interval_limit_buy   = new ArrayList<Integer>(); // time interval of the limit buy order
			List<Integer> interval_limit_sell  = new ArrayList<Integer>(); // time interval of the limit sell order
			List<Integer> interval_market_buy  = new ArrayList<Integer>(); // time interval of the market buy order
			List<Integer> interval_market_sell = new ArrayList<Integer>(); // time interval of the market sell order
			List<Integer> operating_time_bid   = new ArrayList<Integer>();
			List<Integer> operating_time_ask   = new ArrayList<Integer>();
			int bidprice = 0; // 最良買い気配値
			int bidpricetemp = 0; // 最良買い気配値の一時保存
			int askprice = 0; // 最良売り気配値
			int askpricetemp = 0; // 最良売り気配値の一時保存
			int biddepth = 0; // 最良買い気配にかかる数量
			int biddepthtemp = 0; // 最良買い気配数量の一時保存
			int askdepth = 0; // 最良売り気配にかかる数量
			int askdepthtemp = 0; // 最良売り気配数量の一時保存
			int tradeprice = 0; // 約定価格
			int tradevolume = 0; // 約定数量
			String time = ""; // 時刻
			int inttime = 0; // 時間間隔計算用
			int limit_buy_time_temp = 0; // 買い指値注文時間間隔計算用
			int limit_sell_time_temp = 0; // 売り指値注文時間間隔計算用
			int market_buy_time_temp = 0; // 買い成行注文時間間隔計算用
			int market_sell_time_temp = 0; // 売り成行注文時間間隔計算用
			int timediff = 0; // 時間間隔
			String[] closing = new String[2];
			error_detector ed = new error_detector();
			if (Integer.parseInt(rfiledate) < 20110214) {
				if (Integer.parseInt(rfiledate) < 20090130 && (i == 0 || i == filelist.length - 1)) {
					closing[0] = ed.lastTrade(filelist[i], 1110);
				} else {
					closing[0] = ed.lastTrade(filelist[i], 1100);
					closing[1] = ed.lastTrade(filelist[i], 1510);
				}
			} else {
				closing[0] = ed.lastTrade(filelist[i], 1510);
			}
			boolean continuous = false; // ザラバを判定する．場中はtrue.
			boolean isInit = true; // 最良気配に初期値を入れるための判定記号．
			boolean isMorning = true;
			boolean market_buy_order = false; // 買いの成行注文が来たらtrue.
			boolean market_sell_order = false; // 売りの成行注文が来たらtrue.

			while ((line = brtxt.readLine()) != null) {

				time = line.split(",", -1)[1].split(":")[0] + line.split(",", -1)[1].split(":")[1];
				if (line.split(",", -1)[9].equals("  1")) {
					continuous = true;
				}
				if (Arrays.asList(closing).contains(line)) {

					if (inttime > 120000) {
						isMorning = false;
					}
					pw[0].println(rfiledate + "," + getlast(freq_market_buy) + "," + getlast(freq_market_sell) +
							"," + getlast(freq_limit_buy) + "," + getlast(freq_limit_sell) +
							"," + mean(pieces_market_buy) + "," + mean(pieces_market_sell) +
							"," + mean(pieces_limit_buy) + "," + mean(pieces_limit_sell) +
							"," + variance(pieces_market_buy) + "," + variance(pieces_market_sell) +
							"," + variance(pieces_limit_buy) + "," + variance(pieces_limit_sell) +
							"," + getlast(up_times_bid) + "," + getlast(down_times_bid) +
							"," + getlast(up_times_ask) + "," + getlast(down_times_ask));

					filewriter(pieces_limit_buy, currentdir + writedir + "\\pieces\\pieces_limit_buy" + datayear,
							rfiledate, isMorning );
					filewriter(pieces_limit_sell, currentdir + writedir + "\\pieces\\pieces_limit_sell" + datayear,
							rfiledate, isMorning );
					filewriter(pieces_market_buy, currentdir + writedir + "\\pieces\\pieces_market_buy" + datayear,
							rfiledate, isMorning );
					filewriter(pieces_market_sell, currentdir + writedir + "\\pieces\\pieces_market_sell" + datayear,
							rfiledate, isMorning );
					filewriter(interval_limit_buy, currentdir + writedir + "\\time_interval\\time_interval_limit_buy" + datayear,
							rfiledate, isMorning );
					filewriter(interval_limit_sell, currentdir + writedir + "\\time_interval\\time_interval_limit_sell" + datayear,
							rfiledate, isMorning );
					filewriter(interval_market_buy, currentdir + writedir + "\\time_interval\\time_interval_market_buy" + datayear,
							rfiledate, isMorning );
					filewriter(interval_market_sell, currentdir + writedir + "\\time_interval\\time_interval_market_sell" + datayear,
							rfiledate, isMorning );

					// initialize (morning, afternoon session に分かれている日のため)
					freq_market_buy    = new ArrayList<Integer>();
					freq_market_sell   = new ArrayList<Integer>();
					freq_limit_buy     = new ArrayList<Integer>();
					freq_limit_sell    = new ArrayList<Integer>();
					pieces_market_buy  = new ArrayList<Integer>();
					pieces_market_sell = new ArrayList<Integer>();
					pieces_limit_buy   = new ArrayList<Integer>();
					pieces_limit_sell  = new ArrayList<Integer>();
					up_times_bid       = new ArrayList<Integer>();
					down_times_bid     = new ArrayList<Integer>();
					up_times_ask       = new ArrayList<Integer>();
					down_times_ask     = new ArrayList<Integer>();
					interval_limit_buy   = new ArrayList<Integer>();
					interval_limit_sell  = new ArrayList<Integer>();
					interval_market_buy  = new ArrayList<Integer>();
					interval_market_sell = new ArrayList<Integer>();
					continuous = false;
					isInit = true;
					market_buy_order = false;
					market_sell_order = false;
				}

				try {
					inttime = Integer.parseInt(time + line.split(",", -1)[1].split(":")[2]);
				} catch (Exception e) {
					inttime = Integer.parseInt(time + "00");
				}

				if (continuous && isInit) {
					// 最良気配に初期値を入れる．
					if (line.split(",", -1)[2].equals("Quote")) {
						bidpricetemp = Integer.parseInt(line.split(",", -1)[5]);
						biddepthtemp = Integer.parseInt(line.split(",", -1)[6]);
						askpricetemp = Integer.parseInt(line.split(",", -1)[7]);
						askdepthtemp = Integer.parseInt(line.split(",", -1)[8]);
						limit_buy_time_temp = inttime;
						limit_sell_time_temp = inttime;
						market_buy_time_temp = inttime;
						market_sell_time_temp = inttime;
						isInit = false; // 買い気配に初期値を入れたら初期化完了．
					}
				}

				if (continuous && !isInit) {
					// ザラバのみデータ抽出．
					if (line.split(",", -1)[2].equals("Quote")) {

						// 現在の最良気配値・数量を取得する．
						bidprice = Integer.parseInt(line.split(",", -1)[5]);
						biddepth = Integer.parseInt(line.split(",", -1)[6]);
						askprice = Integer.parseInt(line.split(",", -1)[7]);
						askdepth = Integer.parseInt(line.split(",", -1)[8]);

						if (bidprice > bidpricetemp) {
							if (askprice - bidprice == 10) {
								pw[1].println(line);
							}
							count(up_times_bid);
							limit_buy_time_temp = inttime;
							market_sell_time_temp = inttime;
						} else if (bidprice == bidpricetemp) {
							if (biddepth > biddepthtemp) {
								/*
								 * 買い気配数量が増加したら
								 * (1)指値注文の時間間隔を記録する．
								 * (2)買いの指値注文として数える．
								 * (3)増加分は指値注文数として記録する．
								 */
								timediff = sc.time_diff_in_seconds(limit_buy_time_temp, inttime);
								interval_limit_buy.add(timediff);
								limit_buy_time_temp = inttime;
								count(freq_limit_buy);
								pieces_limit_buy.add(biddepth - biddepthtemp);
							} else if (biddepth < biddepthtemp) {
								/*
								 * 買い気配数量が減少したら
								 * (1)成行注文の時間間隔を記録する．
								 * (2)減少分は成行注文数として記録する．
								 */
								timediff = sc.time_diff_in_seconds(market_sell_time_temp, inttime);
								interval_market_sell.add(timediff);
								market_sell_time_temp = inttime;
								pieces_market_sell.add(biddepthtemp - biddepth);
							}
						} else {
							count(down_times_bid);
							if (market_sell_order) {
								timediff = sc.time_diff_in_seconds(market_sell_time_temp, inttime);
								interval_market_sell.add(timediff);
								if (askprice - bidprice == 10) {
									pieces_market_sell.add(tradevolume + askdepth);
								} else if (askprice - bidprice > 10) {
									pieces_market_sell.add(tradevolume);
								}
							}
							limit_buy_time_temp = inttime;
							market_sell_time_temp = inttime;
						}

						if (askprice > askpricetemp) {
							count(up_times_ask);
							if (market_buy_order) {
								/*
								 * 売りの最良気配が0になって板が上に移動した場合，買いの成行注文時間間隔も記録する．
								 */
								timediff = sc.time_diff_in_seconds(market_buy_time_temp, inttime);
								interval_market_buy.add(timediff);
								if (askprice - bidprice == 10) {
									pieces_market_buy.add(tradevolume + biddepth);
								} else if (askprice - bidprice > 10) {
									pieces_market_buy.add(tradevolume);
								}
							}
							limit_sell_time_temp = inttime;
							market_buy_time_temp = inttime;
						} else if (askprice == askpricetemp) {
							if (askdepth > askdepthtemp) {
								/*
								 * 売り気配数量が増加したら
								 * (1)時間間隔を記録する．
								 * (2)売りの指値注文として数える．
								 * (3)増加分は指値注文数として記録する．
								 */
								timediff = sc.time_diff_in_seconds(limit_sell_time_temp, inttime);
								interval_limit_sell.add(timediff);
								limit_sell_time_temp = inttime;
								count(freq_limit_sell);
								pieces_limit_sell.add(askdepth - askdepthtemp);
							} else if (askdepth < askdepthtemp) {
								/* 売り気配数量が減少したら
								 * (1)成行注文の時間間隔を記録する．
								 * (2)減少分は成行注文数として記録する．
								 */
								timediff = sc.time_diff_in_seconds(market_buy_time_temp, inttime);
								interval_market_buy.add(timediff);
								market_buy_time_temp = inttime;
								pieces_market_buy.add(askdepthtemp - askdepth);
							}
						} else {
							if (askprice - bidprice == 10) {
								pw[2].println(line);
							}
							count(down_times_ask);
							limit_sell_time_temp = inttime;
							market_buy_time_temp = inttime;
						}

						bidpricetemp = bidprice; // 最良買い気配値を更新
						biddepthtemp = biddepth; // 最良買い気配数量を更新
						askpricetemp = askprice; // 最良売り気配値を更新
						askdepthtemp = askdepth; // 最良売り気配数量を更新
						market_buy_order = false;
						market_sell_order = false;
					}

					if (line.split(",", -1)[2].equals("Trade")) {

						tradeprice = Integer.parseInt(line.split(",", -1)[3]);
						tradevolume = Integer.parseInt(line.split(",", -1)[4]);
						if (tradeprice == askprice) {
							/*
							 * 約定価格が直前のbest ask に等しい又は高いなら，
							 * (1)買いの成行注文として数える．
							 */
							count(freq_market_buy);
							market_buy_order = true;
						} else if (tradeprice == bidprice) {
							/*
							 *  約定価格が直前のbest bid に等しい又は低いなら，
							 * (1)売りの成行注文として数える．
							 */
							count(freq_market_sell);
							market_sell_order = true;
						}
					}
				}
			}
			brtxt.close();
			fr.close();
		}
		for (int q = 0; q < subpaths.length; q++) {
			pw[q].close();
		}
	}
}
