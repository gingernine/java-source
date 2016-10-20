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
    	String nikkeidatadir = "\\price_or_depth_change\\daily";
    	String reuterdatadir = "\\daily";
    	String writedir = "\\nikkei_reuter_match";
    	String writedaily = "\\daily\\";
    	String writedismatched = "\\dismatched\\";
    	boolean continuous = false; //ザラバ時間を判定する．
    	boolean sepsession = true; //前場と後場に分かれていた年ならtrue．

    	File nikkeipath = new File(nikkeidir + datayear + nikkeidatadir); //読み込む日経NEEDSファイルのディレクトリのパス．
    	File reuterpath = new File(reuterdir + datayear + reuterdatadir); //読み込むreuterファイルのディレクトリのパス．
    	File[] nikkeilist = nikkeipath.listFiles(); //読み込む日経NEEDSファイル名を取得する．
    	File[] reuterlist = reuterpath.listFiles(); //読み込むreuterァイル名を取得する．

        for(int i=0; i<nikkeilist.length; i++){

            String nikkeiline = ""; //読み込む行
            String reuterline = ""; //読み込む行
            String nikkeisep[]; //読み込んだ行をカンマで区切る．
            String reutersep[]; //読み込んだ行をカンマで区切る．
            String nikkeitrade = ""; //nikkeiの約定価格/数量(枚)を文字列にしたもの．
            String reutertrade = ""; //reuterの約定価格/数量(枚)を文字列にしたもの．
            String nikkeiquote = ""; //nikkeiの売り買い両方の気配値/数量(枚)を文字列にしたもの．
            String reuterquote = ""; //reuterの売り買い両方の気配値/数量(枚)を文字列にしたもの．
            boolean isTrade = false; //約定データならtrue．
            boolean matched = false; //日経NEEDSとロイターのデータがマッチしたらtrue．
			int timeflow[] = {0,0}; //日経NEEDSとロイターの収録時間が近いところの照合を行う．

            //読み込むファイルの日付を抽出する．
            String[] rfilename = nikkeilist[i].getAbsolutePath().split("\\_");
            int length = rfilename[6].length();
            String rfiledate = rfilename[6].substring(length-8, length);
            System.out.println(rfiledate);

            //書き出すファイルを設定する．
            File matchedf = new File(rootdir + writedir + datayear + writedaily + rfiledate + "_.csv");
            File dismatchedf = new File(rootdir + writedir + datayear + writedismatched + rfiledate + "_.csv");
         	PrintWriter matchedpw = new PrintWriter(new BufferedWriter(new FileWriter(matchedf)));
         	PrintWriter dismatchedpw = new PrintWriter(new BufferedWriter(new FileWriter(dismatchedf)));

         	//reuterの同日のデータファイルを開いて行数を取得する．
    		FileReader reuterfr = new FileReader(reuterlist[i]);
    		BufferedReader reuterbr = new BufferedReader(reuterfr);
    		int nrow = 0; //行数をカウントする．
         	while ((reuterline = reuterbr.readLine()) != null) {
         		nrow++;
         	}
         	reuterbr.close();

         	//reuterの同日のデータファイルを開いて読み込む．
         	reuterfr = new FileReader(reuterlist[i]);
    		reuterbr = new BufferedReader(reuterfr);
         	String reuterdata[] = new String[nrow]; //配列をロイター社のデータを配列にしまう．
         	int j = 0;
         	while ((reuterline = reuterbr.readLine()) != null) {
         		reuterdata[j] = reuterline;
         		j++;
         	}
         	reuterbr.close();
         	int start = 0; //後の配列操作の際に読み込む配列番号を指定する．

         	//nikkeiの同日のデータファイルを開いて読み込む．
         	FileReader nikkeifr = new FileReader(nikkeilist[i]);
        	BufferedReader nikkeibr = new BufferedReader(nikkeifr);
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
            						+ String.valueOf(Integer.parseInt(nikkeisep[4])); //約定データは"価格数量"．
            		} else {
            			nikkeiquote = String.valueOf(Integer.parseInt(nikkeisep[5]))
            						+ String.valueOf(Integer.parseInt(nikkeisep[6]))
            						+ String.valueOf(Integer.parseInt(nikkeisep[7]))
            						+ String.valueOf(Integer.parseInt(nikkeisep[8])); //気配値データは"最良買い気配数量最良売り気配数量"．
            		}


            		for (int l=start; l<nrow; l++) {
            			//合致するまで走査．
            			reutersep = reuterdata[l].split(",",-1);
            			timeflow[1] = Integer.parseInt(reutersep[1].split(":")[0] + reutersep[1].split(":")[1]); //ロイターの分秒を数値化．

            			if(timeflow[0]-timeflow[1] > 1) {
            				//日経NEEDSの時間とロイター社の時間の差は一分以内とする．
            				continue;
            			} else if (timeflow[0]-timeflow[1] < -1) {
            				//日経NEEDSの時間とロイター社の時間の差は一分以内とする．
            				break; //一分以上遅れた場合は走査をやめる．
            			}

            			if (isTrade && reutersep[2].equals("Trade")) {
                			reutertrade = reutersep[3] + reutersep[4]; //約定データは"価格数量"．
                			if(nikkeitrade.equals(reutertrade)){
                				//約定データでかつ数量が一致すればファイルに書き込む．
                				start = l;
                    			matchedpw.println(nikkeiline + ",," + reuterdata[l]);
                    			matched = true;
                    			break;
                			}
                		} else if (!isTrade && reutersep[2].equals("Quote")) {
                			reuterquote = reutersep[6] + reutersep[7]
                						+ reutersep[8] + reutersep[9]; //気配値データは"最良買い気配数量最良売り気配数量"．
                			if(nikkeiquote.equals(reuterquote)){
                				//気配値データでかつ数量が一致すればファイルに書き込む．
                				start = l;
                    			matchedpw.println(nikkeiline + ",," + reuterdata[l]);
                    			matched = true;
                    			break;
                			}
                		}
            		}

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
