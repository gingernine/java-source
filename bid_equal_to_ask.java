import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
//前回まで：(nikkei needs)2010年7月16日以後のフォーマットでの日経平均先物の約定データ，最良気配値のデータ(限月調整済み)を抽出するプログラム
//		上で抽出したデータを用いて，ロイター社のコンマ秒データの形式に近いものを作成する．
//
//今回:rawcsvフォルダのdailyデータから，ザラバにおいて最良買い気配値が最良売り気配値と等しい(=の関係)場合のデータを抽出する．
public class bid_equal_to_ask{

    public static void main(String[] args) throws IOException{

    	String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
    	String datayear = "\\2007";
    	String datadir = "\\rawcsv_2\\daily";
    	int sep = 4; //ファイルパスの_での区切り位置．作成するファイルに名前をつける場合に使う．[rawcsv_2]
    	String rfilename;
        String rfiledate; //読み込むファイルの日付を格納する．
        int bidprice; //最良買い気配値
        int askprice; //最良売り気配値

        File rfilepath = new File(currentdir + datayear + datadir); //読み込むファイルのディレクトリのパス．
        File[] filelist = rfilepath.listFiles(); //読み込むファイル名を取得する．

        for(int i=0; i<filelist.length; i++){

        	rfilename = filelist[i].getAbsolutePath();
        	int pathlength = rfilename.split("\\_")[sep].length();
        	rfiledate = rfilename.split("\\_")[sep].substring(pathlength-8, pathlength); //読み込むファイルの日付を取得

        	System.out.println(rfiledate);
        	FileReader fr = new FileReader(filelist[i]);
            BufferedReader brtxt = new BufferedReader(fr);
            String line = "";
            boolean makefile = false; //ファイルを作成する場合はtrue．
            boolean continuous = false; //ザラバ時間を判定する．

            File file = new File(currentdir + datayear + "\\dummy.txt" );
	        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file))); //エラー回避のためダミーファイルを用意する．

         	while((line = brtxt.readLine()) != null){

         		if(!continuous){
         			try{
         				if(line.split(",")[9].equals("  1")){
         					continuous = true;
         				}
         			} catch(Exception e) {
         				;
         			}
         		} else {
         			if(line.split(",")[1].split(":")[0].equals("11")){ //"15") && line.split(",")[1].split(":")[1].equals("10")){
         				continuous = false;
         			}
         		}

         		if(continuous){
         			try{
             			bidprice = Integer.parseInt(line.split(",")[5]);
                 		askprice = Integer.parseInt(line.split(",")[7]);
                 		if(bidprice == askprice){
                 			//最良買い気配値　=　最良売り気配値の場合に記録する．
                 			if(!makefile){
                 				String writedir = "\\bid_equal_to_ask_from_rawcsv_2\\daily"; //エラーを発見したときのみファイルを作成
                 				File newdir = new File(currentdir + datayear + writedir);
                 				newdir.mkdir();
                 				file = new File(currentdir + datayear + writedir + "\\" + rfiledate+ "_.csv" );
                 	         	pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                 	         	makefile = true;
                 			}
                 			pw.println(line);
                 			System.out.println(line);
                 		}
             		} catch(Exception e) {
             			continue;
             		}
         		}
         	}
         	brtxt.close();
            fr.close();
            pw.close();
        }

    }
}
