import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
//raw,csv,raw_csvの3フォルダには日次のデータが複数ファイルに分割されているので各日のファイルに統合する．
public class nikkei_daily{

	public static void main(String[] args) throws IOException{

		String currentdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
		String datayear = "\\2014";
		String datadir = "\\price_or_depth_change\\daily_seperated"; //読み込むファイルが在るディレクトリ名
		String writedir = "\\price_or_depth_change\\daily\\"; //ファイルを書き込むディレクトリ名
		//File makedir = new File(currentdir + datayear + writedir);
		//if(!makedir.exists()){
			////書き込む先のディレクトリが存在しなければ作成する．
			//makedir.mkdir();
		//}
        String rfilename;
        String wfiledate = ""; //書き込むファイルの日付を設定する．
        String rfiledate; //読み込むファイルの日付を格納する．

        File rfilepath = new File(currentdir + datayear + datadir); //読み込むファイルのディレクトリのパス．
        File[] filelist = rfilepath.listFiles(); //読み込むファイル名を取得する．
        for(int i=0; i<filelist.length; i++){

        	rfilename = filelist[i].getAbsolutePath();
        	int pathlength = rfilename.split("\\_")[7].length();
        	rfiledate = rfilename.split("\\_")[7].substring(pathlength-8, pathlength); //読み込むファイルの日付を取得

        	if(!rfiledate.equals(wfiledate)){
        		wfiledate = rfiledate;
        	}
        	System.out.println(wfiledate);
        	FileReader fr = new FileReader(filelist[i]);
            BufferedReader brtxt = new BufferedReader(fr);
            String line = "";

            File file = new File(
            		currentdir + datayear + writedir + wfiledate + "_.csv" );
         	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true))); //overwrite true

         	while((line = brtxt.readLine()) != null){
         		pw.println(line);
         	}

         	brtxt.close();
            fr.close();
            pw.close();
        }
	}
}