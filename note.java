					if (line.split(",", -1)[2].equals("Quote")) {

						// 現在の最良気配値・数量を取得する．
						bidprice = Integer.parseInt(line.split(",", -1)[5]);
						biddepth = Integer.parseInt(line.split(",", -1)[6]);
						askprice = Integer.parseInt(line.split(",", -1)[7]);
						askdepth = Integer.parseInt(line.split(",", -1)[8]);

						if (market_buy_order) {
							/*
							 * 直前に成行買い注文があった場合の処理
							 * 成行買い注文はask price が上昇，下落，変化なしの場合で条件分岐
							 * */
							if (askprice > askpricetemp) {
								/* 価格上昇後，最良買い気配値が上昇した場合，成行注文枚数は約定数とbiddepthの和とする．*/
								if (askpricetemp == bidprice) {
									pieces_market_buy.add(tradevolume + biddepth);
									market_buy_line.add(inttimetemp + "," + askprice + "," + (tradevolume + biddepth) + "," + continuoustime + ",traded");
								} else {
									pieces_market_buy.add(tradevolume);
									market_buy_line.add(inttimetemp + "," + tradeprice + "," + tradevolume + "," + continuoustime + ",traded");
								}
							} else if (askprice == askpricetemp) {
								/*
								 * 価格が変化しない場合，成行注文枚数は約定枚数に等しいとする．
								 * しかし約定枚数と累積枚数の変化が一致しない場合がある．
								 * 不一致部分は売り注文のキャンセル亦は売り注文の指値注文として記録する．
								 * */
								pieces_market_buy.add(tradevolume);
								market_buy_line.add(inttimetemp + "," + tradeprice + "," + tradevolume + "," + continuoustime + ",traded");
								if (askdepthtemp - askdepth > buyvolumetemp) {
									/* 約上で減る分よりも次点のask depthが減っている場合，キャンセルとして記録する． */
									freq_cancel_sell++;
									pieces_cancel_sell.add(askdepthtemp - askdepth - buyvolumetemp);
									market_buy_line.add(inttime + "," + askprice + "," + (askdepthtemp - askdepth - buyvolumetemp) + "," + continuoustime + ",canceled");
								} else if (askdepthtemp - askdepth < buyvolumetemp) {
									/* 約上で減る分よりも次点のask depthが多い場合，指値注文として記録する． */
									freq_limit_sell++;
									pieces_limit_sell.add(buyvolumetemp - askdepthtemp + askdepth);
									limit_sell_line.add(inttime + "," + askprice + "," + (buyvolumetemp - askdepthtemp + askdepth) + "," + continuoustime + ",");
								}
							} else {
								/* 価格が下落した場合，
								 * (1)成行売り注文が発生した
								 * (2)2ティック以上離れていて，下の価格帯に売り指値注文が来た
								 * の二通り考えられる．*/
								if (askpricetemp - bidpricetemp > 10) {
									freq_limit_sell++;
									limit_sell_line.add(inttime + "," + askprice + "," + askdepth + "," + continuoustime + ",");
									pieces_limit_sell.add(askdepth);
								}
								continue;
							}

						} else if (market_sell_order) {
							/* 直前に成行売り注文があった場合の処理  */
						} else {
							/* 直前に約定がない場合の処理  */
						}

						if (bidprice > bidpricetemp) {
							//limit_buy_line.add(inttime + ",,,,bidup");
							//market_sell_line.add(inttime + ",,,,bidup");
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
								/*
								 * 買い気配数量が増加したら
								 * (1)買いの指値注文として数える．
								 * (2)指値注文の時間間隔を記録する．
								 * (3)増加分は指値注文枚数として記録する．
								 */
								freq_limit_buy++;
								limit_buy_line.add(inttime + "," + bidprice + "," + (biddepth - biddepthtemp) + "," + continuoustime + ",");
								pieces_limit_buy.add(biddepth - biddepthtemp);
							} else if (biddepth < biddepthtemp) {
								/*
								 * 買い気配数量が減少した場合，直前に買い気配値で約定があったかどうかで場合分けする．
								 */
								if (market_sell_order) {
									market_sell_line.add(inttimetemp + "," + tradeprice + "," + tradevolume + "," + continuoustime + ",traded");
									pieces_market_sell.add(tradevolume);
									if (biddepthtemp - biddepth > sellvolumetemp) {
										/* 約上で減る分よりも次点のbid depthが減っている場合，キャンセルとして記録する． */
										freq_cancel_buy++;
										market_sell_line.add(inttime + "," + bidprice + "," + (biddepthtemp - biddepth - sellvolumetemp) + "," + continuoustime + ",canceled");
										pieces_cancel_buy.add(biddepthtemp - biddepth - sellvolumetemp);
									} else if (biddepthtemp - biddepth < sellvolumetemp) {
										/* 約上で減る分よりも次点のbid depthが多い場合，指値注文として記録する． */
										freq_limit_buy++;
										limit_buy_line.add(inttime + "," + bidprice + "," + (sellvolumetemp - biddepthtemp + biddepth) + "," + continuoustime + ",");
										pieces_limit_buy.add(sellvolumetemp - biddepthtemp + biddepth);
									}
								} else {
									freq_cancel_buy++;
									market_sell_line.add(inttime + "," + bidprice + "," + (biddepthtemp - biddepth) + "," + continuoustime + ",canceled");
									pieces_cancel_buy.add(biddepthtemp - biddepth);
								}
							}
						} else {
							bid_down_move = true;
							down_times_bid++;
							//limit_buy_line.add(inttime + ",,,,biddown");
							//market_sell_line.add(inttime + ",,,,biddown");
							if (market_sell_order) {
								/*
								 * 買いの最良気配が0になって板が下に移動した場合，
								 * (1)売りの成行注文時間間隔を記録する．
								 * (2)買い気配が下に移動しても最良売り気配が移動していない場合がある．売り成行注文枚数の記録は
								 * 直後の気配値の幅で場合分けする．
								 * (3)買いの最良気配の消滅時間として記録する．
								 */
								if (askprice == bidpricetemp) {
									pieces_market_sell.add(tradevolume + askdepth);
									market_sell_line.add(inttimetemp + "," + bidprice + "," + (tradevolume + askdepth) + "," + continuoustime + ",traded");
								} else if (askprice > bidpricetemp) {
									pieces_market_sell.add(tradevolume);
									market_sell_line.add(inttimetemp + "," + bidprice + "," + tradevolume + "," + continuoustime + ",traded");
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
							//limit_sell_line.add(inttime + ",,,,askup");
							//market_buy_line.add(inttime + ",,,,askup");
							if (market_buy_order) {
								/*
								 * 売りの最良気配が0になって板が上に移動した場合，
								 * (1)買いの成行注文時間間隔を記録する．
								 * (2)売り気配が上に移動しても最良買い気配が移動していない場合がある．買い成行注文枚数の記録は
								 * 直後の気配値の幅で場合分けする．
								 * (3)売りの最良気配の消滅時間として記録する．
								 */
								if (askpricetemp == bidprice) {
								} else if (askpricetemp > bidprice) {
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
								 * (1)売りの指値注文として数える．
								 * (2)時間間隔を記録する．
								 * (3)増加分は指値注文数として記録する．
								 */
								freq_limit_sell++;
								limit_sell_line.add(inttime + "," + askprice + "," + (askdepth - askdepthtemp) + "," + continuoustime + ",");
								pieces_limit_sell.add(askdepth - askdepthtemp);
							} else if (askdepth < askdepthtemp) {
								/*
								 * 売り気配数量が減少した場合，直前に売り気配値で約定があったかどうかで場合分けする．
								 */
								if (market_buy_order) {

								} else {
									freq_cancel_sell++;
									market_buy_line.add(inttime + "," + askprice + "," + (askdepthtemp - askdepth) + "," + continuoustime + ",canceled");
									pieces_cancel_sell.add(askdepthtemp - askdepth);
								}
							}
						} else {
							//limit_sell_line.add(inttime + ",,,,askdown");
							//market_buy_line.add(inttime + ",,,,askdown");
							if (bid_down_move) {
								move_frequency.add(sc.time_diff_in_seconds(move_freq_time_temp, inttime));
								move_freq_time_temp = inttime;
								pw[2].println(line);
								initial_depth_down.add(line);
							}
							down_times_ask++;
							operating_time_ask_temp = inttime;

						}

						bidpricetemp = bidprice; // 最良買い気配値を更新
						biddepthtemp = biddepth; // 最良買い気配数量を更新
						askpricetemp = askprice; // 最良売り気配値を更新
						askdepthtemp = askdepth; // 最良売り気配数量を更新
						bid_up_move = false;
						bid_down_move = false;
						market_buy_order = false;
						market_sell_order = false;
						buyvolumetemp = 0;
						sellvolumetemp = 0;
					}