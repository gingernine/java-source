#注文時間間隔のヒストグラムを作成する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\pieces\\"
datayear <- "\\2007"
branchs <- c( "pieces_limit_buy", "pieces_limit_sell", "pieces_market_buy", "pieces_market_sell" )
sessions <- c( "\\morning", "\\afternoon" )

for (branch in branchs) {
    for (session in sessions) {
        dirpath <- paste(maindir, currentdir, branch, datayear, session, sep = "", collapse = NULL)
        for (name in list.files(dirpath)) {
            filename <- paste(maindir, currentdir, branch, datayear, session, "\\", name, sep = "", collapse = NULL)
            data <- read.csv(filename, sep=",", header = T)
            date <- substring(name, 1, 8)
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
            hist(data[, 1], n=25, 
                 main = paste("the_histgram_of_", branch, session, sep = "", collapse = NULL),
                 xlab = "", ylab = "", col = "#b2222220")
            dev.off()
        }
    }
}
    wfilename <- paste(maindir, currentdir, datayear, "statistics_summary_", tail, ".csv", sep = "", collapse = NULL)
    data <- read.csv(filename, sep=",", header = T)
    cols <- c( 7, 9 )
    summary <- matrix(0, nrow = 2, ncol = 7)
    colnames(summary) <- c("Mean", "S.D", "Median", "Kurtosis", "Skewness", "Minimum", "Maximum")
    rownames(summary) <- c( colnames(data)[7], colnames(data)[9] )
    for (k in 1:2) {
        j <- cols[k]
        Mean <- mean(data[,j])
        SD <- sd(data[,j])
        Median <- median(data[,j])
        Kurtosis <- mean((data[,j] - Mean)^4)/(SD^4)
        Skewness <- mean((data[,j] - Mean)^3)/(SD^3)
        summary[k,] <- c(Mean, SD, Median, Kurtosis, Skewness, min(data[,j]), max(data[,j]))
    }
    write.csv(summary, wfilename, quote = F, row.names = T)

    