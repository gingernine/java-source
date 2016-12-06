#一回の注文単位数の中央値等のヒストグラムを作成する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\pieces\\"
datayear <- "\\2007"
branchs <- c( "pieces_limit_buy", "pieces_limit_sell", "pieces_market_buy", "pieces_market_sell" )
sessions <- c( "\\statistics_summary\\morning", "\\statistics_summary\\afternoon" )

for (branch in branchs) {
    for (session in sessions) {
        filepath <- paste(maindir, currentdir, branch, datayear, session, ".csv", sep = "", collapse = NULL)
        data <- read.csv(filepath, sep=",", header = T)
        for (i in 2:ncol(data)) {
            pngname <- paste(maindir, currentdir, branch, datayear, session, gsub("\\.","",colnames(data)[i]), "_.png", sep = "", collapse = NULL)
            png(pngname)
            hist(data[, i], n=100, 
                 main = paste("the_histgram_of_", branch, session, gsub("\\.","",colnames(data)[i]), sep = "", collapse = NULL),
                 xlab = "", ylab = "", col = "#b2222220")
            dev.off()
        }
    }
}