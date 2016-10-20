import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

//使用ファイル:　日経NEEDSはprice_or_depth_change-dailyフォルダ内，ロイター社のデータはdailyフォルダ内の日次ファイル．
//日経NEEDSデータとコンマ秒データとの対応を、約上の出来高，最良気配値の価格数量から判定するプログラム．
//寄付の約定からはじめ，場の終わりの約定(大引け)までを比較する．
//合うデータと合わないデータをそれぞれ別ファイル(日次)で作成する．
public class correspond{

    public static void main(String[] args) throws IOException{

    	String rootdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src";
    	String nikkeidir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
    	String reuterdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\reuter_output";
    	String datayear = "\\2006";
    	String nikkeidata = "\\price_or_depth_change\\daily";
    	String reuterdata = "\\daily";
    	String writedir = "\\nikkei_reuter_match_2";
    	String writedaily = "\\daily\\";
    	String writenikkei = "\\dismatched\\";
    	boolean continuous = false; //ザラバ時間を判定する．
    	boolean sepsession = true; //前場と後場に分かれていた年ならtrue．

    	File nikkeipath = new File(nikkeidir + datayear + nikkeidata); //読み込む日経NEEDSファイルのディレクトリのパス．
    	File reuterpath = new File(reuterdir + datayear + reuterdata); //読み込むreuterファイルのディレクトリのパス．
    	File[] nikkeilist = nikkeipath.listFiles(); //読み込む日経NEEDSファイル名を取得する．
    	File[] reuterlist = reuterpath.listFiles(); //読み込むreuterァイル名を取得する．

        for(int i=0; i<nikkeilist.length; i++){

        	FileReader nikkeifr = new FileReader(nikkeilist[i]);
            BufferedReader nikkeibr = new BufferedReader(nikkeifr);

            String nikkeiline = ""; //読み込む行
            String reuterline = ""; //読み込む行
            String nikkeisep[]; //読み込んだ行をカンマで区切る．
            String reutersep[]; //読み込んだ行をカンマで区切る．
            String nikkeitrade = ""; //nikkeiの約定価格と約定数量(枚)を文字列にしたもの．
            String reutertrade = ""; //reuterの約定価格と約定数量(枚)を文字列にしたもの．
            String nikkeiquote = ""; //nikkeiの売り買い両方の気配値数量(枚)を文字列にしたもの．
            String reuterquote = ""; //reuterの売り買い両方の気配値数量(枚)を文字列にしたもの．
            boolean isTrade = false; //約定データならtrue．
            boolean matched = false; //日経NEEDSとロイターのデータがマッチしたらtrue．
			int timeflow[] = {0,0}; //日経NEEDSとロイターの収録時間が近いところの照合を行う．
			double commatime[] = {0.0, 0.0}; //合致したデータのファイルに書き出すデータの並びが時系列に沿うようにする．

            //読み込むファイルの日付を抽出する．
            String[] rfilename = nikkeilist[i].getAbsolutePath().split("\\_");
            int length = rfilename[6].length();
            String rfiledate = rfilename[6].substring(length-8, length);
            System.out.println(rfiledate);

            //書き出すファイルを設定する．
            File matchedf = new File(rootdir + writedir + datayear + writedaily + rfiledate + "_.csv");
            File dismatchedf = new File(rootdir + writedir + datayear + writenikkei + rfiledate + "_.csv");
         	PrintWriter matchedpw = new PrintWriter(new BufferedWriter(new FileWriter(matchedf)));
         	PrintWriter dismatchedpw = new PrintWriter(new BufferedWriter(new FileWriter(dismatchedf)));

            while ((nikkeiline = nikkeibr.readLine()) != null) {

            	isTrade = false; //initial.
            	matched = false; //initial.
            	nikkeisep = nikkeiline.split(",",-1); //空欄があっても分割数に影響が無いよう-1を付ける．
            	timeflow[0] = Integer.parseInt(nikkeisep[1].split(":")[0] + nikkeisep[1].split(":")[1]); //日経の分秒を数値化．

            	//約定データか気配値データ化を判定する．
            	if(nikkeisep[2].equals("Trade")){
            		isTrade = true;
            	} else {
            		isTrade = false;
            	}

            	if(nikkeisep[9].equals("  1")){
            		//場のはじめの約定からスタート
            		continuous = true;
            	}

            	if(continuous){
            		//ザラバ時間のみのデータを使う．
            		if(isTrade){
            			nikkeitrade = String.valueOf(Integer.parseInt(nikkeisep[3]))
            					+ "_" + String.valueOf(Integer.parseInt(nikkeisep[4])); //約定データは"価格_数量"．
            		} else {
            			nikkeiquote = String.valueOf(Integer.parseInt(nikkeisep[5]))
            					+ "_" + String.valueOf(Integer.parseInt(nikkeisep[6]))
            					+ "_" + String.valueOf(Integer.parseInt(nikkeisep[7]))
            					+ "_" + String.valueOf(Integer.parseInt(nikkeisep[8])); //気配値データは"最良買い気配数量_最良売り気配数量"．
            		}

            		//ロイター社の同日のデータファイルを開いて読み込む．
            		FileReader reuterfr = new FileReader(reuterlist[i]);
            		BufferedReader reuterbr = new BufferedReader(reuterfr);

            		while ((reuterline = reuterbr.readLine()) != null) {
            			//合致するまで走査．
            			reutersep = reuterline.split(",",-1);
            			timeflow[1] = Integer.parseInt(reutersep[1].split(":")[0] + reutersep[1].split(":")[1]); //ロイターの分秒を数値化．
            			commatime[1] = Double.parseDouble(reutersep[1].split(":")[0] + reutersep[1].split(":")[1] + reutersep[1].split(":")[2]);

            			if(timeflow[0]-timeflow[1] > 1) {
            				//日経NEEDSの時間とロイター社の時間の差は一分以内とする．
            				continue;
            			} else if (timeflow[0]-timeflow[1] < -1) {
            				//日経NEEDSの時間とロイター社の時間の差は一分以内とする．
            				break; //一分以上遅れた場合は走査をやめる．
            			}

            			if (isTrade && reutersep[2].equals("Trade")) {
                			reutertrade = reutersep[3] + "_" +  reutersep[4]; //約定データは"価格_数量"．
                			if(nikkeitrade.equals(reutertrade)){
                				if(commatime[0] <= commatime[1]) {
                					//約定データでかつ数量が一致すればファイルに書き込む．
                    				matchedpw.println(nikkeiline + ",," + reuterline);
                    				matched = true;
                    				commatime[0] = commatime[1];
                    				break;
                    			} else {
                    				continue;
                    			}
                			}
                		} else if (!isTrade && reutersep[2].equals("Quote")) {
                			reuterquote = reutersep[6] + "_" + reutersep[7] + "_"
                						+ reutersep[8] + "_" + reutersep[9]; //気配値データは"最良買い気配数量_最良売り気配数量"．
                			if(nikkeiquote.equals(reuterquote)){
                				if(commatime[0] <= commatime[1]) {
                					//気配値データでかつ数量が一致すればファイルに書き込む．
                    				matchedpw.println(nikkeiline + ",," + reuterline);
                    				matched = true;
                    				commatime[0] = commatime[1];
                    				break;
                    			} else {
                    				continue;
                    			}
                			}
                		}
            		}
            		reuterbr.close();

            		if (!matched) {
            			//両社のデータで合致するものがない場合はエラーとして別ファイルに書き出す．
            			dismatchedpw.println(nikkeiline);
            		}
            	}
            }
            matchedpw.close();
            dismatchedpw.close();
            nikkeibr.close();
        }

    }
}
