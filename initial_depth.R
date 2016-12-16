#最後に板が上がった直後�?�数量，最後に板が下がった直後�?�数量�?�ヒストグラ�?を作�?�する�?

#histogram test code 
filepath <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output\\statistics_of_the_limit_order_book\\initial_depth\\2007\\after_up_.csv"
data <- read.csv(filepath, header=T)

date <- data[1,1]
bid <- matrix(0,1,1)
ask <- matrix(0,1,1)
for (i in 1:length(data[,1])) {
    
    if (date != data[i, 1]) {
        date <- data[i,1]
        hist(bid[,1][-1], n=25)
        pressed <- 0
        while(pressed == 0) {
            pressed <- readline()
        }
        hist(ask[,1][-1], n=25)
        pressed <- 0
        while(pressed == 0) {
            pressed <- readline()
        }
        bid <- matrix(0,1,1)
        ask <- matrix(0,1,1)
    }
    bid <- rbind(bid, matrix(data[i, "bid.depth"], 1, 1))
    ask <- rbind(ask, matrix(data[i, "ask.depth"], 1, 1))
}


maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\initial_depth"
datayear <- "\\2007\\"
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
    bidvec <- matrix(0, ncol=1, nrow=x)
    askvec <- matrix(0, ncol=1, nrow=x)
    for (i in data[, 7]) {
        bidvec[i, 1] <- bidvec[i, 1] + 1
    }
    for (i in data[, 9]) {
        askvec[i, 1] <- askvec[i, 1] + 1
    }
    y <- max( max(bidvec[, 1]), max(askvec[, 1]) )
    barplot(bidvec[, 1], 
         main = paste("the_histgram_of_", tail, "depths", sep = "", collapse = NULL),
         xlab = "", ylab = "",
         xlim = c(0, x), ylim = c(0, y), col = "#b2222220")
    par(new = T)
    barplot(askvec[, 1], 
         main = "",
         xlab = "", ylab = "",
         xlim = c(0, x), ylim = c(0, y), col = "#00bfff20")
    legend("topright", legend = c( "bid depth", "ask depth"), pch = c(19, 19), 
           col = c( "#b2222220", "#00bfff20" ))
    dev.off()
}