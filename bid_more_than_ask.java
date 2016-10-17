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
//今回:rawcsvフォルダのdailyデータから，ザラバにおいて最良買い気配値が最良売り気配値よりも大きい(>の関係)場合のデータを抽出し，その個数も把握する．
public class bid_more_than_ask{

    public static void main(String[] args) throws IOException{

    	String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
    	String datayear = "\\2014";
    	//String datadir = "\\rawcsv_2\\daily";
    	//String writedir = "\\rawcsv_2\\bid_more_than_ask\\daily\\";
    	String datadir = "\\price_or_depth_change\\daily";
    	String writedir = "\\price_or_depth_change\\bid_more_than_ask\\daily\\";
    	String rfilename;
        String rfiledate; //読み込むファイルの日付を格納する．
        int bidprice;
        int askprice;

        File rfilepath = new File(currentdir + datayear + datadir); //読み込むファイルのディレクトリのパス．
        File[] filelist = rfilepath.listFiles(); //読み込むファイル名を取得する．
        for(int i=0; i<filelist.length; i++){

        	rfilename = filelist[i].getAbsolutePath();
        	int pathlength = rfilename.split("\\_")[6].length();
        	rfiledate = rfilename.split("\\_")[6].substring(pathlength-8, pathlength); //読み込むファイルの日付を取得

        	System.out.println(rfiledate);
        	FileReader fr = new FileReader(filelist[i]);
            BufferedReader brtxt = new BufferedReader(fr);
            String line = "";
            boolean continuous = false; //ザラバ時間を判定する．

            File file = new File(
            		currentdir + datayear + writedir + rfiledate + "_.csv" );
         	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

         	while((line = brtxt.readLine()) != null){

         		try{
         			if(line.split(",")[9].equals("  1")){
         				//寄付後からエラー数を勘定する．
         				System.out.println(line);
             			continuous = true;
         			}
         		} catch(Exception e) {
         			;
         		}
         		if(line.split(",")[1].equals("15:10:00")){
         			//プレクロージングの最良気配値は勘定しない
         			continuous = false;
         		}

         		if(continuous){
         			//ザラバのエラー数を勘定
         			try{
             			bidprice = Integer.parseInt(line.split(",")[5]);
                 		askprice = Integer.parseInt(line.split(",")[7]);
                 		if(bidprice > askprice){
                 			//最良買い気配値　>　最良売り気配値の場合に記録する．
                 			System.out.println(bidprice + " " + askprice);
                 			pw.println(line);
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
