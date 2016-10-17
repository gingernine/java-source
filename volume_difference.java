import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
//日経Needsのナマのデータから最良気配の価格と数量を補間したdailyファイル(rawcsv_2フォルダ内)
//では見たところ価格数量ともに変化がないにも関わらずデータが収録されている．つまり根本の日経Needsは変化がないにも関わらず
//データを取っている．そこで，価格または数量が変化したところのみを抽出したdailyファイル(price_or_depth_changeフォルダ内)
//を作成した．
//今回は最良気配データがどれほど減ったか，つまり重複データがいくつあったのかを調べる．
public class volume_difference{

    public static void main(String[] args) throws IOException{

    	String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
    	String datayear = "\\2014";
    	String datadir1 = "\\rawcsv_2\\daily";
    	String datadir2 = "\\price_or_depth_change\\daily";
    	String writedir = "\\volume_difference\\";
    	String rfilename;
        String rfiledate; //読み込むファイルの日付を格納する．
        int count1 = 0; //データ行数を勘定する．
        int count2 = 0; //データ行数を勘定する．
        String line;

        File rfilepath1 = new File(currentdir + datayear + datadir1); //読み込むファイルのディレクトリのパス．
        File rfilepath2 = new File(currentdir + datayear + datadir2); //読み込むファイルのディレクトリのパス．
        File[] filelist1 = rfilepath1.listFiles(); //読み込むファイル名を取得する．
        File[] filelist2 = rfilepath2.listFiles(); //読み込むファイル名を取得する．
        File file = new File(
        		currentdir + datayear + writedir + "volume_difference_continuous.csv" );
     	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file,true))); //allow overwrite

     	pw.println("date,rawcsv_2,price_or_depth_change,difference,");
        for(int i=0; i<filelist1.length; i++){

        	rfilename = filelist1[i].getAbsolutePath();
        	int pathlength = rfilename.split("\\_")[4].length();
        	rfiledate = rfilename.split("\\_")[4].substring(pathlength-8, pathlength); //読み込むファイルの日付を取得

        	System.out.println(rfiledate);
        	FileReader fr1 = new FileReader(filelist1[i]);
        	FileReader fr2 = new FileReader(filelist2[i]);
            BufferedReader brtxt1 = new BufferedReader(fr1);
            BufferedReader brtxt2 = new BufferedReader(fr2);
            boolean continuous = false; //ザラバ時間を判定する．

         	while((line = brtxt1.readLine()) != null){
         		try{
         			if(line.split(",")[9].equals("  1")){
         				//寄付後から行数を勘定する．
         				System.out.println(line);
             			continuous = true;
         			}
         		} catch(Exception e) {
         			;
         		}
         		if(line.split(",")[1].equals("15:10:00")){
         			//プレクロージングの行数は勘定しない
         			continuous = false;
         		}
         		if(continuous){
         			//ザラバの行数を勘定
         			count1++;
         		}
         	}
         	brtxt1.close();
            fr1.close();

         	while((line = brtxt2.readLine()) != null){
         		try{
         			if(line.split(",")[9].equals("  1")){
         				//寄付後から行数を勘定する．
         				System.out.println(line);
             			continuous = true;
         			}
         		} catch(Exception e) {
         			;
         		}
         		if(line.split(",")[1].equals("15:10:00")){
         			//プレクロージングの行数は勘定しない
         			continuous = false;
         		}
         		if(continuous){
         			//ザラバの行数を勘定
         			count2++;
         		}
         	}
         	brtxt2.close();
            fr2.close();

            pw.println(rfiledate + "," + count1 + "," + count2 + "," + (count1-count2) + ",");
            count1 = 0;
            count2 = 0;
        }
        pw.close();
    }
}
