#注文時間間隔及び一回の注文単位数のヒストグラムを作成する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\pieces\\"
#currentdir <- "\\statistics_of_the_limit_order_book\\time_interval\\"
datayear <- "\\2007"
branchs <- c( "pieces_limit_buy", "pieces_limit_sell", "pieces_market_buy", "pieces_market_sell" )
#branchs <- c( "time_interval_limit_buy", "time_interval_limit_sell", "time_interval_market_buy", "time_interval_market_sell" )
sessions <- c( "\\morning", "\\afternoon" )

for (branch in branchs) {
    for (session in sessions) {
        dirpath <- paste(maindir, currentdir, branch, datayear, session, sep = "", collapse = NULL)
        summary <- matrix(0, nrow = 1, ncol = 7)
        
        for (name in list.files(dirpath)) {
            filename <- paste(maindir, currentdir, branch, datayear, session, "\\", name, sep = "", collapse = NULL)
            error <- try(read.csv(filename, sep=",", header = T))
            if (class(error)=="try-error"){
                next
            }
            data <- read.csv(filename, sep=",", header = T)
            date <- substring(name, 1, 8)
            
            sumvec <- matrix(0, nrow = 1, ncol = 7)
            rownames(sumvec) <- paste(substring(date, 1, 4), substring(date, 5, 6), substring(date, 7, 8), sep="/", collapse=NULL)
            colnames(sumvec) <- c("Mean", "S.D.", "Median", "Kurtosis", "Skewness", "Minimum", "Maximum")
            Mean <- mean(data[,1])
            SD <- sd(data[,1])
            Median <- median(data[,1])
            Kurtosis <- mean((data[,1] - Mean)^4)/(SD^4)
            Skewness <- mean((data[,1] - Mean)^3)/(SD^3)
            sumvec[1,] <- c(Mean, SD, Median, Kurtosis, Skewness, min(data[,1]), max(data[,1]))
            summary <- rbind(summary, sumvec)
            
            pngdir <- paste(maindir, currentdir, branch, datayear, "\\histogram", sep = "", collapse = NULL)
            if (!file.exists(pngdir)){
                dir.create(pngdir)
            }
            pngdir <- paste(maindir, currentdir, branch, datayear, "\\histogram", session, sep = "", collapse = NULL)
            if (!file.exists(pngdir)){
                dir.create(pngdir)
            }
            pngname <- paste(pngdir, "\\", date, "_.png", sep = "", collapse = NULL)
            png(pngname)
            hist(data[, 1], n=100, 
                 main = paste("the_histogram_of_", branch, session, sep = "", collapse = NULL),
                 xlab = "", ylab = "", col = "#b2222220")
            dev.off()
        }
        
        wfiledir <- paste(maindir, currentdir, branch, datayear, "\\statistics_summary", sep = "", collapse = NULL)
        if (!file.exists(wfiledir)){
            dir.create(wfiledir)
        }
        wfilename <- paste(maindir, currentdir, branch, datayear, "\\statistics_summary", session, ".csv", sep = "", collapse = NULL)
        write.csv(summary[-1,], wfilename, quote = F, row.names = T)
    }
}
    

    