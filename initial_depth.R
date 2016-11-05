#最後に板が上がった直後の数量，最後に板が下がった直後の数量のヒストグラムを作成する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\initial_depth"
datayear <- "\\2009\\"
tails <- c( "after_up_", "after_down_" )

for (tail in tails) {
    filename <- paste(maindir, currentdir, datayear, tail, ".csv", sep = "", collapse = NULL)
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
    
    pngname <- paste(maindir, currentdir, datayear, tail, ".png", sep = "", collapse = NULL)
    png(pngname)
    x <- max( c( max(data[,7]), max(data[,9]) ) )
    y <- nrow(data)
    hist(data[, 7], n=25, 
         main = paste("the_histgram_of_", tail, "depths", sep = "", collapse = NULL),
         xlab = "", ylab = "",
         xlim = c(0, x), ylim = c(0, y), col = "#b2222220")
    par(new = T)
    hist(data[, 9], n=25, 
         main = "",
         xlab = "", ylab = "",
         xlim = c(0, x), ylim = c(0, y), col = "#00bfff20")
    legend("topright", legend = c( "bid depth", "ask depth"), pch = c(19, 19), 
           col = c( "#b2222220", "#00bfff20" ))
    dev.off()
}