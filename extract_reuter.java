import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Date;

//ロイターのコンマ秒データから，日次データを作成する．
public class extract_reuter{

	public static void main(String[] args) throws IOException {

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src";
		String wfiledir = "\\reuter_output\\";
		String rfiledate = "";
		String datapath = "E:\\日経平均先物\\コンマ秒データ\\2016年取得データ";
		String maturity[] = {"03", "06", "09", "12"}; //限月．
		Map<String, Integer> settlementdate = new HashMap<>(); //精算日．

		File rfilepath = new File(datapath); //読み込むファイルのディレクトリのパス．
		File[] filelist = rfilepath.listFiles(); //読み込むファイル名を取得する．

		for(int i=0; i<filelist.length; i++){

			String rfileyear = filelist[i].getAbsolutePath().split("_")[1].split("\\.")[0]; //読み込むファイルの年を格納．
			String[] seperated;// データをカンマで分割する．
			String rline = ""; //読み込む行を格納する．
			String wline = ""; //書き込む行を作成する．
			String JNI; //期近限月の調整のため，ファイルの種類が JNIc1 か JNIc2 かを区別する．
			String month; //読み込むデータの月を格納する．
			int date; //読み込むデータの日を格納する．
			boolean isInit = true; //はじめの行は飛ばす．

			FileReader fr = new FileReader(filelist[i]);
			BufferedReader brtxt = new BufferedReader(fr);

			File newdir = new File(currentdir + wfiledir + rfileyear + "\\daily");
			if(!newdir.exists()) {
				//存在しないフォルダを作成する．
				newdir.mkdirs();
			}

			//rfileyearに格納した年の限月の清算日を計算する．
			Calendar cal = Calendar.getInstance();
			int y = Integer.parseInt(rfileyear);
			for (int j=0; j<4; j++){
				int m = Integer.parseInt(maturity[j]);
				for (int d=1; d<8; d++){
					cal.set(y, m-1, d); //曜日をセット．月番号は0から11まで．
					if(cal.get(Calendar.DAY_OF_WEEK) == 6){
						//金曜日の数字は6．
						settlementdate.put(maturity[j], d+7);
						break;
					}
				}
			}

			System.out.println(rfileyear + settlementdate);

			File file = new File(currentdir + wfiledir + "dummy.txt"); //エラー回避のダミーファイル．
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			while ((rline = brtxt.readLine()) != null) {

				if(isInit){
					isInit = false;
					continue;
				}

				seperated = rline.split(",", -1);// 空欄がある場合にも分割数を揃えるため -1 をつける．
				JNI = seperated[0];
				month = seperated[2].substring(4,6);
				date = Integer.parseInt(seperated[2].substring(6,8));

				if(JNI.equals("JNIc1") && Arrays.asList(maturity).contains(month)){
					if(date < settlementdate.get(month)){
						//ロイターの期近限月のファイルで，データが精算日前日までのものであるならスキップする．
						continue;
					}
				} else if (JNI.equals("JNIc2") && Arrays.asList(maturity).contains(month)){
					if(date >= settlementdate.get(month)){
						//二番目に近い限月のファイルで，データが限月の精算日後のものならスキップする．
						continue;
					}
				} else if (JNI.equals("JNIc2") && !Arrays.asList(maturity).contains(month)){
					//二番目に近い限月のファイルで，データが限月のものでないならスキップする．
					continue;
				}

				//一日ごとにファイルに書き出していく．
				if (!rfiledate.equals(seperated[2])) {
					//日にちを更新して新規日付のファイルを作成する．
					pw.close();
					rfiledate = seperated[2];
					System.out.println(JNI);
					System.out.println(rfiledate);
					file = new File(currentdir + wfiledir + rfileyear + "\\daily\\" + rfiledate + "_.csv");
					pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				} else {
					if(!seperated[4].equals("Mkt.Condition")){
						//"Mkt.Condition"の行は入れない．
						wline = seperated[2] + "," + seperated[3] + "," + seperated[4] + "," + seperated[5] + "," +
								seperated[6] + "," + seperated[7] + "," + seperated[8] + "," + seperated[9] + "," +
								seperated[10] + "," + seperated[11] + ",";
						pw.println(wline);
					}
				}
			}
			pw.close();
			brtxt.close();
			fr.close();
		}
	}
}
