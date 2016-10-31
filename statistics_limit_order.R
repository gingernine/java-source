
maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\reference\\"

rfilename <- paste(maindir, currentdir, "reference_test.csv", sep = "", collapse = NULL)
wfilename <- paste(maindir, currentdir, "statistics_summary.csv", sep = "", collapse = NULL)

data <- read.csv(rfilename, sep=",", header = T)

c <- ncol(data)
r <- nrow(data)
summary <- matrix(0, nrow = c-1, ncol = 7)
colnames(summary) <- c("Mean", "S.D", "Median", "Kurtosis", "Skewness", "Minimum", "Maximum")
rownames(summary) <- colnames(data)[2:c]
for (i in 2:c) {
    Mean <- mean(data[,i])
    SD <- sd(data[,i])
    Median <- median(data[,i])
    Kurtosis <- mean((data[,i] - Mean)^4)/(SD^4)
    Skewness <- mean((data[,i] - Mean)^3)/(SD^3)
    line <- paste(colnames(data[i]), Mean, SD, Median, Kurtosis, Skewness, min(data[,i]), max(data[,i]))
    summary[i-1,] <- c(Mean, SD, Median, Kurtosis, Skewness, min(data[,i]), max(data[,i]))
}
write.csv(summary, wfilename, quote = F, row.names = T)

pngnames <- c( "Arrival_Frequency_of_Market_Buy_Orders", "Arrival_Frequency_of_Market_Sell_Orders",
               "Arrival_Frequency_of_Limit_Buy_Orders", "Arrival_Frequency_of_Limit_Sell_Orders",
               "Average_Pieces_of_One_Market_Buy_Order", "Average_Pieces_of_One_Market_Sell_Order",
               "Average_Pieces_of_One_Limit_Buy_Order", "Average_Pieces_of_One_Limit_Sell_Order",
               "Upmovement_Times_of_the_Best_Bid", "Downmovement_Times_of_the_Best_Bid",
               "Upmovement_Times_of_the_Best_Ask", "Downmovement_Times_of_the_Best_Ask" )

for (i in 1:length(pngnames)) {
    pngname <- paste(maindir, currentdir, pngnames[i], ".png", sep = "", collapse = NULL)
    png(pngname)
    hist(data[, i + 1], n=25, 
         main = pngnames[i], 
         xlab = "")
    dev.off()
}
