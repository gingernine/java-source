import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
//日経ニーズデータとコンマ秒データとの対応を、出来高から判定するプログラム
public class correspond{

    public static void main(String[] args) throws IOException{
    	String Index;
    	int i1 = 0;
    	int number1 = 0;
    	int number2 = 0;
    	int count = 1;//
    	int count1 = 0;//for文
    	int count2 = 0;//for文
    	int count_if = 0;//for文
    	int i2 = 0;
    	int i3 = 0;

    	String buffer1[] = new String[9000000];
    	String buffer2[] = new String[9000000];

    	int volume = 0;

    	int volume_nikkei = 0;//日経ニーズの出来高
    	int volume_konma = 0;//コンマデータの出来高
    	int sum_volume = 0;//出来高の総計(コンマデータ)
    	int count_konma = 0;//コンマデータの行の確認。
    	int length_konma = 0;//コンマデータの長さの確認

    	String a;

        BufferedReader br = new BufferedReader(new FileReader("filelist.txt"));//読み取りたいファイル名の記入
        String txtFileName;

        while((txtFileName = br.readLine()) != null) {

        	FileReader fr = new FileReader(txtFileName);
            BufferedReader brtxt = new BufferedReader(fr);
            String line ="";

            while ((line = brtxt.readLine()) != null) {
            	Index = line;
            	if(1 == count%2){
            		String d1 =  Index.substring(56,66);
            		volume = Integer.parseInt(d1);
            		if(volume != 0){
                		buffer1[number1] = Index;
                		number1++;
            		}
            	} else if(0 == count%2) {
            		buffer2[number2] = Index;
            		number2++;
            	}
            }

            count_konma = number2 - 1;

        	if(0 == count%2){
        		String[] filename = txtFileName.split("\\.");
        		File file = new File(filename[0] +	"_comb2.txt");
             	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        		number1 = 0;
        		number2 = 0;
        		while(number2 <= count_konma){

        			/*String d1 =  buffer1[number1].substring(56,66);//コンマ秒データ2010年まで（ここから）
            		volume_nikkei = Integer.parseInt(d1);

            		length_konma = buffer2[number2].length();

            		for(i1=0;i1<length_konma;i1++){
                        a = buffer2[number2].substring(i1,i1+1);

                        if(count_if == 5 && !(a.equals(",")) && count1 == 0){
                        	i2 = i1;
                        	count1++;
                        }
                        if(count_if == 6 && count1 != 0 && count2 == 0){
                        	i3 = i1-1;
                        	count2++;
                        	String db = buffer2[number2].substring(i2,i3);
                        	volume_konma = Integer.parseInt(db);
                        }
                        if(a.equals(",")){
                            count_if++;
                        }
                    }
            		count_if = 0;
            		count1 = 0;
            		count2 = 0;
            		//System.out.println(volume_konma);
            		if(volume_nikkei == volume_konma){
            			pw.println(buffer1[number1] + " " + buffer2[number2]);
            			//System.out.println(buffer1[number1] + " " + buffer2[number2] + "happy");
            			number1++;
    					number2++;
            		}
            		else if(volume_nikkei != volume_konma){
            			pw.println(buffer1[number1] + " " + buffer2[number2]);
            			//System.out.println(buffer1[number1] + " " + buffer2[number2]);

            			while(volume_nikkei > sum_volume){
            				//System.out.println("come on");

            				sum_volume += volume_konma;


            				number2++;
            				if(volume_nikkei > sum_volume){
            					pw.println("--------------------------------------------------------------------- " + buffer2[number2]);
            				}
            				if(number2 > count_konma){

            				}
            				else{
            					length_konma = buffer2[number2].length();


                        		for(i1=0;i1<length_konma;i1++){
                                    a = buffer2[number2].substring(i1,i1+1);

                                    if(count_if == 6 && !(a.equals(",")) && count1 == 0){
                                    	i2 = i1;
                                    	count1++;
                                    }
                                    if(count_if == 7 && count1 != 0 && count2 == 0){
                                    	i3 = i1-1;
                                    	count2++;
                                    	String db = buffer2[number2].substring(i2,i3);
                                    	volume_konma = Integer.parseInt(db);
                                    }
                                    if(a.equals(",")){
                                        count_if++;
                                    }
                                }
                        		count_if = 0;
                        		count1 = 0;
                        		count2 = 0;
            				}

            			}
            			if(volume_nikkei == sum_volume){
            				//System.out.println("ok");
            			}
            			else if(volume_nikkei != sum_volume){
            				System.out.println("NG");
            				System.out.println(volume_nikkei);
            				System.out.println(sum_volume);

            				System.out.println(buffer1[number1] + " " + buffer2[number2]);
            				pw.close();
            				return;
            			}
            			sum_volume = 0;
            			number1++;
            		}//コンマ秒データ2010年まで（ここまで）*/


        			String d1 =  buffer1[number1].substring(56,66);//コンマ秒データ2011年まで（ここから）
            		volume_nikkei = Integer.parseInt(d1);

            		length_konma = buffer2[number2].length();

            		for(i1=0;i1<length_konma;i1++){
                        a = buffer2[number2].substring(i1,i1+1);

                        if(count_if == 6 && !(a.equals(",")) && count1 == 0){
                        	i2 = i1;
                        	count1++;
                        }
                        if(count_if == 7 && count1 != 0 && count2 == 0){
                        	i3 = i1-1;
                        	count2++;
                        	String db = buffer2[number2].substring(i2,i3);
                        	volume_konma = Integer.parseInt(db);
                        }
                        if(a.equals(",")){
                            count_if++;
                        }
                    }
            		count_if = 0;
            		count1 = 0;
            		count2 = 0;
            		//System.out.println(volume_konma);
            		if(volume_nikkei == volume_konma){
            			pw.println(buffer1[number1] + " " + buffer2[number2]);
            			//System.out.println(buffer1[number1] + " " + buffer2[number2] + "happy");
            			number1++;
    					number2++;
            		}
            		else if(volume_nikkei != volume_konma){
            			pw.println(buffer1[number1] + " " + buffer2[number2]);
            			//System.out.println(buffer1[number1] + " " + buffer2[number2]);

            			while(volume_nikkei > sum_volume){
            				//System.out.println("come on");

            				sum_volume += volume_konma;


            				number2++;
            				if(volume_nikkei > sum_volume){
            					pw.println("--------------------------------------------------------------------- " + buffer2[number2]);
            				}
            				if(number2 > count_konma){

            				}
            				else{
            					length_konma = buffer2[number2].length();


                        		for(i1=0;i1<length_konma;i1++){
                                    a = buffer2[number2].substring(i1,i1+1);

                                    if(count_if == 6 && !(a.equals(",")) && count1 == 0){
                                    	i2 = i1;
                                    	count1++;
                                    }
                                    if(count_if == 7 && count1 != 0 && count2 == 0){
                                    	i3 = i1-1;
                                    	count2++;
                                    	String db = buffer2[number2].substring(i2,i3);
                                    	volume_konma = Integer.parseInt(db);
                                    }
                                    if(a.equals(",")){
                                        count_if++;
                                    }
                                }
                        		count_if = 0;
                        		count1 = 0;
                        		count2 = 0;
            				}

            			}
            			if(volume_nikkei == sum_volume){
            				//System.out.println("ok");
            			}
            			else if(volume_nikkei != sum_volume){
            				System.out.println("NG");
            				System.out.println(volume_nikkei);
            				System.out.println(sum_volume);

            				System.out.println(buffer1[number1] + " " + buffer2[number2]);
            				pw.close();
            				return;
            			}
            			sum_volume = 0;
            			number1++;
            		}//コンマ秒データ2011年から（ここまで）

            	}
                pw.close();
                for(int i=0;i<=number1;i++){
            		buffer1[i] = null;
            	}
                for(int i=0;i<=count_konma + 1;i++){
            		buffer2[i] = null;
            	}
                number1 = 0;
                number2 = 0;
        	}
            count++;

            brtxt.close();
            fr.close();

        	// txtファイル名を一行ずつロードする
        }
        br.close();

    }

}
