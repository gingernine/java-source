#到着時間間隔とサービス時間間隔の分布の検定を行う．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\time_interval\\"
datayear <- "\\2007"
branchs <- c( "time_interval_limit_buy", "time_interval_limit_sell", "time_interval_market_buy", "time_interval_market_sell" )
sessions <- c( "\\morning", "\\afternoon" )

#はじめに時間間隔のテーブルを作成する．10秒以上はプールする．
intervals <- c( "[0-1)", "[1-2)", "[2-3)", "[3-4)", "[4-5)", 
                "[5-6)", "[6-7)", "[7-8)", "[8-9)", "[9-10)","[10-infty)", "statistic", "chi-square", "adopted" )

#                "[10-11)", "[11-12)", "[12-13)", "[13-14)", "[14-15)",
#                "[15-16)", "[16-17)", "[17-18)", "[18-19)", "[19-20)", "[20-infty)" )
colnum <- length(intervals)
intervalnum <- colnum - 3

for (branch in branchs) {
    for (session in sessions) {
        dirpath <- paste(maindir, currentdir, branch, datayear, session, sep = "", collapse = NULL)
        table <- matrix(0, nrow = 1, ncol = colnum)
        for (name in list.files(dirpath)) {
            filename <- paste(maindir, currentdir, branch, datayear, session, "\\", name, sep = "", collapse = NULL)
            error <- try(read.csv(filename, sep=",", header = T))
            if (class(error)=="try-error"){
                next
            }
            data <- read.csv(filename, sep=",", header = T)
            lambda <- 1 / mean(data[, 1]) #parameter
            num <- length(data[, 1])
            date <- substring(name, 1, 8)
            vector <- matrix(0, nrow = 1, ncol = colnum)
            rownames(vector) <- date
            colnames(vector) <- intervals
            maxsec <- intervalnum - 1
            for (n in data[,1]) {
                if (n >= maxsec) {
                    vector[1,maxsec+1] <- vector[1,maxsec+1] + 1
                    next
                }
                for (i in 1:maxsec) {
                    if(n == i-1) {
                        #例えばデータが3秒としていたら，これは[3,4)秒の間に来たと考える
                        vector[1,i] <- vector[1,i] + 1
                        break
                    }
                }
            }
            
            #カイ二乗検定統計量を計算する
            f <- function(x){
                lambda * exp(-lambda*x)
            }
            statistic <- 0 #検定統計量
            DF <- intervalnum - 2
            for (i in 1:maxsec) {
                theoritical <- num * integrate(f, i-1, i)$value
                realized <- vector[1,i]
                statistic <- statistic + (theoritical - realized) * (theoritical - realized) / theoritical
            }
            theoritical <- num * (1 - integrate(f, 0, maxsec)$value)
            realized <- vector[1,maxsec+1]
            statistic <- statistic + (theoritical - realized)*(theoritical - realized)/theoritical
            vector[1,intervalnum+1] <- statistic
            vector[1,intervalnum+2] <- qchisq(df=DF, 0.95)
            if (statistic < qchisq(df=DF, 0.95)) {
                vector[1,intervalnum+3] <- adopted
            }
            table <- rbind(table, vector)
        }
        wfiledir <- paste(maindir, currentdir, branch, datayear, "\\table", sep = "", collapse = NULL)
        if (!file.exists(wfiledir)){
            dir.create(wfiledir)
        }
        wfilename <- paste(maindir, currentdir, branch, datayear, "\\table", session, ".csv", sep = "", collapse = NULL)
        write.csv(table[-1,], wfilename, quote = F, row.names = T)
    }
}

