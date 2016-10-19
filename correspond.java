import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

//使用ファイル:　日経NEEDSはprice_or_depth_change-dailyフォルダ内，ロイター社のデータはdailyフォルダ内の日次ファイル．
//日経NEEDSデータとコンマ秒データとの対応を、約上の出来高，最良気配値の数量から判定するプログラム．
//寄付の約定からはじめ，場の終わりの約定(大引け)までを比較する．
//合うデータと合わないデータをそれぞれ別ファイル(日次)で作成する．
public class correspond{

    public static void main(String[] args) throws IOException{

    	String nikkeidir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\nikkei_needs_output";
    	String reuterdir = "C:\\Users\\kklab\\Desktop\\yurispace\\plate_fluctuation\\src\\reuter_output";
    	String datayear = "\\2006";
    	String nikkeidata = "\\price_or_depth_change\\daily";
    	String reuterdata = "\\daily";
    	boolean continuous = false; //ザラバ時間を判定する．
    	boolean sepsession = true; //前場と後場に分かれていた年ならtrue．

    	File nikkeipath = new File(nikkeidir + datayear + nikkeidata); //読み込む日経NEEDSファイルのディレクトリのパス．
    	File reuterpath = new File(reuterdir + datayear + reuterdata); //読み込むreuterファイルのディレクトリのパス．
    	File[] nikkeilist = nikkeipath.listFiles(); //読み込む日経NEEDSファイル名を取得する．
    	File[] reuterlist = reuterpath.listFiles(); //読み込むreuterァイル名を取得する．

        for(int i=0; i<nikkeilist.length; i++){

        	FileReader nikkeifr = new FileReader(nikkeilist[i]);
        	FileReader reuterfr = new FileReader(reuterlist[i]);
            BufferedReader nikkeibr = new BufferedReader(nikkeifr);
            BufferedReader reuterbr = new BufferedReader(reuterfr);
            String nikkeiline = ""; //読み込む行
            String reuterline = ""; //読み込む行
            String nikkeisep[]; //csv
            String reutersep[]; //csv
            String nikkeitrade; //nikkeiの約定価格と約定数量(枚)を文字列にしたもの．
            String reutertrade; //reuterの約定価格と約定数量(枚)を文字列にしたもの．
            String nikkeiquote; //nikkeiの売り買い両方の気配値数量(枚)を文字列にしたもの．
            String reuterquote; //reuterの売り買い両方の気配値数量(枚)を文字列にしたもの．
            boolean isTrade = false; //約定データならtrue．
            boolean matched = false; //日経NEEDSとロイターのデータがマッチしたらtrue．

            File matchedfile = new File("");
            File nikkeifile = new File("");
            File reiterfile = new File("");
         	PrintWriter matchedpw = new PrintWriter(new BufferedWriter(new FileWriter(matchedfile)));
         	PrintWriter nikkeipw = new PrintWriter(new BufferedWriter(new FileWriter(nikkeifile)));
         	PrintWriter reuterpw = new PrintWriter(new BufferedWriter(new FileWriter(reuterfile)));

            while ((nikkeiline = nikkeibr.readLine()) != null) {

            	isTrade = false; //initial.
            	matched = false; //initial.
            	nikkeisep = nikkeiline.split(",",-1); //空欄があっても分割数に影響が無いよう-1を付ける．

            	if(nikkeisep[2].equals("Trade")){
            		isTrade = true;
            	} else {
            		isTrade = false;
            	}

            	if(nikkeisep[9].equals("1")){
            		//場のはじめの約定からスタート
            		continuous = true;
            	}

            	if(continuous){
            		//ザラバ時間のみのデータを使う．

            		if(isTrade){
            			nikkeitrade = nikkeisep[3] + "_" + nikkeisep[4]; //約定データは"価格_数量"．
            		} else {
            			nikkeiquote = nikkeisep[6] + "_" + nikkeisep[8]; //気配値データは"最良買い気配数量_最良売り気配数量"．
            		}

            		while ((reuterline = reuterbr.readline()) != null) {

            			reutersep = reuterline.split(",",-1);
            			if (isTrade) {
            				if(reutersep.equals("Trade")){
                				reutertrade = reutersep[3] + reutersep[4]; //約定データは"価格_数量"．
                				if(nikkeitrade.equals(reutertrade)){
                					//約定データでかつ数量が一致すればファイルに書き込む．
                					matchedpw.println(nikkeiline + ",," + reuterline);
                					matched = true;
                					break;
                				}
                			}
            			} else {
            				if(reutersep.equals("Quote")){
                				reuterquote = reutersep[6] + reutersep[8]; //気配値データは"最良買い気配数量_最良売り気配数量"．
                				if(nikkeitquote.equals(reuterquote)){
                					//気配値データでかつ数量が一致すればファイルに書き込む．
                					matchedpw.println(nikkeiline + ",," + reuterline);
                					matched = true;
                					break;
                				}
                			}
            			}
            		}

            		if (!matched) {
            			//両社のデータで合致するものがない場合はエラーとして別ファイルに書き出す．
            			nikkeipw.println(nikkeiline);
            			reuterpw.println(reuterline);
            		}
            	}
            }
            matchedpw.close();
            nikkeipw.close();
            reuterpw.close();
        }
    }
}
