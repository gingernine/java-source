#注文の一回あたり枚数の表を作成する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\pieces\\"
datayear <- "\\2007"
branchs <- c( "pieces_limit_buy", "pieces_limit_sell", "pieces_market_buy", "pieces_market_sell" )
sessions <- c( "\\morning", "\\afternoon" )

colmax <- 500 #最大列数

for (branch in branchs) {
    for (session in sessions) {
        dirpath <- paste(maindir, currentdir, branch, datayear, session, sep = "", collapse = NULL)
        table <- matrix(0, nrow = 1, ncol = colmax+1)
        
        for (name in list.files(dirpath)) {
            filename <- paste(maindir, currentdir, branch, datayear, session, "\\", name, sep = "", collapse = NULL)
            error <- try(read.csv(filename, sep=",", header = T))
            if (class(error)=="try-error"){
                #ファイルが空の場合のエラーキャッチ．
                next
            }
            data <- read.csv(filename, sep=",", header = T)
            date <- substring(name, 1, 8)
            
            vec <- matrix(0, nrow = 1, ncol = colmax+1)
            rownames(vec) <- paste(substring(date, 1, 4), substring(date, 5, 6), substring(date, 7, 8), sep="/", collapse=NULL)
            colnames(vec) <- 1:(colmax+1)
            colnames(vec)[colmax+1] <- paste("more than ", colmax, sep="", collapse=NULL)
            
            for (n in data[,1]) {
                if (n <= colmax) {
                    vec[n] <- vec[n] + 1
                } else {
                    vec[colmax+1] <- vec[colmax+1] + 1
                }
            }
            table <- rbind(table, vec)
            
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
            barplot(vec[1,],
                 main = paste("The_histogram_of_", branch, "_", session, sep = "", collapse = NULL),
                 xlab = "pieces_per_arrival", ylab = "frequency", col = "#b2222220")
            dev.off()
            
        }
        wfiledir <- paste(maindir, currentdir, branch, datayear, "\\pieces_per_arrival", sep = "", collapse = NULL)
        if (!file.exists(wfiledir)){
            dir.create(wfiledir)
        }
        wfilename <- paste(maindir, currentdir, branch, datayear, "\\pieces_per_arrival", session, ".csv", sep = "", collapse = NULL)
        write.csv(table[-1,], wfilename, quote = F, row.names = T)
    }
}