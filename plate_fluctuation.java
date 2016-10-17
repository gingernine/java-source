import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

//(ロイターのコンマ秒データ)2011~2013板が変わったところを抽出する．
public class plate_fluctuation {

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src";
		String datadir = "\\reuter_data";
		BufferedReader br = new BufferedReader(new FileReader(currentdir + datadir + "\\filelist.txt"));// 読み取りたいファイル名の記入．
		String txtFileName;
		Map<String, String> month = new HashMap<String, String>() {
			{put("JAN","01");} {put("FEB","02");} {put("MAR","03");} {put("APR","04");}
			{put("MAY","05");} {put("JUN","06");} {put("JUL","07");} {put("AUG","08");}
			{put("SEP","09");} {put("OCT","10");} {put("NOV","11");} {put("DEC","12");}
		};

		while ((txtFileName = br.readLine()) != null) {

			String[] seperated;// データをカンマで分割する．
			String bid[] = new String[2];// 最良買い気配値を一時保存して比較する．
			String ask[] = new String[2];// 最良売り気配値を一時保存して比較する．
			String index[] = new String[5000000];// 気配値が変化した箇所の行を保存する．
			String linetmp = "";
			String date = "";// 取引日
			double time = 0;// データが記録された時刻
			int count = 0;//データ行数
			;

			FileReader fr = new FileReader(txtFileName);
			BufferedReader brtxt = new BufferedReader(fr);
			String line = "";
			boolean isFirst = true;// 以下のwhile文で，各取引日ご毎に，ループが1巡目か2巡目以後かを判定する．

			while ((line = brtxt.readLine()) != null) {

				seperated = line.split(",", -1);// 空欄がある場合にも分割数を揃えるため -1 をつける．
				try {
					// 取引時間は9:00 ~ 15:15.
					time = Double.parseDouble(seperated[2].replace(":", ""));//時刻をダブル型数値に変換．
				} catch (Exception e) {
					continue;
				}

				if (!seperated[6].equals("")) {
					// 板の変動ではなくて約定に反応した場合はスキップ．
					linetmp = line;
					continue;
				}

				if (seperated[1].equals(date) && (90000.000 <= time) && (time <= 151500.000)) {
					// 日が違うデータは区別する．

					bid[1] = seperated[8];
					ask[1] = seperated[10];

					if (!bid[1].equals(bid[0]) || !ask[1].equals(ask[0])) {
						if(!linetmp.equals("")){
							//最良気配値が変わるときはおそらく直前の約定が原因．
							//約定の売買高と直後の板の厚さを足せばいくらの注文数で板が動いたかわかる．
							index[count] = linetmp;
							count++;
							linetmp = "";
						}
						index[count] = line;
						count++;
						bid[0] = bid[1];
						ask[0] = ask[1];
					}

				} else {
					//一日ごとにファイルに書き出していく．
					if (!isFirst) {
						String outputdir = "\\reuter_output\\";
						String fileID1 = seperated[1].split("-")[0];
						String fileID2 = month.get(seperated[1].split("-")[1]);
						String fileID3 = seperated[1].split("-")[2];
						File file = new File(currentdir + outputdir + fileID3 + fileID2 + fileID1 + "_book.txt");
						PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
						for(int i = 0;i<count;i++){
							pw.println(index[i]);
						}
						pw.close();
					}

					isFirst = true;// 日が変わったら下のif文を実行する．
					count = 0;
				}

				if (isFirst && (90000.000 <= time) && (time <= 151500.000)) {
					index[count] = line;
					count++;
					bid[0] = seperated[8];
					ask[0] = seperated[10];
					date = seperated[1];
					isFirst = false;// ループ1巡目が終わった．
				}

			}
			brtxt.close();
			fr.close();
		}

		br.close();

	}
}
