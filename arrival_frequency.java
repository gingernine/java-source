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
 time interval of fluctuations (板が移動する時間間隔(上下板同時に動いた時のみ))

 ついでに調べるものを以下の3つとする．
 (1) 到着時間間隔(指値注文時間間隔)
 (2) サービス時間間隔(成行注文時間間隔)
 (3) 厚みが0になるまでの時間間隔

 上記の量の計算は前場/後場で別にする．
 使用データは，ザラバ(continuous session) のみ．(寄付直後～場の最終約定直前)
 */

public class arrival_frequency {

	private static double mean(List<Integer> list) {
		// リスト内要素の平均値を計算する．
		int n = list.size();
		int sum = 0;
		for (int j = 0; j < n; j++) {
			sum += ((Integer) list.get(j)).intValue();
		}
		return (double) sum / n;
	}

	private static double variance(List<Integer> list) {
		// リスト内要素の不偏分散を計算する．
		int n = list.size();
		double sum = 0;
		double average = mean(list);
		for (int j = 0; j < n; j++) {
			sum += (((Integer) list.get(j)).intValue() - average)*(((Integer) list.get(j)).intValue() - average);
		}
		return sum / (n-1);
	}

	private static void mkdirs(String dirpath) throws IOException {
		File dir = new File(dirpath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	private static void filewriter(List list, String dirpath, String filedate, boolean morning ) throws IOException {
		int n = list.size();
		String ampm = "\\morning\\";
		if (!morning) {
			ampm = "\\afternoon\\";
		}
		if (Integer.parseInt(filedate) >= 20110214) {
			ampm = "\\";
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
				"\\initial_depth" + datayear + "\\after_down", "\\move_frequency" + datayear };
		PrintWriter pw[] = new PrintWriter[subpaths.length];
		for (int q = 0; q < subpaths.length; q++) {
			File file = new File(currentdir + writedir + subpaths[q] + "_.csv");
			pw[q] = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		}
		pw[0].println("date, Arrival Frequency of Market Buy Orders,Arrival Frequency of Market sell Orders,"
				+ "Arrival Frequency of canceled Buy Orders,Arrival Frequency of canceled sell Orders,"
				+ "Arrival Frequency of limit Buy Orders,Arrival Frequency of limit sell Orders,"
				+ "Averege Pieces of One Market Buy Order,Averege Pieces of One Market sell Order,"
				+ "Averege Pieces of One canceled Buy Order,Averege Pieces of One canceled sell Order,"
				+ "Averege Pieces of One limit Buy Order,Averege Pieces of One limit sell Order,"
				+ "Upmovement Times Of the Best Bid,Downmovement Times Of the Best Bid,"
				+ "Upmovement Times Of the Best Ask,Downmovement Times Of the Best Ask,");
		pw[1].println("date,time,Quote/Trade,,,bid price,bid depth,ask price,ask depth,na,na");
		pw[2].println("date,time,Quote/Trade,,,bid price,bid depth,ask price,ask depth,na,na");
		pw[3].println("date,time interval per second,");

		serial_correlation sc = new serial_correlation(); // serial_correlationクラスのインスタンス，時間感覚の計算に使う．

		for (int i = 0; i < filelist.length; i++) {

			rfilepath = filelist[i].getAbsolutePath();
			rfiledate = new File(rfilepath).getName().substring(0, 8); // 読み込むファイルの日付を取得

			System.out.println(rfiledate);
			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";

			// データ抽出に使う変数の定義．
			int freq_market_buy                = 0; // arrival frequency of market buy order
			int freq_market_sell               = 0; // arrival frequency of market sell order
			int freq_cancel_buy                = 0; // frequency of canceled buy order
			int freq_cancel_sell               = 0; // frequency of canceled sell order
			int freq_limit_buy                 = 0; // arrival frequency of limit buy order
			int freq_limit_sell                = 0; // arrival frequency of limit sell order
			List<Integer> pieces_market_buy    = new ArrayList<Integer>(); // average piecese of one market buy order
			List<Integer> pieces_market_sell   = new ArrayList<Integer>(); // average piecese of one market sell order
			List<Integer> pieces_cancel_buy    = new ArrayList<Integer>(); // average piecese of one cenceled buy order
			List<Integer> pieces_cancel_sell   = new ArrayList<Integer>(); // average piecese of one cenceled sell order
			List<Integer> pieces_limit_buy     = new ArrayList<Integer>(); // average piecese of one limit buy order
			List<Integer> pieces_limit_sell    = new ArrayList<Integer>(); // average piecese of one limit sell order
			int up_times_bid                   = 0; // upmovement times of the best bid
			int down_times_bid                 = 0; // downmovement times of the best bid
			int up_times_ask                   = 0; // upmovement times of the best ask
			int down_times_ask                 = 0; // downmovement times of the best ask
			List<String> initial_depth_up      = new ArrayList<String>(); // initial depth after up
			List<String> initial_depth_down    = new ArrayList<String>(); // initial depth after down
			List<String> limit_buy_line        = new ArrayList<String>(); // series of arrival of limit buy
			List<String> limit_sell_line       = new ArrayList<String>(); // series of arrival of limit sell
			List<String> market_buy_line       = new ArrayList<String>(); // series of arrival of market buy
			List<String> market_sell_line      = new ArrayList<String>(); // series of arrival of market sell
			List<Integer> move_frequency       = new ArrayList<Integer>(); // time interval of fluctuations
			List<Integer> operating_time_bid   = new ArrayList<Integer>(); // 買い板が消滅するまでの時間
			List<Integer> operating_time_ask   = new ArrayList<Integer>(); // 売り板が消滅するまでの時間
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
			int closingtime = 0; // 引け時間
			int continuoustime = 0; // ザラバ時間 = closingtime - openingtime
			int move_freq_time_temp = 0; // 板の変動の時間間隔計算用
			int operating_time_bid_temp = 0;
			int operating_time_ask_temp = 0;
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
			boolean bid_up_move = false; // 上下板が同時に動くかを判定する．
			boolean bid_down_move = false; // 上下板が同時に動くかを判定する．
			boolean market_buy_order = false; // 買いの成行注文が来たらtrue.
			boolean market_sell_order = false; // 売りの成行注文が来たらtrue.
			int t = 0;

			while ((line = brtxt.readLine()) != null) {

				time = line.split(",", -1)[1].split(":")[0] + line.split(",", -1)[1].split(":")[1];

				try {
					inttime = Integer.parseInt(time + line.split(",", -1)[1].split(":")[2]);
				} catch (Exception e) {
					inttime = Integer.parseInt(time + "00");
				}

				if (line.split(",", -1)[9].equals("  1")) {
					try {
						closingtime = Integer.parseInt(closing[t].split(",", -1)[1].split(":")[0]
								+ closing[t].split(",", -1)[1].split(":")[1]
								+ closing[t].split(",", -1)[1].split(":")[2]);
					} catch (Exception e) {
						closingtime = Integer.parseInt(closing[t].split(",", -1)[1].split(":")[0]
								+ closing[t].split(",", -1)[1].split(":")[1] + "00");
					}
					continuoustime = sc.time_diff_in_seconds(inttime, closingtime);
					continuous = true;
				}
				if (Arrays.asList(closing).contains(line)) {
					t = 1;
					if (inttime > 120000) {
						isMorning = false;
					}
					pw[0].println(rfiledate + "," + freq_market_buy + "," + freq_market_sell +
							"," + freq_cancel_buy + "," + freq_cancel_sell +
							"," + freq_limit_buy + "," + freq_limit_sell +
							"," + mean(pieces_market_buy) + "," + mean(pieces_market_sell) +
							"," + mean(pieces_cancel_buy) + "," + mean(pieces_cancel_sell) +
							"," + mean(pieces_limit_buy) + "," + mean(pieces_limit_sell) +
							"," + up_times_bid + "," + down_times_bid +
							"," + up_times_ask + "," + down_times_ask);
					pw[3].println(rfiledate + "," + mean(move_frequency));

					filewriter(pieces_limit_buy, currentdir + writedir + "\\pieces\\pieces_limit_buy" + datayear,
							rfiledate, isMorning );
					filewriter(pieces_limit_sell, currentdir + writedir + "\\pieces\\pieces_limit_sell" + datayear,
							rfiledate, isMorning );
					filewriter(pieces_market_buy, currentdir + writedir + "\\pieces\\pieces_market_buy" + datayear,
							rfiledate, isMorning );
					filewriter(pieces_market_sell, currentdir + writedir + "\\pieces\\pieces_market_sell" + datayear,
							rfiledate, isMorning );
					filewriter(operating_time_bid, currentdir + writedir + "\\operating_time\\bid" + datayear, rfiledate, isMorning);
					filewriter(operating_time_ask, currentdir + writedir + "\\operating_time\\ask" + datayear, rfiledate, isMorning);
					filewriter(initial_depth_up, currentdir + writedir + "\\initial_depth" + datayear + "\\after_up", rfiledate, isMorning);
					filewriter(initial_depth_down, currentdir + writedir + "\\initial_depth" + datayear + "\\after_down", rfiledate, isMorning);
					filewriter(limit_buy_line, currentdir + writedir + "\\arrival_time_series" + datayear + "\\limit_buy", rfiledate, isMorning);
					filewriter(limit_sell_line, currentdir + writedir + "\\arrival_time_series" + datayear + "\\limit_sell", rfiledate, isMorning);
					filewriter(market_buy_line, currentdir + writedir + "\\arrival_time_series" + datayear + "\\market_buy", rfiledate, isMorning);
					filewriter(market_sell_line, currentdir + writedir + "\\arrival_time_series" + datayear + "\\market_sell", rfiledate, isMorning);

					// initialize (morning, afternoon session に分かれている日のため)
					freq_market_buy    = 0;
					freq_market_sell   = 0;
					freq_cancel_buy    = 0;
					freq_cancel_sell   = 0;
					freq_limit_buy     = 0;
					freq_limit_sell    = 0;
					pieces_market_buy  = new ArrayList<Integer>();
					pieces_market_sell = new ArrayList<Integer>();
					pieces_cancel_buy  = new ArrayList<Integer>();
					pieces_cancel_sell = new ArrayList<Integer>();
					pieces_limit_buy   = new ArrayList<Integer>();
					pieces_limit_sell  = new ArrayList<Integer>();
					up_times_bid       = 0;
					down_times_bid     = 0;
					up_times_ask       = 0;
					down_times_ask     = 0;
					initial_depth_up   = new ArrayList<String>();
					initial_depth_down = new ArrayList<String>();
					limit_buy_line     = new ArrayList<String>();
					limit_sell_line     = new ArrayList<String>();
					market_buy_line     = new ArrayList<String>();
					market_sell_line    = new ArrayList<String>();
					move_frequency     = new ArrayList<Integer>();
					operating_time_bid   = new ArrayList<Integer>();
					operating_time_ask   = new ArrayList<Integer>();
					continuous = false;
					isInit = true;
					bid_up_move = false;
					bid_down_move = false;
					market_buy_order = false;
					market_sell_order = false;
				}

				if (continuous && isInit) {
					// 最良気配に初期値を入れる．
					if (line.split(",", -1)[2].equals("Quote")) {
						bidpricetemp = Integer.parseInt(line.split(",", -1)[5]);
						biddepthtemp = Integer.parseInt(line.split(",", -1)[6]);
						askpricetemp = Integer.parseInt(line.split(",", -1)[7]);
						askdepthtemp = Integer.parseInt(line.split(",", -1)[8]);
						limit_buy_line.add(inttime + ",,,,opening");
						limit_sell_line.add(inttime + ",,,,opening");
						market_buy_line.add(inttime + ",,,,opening");
						market_sell_line.add(inttime + ",,,,opening");
						move_freq_time_temp = inttime;
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
							limit_buy_line.add(inttime + ",,,,bidup");
							market_sell_line.add(inttime + ",,,,bidup");
							bid_up_move = true;
							up_times_bid++;
							operating_time_bid_temp = inttime;
							if (!market_buy_order) {
								/* 直前に成行買い注文が無くて価格が上昇した場合，指値注文として記録する． */
								freq_limit_buy++;
								limit_buy_line.add(inttime + "," + bidprice + "," + biddepth + "," + continuoustime + ",");
							}
						} else if (bidprice == bidpricetemp) {
							if (biddepth > biddepthtemp) {
								/*
								 * 買い気配数量が増加したら
								 * (1)指値注文の時間間隔を記録する．
								 * (2)買いの指値注文として数える．
								 * (3)増加分は指値注文数として記録する．
								 */
								limit_buy_line.add(inttime + "," + bidprice + "," + (biddepth - biddepthtemp) + "," + continuoustime + ",");
								freq_limit_buy++;
								pieces_limit_buy.add(biddepth - biddepthtemp);
							} else if (biddepth < biddepthtemp) {
								/*
								 * 買い気配数量が減少したら，直前に買い気配値での約定があった場合のみ
								 * (1)成行注文の時間間隔を記録する．
								 * (2)減少分は成行注文数として記録する．
								 */
								if (market_sell_order) {
									market_sell_line.add(inttime + "," + bidprice + "," + (biddepthtemp - biddepth) + "," + continuoustime + ",traded");
									pieces_market_sell.add(biddepthtemp - biddepth);
								} else {
									freq_cancel_buy++;
									market_sell_line.add(inttime + "," + bidprice + "," + (biddepthtemp - biddepth) + "," + continuoustime + ",canceled");
									pieces_cancel_buy.add(biddepthtemp - biddepth);
								}
							}
						} else {
							bid_down_move = true;
							down_times_bid++;
							limit_buy_line.add(inttime + ",,,,biddown");
							market_sell_line.add(inttime + ",,,,biddown");
							if (market_sell_order) {
								/*
								 * 買いの最良気配が0になって板が下に移動した場合，
								 * (1)売りの成行注文時間間隔を記録する．
								 * (2)買い気配が下に移動しても最良売り気配が移動していない場合がある．売り成行注文枚数の記録は
								 * 直後の気配値の幅で場合分けする．
								 * (3)買いの最良気配の消滅時間として記録する．
								 */
								if (askprice - bidprice == 10) {
									pieces_market_sell.add(tradevolume + askdepth);
									market_sell_line.add(inttime + "," + bidprice + "," + tradevolume + askdepth + "," + continuoustime + ",traded");
								} else if (askprice - bidprice > 10) {
									pieces_market_sell.add(tradevolume);
									market_sell_line.add(inttime + "," + bidprice + "," + tradevolume + "," + continuoustime + ",traded");
								}
								operating_time_bid.add(sc.time_diff_in_seconds(operating_time_bid_temp, inttime));
							} else {
								freq_cancel_buy++;
								pieces_cancel_buy.add(biddepthtemp);
								market_sell_line.add(inttime + "," + bidprice + "," + biddepthtemp + "," + continuoustime + ",canceled");
							}
							operating_time_bid_temp = inttime;
						}

						if (askprice > askpricetemp) {
							if (bid_up_move) {
								move_frequency.add(sc.time_diff_in_seconds(move_freq_time_temp, inttime));
								move_freq_time_temp = inttime;
								pw[1].println(line);
								initial_depth_up.add(line);
							}
							up_times_ask++;
							limit_sell_line.add(inttime + ",,,,askup");
							market_buy_line.add(inttime + ",,,,askup");
							if (market_buy_order) {
								/*
								 * 売りの最良気配が0になって板が上に移動した場合，
								 * (1)買いの成行注文時間間隔を記録する．
								 * (2)売り気配が上に移動しても最良買い気配が移動していない場合がある．買い成行注文枚数の記録は
								 * 直後の気配値の幅で場合分けする．
								 * (3)売りの最良気配の消滅時間として記録する．
								 */
								if (askprice - bidprice == 10) {
									pieces_market_buy.add(tradevolume + biddepth);
									market_buy_line.add(inttime + "," + askprice + "," + tradevolume + biddepth + "," + continuoustime + ",traded");
								} else if (askprice - bidprice > 10) {
									pieces_market_buy.add(tradevolume);
									market_buy_line.add(inttime + "," + askprice + "," + tradevolume + "," + continuoustime + ",traded");
								}
								operating_time_ask.add(sc.time_diff_in_seconds(operating_time_ask_temp, inttime));
							} else {
								freq_cancel_sell++;
								pieces_cancel_sell.add(askdepthtemp);
								market_buy_line.add(inttime + "," + askprice + "," + askdepthtemp + "," + continuoustime + ",canceled");
							}
							operating_time_ask_temp = inttime;
						} else if (askprice == askpricetemp) {
							if (askdepth > askdepthtemp) {
								/*
								 * 売り気配数量が増加したら
								 * (1)時間間隔を記録する．
								 * (2)売りの指値注文として数える．
								 * (3)増加分は指値注文数として記録する．
								 */
								limit_sell_line.add(inttime + "," + askprice + "," + (askdepth - askdepthtemp) + "," + continuoustime + ",");
								freq_limit_sell++;
								pieces_limit_sell.add(askdepth - askdepthtemp);
							} else if (askdepth < askdepthtemp) {
								/* 売り気配数量が減少したら，直前に売り気配値での約定があった場合のみ
								 * (1)成行注文の時間間隔を記録する．
								 * (2)減少分は成行注文数として記録する．
								 */
								if (market_buy_order) {
									market_buy_line.add(inttime + "," + askprice + "," + (askdepthtemp - askdepth) + "," + continuoustime + ",traded");
									pieces_market_buy.add(askdepthtemp - askdepth);
								} else {
									freq_cancel_sell++;
									market_buy_line.add(inttime + "," + askprice + "," + (askdepthtemp - askdepth) + "," + continuoustime + ",canceled");
									pieces_cancel_sell.add(askdepthtemp - askdepth);
								}
							}
						} else {
							limit_sell_line.add(inttime + ",,,,askdown");
							market_buy_line.add(inttime + ",,,,askdown");
							if (bid_down_move) {
								move_frequency.add(sc.time_diff_in_seconds(move_freq_time_temp, inttime));
								move_freq_time_temp = inttime;
								pw[2].println(line);
								initial_depth_down.add(line);
							}
							down_times_ask++;
							operating_time_ask_temp = inttime;
							if (!market_sell_order) {
								/* 直前に成行売り注文が無くて価格が下降した場合，指値注文として記録する． */
								freq_limit_sell++;
								limit_sell_line.add(inttime + "," + askprice + "," + askdepth + "," + continuoustime + ",");
							}
						}

						bidpricetemp = bidprice; // 最良買い気配値を更新
						biddepthtemp = biddepth; // 最良買い気配数量を更新
						askpricetemp = askprice; // 最良売り気配値を更新
						askdepthtemp = askdepth; // 最良売り気配数量を更新
						bid_up_move = false;
						bid_down_move = false;
						market_buy_order = false;
						market_sell_order = false;
					}

					if (line.split(",", -1)[2].equals("Trade")) {

						tradeprice = Integer.parseInt(line.split(",", -1)[3]);
						tradevolume = Integer.parseInt(line.split(",", -1)[4]);
						if (tradeprice == askprice) {
							freq_market_buy++;
							market_buy_order = true;
						} else if (tradeprice == bidprice) {
							freq_market_sell++;
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
