#板の移動回数を時間間隔毎に取得する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
subdir <- "\\statistics_of_the_limit_order_book\\move_frequency"
datayear <- "\\2007"
sessions <- c( "\\morning", "\\afternoon" )

convert_to_seconds <- function(time) {
    # "hhmmss" (時分秒)表示される時間を始点0時0分0秒からの秒で表示する． #
    h <- (time - time %% 10000) * 3600 / 10000
    m <- (time %% 10000 - time %% 100) * 60 / 100
    s <- time %% 100
    return(h + m + s)
}

intervals <- c(60, 180, 300, 600) # second で指定
for (session in sessions) {
    dirpath <- paste(maindir, subdir, datayear, session, sep="", collapse=NULL)
    table <- matrix(0, nrow=1, ncol=length(intervals))
    wfilepaths <- c(paste(dirpath, "1min_int.csv", sep="", collapse=NULL),
                    paste(dirpath, "3min_int.csv", sep="", collapse=NULL),
                    paste(dirpath, "5min_int.csv", sep="", collapse=NULL),
                    paste(dirpath, "10min_int.csv", sep="", collapse=NULL))
    
    app <- c(F, F, F, F)
    for (name in list.files(dirpath)) {
        filepath <- paste(dirpath, "\\", name, sep="", collapse=NULL)
        data <- read.csv(filepath, sep=",", header=F)
        opening <- paste(substring(data[1,2],1,2), substring(data[1,2],4,5), substring(data[1,2],7,8), sep="", collapse=NULL)
        closing <- paste(substring(data[nrow(data),2],1,2), substring(data[nrow(data),2],4,5), substring(data[nrow(data),2],7,8), sep="", collapse=NULL)
        opening <- convert_to_seconds(as.numeric(opening))
        closing <- convert_to_seconds(as.numeric(closing))
        datevec <- matrix(0, nrow=1, ncol=length(intervals))
        #時間間隔毎に変動回数の平均値を計算する．
        for (i in 1:length(intervals)) {
            interval <- intervals[i]
            len <- ceiling( (closing - opening)/interval ) # 時間間隔によってベクトルの長さが違う．
            vector <- matrix(0, nrow=1, ncol=len) 
            
            for (j in 2:(nrow(data)-1)) {
                time <- paste(substring(data[j,2],1,2), substring(data[j,2],4,5), substring(data[j,2],7,8), sep="", collapse=NULL)
                sectime <- convert_to_seconds(as.numeric(time))
                vector[1, ceiling( (sectime - opening) / interval )] <- vector[1, ceiling( (sectime - opening) / interval )] + 1
            }
            
            rownames(vector) <- substring(name, 1, 8)
            datevec[1,i] <- mean(vector[1,])
            if (!app[i]) {
                write.table(vector, wfilepaths[i], sep=",", row.names=T, col.names=F, append=F)
                app[i] <- T
            } else {
                write.table(vector, wfilepaths[i], sep=",", row.names=T, col.names=F, append=T)
            }
        }
        rownames(datevec) <- paste(substring(name, 1, 4), substring(name, 5, 6), substring(name, 7, 8), sep="/", collapse=NULL)
        table <- rbind(table, datevec)
    }
    colnames(table) <- intervals
    wfilepath <- paste(dirpath, ".csv", sep="", collapse=NULL)
    write.csv(table[-1,], wfilepath, row.names = T)
}
