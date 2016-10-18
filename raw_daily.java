import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
//(nikkei needs)2010年7月16日前のフォーマットでの日経平均先物の約定データ，最良気配値のデータ(限月調整済み)として抽出された
//データを日毎のデータに分割する．日ごとにファイルは複数存在することになるが，一日一ファイルの加工は後にやる．
public class raw_daily{

    public static void main(String[] args) throws IOException{

    	String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
    	String datayear[] = {"\\2006", "\\2007"};

    	//取り出すデータに関する変数の定義
    	String fileID; //rawファイルの識別番号．200605_2_.txt　なら _ で挟まれる　2　に相当する部分．
    	String rfiledate; //読み込むデータの日付(年月日)
    	String wfiledate = ""; //書き込むファイルの日付(年月日)

    	for(String dy: datayear){
    		File rfilepath = new File(currentdir + dy + "\\raw"); //読み込むファイルのディレクトリのパス．
            File[] filelist = rfilepath.listFiles(); //読み込むファイル名を取得する．
            for(int i=1; i<filelist.length; i++){

            	fileID = filelist[i].getAbsolutePath().split("_")[4];

            	//扱うファイル名の取得と書き出すファイルの指定
            	FileReader fr = new FileReader(filelist[i]);
                BufferedReader brtxt = new BufferedReader(fr);
                String line = "";
                File file = new File(currentdir + dy + "\\raw_daily\\" + "dummy_.txt" ); //pw.close()のエラー回避．
             	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

                while ((line = brtxt.readLine()) != null) { //txtファイル名を一行ずつロードする.
                	rfiledate = line.substring(4,12);
                	if(!wfiledate.equals(rfiledate)){
                		wfiledate = rfiledate;
                		pw.close();
                		file = new File(currentdir + dy + "\\raw_daily\\" + wfiledate + "_" + fileID + "_.txt" );
                     	pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                	}
                	pw.println(line);
                }
                pw.close();
                file = new File(currentdir + dy + "\\raw_daily\\" + "dummy_.txt" );
                file.delete(); //ダミーファイルを削除
                brtxt.close();
                fr.close();
            }
    	}
    }
}


