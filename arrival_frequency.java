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
		String datayear = "\\2007";
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
			List<String> transient_prob        = new ArrayList<String>(); // 推移行列の計算のため推移の仕方を記録する
			List<String> move_record           = new ArrayList<String>(); // 板の推移を記録する．
			List<String> sessionsep            = new ArrayList<String>(); // 一日のデータをセッションに分けてデータを作る．
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
			int inttimetemp = 0;
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
					move_record.add(line+",opening");
					sessionsep.add(line+",opening");
					continuous = true;
					continue;
				}
				if (Arrays.asList(closing).contains(line)) {
					move_record.add(line+",closing");
					sessionsep.add(line+",closing");

					if (freq_limit_sell != pieces_limit_sell.size()){
						System.out.println("1:" + freq_limit_sell + "," + pieces_limit_sell.size());
					} if (freq_limit_buy != pieces_limit_buy.size()) {
						System.out.println("2:" + freq_limit_buy + "," + pieces_limit_buy.size());
					} if (freq_market_sell != pieces_market_sell.size()) {
						System.out.println("3:" + freq_market_sell + "," + pieces_market_sell.size());
					} if (freq_market_buy != pieces_market_buy.size()) {
						System.out.println("4:" + freq_market_buy + "," + pieces_market_buy.size());
					} if (freq_cancel_sell != pieces_cancel_sell.size()) {
						System.out.println("5:" + freq_cancel_sell + "," + pieces_cancel_sell.size());
					} if (freq_cancel_buy != pieces_cancel_buy.size()){
						System.out.println("6:" + freq_cancel_buy + "," + pieces_cancel_buy.size());
					} if (freq_limit_sell != limit_sell_line.size()) {
						System.out.println("7:" + freq_limit_sell + "," + limit_sell_line.size());
					} if (freq_limit_buy != limit_buy_line.size()) {
						System.out.println("8:" + freq_limit_buy + "," + limit_buy_line.size());
					} if (freq_market_sell + freq_cancel_buy != market_sell_line.size()) {
						System.out.println("9:" + (freq_market_sell + freq_cancel_buy) + "," + market_sell_line.size());
					} if (freq_market_buy + freq_cancel_sell != market_buy_line.size()) {
						System.out.println("10:" + (freq_market_buy + freq_cancel_sell) + "," + market_buy_line.size());
					}

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
					filewriter(transient_prob, currentdir + writedir + "\\transition_probability" + datayear + "\\observed", rfiledate, isMorning);
					filewriter(move_record, currentdir + writedir + "\\move_frequency" + datayear, rfiledate, isMorning);
					filewriter(sessionsep, currentdir + datayear + "\\sessionsep", rfiledate, isMorning);

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
					transient_prob       = new ArrayList<String>();
					move_record          = new ArrayList<String>();
					sessionsep           = new ArrayList<String>();
					continuous = false;
					isInit = true;
					bid_up_move = false;
					bid_down_move = false;
					market_buy_order = false;
					market_sell_order = false;
					inttimetemp = 0;
				}

				if (continuous && isInit) {
					sessionsep.add(line);
					// 最良気配に初期値を入れる．
					if (line.split(",", -1)[2].equals("Quote")) {
						bidpricetemp = Integer.parseInt(line.split(",", -1)[5]);
						biddepthtemp = Integer.parseInt(line.split(",", -1)[6]);
						askpricetemp = Integer.parseInt(line.split(",", -1)[7]);
						askdepthtemp = Integer.parseInt(line.split(",", -1)[8]);
						limit_buy_line.add(inttime + ",,," + continuoustime + ",opening");
						limit_sell_line.add(inttime + ",,," + continuoustime + ",opening");
						market_buy_line.add(inttime + ",,," + continuoustime + ",opening");
						market_sell_line.add(inttime + ",,," + continuoustime + ",opening");
						move_freq_time_temp = inttime;
						isInit = false; // 買い気配に初期値を入れたら初期化完了．
					}
					continue;
				}

				if (continuous && !isInit) {
					sessionsep.add(line);
					// ザラバのみデータ抽出．
					if (line.split(",", -1)[2].equals("Quote")) {

						// 現在の最良気配値・数量を取得する．
						bidprice = Integer.parseInt(line.split(",", -1)[5]);
						biddepth = Integer.parseInt(line.split(",", -1)[6]);
						askprice = Integer.parseInt(line.split(",", -1)[7]);
						askdepth = Integer.parseInt(line.split(",", -1)[8]);

						if (market_sell_order) {
							bid_down_move = true;
							down_times_bid++;
							limit_buy_line.add(inttime + ",,,,biddown");
							market_sell_line.add(inttime + ",,,,biddown");
							if (askprice - bidpricetemp == 10) {
								/*
								 * 成行売り注文によって価格が下落した場合，今の最良売り気配値と前の最良買い気配値が等しいなら
								 * 成行注文枚数は約定枚数と気配数量の和とする．
								 */
								pieces_market_sell.add(tradevolume + askdepth);
								market_sell_line.add(inttimetemp + "," + tradeprice + "," + (tradevolume + askdepth) + "," + continuoustime + ",traded");
								operating_time_bid.add(sc.time_diff_in_seconds(operating_time_bid_temp, inttime));
							} else if (askprice - bidpricetemp > 10) {
								pieces_market_sell.add(tradevolume);
								market_sell_line.add(inttimetemp + "," + tradeprice + "," + tradevolume + "," + continuoustime + ",traded");
								operating_time_bid.add(sc.time_diff_in_seconds(operating_time_bid_temp, inttime));
							}
							operating_time_bid_temp = inttime;
						} else if (bidprice > bidpricetemp) {
							limit_buy_line.add(inttime + ",,,,bidup");
							market_sell_line.add(inttime + ",,,,bidup");
							bid_up_move = true;
							up_times_bid++;
							operating_time_bid_temp = inttime;
							if (askpricetemp - bidpricetemp > 10) {
								/* 直前に1ティック以上離れていて価格が上昇した場合，指値注文として記録する． */
								freq_limit_buy++;
								limit_buy_line.add(inttime + "," + bidprice + "," + biddepth + "," + continuoustime + ",");
								pieces_limit_buy.add(biddepth);
							}
						} else if (bidprice == bidpricetemp) {
							if (biddepth > biddepthtemp) {
								freq_limit_buy++;
								limit_buy_line.add(inttime + "," + bidprice + "," + (biddepth - biddepthtemp) + "," + continuoustime + ",");
								pieces_limit_buy.add(biddepth - biddepthtemp);
							} else if (biddepth < biddepthtemp) {
								freq_cancel_buy++;
								market_sell_line.add(inttime + "," + bidprice + "," + (biddepthtemp - biddepth) + "," + continuoustime + ",canceled");
								pieces_cancel_buy.add(biddepthtemp - biddepth);
							}
						} else {
							freq_cancel_buy++;
							pieces_cancel_buy.add(biddepthtemp);
							market_sell_line.add(inttime + "," + bidpricetemp + "," + biddepthtemp + "," + continuoustime + ",canceled");
							operating_time_bid_temp = inttime;
						}

						if (market_buy_order) {
							if (bid_up_move) {
								transient_prob.add("up");
								move_frequency.add(sc.time_diff_in_seconds(move_freq_time_temp, inttime));
								move_freq_time_temp = inttime;
								pw[1].println(line);
								move_record.add(line+",up");
								initial_depth_up.add(line);
							}
							up_times_ask++;
							limit_sell_line.add(inttime + ",,,,askup");
							market_buy_line.add(inttime + ",,,,askup");
							if (askpricetemp - bidprice == 10) {
								/*
								 * 成行買い注文によって価格が上昇した場合，今の最良買い気配値と前の最良売り気配値が等しいなら
								 * 成行注文枚数は約定枚数と気配数量の和とする．
								 */
								pieces_market_buy.add(tradevolume + biddepth);
								market_buy_line.add(inttimetemp + "," + tradeprice + "," + (tradevolume + biddepth) + "," + continuoustime + ",traded");
								operating_time_ask.add(sc.time_diff_in_seconds(operating_time_ask_temp, inttime));
							} else if (askpricetemp - bidprice > 10) {
								pieces_market_buy.add(tradevolume);
								market_buy_line.add(inttimetemp + "," + tradeprice + "," + tradevolume + "," + continuoustime + ",traded");
								operating_time_ask.add(sc.time_diff_in_seconds(operating_time_ask_temp, inttime));
							}
							operating_time_ask_temp = inttime;
						} else if (askprice > askpricetemp) {
							freq_cancel_sell++;
							pieces_cancel_sell.add(askdepthtemp);
							market_buy_line.add(inttime + "," + askpricetemp + "," + askdepthtemp + "," + continuoustime + ",canceled");
							operating_time_ask_temp = inttime;
						} else if (askprice == askpricetemp) {
							if (askdepthtemp > askdepth) {
								freq_cancel_sell++;
								market_buy_line.add(inttime + "," + askprice + "," + (askdepthtemp - askdepth) + "," + continuoustime + ",canceled");
								pieces_cancel_sell.add(askdepthtemp - askdepth);
							} else if (askdepthtemp < askdepth) {
								freq_limit_sell++;
								limit_sell_line.add(inttime + "," + askprice + "," + (askdepth - askdepthtemp) + "," + continuoustime + ",");
								pieces_limit_sell.add(askdepth - askdepthtemp);
							}
						} else {
							limit_sell_line.add(inttime + ",,,,askdown");
							market_buy_line.add(inttime + ",,,,askdown");
							if (bid_down_move) {
								transient_prob.add("down");
								move_frequency.add(sc.time_diff_in_seconds(move_freq_time_temp, inttime));
								move_freq_time_temp = inttime;
								pw[2].println(line);
								move_record.add(line+",down");
								initial_depth_down.add(line);
							}
							down_times_ask++;
							operating_time_ask_temp = inttime;
							if (askpricetemp - bidpricetemp > 10) {
								/* 直前に2ティック以上離れていて価格が下降した場合，指値注文として記録する． */
								freq_limit_sell++;
								limit_sell_line.add(inttime + "," + askprice + "," + askdepth + "," + continuoustime + ",");
								pieces_limit_sell.add(askdepth);
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
						inttimetemp = inttime;
						if (tradeprice == askpricetemp) {
							freq_market_buy++;
							askdepthtemp -= tradevolume;
							if (askdepthtemp == 0) {
								askpricetemp += 10;
								market_buy_order = true;
								continue;
							}
							pieces_market_buy.add(tradevolume);
							market_buy_line.add(inttimetemp + "," + tradeprice + "," + tradevolume + "," + continuoustime + ",traded");
						} else if (tradeprice == bidpricetemp) {
							freq_market_sell++;
							biddepthtemp -= tradevolume;
							if (biddepthtemp == 0) {
								bidpricetemp -= 10;
								market_sell_order = true;
								continue;
							}
							pieces_market_sell.add(tradevolume);
							market_sell_line.add(inttimetemp + "," + tradeprice + "," + tradevolume + "," + continuoustime + ",traded");
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
